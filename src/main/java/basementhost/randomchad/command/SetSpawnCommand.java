package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCommand implements BasicCommand {

	private final ChadteleportPlugin plugin;

	public SetSpawnCommand(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSourceStack source, String[] args) {
		CommandSender sender = source.getSender();
		if (!(sender instanceof Player player)) {
			plugin.getLangService().send(sender, "spawn.player-only-setspawn");
			return;
		}

		if (!player.hasPermission("chadteleport.setspawn") && !player.hasPermission("chadteleport.admin")) {
			plugin.getLangService().send(player, "general.no-permission");
			return;
		}

		if (args.length != 0) {
			plugin.getLangService().send(player, "general.usage.setspawn");
			return;
		}

		boolean success = plugin.getSpawnService().setSpawnLocation(player.getLocation());

		if (success) {
			plugin.getLangService().send(player, "spawn.set-success");
		} else {
			plugin.getLangService().send(player, "spawn.set-failed");
		}

	}
}
