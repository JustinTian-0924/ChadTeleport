package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class AdminCommand implements BasicCommand {

	private final ChadteleportPlugin plugin;

	public AdminCommand(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSourceStack source, String[] args) {
		CommandSender sender = source.getSender();
		if (!sender.hasPermission("chadteleport.admin")) {
			plugin.getLangService().send(sender, "general.no-permission");
			return;
		}

		if (args.length != 1 || !args[0].equalsIgnoreCase("reload")) {
			plugin.getLangService().send(sender, "admin.usage");
			return;
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

	}

	@Override
	public List<String> suggest(CommandSourceStack source, String[] args) {
		CommandSender sender = source.getSender();
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
