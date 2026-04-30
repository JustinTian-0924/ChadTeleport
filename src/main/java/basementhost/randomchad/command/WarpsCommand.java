package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.model.WarpEntry;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WarpsCommand implements CommandExecutor {

	private static final String PERMISSION = "chadteleport.warp";

	private final ChadteleportPlugin plugin;

	public WarpsCommand(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			plugin.getLangService().send(sender, "warp.player-only-warps");
			return true;
		}

		if (!plugin.getConfigService().isWarpEnabled()) {
			plugin.getLangService().send(player, "warp.disabled");
			return true;
		}

		if (!player.hasPermission(PERMISSION)) {
			plugin.getLangService().send(player, "general.no-permission");
			return true;
		}

		if (args.length != 0) {
			plugin.getLangService().send(player, "general.usage.warps");
			return true;
		}

		Map<String, WarpEntry> warps = plugin.getWarpService().getWarps();

		if (warps.isEmpty()) {
			plugin.getLangService().send(player, "warp.list-header-empty");
			plugin.getLangService().send(player, "warp.none");
			return true;
		}

		plugin.getLangService().send(player, "warp.list-header", Map.of(
				"count", String.valueOf(warps.size())
		));

		List<String> names = new ArrayList<>(warps.keySet());
		names.sort(String::compareToIgnoreCase);

		for (String name : names) {
			Component line = plugin.getLangService().get("warp.list-entry", Map.of(
					"name", name
			));
			player.sendMessage(line);
		}

		return true;
	}
}