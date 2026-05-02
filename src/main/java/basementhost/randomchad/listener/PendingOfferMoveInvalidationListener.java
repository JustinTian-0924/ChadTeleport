package basementhost.randomchad.listener;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.model.PendingTeleportOffer;
import basementhost.randomchad.model.TeleportOffer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PendingOfferMoveInvalidationListener implements Listener {

	private final ChadteleportPlugin plugin;

	public PendingOfferMoveInvalidationListener(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		if (!plugin.getConfigService().isQuoteCancelledOnMove()) {
			return;
		}

		Player player = event.getPlayer();

		PendingTeleportOffer pendingOffer = plugin.getTeleportManager().getPendingOffer(player);
		if (pendingOffer == null) {
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

		TeleportOffer offer = pendingOffer.getOffer();
		Location anchor = offer.getFrom();

		if (anchor == null || anchor.getWorld() == null || to.getWorld() == null) {
			plugin.getTeleportManager().removePendingOffer(player);
			plugin.getLangService().send(player, "quote.expired");
			return;
		}

		if (!anchor.getWorld().equals(to.getWorld())) {
			plugin.getTeleportManager().removePendingOffer(player);
			plugin.getLangService().send(player, "quote.expired-moved");
			return;
		}

		double maxDistance = plugin.getConfigService().getQuoteMaxMoveDistance();
		double moved = flatDistance(anchor, to);

		if (moved > maxDistance) {
			plugin.getTeleportManager().removePendingOffer(player);
			plugin.getLangService().send(player, "quote.expired-moved");
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