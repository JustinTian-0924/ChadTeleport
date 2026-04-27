package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.model.HomeEntry;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomesCommand implements CommandExecutor {

	private final ChadteleportPlugin plugin;

	public HomesCommand(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			plugin.getLangService().send(sender, "home.player-only-homes");
			return true;
		}

		if (args.length != 0) {
			plugin.getLangService().send(player, "general.usage.homes");
			return true;
		}

		Map<String, HomeEntry> homes = plugin.getHomeService().getHomes(player);
		int used = homes.size();
		int max = plugin.getHomeLimitService().getMaxHomes(player);

		if (homes.isEmpty()) {
			plugin.getLangService().send(player, "home.list-header-empty", Map.of(
					"used", String.valueOf(used),
					"max", formatMax(max)
			));
			plugin.getLangService().send(player, "home.none");
			return true;
		}

		plugin.getLangService().send(player, "home.list-header", Map.of(
				"used", String.valueOf(used),
				"max", formatMax(max)
		));

		List<String> names = new ArrayList<>(homes.keySet());
		names.sort(String::compareToIgnoreCase);

		for (String name : names) {
			boolean isDefault = name.equalsIgnoreCase("default");

			String displayName = isDefault
					? plugin.getLangService().getRaw("home.default-name")
					: name;

			Component line = plugin.getLangService().get("home.list-entry", Map.of(
					"name", displayName
			));

			player.sendMessage(line);
		}

		return true;
	}

	private String formatMax(int max) {
		if (max == Integer.MAX_VALUE) {
			return plugin.getLangService().getRaw("home.unlimited");
		}
		return String.valueOf(max);
	}
}