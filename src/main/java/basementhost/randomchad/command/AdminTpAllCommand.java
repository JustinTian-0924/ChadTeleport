package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class AdminTpAllCommand implements BasicCommand {

	private static final String PERMISSION = "chadteleport.admin.tpall";

	private final ChadteleportPlugin plugin;

	public AdminTpAllCommand(ChadteleportPlugin plugin) {
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

		if (args.length > 1) {
			plugin.getLangService().send(player, "general.usage.tpall");
			return;
		}

		Player destinationPlayer = player;

		if (args.length == 1) {
			Player namedTarget = plugin.getServer().getPlayerExact(args[0]);
			if (namedTarget == null || !namedTarget.isOnline()) {
				plugin.getLangService().send(player, "general.target-offline");
				return;
			}
			destinationPlayer = namedTarget;
		}

		Collection<? extends Player> onlinePlayers = plugin.getServer().getOnlinePlayers();
		if (plugin.getConfigService().usesRegionThreading()) {
			teleportAllOnFlameMc(player, destinationPlayer, List.copyOf(onlinePlayers));
			return;
		}
		int teleportedCount = 0;

		for (Player online : onlinePlayers) {
			if (online.getUniqueId().equals(destinationPlayer.getUniqueId())) {
				continue;
			}

			online.teleport(destinationPlayer.getLocation());
			teleportedCount++;

			plugin.getLangService().send(online, "admin-tp.tpall-target-notice", Map.of(
					"target", destinationPlayer.getName(),
					"sender", player.getName()
			));
		}

		plugin.getLangService().send(player, "admin-tp.tpall-success", Map.of(
				"target", destinationPlayer.getName(),
				"count", String.valueOf(teleportedCount)
		));

	}

	private void teleportAllOnFlameMc(Player sender, Player target, List<? extends Player> onlinePlayers) {
		var regions = plugin.getRegionTeleportService();
		Map<String, String> notice = Map.of("target", target.getName(), "sender", sender.getName());
		regions.onPlayerRegion(target, target::getLocation).whenComplete((destination, error) -> {
			if (error != null) {
				plugin.getLogger().log(Level.WARNING, "Could not read the destination for /tpall.", error);
				regions.notify(sender, "teleport.failed", Map.of());
				return;
			}
			if (destination == null) {
				regions.notify(sender, "general.target-offline", Map.of());
				return;
			}

			List<CompletableFuture<Boolean>> moves = onlinePlayers.stream()
					.filter(player -> !player.getUniqueId().equals(target.getUniqueId()))
					.map(player -> {
						var move = regions.teleport(player, destination.clone(), TeleportCause.PLUGIN)
								.exceptionally(failure -> {
									plugin.getLogger().log(Level.WARNING, "Teleport failed for " + player.getName() + " with /tpall.", failure);
									return false;
								});
						move.thenAccept(success -> {
							if (Boolean.TRUE.equals(success)) {
								regions.notify(player, "admin-tp.tpall-target-notice", notice);
							}
						});
						return move;
					})
					.toList();
			CompletableFuture.allOf(moves.toArray(CompletableFuture<?>[]::new)).thenRun(() -> {
				long moved = moves.stream().filter(move -> Boolean.TRUE.equals(move.getNow(false))).count();
				long failed = moves.size() - moved;
				regions.notify(sender, failed == 0 ? "admin-tp.tpall-success" : "admin-tp.tpall-partial", Map.of(
						"target", target.getName(), "count", String.valueOf(moved), "failed", String.valueOf(failed)));
			});
		});
	}
}
