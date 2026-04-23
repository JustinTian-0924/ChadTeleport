package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.model.PendingTeleportRequest;
import basementhost.randomchad.model.TeleportQuote;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class TpDenyCommand implements CommandExecutor {

	private final ChadteleportPlugin plugin;

	public TpDenyCommand(ChadteleportPlugin plugin) {
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
			plugin.getLangService().send(player, "general.usage.tpdeny");
			return true;
		}

		if (request == null) {
			plugin.getLangService().send(player, "request.none-matching");
			return true;
		}

		plugin.getTeleportManager().removePendingRequest(player, request.getRequesterId());

		TeleportQuote quote = request.getQuote();
		Player requester = quote.getRequester();

		plugin.getLangService().send(player, "request.denied-target", Map.of(
				"requester", request.getRequesterName()
		));

		if (requester != null && requester.isOnline()) {
			plugin.getLangService().send(requester, "request.denied-requester", Map.of(
					"target", player.getName()
			));
		}

		return true;
	}
}