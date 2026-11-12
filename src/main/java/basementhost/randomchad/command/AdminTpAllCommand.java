package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;

public class AdminTpAllCommand implements BasicCommand {

	private static final String PERMISSION = "chadteleport.admin.tpall";

	private final ChadteleportPlugin plugin;

	public AdminTpAllCommand(ChadteleportPlugin plugin) {
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

		if (args.length > 1) {
			plugin.getLangService().send(player, "general.usage.tpall");
			return;
		}

		Player destinationPlayer = player;

		if (args.length == 1) {
			Player namedTarget = plugin.getServer().getPlayerExact(args[0]);
			if (namedTarget == null || !namedTarget.isOnline()) {
				plugin.getLangService().send(player, "general.target-offline");
				return;
			}
			destinationPlayer = namedTarget;
		}

		Collection<? extends Player> onlinePlayers = plugin.getServer().getOnlinePlayers();
		int teleportedCount = 0;

		for (Player online : onlinePlayers) {
			if (online.getUniqueId().equals(destinationPlayer.getUniqueId())) {
				continue;
			}

			online.teleport(destinationPlayer.getLocation());
			teleportedCount++;

			plugin.getLangService().send(online, "admin-tp.tpall-target-notice", Map.of(
					"target", destinationPlayer.getName(),
					"sender", player.getName()
			));
		}

		plugin.getLangService().send(player, "admin-tp.tpall-success", Map.of(
				"target", destinationPlayer.getName(),
				"count", String.valueOf(teleportedCount)
		));

	}
}
