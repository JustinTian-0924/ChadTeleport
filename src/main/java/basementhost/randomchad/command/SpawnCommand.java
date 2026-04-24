package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.exception.LocalizedException;
import basementhost.randomchad.model.PendingTeleportOffer;
import basementhost.randomchad.model.TeleportOffer;
import basementhost.randomchad.util.SpawnMessageUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class SpawnCommand implements CommandExecutor {

	private final ChadteleportPlugin plugin;

	public SpawnCommand(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			plugin.getLangService().send(sender, "general.player-only");
			return true;
		}

		if (args.length != 0) {
			plugin.getLangService().send(player, "general.usage.spawn");
			return true;
		}

		if (!plugin.getConfigService().isSpawnEnabled()) {
			plugin.getLangService().send(player, "general.feature-disabled");
			return true;
		}

		if (plugin.getConfigService().isSpawnPermissionRequired()) {
			String permission = plugin.getConfigService().getSpawnPermission();
			if (permission != null && !permission.isBlank() && !player.hasPermission(permission)) {
				plugin.getLangService().send(player, "spawn.permission-denied");
				return true;
			}
		}

		Location spawnLocation = plugin.getSpawnService().getSpawnLocation();
		if (spawnLocation == null) {
			plugin.getLangService().send(player, "spawn.not-set");
			return true;
		}

		try {
			TeleportOffer offer = plugin.getTeleportCalculator()
					.calculateLocationOffer("spawn", player, spawnLocation);

			if (plugin.getConfigService().isSpawnMoneyEnabled()
					&& !player.hasPermission("chadteleport.bypass.fee")
					&& !player.hasPermission("chadteleport.bypass.*")
					&& offer.getPrice() > 0.0) {

				if (!plugin.getEconomyService().isAvailable()) {
					plugin.getEconomyService().tryHook();
				}

				if (!plugin.getEconomyService().isAvailable()) {
					plugin.getLangService().send(player, "economy.unavailable-cancelled");
					return true;
				}

				if (!plugin.getEconomyService().has(player, offer.getPrice())) {
					plugin.getLangService().send(player, "economy.insufficient");
					plugin.getLangService().send(player, "economy.required", Map.of(
							"price", String.valueOf(offer.getPrice())
					));
					return true;
				}
			}

			plugin.getTeleportManager().setPendingOffer(player, new PendingTeleportOffer(offer));
			SpawnMessageUtil.sendSpawnQuoteMessage(player, offer, plugin.getLangService());

		} catch (LocalizedException e) {
			plugin.getLangService().send(player, e.getMessageKey(), e.getPlaceholders());
		} catch (Exception e) {
			plugin.getLangService().send(player, "quote.calculate-failed");
			plugin.getLogger().warning("Failed to calculate spawn quote: " + e.getMessage());
			e.printStackTrace();
		}

		return true;
	}
}