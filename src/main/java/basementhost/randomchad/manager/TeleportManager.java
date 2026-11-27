package basementhost.randomchad.manager;

import basementhost.randomchad.model.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TeleportManager {

	private final JavaPlugin plugin;
	private final Map<UUID, PendingTeleportQuote> pendingQuotes = new ConcurrentHashMap<>();

	// target UUID -> (requester UUID -> request), @_@
	private final Map<UUID, Map<UUID, PendingTeleportRequest>> pendingRequestsByTarget = new HashMap<>();

	private final Map<UUID, ActiveTeleport> activeTeleportsByRequester = new ConcurrentHashMap<>();

	private final Map<UUID, PendingTeleportOffer> pendingOffers = new ConcurrentHashMap<>();

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
		return removePendingQuote(player.getUniqueId());
	}

	public PendingTeleportQuote removePendingQuote(UUID playerId) {
		return pendingQuotes.remove(playerId);
	}

	public boolean removePendingQuote(UUID playerId, PendingTeleportQuote expected) {
		return pendingQuotes.remove(playerId, expected);
	}

	public boolean hasPendingQuote(Player player) {
		return pendingQuotes.containsKey(player.getUniqueId());
	}

	public Collection<Map.Entry<UUID, PendingTeleportQuote>> getAllPendingQuotes() {
		return new ArrayList<>(pendingQuotes.entrySet());
	}

	public synchronized void setPendingRequest(Player target, PendingTeleportRequest request) {
		pendingRequestsByTarget
				.computeIfAbsent(target.getUniqueId(), k -> new LinkedHashMap<>())
				.put(request.getRequesterId(), request);
	}

	public synchronized PendingTeleportRequest getPendingRequest(Player target, UUID requesterId) {
		Map<UUID, PendingTeleportRequest> requests = pendingRequestsByTarget.get(target.getUniqueId());
		if (requests == null) {
			return null;
		}
		return requests.get(requesterId);
	}

	public synchronized PendingTeleportRequest getPendingRequestByRequesterName(Player target, String requesterName) {
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

	public synchronized PendingTeleportRequest getLatestPendingRequest(Player target) {
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

	public synchronized PendingTeleportRequest removePendingRequest(Player target, UUID requesterId) {
		return removePendingRequest(target.getUniqueId(), requesterId);
	}

	public synchronized PendingTeleportRequest removePendingRequest(UUID targetId, UUID requesterId) {
		Map<UUID, PendingTeleportRequest> requests = pendingRequestsByTarget.get(targetId);
		if (requests == null) {
			return null;
		}

		PendingTeleportRequest removed = requests.remove(requesterId);
		if (requests.isEmpty()) {
			pendingRequestsByTarget.remove(targetId);
		}
		return removed;
	}

	public synchronized boolean removePendingRequest(PendingTeleportRequest expected) {
		Map<UUID, PendingTeleportRequest> requests = pendingRequestsByTarget.get(expected.getTargetId());
		if (requests == null || !requests.remove(expected.getRequesterId(), expected)) {
			return false;
		}
		if (requests.isEmpty()) {
			pendingRequestsByTarget.remove(expected.getTargetId());
		}
		return true;
	}

	public synchronized PendingTeleportRequest removePendingRequestByRequesterName(Player target, String requesterName) {
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

	public synchronized int getPendingRequestCount(Player target) {
		Map<UUID, PendingTeleportRequest> requests = pendingRequestsByTarget.get(target.getUniqueId());
		return requests == null ? 0 : requests.size();
	}

	public synchronized Collection<PendingTeleportRequest> getPendingRequests(Player target) {
		Map<UUID, PendingTeleportRequest> requests = pendingRequestsByTarget.get(target.getUniqueId());
		if (requests == null) {
			return Collections.emptyList();
		}
		return new ArrayList<>(requests.values());
	}

	public synchronized Collection<PendingTeleportRequest> getAllPendingRequests() {
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

	public boolean removeActiveTeleport(Player requester, ActiveTeleport expected) {
		return activeTeleportsByRequester.remove(requester.getUniqueId(), expected);
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
		return removePendingOffer(player.getUniqueId());
	}

	public PendingTeleportOffer removePendingOffer(UUID playerId) {
		return pendingOffers.remove(playerId);
	}

	public boolean removePendingOffer(UUID playerId, PendingTeleportOffer expected) {
		return pendingOffers.remove(playerId, expected);
	}

	public Collection<Map.Entry<UUID, PendingTeleportOffer>> getAllPendingOffers() {
		return new ArrayList<>(pendingOffers.entrySet());
	}

	public boolean hasPendingOffer(Player player) {
		return pendingOffers.containsKey(player.getUniqueId());
	}
}
