package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class AdminTpCommand implements BasicCommand {

	private static final String PERMISSION = "chadteleport.admin.tp";

	private final ChadteleportPlugin plugin;

	public AdminTpCommand(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSourceStack source, String[] args) {
		CommandSender sender = source.getSender();
		if (!(sender instanceof Player player)) {
			plugin.getLangService().send(sender, "general.player-only");
			return;
		}

		if (!player.hasPermission(PERMISSION)) {
			plugin.getLangService().send(player, "general.no-permission");
			return;
		}

		if (args.length != 1) {
			plugin.getLangService().send(player, "general.usage.tp");
			return;
		}

		Player target = plugin.getServer().getPlayerExact(args[0]);
		if (target == null || !target.isOnline()) {
			plugin.getLangService().send(player, "general.target-offline");
			return;
		}

		if (target.getUniqueId().equals(player.getUniqueId())) {
			plugin.getLangService().send(player, "admin-tp.self-tp");
			return;
		}

		player.teleport(target.getLocation());
		plugin.getLangService().send(player, "admin-tp.teleported-to-player", Map.of(
				"target", target.getName()
		));

	}
}
