package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlayerNameTabCompleter implements CommandSuggestions {

	private final ChadteleportPlugin plugin;

	public PlayerNameTabCompleter(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<String> suggest(CommandSourceStack source, String[] args) {
		if (args.length == 0) {
			return List.of();
		}

		CommandSender sender = source.getSender();
		String input = args[args.length - 1].toLowerCase(Locale.ROOT);
		List<String> suggestions = new ArrayList<>();
		for (Player online : plugin.getServer().getOnlinePlayers()) {
			if (sender instanceof Player player && !player.canSee(online)) {
				continue;
			}
			if (online.getName().toLowerCase(Locale.ROOT).startsWith(input)) {
				suggestions.add(online.getName());
			}
		}

		suggestions.sort(String::compareToIgnoreCase);
		return suggestions;
	}
}
