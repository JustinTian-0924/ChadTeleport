package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.service.TeleportHeatService;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminOverheatCommand implements BasicCommand {

	private static final String CHECK_PERMISSION = "chadteleport.admin.overheat.check";
	private static final String RESET_PERMISSION = "chadteleport.admin.overheat.reset";

	private final ChadteleportPlugin plugin;
	private final TeleportHeatService heatService;
	private final PlayerNameTabCompleter playerNames;

	public AdminOverheatCommand(ChadteleportPlugin plugin, TeleportHeatService heatService) {
		this.plugin = plugin;
		this.heatService = heatService;
		this.playerNames = new PlayerNameTabCompleter(plugin);
	}

	@Override
	public boolean canUse(CommandSender sender) {
		return sender.hasPermission(CHECK_PERMISSION) || sender.hasPermission(RESET_PERMISSION);
	}

	@Override
	public void execute(CommandSourceStack source, String[] args) {
		CommandSender sender = source.getSender();
		var messages = plugin.getLangService();
		if (!canUse(sender)) {
			messages.send(sender, "general.no-permission");
			return;
		}

		String action = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
		String permission = permissionFor(action);
		if (permission == null) {
			sendHelp(sender);
			return;
		}
		if (!sender.hasPermission(permission)) {
			messages.send(sender, "general.no-permission");
			return;
		}
		if (args.length != 2) {
			sendHelp(sender);
			return;
		}

		Player target = plugin.getServer().getPlayerExact(args[1]);
		if (target == null || !target.isOnline()) {
			messages.send(sender, "general.target-offline");
			return;
		}
		var placeholders = Map.of("player", target.getName());
		if ("check".equals(action)) {
			messages.send(sender, "overheat.admin-check", placeholders);
			heatService.sendStatus(sender, target, messages);
		} else {
			heatService.reset(target);
			plugin.getLogger().info(sender.getName() + " reset teleport heat for " + target.getName()
					+ " (" + target.getUniqueId() + ").");
			messages.send(sender, "overheat.admin-reset", placeholders);
		}
	}

	void sendHelp(CommandSender sender) {
		if (sender.hasPermission(CHECK_PERMISSION)) {
			plugin.getLangService().send(sender, "help.overheat-check");
		}
		if (sender.hasPermission(RESET_PERMISSION)) {
			plugin.getLangService().send(sender, "help.overheat-reset");
		}
	}

	@Override
	public List<String> suggest(CommandSourceStack source, String[] args) {
		CommandSender sender = source.getSender();
		if (args.length < 2) {
			String input = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
			return List.of("check", "reset").stream().filter(action -> action.startsWith(input)
					&& sender.hasPermission(permissionFor(action))).toList();
		}
		String permission = permissionFor(args[0].toLowerCase(Locale.ROOT));
		if (args.length == 2 && permission != null && sender.hasPermission(permission)) {
			return playerNames.suggest(source, args);
		}
		return List.of();
	}

	private String permissionFor(String action) {
		return switch (action) {
			case "check" -> CHECK_PERMISSION;
			case "reset" -> RESET_PERMISSION;
			default -> null;
		};
	}
}
