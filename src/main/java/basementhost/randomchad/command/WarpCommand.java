package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.exception.LocalizedException;
import basementhost.randomchad.model.PendingTeleportOffer;
import basementhost.randomchad.model.TeleportOffer;
import basementhost.randomchad.model.WarpEntry;
import basementhost.randomchad.util.HomeNameValidator;
import basementhost.randomchad.util.WarpMessageUtil;
import org.bukkit.Location;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class WarpCommand implements BasicCommand {

	private static final String PERMISSION = "chadteleport.warp";

	private final ChadteleportPlugin plugin;

	public WarpCommand(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSourceStack source, String[] args) {
		CommandSender sender = source.getSender();
		if (!(sender instanceof Player player)) {
			plugin.getLangService().send(sender, "warp.player-only-warp");
			return;
		}

		if (!plugin.getConfigService().isWarpEnabled()) {
			plugin.getLangService().send(player, "warp.disabled");
			return;
		}

		if (!player.hasPermission(PERMISSION)) {
			plugin.getLangService().send(player, "general.no-permission");
			return;
		}

		if (args.length != 1) {
			plugin.getLangService().send(player, "general.usage.warp");
			return;
		}

		if (!HomeNameValidator.isValid(args[0])) {
			plugin.getLangService().send(player, "warp.invalid-name", Map.of(
					"min", String.valueOf(HomeNameValidator.getMinLength()),
					"max", String.valueOf(HomeNameValidator.getMaxLength())
			));
			return;
		}

		String warpName = HomeNameValidator.normalize(args[0]);

		WarpEntry warp = plugin.getWarpService().getWarp(warpName);
		if (warp == null) {
			plugin.getLangService().send(player, "warp.not-found", Map.of(
					"name", warpName
			));
			return;
		}

		Location destination = warp.getLocation();

		try {
			TeleportOffer offer = plugin.getTeleportCalculator()
					.calculateLocationOffer("warp", player, destination);

			if (plugin.getConfigService().isWarpMoneyEnabled()
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
			WarpMessageUtil.sendWarpQuoteMessage(player, warpName, offer, plugin.getLangService());

		} catch (LocalizedException e) {
			plugin.getLangService().send(player, e.getMessageKey(), e.getPlaceholders());
		} catch (Exception e) {
			plugin.getLangService().send(player, "quote.calculate-failed");
			plugin.getLogger().warning("Failed to calculate warp quote: " + e.getMessage());
			e.printStackTrace();
		}

	}
}
