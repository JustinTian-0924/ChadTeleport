package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.model.PendingTeleportRequest;
import basementhost.randomchad.model.TeleportQuote;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class TpAcceptCommand implements CommandExecutor {

	private final ChadteleportPlugin plugin;

	public TpAcceptCommand(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			plugin.getLangService().send(sender, "general.player-only");
			return true;
		}

		PendingTeleportRequest request;

		if (args.length == 0) {
			request = plugin.getTeleportManager().getLatestPendingRequest(player);
		} else if (args.length == 1) {
			request = plugin.getTeleportManager().getPendingRequestByRequesterName(player, args[0]);
		} else {
			plugin.getLangService().send(player, "general.usage.tpaccept");
			return true;
		}

		if (request == null) {
			plugin.getLangService().send(player, "request.none-matching");
			return true;
		}

		long ageMillis = System.currentTimeMillis() - request.getCreatedAt();
		long maxAgeMillis = plugin.getConfigService().getRequestExpireSeconds() * 1000L;
		if (ageMillis > maxAgeMillis) {
			plugin.getTeleportManager().removePendingRequest(player, request.getRequesterId());

			Map<String, String> placeholders = Map.of(
					"requester", request.getRequesterName(),
					"target", request.getTargetName()
			);

			plugin.getLangService().send(player, "request.expired", placeholders);

			Player requesterPlayer = plugin.getServer().getPlayer(request.getRequesterId());
			if (requesterPlayer != null && requesterPlayer.isOnline()) {
				plugin.getLangService().send(requesterPlayer, "request.expired", placeholders);
			}
			return true;
		}

		TeleportQuote quote = request.getQuote();
		Player requester = quote.getRequester();

		if (requester == null || !requester.isOnline()) {
			plugin.getTeleportManager().removePendingRequest(player, request.getRequesterId());
			plugin.getLangService().send(player, "general.requester-offline");
			return true;
		}

		plugin.getTeleportManager().removePendingRequest(player, request.getRequesterId());

		Location lockedDestination = player.getLocation().clone();

		plugin.getLangService().send(player, "request.accepted-target", Map.of(
				"requester", requester.getName()
		));
		plugin.getLangService().send(requester, "request.accepted-requester", Map.of(
				"target", player.getName()
		));

		plugin.getWarmupService().startTpaWarmup(quote, lockedDestination);
		return true;
	}
}