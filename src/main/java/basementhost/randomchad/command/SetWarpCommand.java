package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.util.HomeNameValidator;
import org.bukkit.Location;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class SetWarpCommand implements BasicCommand {

	private static final String PERMISSION = "chadteleport.setwarp";

	private final ChadteleportPlugin plugin;

	public SetWarpCommand(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSourceStack source, String[] args) {
		CommandSender sender = source.getSender();
		if (!(sender instanceof Player player)) {
			plugin.getLangService().send(sender, "warp.player-only-setwarp");
			return;
		}

		if (!player.hasPermission(PERMISSION)) {
			plugin.getLangService().send(player, "general.no-permission");
			return;
		}

		if (args.length != 1) {
			plugin.getLangService().send(player, "general.usage.setwarp");
			return;
		}

		if (!HomeNameValidator.isValid(args[0])) {
			plugin.getLangService().send(player, "warp.invalid-name", Map.of(
					"min", String.valueOf(HomeNameValidator.getMinLength()),
					"max", String.valueOf(HomeNameValidator.getMaxLength())
			));
			return;
		}

		String warpName = HomeNameValidator.normalize(args[0]);
		boolean alreadyExists = plugin.getWarpService().hasWarp(warpName);

		Location location = player.getLocation().clone();
		boolean success = plugin.getWarpService().setWarp(warpName, location);

		if (!success) {
			plugin.getLangService().send(player, "warp.save-failed", Map.of(
					"name", warpName
			));
			return;
		}

		plugin.getLangService().send(player,
				alreadyExists ? "warp.updated" : "warp.created",
				Map.of("name", warpName)
		);

	}
}
