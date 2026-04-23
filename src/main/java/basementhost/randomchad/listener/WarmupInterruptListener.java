package basementhost.randomchad.listener;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.model.ActiveTeleport;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class WarmupInterruptListener implements Listener {

	private final ChadteleportPlugin plugin;

	public WarmupInterruptListener(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();

		ActiveTeleport activeTeleport = plugin.getTeleportManager().getActiveTeleport(player);
		if (activeTeleport == null) {
			return;
		}

		Location from = event.getFrom();
		Location to = event.getTo();
		if (to == null) {
			return;
		}

		if (positionChanged(from, to)) {
			plugin.getWarmupService().cancelWarmup(player, "warmup.cancelled-move");
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player player)) {
			return;
		}

		ActiveTeleport activeTeleport = plugin.getTeleportManager().getActiveTeleport(player);
		if (activeTeleport == null) {
			return;
		}

		plugin.getWarmupService().cancelWarmup(player, "warmup.cancelled-damage");
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player quittingPlayer = event.getPlayer();

		plugin.getWarmupService().cancelWarmup(quittingPlayer, "warmup.cancelled-left");

		for (ActiveTeleport activeTeleport : plugin.getTeleportManager().getActiveTeleports()) {
			Player target = activeTeleport.getQuote().getTarget();
			Player requester = activeTeleport.getQuote().getRequester();

			if (target != null && target.getUniqueId().equals(quittingPlayer.getUniqueId())) {
				if (requester != null) {
					plugin.getWarmupService().cancelWarmup(requester, "warmup.cancelled-target-left");
				}
			}
		}
	}

	private boolean positionChanged(Location from, Location to) {
		return from.getX() != to.getX()
				|| from.getY() != to.getY()
				|| from.getZ() != to.getZ();
	}
}