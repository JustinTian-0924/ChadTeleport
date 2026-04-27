package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;

public class DelHomeCommand implements CommandExecutor {

	private final ChadteleportPlugin plugin;

	public DelHomeCommand(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			plugin.getLangService().send(sender, "home.player-only-delhome");
			return true;
		}

		if (args.length != 1) {
			plugin.getLangService().send(player, "general.usage.delhome");
			return true;
		}

		String homeName = normalizeHomeName(args[0]);

		boolean deleted = plugin.getHomeService().deleteHome(player, homeName);
		if (!deleted) {
			plugin.getLangService().send(player, "home.not-found", Map.of(
					"name", homeName
			));
			return true;
		}

		plugin.getLangService().send(player, "home.deleted", Map.of(
				"name", homeName
		));
		return true;
	}

	private String normalizeHomeName(String input) {
		if (input == null || input.isBlank()) {
			return "default";
		}
		return input.toLowerCase(Locale.ROOT);
	}
}