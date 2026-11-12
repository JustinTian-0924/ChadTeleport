package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.model.HomeEntry;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeTabCompleter implements CommandSuggestions {

	private final ChadteleportPlugin plugin;

	public HomeTabCompleter(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<String> suggest(CommandSourceStack source, String[] args) {
		CommandSender sender = source.getSender();
		if (!(sender instanceof Player player)) {
			return Collections.emptyList();
		}

		if (args.length != 1) {
			return Collections.emptyList();
		}

		Map<String, HomeEntry> homes = plugin.getHomeService().getHomes(player);
		List<String> suggestions = new ArrayList<>();

		String input = args[0].toLowerCase(Locale.ROOT);

		for (String homeName : homes.keySet()) {
			if (homeName.toLowerCase(Locale.ROOT).startsWith(input)) {
				suggestions.add(homeName);
			}
		}

		suggestions.sort(String::compareToIgnoreCase);
		return suggestions;
	}
}
