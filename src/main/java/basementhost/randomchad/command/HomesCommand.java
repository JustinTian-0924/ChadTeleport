package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.model.HomeEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomesCommand implements BasicCommand {

	private final ChadteleportPlugin plugin;

	public HomesCommand(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSourceStack source, String[] args) {
		CommandSender sender = source.getSender();
		if (!(sender instanceof Player player)) {
			plugin.getLangService().send(sender, "home.player-only-homes");
			return;
		}

		if (args.length != 0) {
			plugin.getLangService().send(player, "general.usage.homes");
			return;
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
			return;
		}

		plugin.getLangService().send(player, "home.list-header", Map.of(
				"used", String.valueOf(used),
				"max", formatMax(max)
		));

		List<String> names = new ArrayList<>(homes.keySet());
		names.sort((a, b) -> {
			if (a.equalsIgnoreCase("default")) {
				return -1;
			}
			if (b.equalsIgnoreCase("default")) {
				return 1;
			}
			return a.compareToIgnoreCase(b);
		});

		for (String name : names) {
			boolean isDefault = name.equalsIgnoreCase("default");

			String displayName = isDefault
					? plugin.getLangService().getRaw("home.default-name")
					: name;

			String commandText = "/home " + name;

			Component line = plugin.getLangService().get("home.list-entry", Map.of(
							"name", displayName
					)).clickEvent(ClickEvent.runCommand(commandText))
					.hoverEvent(HoverEvent.showText(
							plugin.getLangService().get("home.list-entry-hover", Map.of(
									"name", displayName,
									"command", commandText
							))
					));

			player.sendMessage(line);
		}

		player.sendMessage(plugin.getLangService().get("home.list-footer"));

	}

	private String formatMax(int max) {
		if (max == Integer.MAX_VALUE) {
			return plugin.getLangService().getRaw("home.unlimited");
		}
		return String.valueOf(max);
	}
}
