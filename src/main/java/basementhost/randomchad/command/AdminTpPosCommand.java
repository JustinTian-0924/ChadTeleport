package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class AdminTpPosCommand implements BasicCommand {

	private static final String PERMISSION = "chadteleport.admin.tppos";
	private static final double HORIZONTAL_LIMIT = 30_000_000.0;

	private final ChadteleportPlugin plugin;

	public AdminTpPosCommand(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSourceStack source, String[] args) {
		CommandSender sender = source.getSender();
		if (!(sender instanceof Player player)) {
			plugin.getLangService().send(sender, "general.player-only");
			return;
		}

		if (!player.hasPermission(PERMISSION)) {
			plugin.getLangService().send(player, "general.no-permission");
			return;
		}

		if (args.length != 3) {
			plugin.getLangService().send(player, "general.usage.tppos");
			return;
		}

		double x;
		double y;
		double z;
		try {
			x = Double.parseDouble(args[0]);
			y = Double.parseDouble(args[1]);
			z = Double.parseDouble(args[2]);
		} catch (NumberFormatException e) {
			plugin.getLangService().send(player, "admin-tp.invalid-coordinates");
			return;
		}

		if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
			plugin.getLangService().send(player, "admin-tp.invalid-coordinates");
			return;
		}

		if (Math.abs(x) >= HORIZONTAL_LIMIT || Math.abs(z) >= HORIZONTAL_LIMIT) {
			plugin.getLangService().send(player, "admin-tp.horizontal-out-of-range", Map.of(
					"limit", String.valueOf((int) HORIZONTAL_LIMIT)
			));
			return;
		}

		World world = player.getWorld();
		if (y < world.getMinHeight() || y >= world.getMaxHeight()) {
			plugin.getLangService().send(player, "admin-tp.height-out-of-range", Map.of(
					"min", String.valueOf(world.getMinHeight()),
					"max", String.valueOf(world.getMaxHeight())
			));
			return;
		}

		Location origin = player.getLocation();
		Location destination = new Location(world, x, y, z, origin.getYaw(), origin.getPitch());
		if (!world.getWorldBorder().isInside(destination)) {
			plugin.getLangService().send(player, "admin-tp.outside-world-border");
			return;
		}

		// 先异步加载目标地图区域，成功传送后才提示完成；保留玩家原本的朝向。
		String playerName = player.getName();
		try {
			player.teleportAsync(destination, TeleportCause.COMMAND).whenComplete((success, error) -> {
				if (error != null) {
					plugin.getLogger().log(Level.WARNING, "Failed to teleport " + playerName + " with /tppos.", error);
				}
				if (!plugin.isEnabled()) {
					return;
				}

				// 通知统一回到服务器主线程，兼顾异步加载失败的情况。
				Runnable notification = () -> {
					if (plugin.isEnabled() && player.isOnline()) {
						sendResult(player, error == null && Boolean.TRUE.equals(success));
					}
				};
				if (Bukkit.isPrimaryThread()) {
					notification.run();
				} else {
					plugin.getServer().getScheduler().runTask(plugin, notification);
				}
			});
		} catch (RuntimeException e) {
			plugin.getLogger().log(Level.WARNING, "Failed to start /tppos for " + player.getName() + ".", e);
			plugin.getLangService().send(player, "teleport.failed");
		}
	}

	private void sendResult(Player player, boolean success) {
		if (!success) {
			plugin.getLangService().send(player, "teleport.failed");
			return;
		}

		Location location = player.getLocation();
		plugin.getLangService().send(player, "admin-tp.teleported-to-position", Map.of(
				"world", player.getWorld().getKey().asString(),
				"x", String.valueOf(location.getX()),
				"y", String.valueOf(location.getY()),
				"z", String.valueOf(location.getZ())
		));
	}

	@Override
	public List<String> suggest(CommandSourceStack source, String[] args) {
		if (!(source.getSender() instanceof Player player) || !player.hasPermission(PERMISSION)
				|| args.length < 1 || args.length > 3) {
			return List.of();
		}

		Location location = player.getLocation();
		String coordinate = switch (args.length) {
			case 1 -> String.valueOf(location.getBlockX());
			case 2 -> String.valueOf(location.getBlockY());
			default -> String.valueOf(location.getBlockZ());
		};
		return coordinate.startsWith(args[args.length - 1]) ? List.of(coordinate) : List.of();
	}
}
