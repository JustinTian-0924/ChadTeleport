package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class AdminTpCommand implements CommandExecutor {

	private static final String PERMISSION = "chadteleport.admin.tp";

	private final ChadteleportPlugin plugin;

	public AdminTpCommand(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			plugin.getLangService().send(sender, "general.player-only");
			return true;
		}

		if (!player.hasPermission(PERMISSION)) {
			plugin.getLangService().send(player, "general.no-permission");
			return true;
		}

		if (args.length != 1) {
			plugin.getLangService().send(player, "general.usage.tp");
			return true;
		}

		Player target = plugin.getServer().getPlayerExact(args[0]);
		if (target == null || !target.isOnline()) {
			plugin.getLangService().send(player, "general.target-offline");
			return true;
		}

		if (target.getUniqueId().equals(player.getUniqueId())) {
			plugin.getLangService().send(player, "admin-tp.self-tp");
			return true;
		}

		player.teleport(target.getLocation());
		plugin.getLangService().send(player, "admin-tp.teleported-to-player", Map.of(
				"target", target.getName()
		));

		return true;
	}
}