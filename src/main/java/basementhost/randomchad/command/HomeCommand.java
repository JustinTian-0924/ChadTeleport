package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.exception.LocalizedException;
import basementhost.randomchad.model.HomeEntry;
import basementhost.randomchad.model.PendingTeleportOffer;
import basementhost.randomchad.model.TeleportOffer;
import basementhost.randomchad.util.HomeMessageUtil;
import basementhost.randomchad.util.HomeNameValidator;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;

public class HomeCommand implements BasicCommand {

	private final ChadteleportPlugin plugin;

	public HomeCommand(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSourceStack source, String[] args) {
		CommandSender sender = source.getSender();
		if (!(sender instanceof Player player)) {
			plugin.getLangService().send(sender, "general.player-only");
			return;
		}

		if (args.length > 1) {
			plugin.getLangService().send(player, "general.usage.home");
			return;
		}

		if (!plugin.getConfigService().isHomeEnabled()) {
			plugin.getLangService().send(player, "general.feature-disabled");
			return;
		}

		String homeName;
		if (args.length == 0) {
			homeName = "default";
		} else {
			if (!HomeNameValidator.isValid(args[0])) {
				plugin.getLangService().send(player, "home.invalid-name", Map.of(
						"min", String.valueOf(HomeNameValidator.getMinLength()),
						"max", String.valueOf(HomeNameValidator.getMaxLength())
				));
				return;
			}
			homeName = HomeNameValidator.normalize(args[0]);
		}

		HomeEntry home = plugin.getHomeService().getHome(player, homeName);
		if (home == null) {
			plugin.getLangService().send(player, "home.not-found", Map.of(
					"name", homeName
			));
			return;
		}

		try {
			TeleportOffer offer = plugin.getTeleportCalculator()
					.calculateLocationOffer("home", player, home.getLocation());

			if (plugin.getConfigService().isHomeMoneyEnabled()
					&& !player.hasPermission("chadteleport.bypass.fee")
					&& !player.hasPermission("chadteleport.bypass.*")
					&& offer.getPrice() > 0.0) {

				if (!plugin.getEconomyService().isAvailable()) {
					plugin.getEconomyService().tryHook();
				}

				if (!plugin.getEconomyService().isAvailable()) {
					plugin.getLangService().send(player, "economy.unavailable-cancelled");
					return;
				}

				if (!plugin.getEconomyService().has(player, offer.getPrice())) {
					plugin.getLangService().send(player, "economy.insufficient");
					plugin.getLangService().send(player, "economy.required", Map.of(
							"price", String.valueOf(offer.getPrice())
					));
					return;
				}
			}

			plugin.getTeleportManager().setPendingOffer(player, new PendingTeleportOffer(offer));
			HomeMessageUtil.sendHomeQuoteMessage(player, homeName, offer, plugin.getLangService());

		} catch (LocalizedException e) {
			plugin.getLangService().send(player, e.getMessageKey(), e.getPlaceholders());
		} catch (Exception e) {
			plugin.getLangService().send(player, "quote.calculate-failed");
			plugin.getLogger().warning("Failed to calculate home quote: " + e.getMessage());
			e.printStackTrace();
		}

	}

	private String normalizeHomeName(String input) {
		if (input == null || input.isBlank()) {
			return "default";
		}
		return input.toLowerCase(Locale.ROOT);
	}
}
