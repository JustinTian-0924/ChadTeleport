package basementhost.randomchad.service;

import basementhost.randomchad.exception.LocalizedException;
import basementhost.randomchad.model.TeleportBalance;
import basementhost.randomchad.model.TeleportPayment;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TeleportPaymentService {

	private final EconomyService economyService;
	private final LangService langService;
	private final ConfigService configService;
	private final TeleportBalanceService balanceService;
	private final TeleportHeatService heatService;
	private final Logger logger;
	private final Set<UUID> paying = new HashSet<>();

	public TeleportPaymentService(EconomyService economyService, LangService langService,
			ConfigService configService, TeleportBalanceService balanceService, TeleportHeatService heatService,
			Logger logger) {
		this.economyService = economyService;
		this.langService = langService;
		this.configService = configService;
		this.balanceService = balanceService;
		this.heatService = heatService;
		this.logger = logger;
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

	public void payAndTeleport(Player player, Location destination, TeleportPayment payment) {
		// Other plugins may call a command while handling the payment or teleport event.
		UUID playerId = player.getUniqueId();
		if (!paying.add(playerId)) {
			langService.send(player, "balance.payment-in-progress");
			return;
		}
		try {
			heatService.requireCoolEnough(player);
			if (!executePayment(player, destination, payment)) {
				return;
			}
			heatService.recordSuccessfulTeleport(player);
			langService.send(player, "teleport.success");
			if (payment.points() > 0) {
				langService.send(player, "balance.spent", Map.of(
						"points", String.valueOf(payment.points()), "name", payment.balanceName()));
			}
			if (payment.coins() > 0.0) {
				langService.send(player, "economy.charged", Map.of("price", String.valueOf(payment.coins())));
			}
		} catch (LocalizedException e) {
			langService.send(player, e.getMessageKey(), e.getPlaceholders());
		} finally {
			paying.remove(playerId);
		}
	}

	private boolean executePayment(Player player, Location destination, TeleportPayment payment) {
		EconomyService.CashDebit debit = null;
		TeleportBalance originalBalance = null;
		boolean teleported = false;
		try {
			if (!checkCanAfford(player, payment)) {
				return false;
			}
			if (payment.points() > 0) {
				originalBalance = balanceService.reserve(player, payment.points());
				if (originalBalance == null) {
					langService.send(player, "balance.quote-outdated");
					return false;
				}
			}
			if (payment.coins() > 0.0) {
				debit = economyService.withdraw(player, payment.coins());
				if (debit == null || Double.compare(debit.amount(), payment.coins()) != 0) {
					langService.send(player, "economy.payment-failed");
					return false;
				}
			}
			teleported = player.teleport(destination);
			if (!teleported) {
				langService.send(player, "teleport.failed");
			}
			return teleported;
		} catch (RuntimeException e) {
			logger.log(Level.WARNING, "Teleport payment or movement failed for " + player.getUniqueId(), e);
			langService.send(player, "teleport.failed");
			return false;
		} finally {
			if (!teleported) {
				try {
					if (originalBalance != null) {
						balanceService.save(player, originalBalance);
					}
				} finally {
					if (debit != null) {
						refund(player, debit);
					}
				}
			}
		}
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
}
