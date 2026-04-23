package basementhost.randomchad.listener;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.model.PendingTeleportRequest;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Map;

public class RequestTargetMoveInvalidationListener implements Listener {

	private final ChadteleportPlugin plugin;

	public RequestTargetMoveInvalidationListener(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		if (!plugin.getConfigService().isRequestCancelledOnTargetMove()) {
			return;
		}

		Player target = event.getPlayer();

		if (plugin.getTeleportManager().getPendingRequestCount(target) <= 0) {
			return;
		}

		Location from = event.getFrom();
		Location to = event.getTo();
		if (to == null) {
			return;
		}

		if (sameBlock(from, to)) {
			return;
		}

		double maxDistance = plugin.getConfigService().getRequestTargetMaxMoveDistance();

		for (PendingTeleportRequest request : plugin.getTeleportManager().getPendingRequests(target)) {
			Location anchor = request.getTargetAnchorLocation();

			if (anchor.getWorld() == null || to.getWorld() == null || !anchor.getWorld().equals(to.getWorld())) {
				expireRequest(target, request, "request.expired-target-world");
				continue;
			}

			double moved = flatDistance(anchor, to);
			if (moved > maxDistance) {
				expireRequest(target, request, "request.expired-target-moved");
			}
		}
	}

	private void expireRequest(Player target, PendingTeleportRequest request, String key) {
		PendingTeleportRequest removed = plugin.getTeleportManager()
				.removePendingRequest(target, request.getRequesterId());

		if (removed == null) {
			return;
		}

		Map<String, String> placeholders = Map.of(
				"requester", request.getRequesterName(),
				"target", request.getTargetName()
		);

		plugin.getLangService().send(target, key, placeholders);

		Player requester = plugin.getServer().getPlayer(request.getRequesterId());
		if (requester != null && requester.isOnline()) {
			plugin.getLangService().send(requester, key, placeholders);
		}
	}

	private boolean sameBlock(Location a, Location b) {
		return a.getBlockX() == b.getBlockX()
				&& a.getBlockY() == b.getBlockY()
				&& a.getBlockZ() == b.getBlockZ();
	}

	private double flatDistance(Location a, Location b) {
		double dx = a.getX() - b.getX();
		double dz = a.getZ() - b.getZ();
		return Math.sqrt(dx * dx + dz * dz);
	}
}