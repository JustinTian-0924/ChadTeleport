package basementhost.randomchad.listener;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.model.PendingTeleportQuote;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Map;
import java.util.UUID;

public class QuoteInvalidationListener implements Listener {

	private final ChadteleportPlugin plugin;

	public QuoteInvalidationListener(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player movedPlayer = event.getPlayer();

		Location from = event.getFrom();
		Location to = event.getTo();
		if (to == null) {
			return;
		}

		if (sameBlock(from, to)) {
			return;
		}

		invalidateRequesterOwnedQuoteIfNeeded(movedPlayer, to);
		invalidateQuotesIfTargetMovedTooFar(movedPlayer, to);
	}

	private void invalidateRequesterOwnedQuoteIfNeeded(Player player, Location to) {
		PendingTeleportQuote pendingQuote = plugin.getTeleportManager().getPendingQuote(player);
		if (pendingQuote == null) {
			return;
		}

		Location anchor = pendingQuote.getAnchorLocation();

		if (anchor.getWorld() == null || to.getWorld() == null || !anchor.getWorld().equals(to.getWorld())) {
			plugin.getTeleportManager().removePendingQuote(player);
			plugin.getLangService().send(player, "quote.expired-changed-world-self");
			return;
		}

		double maxMove = plugin.getConfigService().getQuoteMaxMoveDistance();
		double moved = flatDistance(anchor, to);

		if (moved > maxMove) {
			plugin.getTeleportManager().removePendingQuote(player);
			plugin.getLangService().send(player, "quote.expired-moved-self");
		}
	}

	private void invalidateQuotesIfTargetMovedTooFar(Player movedPlayer, Location to) {
		if (!plugin.getConfigService().isQuoteCancelledOnTargetMove()) {
			return;
		}

		double maxMove = plugin.getConfigService().getQuoteTargetMaxMoveDistance();
		UUID movedPlayerId = movedPlayer.getUniqueId();

		for (Map.Entry<UUID, PendingTeleportQuote> entry : plugin.getTeleportManager().getAllPendingQuotes()) {
			UUID requesterId = entry.getKey();
			PendingTeleportQuote pendingQuote = entry.getValue();

			if (!pendingQuote.getTargetId().equals(movedPlayerId)) {
				continue;
			}

			Location anchor = pendingQuote.getTargetAnchorLocation();

			String key;
			if (anchor.getWorld() == null || to.getWorld() == null || !anchor.getWorld().equals(to.getWorld())) {
				key = "quote.expired-target-world";
			} else {
				double moved = flatDistance(anchor, to);
				if (moved > maxMove) {
					key = "quote.expired-target-moved";
				} else {
					continue;
				}
			}

			Player requester = plugin.getServer().getPlayer(requesterId);
			if (requester == null || !requester.isOnline()) {
				continue;
			}

			plugin.getTeleportManager().removePendingQuote(requester);

			Map<String, String> placeholders = Map.of(
					"requester", requester.getName(),
					"target", pendingQuote.getTargetName()
			);

			plugin.getLangService().send(requester, key, placeholders);
			plugin.getLangService().send(movedPlayer, key, placeholders);
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