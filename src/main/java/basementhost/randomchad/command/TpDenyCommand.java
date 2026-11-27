package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.model.PendingTeleportRequest;
import basementhost.randomchad.model.TeleportQuote;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class TpDenyCommand implements BasicCommand {

	private final ChadteleportPlugin plugin;

	public TpDenyCommand(ChadteleportPlugin plugin) {
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
			plugin.getLangService().send(player, "general.usage.tpdeny");
			return;
		}

		if (request == null || !plugin.getTeleportManager().removePendingRequest(request)) {
			plugin.getLangService().send(player, "request.none-matching");
			return;
		}

		TeleportQuote quote = request.getQuote();
		Player requester = quote.getRequester();

		plugin.getLangService().send(player, "request.denied-target", Map.of(
				"requester", request.getRequesterName()
		));

		if (requester != null && requester.isOnline()) {
			Map<String, String> placeholders = Map.of("target", player.getName());
			if (plugin.getConfigService().usesRegionThreading()) {
				plugin.getRegionTeleportService().notify(requester, "request.denied-requester", placeholders);
			} else {
				plugin.getLangService().send(requester, "request.denied-requester", placeholders);
			}
		}

	}
}
