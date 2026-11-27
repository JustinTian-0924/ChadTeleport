package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.model.PendingTeleportRequest;
import basementhost.randomchad.model.TeleportQuote;
import org.bukkit.Location;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class TpAcceptCommand implements BasicCommand {

	private final ChadteleportPlugin plugin;

	public TpAcceptCommand(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSourceStack source, String[] args) {
		CommandSender sender = source.getSender();
		if (!(sender instanceof Player player)) {
			plugin.getLangService().send(sender, "general.player-only");
			return;
		}

		PendingTeleportRequest request;

		if (args.length == 0) {
			request = plugin.getTeleportManager().getLatestPendingRequest(player);
		} else if (args.length == 1) {
			request = plugin.getTeleportManager().getPendingRequestByRequesterName(player, args[0]);
		} else {
			plugin.getLangService().send(player, "general.usage.tpaccept");
			return;
		}

		if (request == null || !plugin.getTeleportManager().removePendingRequest(request)) {
			plugin.getLangService().send(player, "request.none-matching");
			return;
		}

		long ageMillis = System.currentTimeMillis() - request.getCreatedAt();
		long maxAgeMillis = plugin.getConfigService().getRequestExpireSeconds() * 1000L;
		if (ageMillis > maxAgeMillis) {
			Map<String, String> placeholders = Map.of(
					"requester", request.getRequesterName(),
					"target", request.getTargetName()
			);

			plugin.getLangService().send(player, "request.expired", placeholders);

			Player requesterPlayer = plugin.getServer().getPlayer(request.getRequesterId());
			if (requesterPlayer != null && requesterPlayer.isOnline()) {
				notifyRequester(requesterPlayer, "request.expired", placeholders);
			}
			return;
		}

		TeleportQuote quote = request.getQuote();
		Player requester = quote.getRequester();

		if (requester == null || !requester.isOnline()) {
			plugin.getLangService().send(player, "general.requester-offline");
			return;
		}

		Location lockedDestination = player.getLocation().clone();

		plugin.getLangService().send(player, "request.accepted-target", Map.of(
				"requester", requester.getName()
		));
		notifyRequester(requester, "request.accepted-requester", Map.of(
				"target", player.getName()
		));

		plugin.getWarmupService().startTpaWarmup(quote, lockedDestination);
	}

	private void notifyRequester(Player requester, String key, Map<String, String> placeholders) {
		if (plugin.getConfigService().usesRegionThreading()) {
			plugin.getRegionTeleportService().notify(requester, key, placeholders);
		} else {
			plugin.getLangService().send(requester, key, placeholders);
		}
	}
}
