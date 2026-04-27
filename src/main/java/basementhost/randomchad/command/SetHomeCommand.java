package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;

public class SetHomeCommand implements CommandExecutor {

	private final ChadteleportPlugin plugin;

	public SetHomeCommand(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			plugin.getLangService().send(sender, "home.player-only-sethome");
			return true;
		}

		if (args.length > 1) {
			plugin.getLangService().send(player, "general.usage.sethome");
			return true;
		}

		String homeName = (args.length == 0) ? "default" : normalizeHomeName(args[0]);

		boolean alreadyExists = plugin.getHomeService().hasHome(player, homeName);

		if (!alreadyExists) {
			int currentCount = plugin.getHomeService().getHomeCount(player);
			int maxHomes = plugin.getHomeLimitService().getMaxHomes(player);

			if (currentCount >= maxHomes) {
				plugin.getLangService().send(player, "home.limit-reached", Map.of(
						"max", String.valueOf(maxHomes)
				));
				return true;
			}
		}

		Location location = player.getLocation().clone();
		plugin.getHomeService().setHome(player, homeName, location);

		plugin.getLangService().send(player,
				alreadyExists ? "home.updated" : "home.created",
				Map.of("name", homeName)
		);

		return true;
	}

	private String normalizeHomeName(String input) {
		if (input == null || input.isBlank()) {
			return "default";
		}
		return input.toLowerCase(Locale.ROOT);
	}
}