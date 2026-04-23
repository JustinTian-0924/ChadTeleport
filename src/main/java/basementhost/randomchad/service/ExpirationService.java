package basementhost.randomchad.service;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.model.PendingTeleportQuote;
import basementhost.randomchad.model.PendingTeleportRequest;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;

public class ExpirationService {

	private final ChadteleportPlugin plugin;
	private BukkitTask task;

	public ExpirationService(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	public void start() {
		if (task != null) {
			task.cancel();
		}

		task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::cleanupExpiredData, 20L, 20L);
	}

	public void stop() {
		if (task != null) {
			task.cancel();
			task = null;
		}
	}

	private void cleanupExpiredData() {
		cleanupExpiredQuotes();
		cleanupExpiredRequests();
	}

	private void cleanupExpiredQuotes() {
		long now = System.currentTimeMillis();
		long maxAgeMillis = plugin.getConfigService().getQuoteExpireSeconds() * 1000L;

		for (Map.Entry<UUID, PendingTeleportQuote> entry : plugin.getTeleportManager().getAllPendingQuotes()) {
			UUID playerId = entry.getKey();
			PendingTeleportQuote pendingQuote = entry.getValue();

			if (now - pendingQuote.getCreatedAt() > maxAgeMillis) {
				Player player = plugin.getServer().getPlayer(playerId);
				if (player != null && player.isOnline()) {
					plugin.getTeleportManager().removePendingQuote(player);
					plugin.getLangService().send(player, "quote.expired");
				}
			}
		}
	}

	private void cleanupExpiredRequests() {
		long now = System.currentTimeMillis();
		long maxAgeMillis = plugin.getConfigService().getRequestExpireSeconds() * 1000L;

		for (PendingTeleportRequest request : plugin.getTeleportManager().getAllPendingRequests()) {
			if (now - request.getCreatedAt() > maxAgeMillis) {
				Player target = plugin.getServer().getPlayer(request.getTargetId());
				if (target != null) {
					plugin.getTeleportManager().removePendingRequest(target, request.getRequesterId());
				}

				Map<String, String> placeholders = Map.of(
						"requester", request.getRequesterName(),
						"target", request.getTargetName()
				);

				Player requester = plugin.getServer().getPlayer(request.getRequesterId());
				if (requester != null && requester.isOnline()) {
					plugin.getLangService().send(requester, "request.expired", placeholders);
				}

				if (target != null && target.isOnline()) {
					plugin.getLangService().send(target, "request.expired", placeholders);
				}
			}
		}
	}
}