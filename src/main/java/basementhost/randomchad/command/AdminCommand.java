package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;

public class AdminCommand implements CommandExecutor, TabCompleter {

	private final ChadteleportPlugin plugin;

	public AdminCommand(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("chadteleport.admin")) {
			plugin.getLangService().send(sender, "general.no-permission");
			return true;
		}

		if (args.length != 1 || !args[0].equalsIgnoreCase("reload")) {
			plugin.getLangService().send(sender, "admin.usage");
			return true;
		}

		plugin.getLangService().send(sender, "admin.reloading");

		try {
			plugin.reloadPluginResources();
			plugin.getLangService().send(sender, "admin.reload-success");
		} catch (Exception e) {
			plugin.getLogger().severe("Failed to reload plugin resources: " + e.getMessage());
			e.printStackTrace();
			plugin.getLangService().send(sender, "admin.reload-failed");
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (!sender.hasPermission("chadteleport.admin")) {
			return Collections.emptyList();
		}

		if (args.length == 1) {
			String input = args[0].toLowerCase();

			if ("reload".startsWith(input)) {
				return List.of("reload");
			}

			return Collections.emptyList();
		}

		return Collections.emptyList();
	}
}