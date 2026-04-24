package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCommand implements CommandExecutor {

	private final ChadteleportPlugin plugin;

	public SetSpawnCommand(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			plugin.getLangService().send(sender, "spawn.player-only-setspawn");
			return true;
		}

		if (!player.hasPermission("chadteleport.setspawn") && !player.hasPermission("chadteleport.admin")) {
			plugin.getLangService().send(player, "general.no-permission");
			return true;
		}

		if (args.length != 0) {
			plugin.getLangService().send(player, "general.usage.setspawn");
			return true;
		}

		boolean success = plugin.getSpawnService().setSpawnLocation(player.getLocation());

		if (success) {
			plugin.getLangService().send(player, "spawn.set-success");
		} else {
			plugin.getLangService().send(player, "spawn.set-failed");
		}

		return true;
	}
}