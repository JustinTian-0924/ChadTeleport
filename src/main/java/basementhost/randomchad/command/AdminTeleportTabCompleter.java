package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AdminTeleportTabCompleter implements CommandSuggestions {

	private final ChadteleportPlugin plugin;

	public AdminTeleportTabCompleter(ChadteleportPlugin plugin) {
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

		String input = args[0].toLowerCase(Locale.ROOT);
		List<String> suggestions = new ArrayList<>();

		for (Player online : plugin.getServer().getOnlinePlayers()) {
			String name = online.getName();
			if (name.toLowerCase(Locale.ROOT).startsWith(input)) {
				suggestions.add(name);
			}
		}

		suggestions.sort(String::compareToIgnoreCase);
		return suggestions;
	}
}
