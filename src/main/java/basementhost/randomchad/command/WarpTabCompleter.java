package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.model.WarpEntry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WarpTabCompleter implements TabCompleter {

	private final ChadteleportPlugin plugin;

	public WarpTabCompleter(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (!(sender instanceof Player player)) {
			return Collections.emptyList();
		}

		if (args.length != 1) {
			return Collections.emptyList();
		}

		Map<String, WarpEntry> warps = plugin.getWarpService().getWarps();
		List<String> suggestions = new ArrayList<>();

		String input = args[0].toLowerCase(Locale.ROOT);

		for (String warpName : warps.keySet()) {
			if (warpName.toLowerCase(Locale.ROOT).startsWith(input)) {
				suggestions.add(warpName);
			}
		}

		suggestions.sort(String::compareToIgnoreCase);
		return suggestions;
	}
}