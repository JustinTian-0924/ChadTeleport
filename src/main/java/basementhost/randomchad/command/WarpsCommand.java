package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.model.WarpEntry;
import net.kyori.adventure.text.Component;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WarpsCommand implements BasicCommand {

	private static final String PERMISSION = "chadteleport.warp";

	private final ChadteleportPlugin plugin;

	public WarpsCommand(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSourceStack source, String[] args) {
		CommandSender sender = source.getSender();
		if (!(sender instanceof Player player)) {
			plugin.getLangService().send(sender, "warp.player-only-warps");
			return;
		}

		if (!plugin.getConfigService().isWarpEnabled()) {
			plugin.getLangService().send(player, "warp.disabled");
			return;
		}

		if (!player.hasPermission(PERMISSION)) {
			plugin.getLangService().send(player, "general.no-permission");
			return;
		}

		if (args.length != 0) {
			plugin.getLangService().send(player, "general.usage.warps");
			return;
		}

		Map<String, WarpEntry> warps = plugin.getWarpService().getWarps();

		if (warps.isEmpty()) {
			plugin.getLangService().send(player, "warp.list-header-empty");
			plugin.getLangService().send(player, "warp.none");
			return;
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

	}
}
