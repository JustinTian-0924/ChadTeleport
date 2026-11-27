package basementhost.randomchad.service;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.exception.LocalizedException;
import basementhost.randomchad.model.TeleportBalance;
import basementhost.randomchad.model.TeleportOffer;
import basementhost.randomchad.model.TeleportPayment;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TeleportPaymentService {

	private final ChadteleportPlugin plugin;
	private final EconomyService economyService;
	private final LangService langService;
	private final ConfigService configService;
	private final TeleportBalanceService balanceService;
	private final TeleportHeatService heatService;
	private final Logger logger;
	private final Set<UUID> paying = ConcurrentHashMap.newKeySet();
	private final Map<UUID, CompletedPayment> completedPayments = new ConcurrentHashMap<>();

	public TeleportPaymentService(ChadteleportPlugin plugin, EconomyService economyService, LangService langService,
			ConfigService configService, TeleportBalanceService balanceService, TeleportHeatService heatService) {
		this.plugin = plugin;
		this.economyService = economyService;
		this.langService = langService;
		this.configService = configService;
		this.balanceService = balanceService;
		this.heatService = heatService;
		this.logger = plugin.getLogger();
	}

	public TeleportPayment quote(Player player, double price) {
		var settings = configService.getBalanceSettings();
		if (price == 0.0 || !settings.enabled()) {
			return TeleportPayment.coinsOnly(price);
		}
		int points = balanceService.getOrCreate(player, System.currentTimeMillis()).points();
		return TeleportPayment.split(price, points, settings.coinValue(), settings.displayName());
	}

	public boolean checkCanAfford(Player player, TeleportPayment payment) {
		if (payment.points() > 0 && balanceService.getAvailablePoints(player) < payment.points()) {
			langService.send(player, "balance.quote-outdated");
			return false;
		}
		double coins = payment.coins();
		if (coins == 0.0) {
			return true;
		}
		if (!economyService.isAvailable()) {
			economyService.tryHook();
		}
		if (!economyService.isAvailable()) {
			langService.send(player, "economy.unavailable-cancelled");
			return false;
		}
		if (!economyService.has(player, coins)) {
			langService.send(player, "economy.insufficient");
			langService.send(player, "economy.required", Map.of("price", String.valueOf(coins)));
			return false;
		}
		return true;
	}

	public void payAndTeleport(TeleportOffer offer, Location destination) {
		Player player = offer.getRequester();
		settleCompletedPayment(player);
		// Other plugins may call a command while handling the payment or teleport event.
		UUID playerId = player.getUniqueId();
		if (!paying.add(playerId)) {
			langService.send(player, "balance.payment-in-progress");
			return;
		}
		boolean waitingForRegion = false;
		try {
			heatService.requireCoolEnough(player);
			Reservation reservation = reservePayment(player, offer.getPayment());
			if (reservation == null) {
				return;
			}
			if (configService.usesRegionThreading()) {
				waitingForRegion = true;
				teleportOnRegion(player, offer, destination, reservation);
			} else {
				boolean success = false;
				try {
					success = player.teleport(destination);
				} catch (RuntimeException e) {
					logFailure(player, e);
				} finally {
					finishPayment(player, new CompletedPayment(offer, reservation, success));
				}
			}
		} catch (LocalizedException e) {
			langService.send(player, e.getMessageKey(), e.getPlaceholders());
		} finally {
			if (!waitingForRegion) {
				paying.remove(playerId);
			}
		}
	}

	private Reservation reservePayment(Player player, TeleportPayment payment) {
		EconomyService.CashDebit debit = null;
		TeleportBalance originalBalance = null;
		boolean reserved = false;
		try {
			if (!checkCanAfford(player, payment)) {
				return null;
			}
			if (payment.points() > 0) {
				originalBalance = balanceService.reserve(player, payment.points());
				if (originalBalance == null) {
					langService.send(player, "balance.quote-outdated");
					return null;
				}
			}
			if (payment.coins() > 0.0) {
				debit = economyService.withdraw(player, payment.coins());
				if (debit == null || Double.compare(debit.amount(), payment.coins()) != 0) {
					langService.send(player, "economy.payment-failed");
					return null;
				}
			}
			reserved = true;
			return new Reservation(originalBalance, debit);
		} catch (RuntimeException e) {
			logFailure(player, e);
			langService.send(player, "teleport.failed");
			return null;
		} finally {
			if (!reserved) {
				rollback(player, new Reservation(originalBalance, debit));
			}
		}
	}

	private void teleportOnRegion(Player player, TeleportOffer offer, Location destination, Reservation reservation) {
		CompletableFuture<Boolean> movement;
		try {
			// WarmupService starts this step on the region that owns the player.
			movement = player.teleportAsync(destination);
		} catch (RuntimeException e) {
			movement = CompletableFuture.failedFuture(e);
		}
		movement.whenComplete((success, error) -> {
			if (error != null) {
				logFailure(player, error);
			}
			UUID playerId = player.getUniqueId();
			completedPayments.put(playerId, new CompletedPayment(offer, reservation,
					error == null && Boolean.TRUE.equals(success)));
			if (plugin.getServer().isOwnedByCurrentRegion(player) && player.isOnline()) {
				settleCompletedPayment(player);
				return;
			}
			scheduleSettlement(player).whenComplete((delivered, schedulingError) -> {
				if (schedulingError != null) {
					logFailure(player, schedulingError);
				}
				if (delivered == null && plugin.isEnabled()) {
					// The player may have reconnected before the old entity's scheduler retired.
					plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
						Player current = plugin.getServer().getPlayer(playerId);
						if (current != null && current != player) {
							scheduleSettlement(current);
						}
					});
				}
			});
		});
	}

	private CompletableFuture<Boolean> scheduleSettlement(Player player) {
		return plugin.getRegionTeleportService().onPlayerRegion(player, () -> {
			settleCompletedPayment(player);
			return true;
		});
	}

	public void settleCompletedPayment(Player player) {
		UUID playerId = player.getUniqueId();
		CompletedPayment completed = completedPayments.remove(playerId);
		if (completed == null) {
			return;
		}
		try {
			finishPayment(player, completed);
		} catch (RuntimeException e) {
			logger.log(Level.SEVERE, "Could not settle completed teleport for " + playerId, e);
		} finally {
			paying.remove(playerId);
		}
	}

	private void finishPayment(Player player, CompletedPayment completed) {
		if (!completed.success()) {
			rollback(player, completed.reservation());
			langService.send(player, "teleport.failed");
			return;
		}
		heatService.recordSuccessfulTeleport(player, completed.offer());
		langService.send(player, "teleport.success");
		TeleportPayment payment = completed.offer().getPayment();
		if (payment.points() > 0) {
			langService.send(player, "balance.spent", Map.of(
					"points", String.valueOf(payment.points()), "name", payment.balanceName()));
		}
		if (payment.coins() > 0.0) {
			langService.send(player, "economy.charged", Map.of("price", String.valueOf(payment.coins())));
		}
	}

	private void rollback(Player player, Reservation reservation) {
		try {
			if (reservation.originalBalance() != null) {
				balanceService.save(player, reservation.originalBalance());
			}
		} finally {
			if (reservation.debit() != null) {
				refund(player, reservation.debit());
			}
		}
	}

	private void logFailure(Player player, Throwable error) {
		logger.log(Level.WARNING, "Teleport payment or movement failed for " + player.getUniqueId(), error);
	}

	private void refund(Player player, EconomyService.CashDebit debit) {
		try {
			if (debit.refund(player)) {
				return;
			}
		} catch (RuntimeException e) {
			logger.log(Level.WARNING, "Economy provider threw during teleport refund", e);
		}
		logger.severe("Could not refund " + debit.amount() + " coins to " + player.getUniqueId());
		langService.send(player, "economy.refund-failed", Map.of("price", String.valueOf(debit.amount())));
	}

	private record Reservation(TeleportBalance originalBalance, EconomyService.CashDebit debit) {
	}

	private record CompletedPayment(TeleportOffer offer, Reservation reservation, boolean success) {
	}
}
