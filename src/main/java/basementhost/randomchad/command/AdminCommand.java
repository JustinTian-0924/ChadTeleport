package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AdminCommand implements BasicCommand {

	private static final String RELOAD_PERMISSION = "chadteleport.admin";

	private final ChadteleportPlugin plugin;
	private final TeleportHeatCommand heatStatusCommand;
	private final AdminOverheatCommand overheatCommand;

	public AdminCommand(ChadteleportPlugin plugin, TeleportHeatCommand heatStatusCommand,
			AdminOverheatCommand overheatCommand) {
		this.plugin = plugin;
		this.heatStatusCommand = heatStatusCommand;
		this.overheatCommand = overheatCommand;
	}

	@Override
	public void execute(CommandSourceStack source, String[] args) {
		CommandSender sender = source.getSender();
		if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) {
			sendHelp(sender);
			return;
		}
		if (args[0].equalsIgnoreCase("overheat")) {
			String[] overheatArgs = Arrays.copyOfRange(args, 1, args.length);
			if (overheatArgs.length == 0 && sender instanceof Player) {
				heatStatusCommand.execute(source, overheatArgs);
			} else {
				overheatCommand.execute(source, overheatArgs);
			}
			return;
		}
		if (args.length != 1 || !args[0].equalsIgnoreCase("reload")) {
			plugin.getLangService().send(sender, sender.hasPermission(RELOAD_PERMISSION) ? "admin.usage" : "help.usage");
			return;
		}
		if (!sender.hasPermission(RELOAD_PERMISSION)) {
			plugin.getLangService().send(sender, "general.no-permission");
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

	private void sendHelp(CommandSender sender) {
		var messages = plugin.getLangService();
		messages.send(sender, "help.header");
		messages.send(sender, "help.usage");
		if (sender instanceof Player) {
			sendPlayerHelp(sender);
		}
		if (sender.hasPermission("chadteleport.admin.tp")) {
			messages.send(sender, sender instanceof Player ? "help.tp" : "help.tp-console");
		}
		sendHelpIfAllowed(sender, RELOAD_PERMISSION, "help.reload");
		overheatCommand.sendHelp(sender);
	}

	private void sendPlayerHelp(CommandSender sender) {
		var messages = plugin.getLangService();
		var config = plugin.getConfigService();
		if (config.isFeatureEnabled("tpa")) {
			messages.send(sender, "help.tpa");
		}
		messages.send(sender, "help.requests");
		messages.send(sender, "help.confirm");
		String spawnPermission = config.getSpawnPermission();
		if (config.isSpawnEnabled() && sender.hasPermission("chadteleport.spawn")
				&& (!config.isSpawnPermissionRequired() || spawnPermission == null || spawnPermission.isBlank()
				|| sender.hasPermission(spawnPermission))) {
			messages.send(sender, "help.spawn");
		}
		if (config.isHomeEnabled()) {
			messages.send(sender, "help.home");
		}
		messages.send(sender, "help.manage-homes");
		if (config.isWarpEnabled()) {
			sendHelpIfAllowed(sender, "chadteleport.warp", "help.warp");
		}
		if (config.getBalanceSettings().enabled()) {
			messages.send(sender, "help.balance");
		}
		messages.send(sender, "help.overheat");
		sendHelpIfAllowed(sender, "chadteleport.admin.tppos", "help.tppos");
		sendHelpIfAllowed(sender, "chadteleport.admin.tphere", "help.tphere");
		sendHelpIfAllowed(sender, "chadteleport.admin.tpall", "help.tpall");
		sendHelpIfAllowed(sender, "chadteleport.setspawn", "help.setspawn");
		sendHelpIfAllowed(sender, "chadteleport.setwarp", "help.setwarp");
		sendHelpIfAllowed(sender, "chadteleport.delwarp", "help.delwarp");
	}

	private void sendHelpIfAllowed(CommandSender sender, String permission, String key) {
		if (sender.hasPermission(permission)) {
			plugin.getLangService().send(sender, key);
		}
	}

	@Override
	public List<String> suggest(CommandSourceStack source, String[] args) {
		if (args.length > 1) {
			if (args[0].equalsIgnoreCase("overheat")) {
				return overheatCommand.suggest(source, Arrays.copyOfRange(args, 1, args.length));
			}
			return List.of();
		}
		String input = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
		CommandSender sender = source.getSender();
		List<String> commands = new ArrayList<>();
		commands.add("help");
		if (sender.hasPermission(RELOAD_PERMISSION)) {
			commands.add("reload");
		}
		if (sender instanceof Player || overheatCommand.canUse(sender)) {
			commands.add("overheat");
		}
		return commands.stream().filter(command -> command.startsWith(input)).toList();
	}
}
