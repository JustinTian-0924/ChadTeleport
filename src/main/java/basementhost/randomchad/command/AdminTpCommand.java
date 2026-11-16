package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class AdminTpCommand implements BasicCommand {

	private static final String PERMISSION = "chadteleport.admin.tp";

	private final ChadteleportPlugin plugin;
	private final PlayerNameTabCompleter tabCompleter;

	public AdminTpCommand(ChadteleportPlugin plugin) {
		this.plugin = plugin;
		this.tabCompleter = new PlayerNameTabCompleter(plugin);
	}

	@Override
	public void execute(CommandSourceStack source, String[] args) {
		CommandSender sender = source.getSender();
		if (!sender.hasPermission(PERMISSION)) {
			plugin.getLangService().send(sender, "general.no-permission");
			return;
		}

		if (args.length < 1 || args.length > 2) {
			plugin.getLangService().send(sender, "general.usage.tp-extended");
			return;
		}

		Player movedPlayer;
		if (args.length == 1) {
			if (!(sender instanceof Player player)) {
				plugin.getLangService().send(sender, "admin-tp.source-required");
				return;
			}
			movedPlayer = player;
		} else {
			movedPlayer = plugin.getServer().getPlayerExact(args[0]);
			if (movedPlayer == null || !movedPlayer.isOnline()) {
				plugin.getLangService().send(sender, "admin-tp.player-offline", Map.of("player", args[0]));
				return;
			}
		}

		String targetName = args[args.length - 1];
		Player target = plugin.getServer().getPlayerExact(targetName);
		if (target == null || !target.isOnline()) {
			plugin.getLangService().send(sender, "admin-tp.player-offline", Map.of("player", targetName));
			return;
		}

		if (target.getUniqueId().equals(movedPlayer.getUniqueId())) {
			plugin.getLangService().send(sender, args.length == 1 ? "admin-tp.self-tp" : "admin-tp.same-player");
			return;
		}

		Map<String, String> placeholders = Map.of(
				"source", movedPlayer.getName(),
				"target", target.getName(),
				"sender", sender.getName()
		);

		boolean success;
		try {
			// 目标玩家所在区域已经加载，直接执行，不经过请求、收费或预热流程。
			success = movedPlayer.teleport(target.getLocation(), TeleportCause.COMMAND);
		} catch (RuntimeException e) {
			plugin.getLogger().log(Level.WARNING,
					"Failed to teleport " + movedPlayer.getName() + " to " + target.getName() + " with /tp.", e);
			plugin.getLangService().send(sender, "admin-tp.forced-teleport-failed", placeholders);
			return;
		}

		if (!success) {
			plugin.getLangService().send(sender, "admin-tp.forced-teleport-failed", placeholders);
			return;
		}

		// 强制传送成功后取消旧预热，避免旧计时结束时把玩家再次传走。
		plugin.getWarmupService().cancelWarmup(movedPlayer, "warmup.cancelled-admin");
		plugin.getLangService().send(sender,
				args.length == 1 ? "admin-tp.teleported-to-player" : "admin-tp.forced-teleport-success",
				placeholders);
		if (!movedPlayer.equals(sender)) {
			plugin.getLangService().send(movedPlayer, "admin-tp.forced-teleport-notice", placeholders);
		}
	}

	@Override
	public List<String> suggest(CommandSourceStack source, String[] args) {
		if (!source.getSender().hasPermission(PERMISSION) || args.length < 1 || args.length > 2) {
			return List.of();
		}
		return tabCompleter.suggest(source, args);
	}
}
