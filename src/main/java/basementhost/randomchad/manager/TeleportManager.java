package basementhost.randomchad.manager;

import basementhost.randomchad.model.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class TeleportManager {

	private final JavaPlugin plugin;
	private final Map<UUID, PendingTeleportQuote> pendingQuotes = new HashMap<>();

	// target UUID -> (requester UUID -> request), @_@
	private final Map<UUID, Map<UUID, PendingTeleportRequest>> pendingRequestsByTarget = new HashMap<>();

	private final Map<UUID, ActiveTeleport> activeTeleportsByRequester = new HashMap<>();

	private final Map<UUID, PendingTeleportOffer> pendingOffers = new HashMap<>();

	public TeleportManager(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	public JavaPlugin getPlugin() {
		return plugin;
	}

	public void setPendingQuote(Player player, PendingTeleportQuote pendingQuote) {
		pendingQuotes.put(player.getUniqueId(), pendingQuote);
	}

	public PendingTeleportQuote getPendingQuote(Player player) {
		return pendingQuotes.get(player.getUniqueId());
	}

	public PendingTeleportQuote removePendingQuote(Player player) {
		return pendingQuotes.remove(player.getUniqueId());
	}

	public boolean hasPendingQuote(Player player) {
		return pendingQuotes.containsKey(player.getUniqueId());
	}

	public Collection<Map.Entry<UUID, PendingTeleportQuote>> getAllPendingQuotes() {
		return new ArrayList<>(pendingQuotes.entrySet());
	}

	public void setPendingRequest(Player target, PendingTeleportRequest request) {
		pendingRequestsByTarget
				.computeIfAbsent(target.getUniqueId(), k -> new LinkedHashMap<>())
				.put(request.getRequesterId(), request);
	}

	public PendingTeleportRequest getPendingRequest(Player target, UUID requesterId) {
		Map<UUID, PendingTeleportRequest> requests = pendingRequestsByTarget.get(target.getUniqueId());
		if (requests == null) {
			return null;
		}
		return requests.get(requesterId);
	}

	public PendingTeleportRequest getPendingRequestByRequesterName(Player target, String requesterName) {
		Map<UUID, PendingTeleportRequest> requests = pendingRequestsByTarget.get(target.getUniqueId());
		if (requests == null) {
			return null;
		}

		for (PendingTeleportRequest request : requests.values()) {
			if (request.getRequesterName().equalsIgnoreCase(requesterName)) {
				return request;
			}
		}
		return null;
	}

	public PendingTeleportRequest getLatestPendingRequest(Player target) {
		Map<UUID, PendingTeleportRequest> requests = pendingRequestsByTarget.get(target.getUniqueId());
		if (requests == null || requests.isEmpty()) {
			return null;
		}

		PendingTeleportRequest latest = null;
		for (PendingTeleportRequest request : requests.values()) {
			if (latest == null || request.getCreatedAt() > latest.getCreatedAt()) {
				latest = request;
			}
		}
		return latest;
	}

	public PendingTeleportRequest removePendingRequest(Player target, UUID requesterId) {
		Map<UUID, PendingTeleportRequest> requests = pendingRequestsByTarget.get(target.getUniqueId());
		if (requests == null) {
			return null;
		}

		PendingTeleportRequest removed = requests.remove(requesterId);
		if (requests.isEmpty()) {
			pendingRequestsByTarget.remove(target.getUniqueId());
		}
		return removed;
	}

	public PendingTeleportRequest removePendingRequestByRequesterName(Player target, String requesterName) {
		Map<UUID, PendingTeleportRequest> requests = pendingRequestsByTarget.get(target.getUniqueId());
		if (requests == null) {
			return null;
		}

		UUID matchedId = null;
		for (Map.Entry<UUID, PendingTeleportRequest> entry : requests.entrySet()) {
			if (entry.getValue().getRequesterName().equalsIgnoreCase(requesterName)) {
				matchedId = entry.getKey();
				break;
			}
		}

		if (matchedId == null) {
			return null;
		}

		PendingTeleportRequest removed = requests.remove(matchedId);
		if (requests.isEmpty()) {
			pendingRequestsByTarget.remove(target.getUniqueId());
		}
		return removed;
	}

	public int getPendingRequestCount(Player target) {
		Map<UUID, PendingTeleportRequest> requests = pendingRequestsByTarget.get(target.getUniqueId());
		return requests == null ? 0 : requests.size();
	}

	public Collection<PendingTeleportRequest> getPendingRequests(Player target) {
		Map<UUID, PendingTeleportRequest> requests = pendingRequestsByTarget.get(target.getUniqueId());
		if (requests == null) {
			return Collections.emptyList();
		}
		return new ArrayList<>(requests.values());
	}

	public Collection<PendingTeleportRequest> getAllPendingRequests() {
		List<PendingTeleportRequest> all = new ArrayList<>();
		for (Map<UUID, PendingTeleportRequest> map : pendingRequestsByTarget.values()) {
			all.addAll(map.values());
		}
		return all;
	}

	public void setActiveTeleport(Player requester, ActiveTeleport activeTeleport) {
		activeTeleportsByRequester.put(requester.getUniqueId(), activeTeleport);
	}

	public ActiveTeleport getActiveTeleport(Player requester) {
		return activeTeleportsByRequester.get(requester.getUniqueId());
	}

	public ActiveTeleport removeActiveTeleport(Player requester) {
		return activeTeleportsByRequester.remove(requester.getUniqueId());
	}

	public boolean hasActiveTeleport(Player requester) {
		return activeTeleportsByRequester.containsKey(requester.getUniqueId());
	}

	public Collection<ActiveTeleport> getActiveTeleports() {
		return new ArrayList<>(activeTeleportsByRequester.values());
	}

	public void setPendingOffer(Player player, PendingTeleportOffer offer) {
		pendingOffers.put(player.getUniqueId(), offer);
	}

	public PendingTeleportOffer getPendingOffer(Player player) {
		return pendingOffers.get(player.getUniqueId());
	}

	public PendingTeleportOffer removePendingOffer(Player player) {
		return pendingOffers.remove(player.getUniqueId());
	}

	public boolean hasPendingOffer(Player player) {
		return pendingOffers.containsKey(player.getUniqueId());
	}
}