package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.exception.LocalizedException;
import basementhost.randomchad.model.PendingTeleportQuote;
import basementhost.randomchad.model.TeleportQuote;
import basementhost.randomchad.util.TeleportMessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class TpaCommand implements CommandExecutor {

	private final ChadteleportPlugin plugin;

	public TpaCommand(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			plugin.getLangService().send(sender, "general.player-only");
			return true;
		}

		if (!plugin.getConfigService().isFeatureEnabled("tpa")) {
			plugin.getLangService().send(player, "feature.tpa-disabled");
			return true;
		}

		if (args.length != 1) {
			plugin.getLangService().send(player, "general.usage.tpa");
			return true;
		}

		Player target = Bukkit.getPlayerExact(args[0]);
		if (target == null || !target.isOnline()) {
			plugin.getLangService().send(player, "general.target-offline");
			return true;
		}

		if (target.equals(player)) {
			plugin.getLangService().send(player, "feature.self-request");
			return true;
		}

		try {
			TeleportQuote quote = plugin.getTeleportCalculator().calculateTpaQuote(player, target);

			boolean shouldCharge = plugin.getConfigService().isMoneyEnabled("tpa")
					&& !player.hasPermission("chadteleport.bypass.fee")
					&& !player.hasPermission("chadteleport.bypass.*");

			if (shouldCharge && quote.getPrice() > 0.0) {
				if (!plugin.getEconomyService().isAvailable()) {
					plugin.getEconomyService().tryHook();
				}

				if (!plugin.getEconomyService().isAvailable()) {
					plugin.getLangService().send(player, "economy.unavailable");
					return true;
				}

				if (!plugin.getEconomyService().has(player, quote.getPrice())) {
					plugin.getLangService().send(player, "economy.insufficient");
					plugin.getLangService().send(player, "economy.required", Map.of(
							"price", String.valueOf(quote.getPrice())
					));
					return true;
				}
			}

			plugin.getTeleportManager().setPendingQuote(player, new PendingTeleportQuote(quote));
			TeleportMessageUtil.sendQuoteMessage(player, quote, plugin.getLangService());

		} catch (LocalizedException e) {
			plugin.getLangService().send(player, e.getMessageKey(), e.getPlaceholders());
		} catch (IllegalArgumentException e) {
			player.sendMessage(e.getMessage());
		} catch (Exception e) {
			plugin.getLangService().send(player, "quote.calculate-failed");
			plugin.getLogger().warning("Failed to calculate TPA quote: " + e.getMessage());
			e.printStackTrace();
		}

		return true;
	}
}