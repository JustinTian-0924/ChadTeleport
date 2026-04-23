package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.model.PendingTeleportQuote;
import basementhost.randomchad.model.PendingTeleportRequest;
import basementhost.randomchad.model.TeleportQuote;
import basementhost.randomchad.util.TeleportMessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class TeleportCommand implements CommandExecutor {

	private final ChadteleportPlugin plugin;

	public TeleportCommand(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			plugin.getLangService().send(sender, "general.player-only");
			return true;
		}

		if (args.length != 1) {
			plugin.getLangService().send(player, "general.usage.teleport");
			return true;
		}

		if (args[0].equalsIgnoreCase("confirm")) {
			PendingTeleportQuote pending = plugin.getTeleportManager().getPendingQuote(player);
			if (pending == null) {
				plugin.getLangService().send(player, "quote.none");
				return true;
			}

			long ageMillis = System.currentTimeMillis() - pending.getCreatedAt();
			long maxAgeMillis = plugin.getConfigService().getQuoteExpireSeconds() * 1000L;
			if (ageMillis > maxAgeMillis) {
				plugin.getTeleportManager().removePendingQuote(player);
				plugin.getLangService().send(player, "quote.expired");
				return true;
			}

			TeleportQuote quote = pending.getQuote();
			Player target = quote.getTarget();

			if (target == null || !target.isOnline()) {
				plugin.getTeleportManager().removePendingQuote(player);
				plugin.getLangService().send(player, "general.target-offline");
				return true;
			}

			plugin.getTeleportManager().removePendingQuote(player);

			PendingTeleportRequest request = new PendingTeleportRequest(quote);
			plugin.getTeleportManager().setPendingRequest(target, request);

			int requestWindow = plugin.getConfigService().getRequestExpireSeconds();

			plugin.getLangService().send(player, "request.sent", Map.of(
					"target", target.getName()
			));
			plugin.getLangService().send(player, "request.waiting");

			TeleportMessageUtil.sendIncomingRequestMessage(
					target,
					player.getName(),
					requestWindow,
					plugin.getLangService()
			);

			int count = plugin.getTeleportManager().getPendingRequestCount(target);
			if (count > 1) {
				plugin.getLangService().send(target, "request.multiple-hint-count", Map.of(
						"count", String.valueOf(count)
				));
				plugin.getLangService().send(target, "request.multiple-hint-usage");
			}

			return true;
		}

		if (args[0].equalsIgnoreCase("cancel")) {
			PendingTeleportQuote removedQuote = plugin.getTeleportManager().removePendingQuote(player);
			boolean cancelledWarmup = plugin.getWarmupService().cancelWarmup(
					player,
					"warmup.cancel-command"
			);

			if (removedQuote == null && !cancelledWarmup) {
				plugin.getLangService().send(player, "teleport.no-pending-or-warmup");
				return true;
			}

			if (removedQuote != null) {
				plugin.getLangService().send(player, "quote.cancelled");
			}

			return true;
		}

		plugin.getLangService().send(player, "general.usage.teleport");
		return true;
	}
}