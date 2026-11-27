package basementhost.randomchad.service;

import basementhost.randomchad.ChadteleportPlugin;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class RegionTeleportService {

	private final ChadteleportPlugin plugin;

	public RegionTeleportService(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	public <T> CompletableFuture<T> onPlayerRegion(Player player, Supplier<T> operation) {
		CompletableFuture<T> result = new CompletableFuture<>();
		Runnable unavailable = () -> result.complete(null);
		try {
			if (!player.getScheduler().execute(plugin, () -> {
				try {
					result.complete(player.isOnline() ? operation.get() : null);
				} catch (RuntimeException e) {
					result.completeExceptionally(e);
				}
			}, unavailable, 1L)) {
				unavailable.run();
			}
		} catch (RuntimeException e) {
			result.completeExceptionally(e);
		}
		return result;
	}

	public CompletableFuture<Boolean> teleport(Player player, Location destination, TeleportCause cause) {
		return onPlayerRegion(player, () -> player.teleportAsync(destination, cause))
				.thenCompose(result -> result == null ? CompletableFuture.completedFuture(false) : result);
	}

	public CompletableFuture<Boolean> teleportToPlayer(Player player, Player target, TeleportCause cause) {
		return onPlayerRegion(target, target::getLocation).thenCompose(destination ->
				destination == null ? CompletableFuture.completedFuture(false) : teleport(player, destination, cause));
	}

	public void notify(CommandSender recipient, String key, Map<String, String> placeholders) {
		Runnable send = () -> plugin.getLangService().send(recipient, key, placeholders);
		if (recipient instanceof Player player) {
			player.getScheduler().run(plugin, task -> send.run(), null);
		} else {
			plugin.getServer().getGlobalRegionScheduler().execute(plugin, send);
		}
	}
}
