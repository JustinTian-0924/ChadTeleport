package basementhost.randomchad.service;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class TeleportBalanceService {

	private final NamespacedKey balanceKey;

	public TeleportBalanceService(JavaPlugin plugin) {
		this.balanceKey = new NamespacedKey(plugin, "teleport_balance");
	}

	public int getOrCreate(Player player, int startingPoints) {
		var data = player.getPersistentDataContainer();
		Integer points = data.get(balanceKey, PersistentDataType.INTEGER);
		if (points == null) {
			// Keep zero balances too, so a depleted account never receives another starter grant.
			data.set(balanceKey, PersistentDataType.INTEGER, startingPoints);
			return startingPoints;
		}
		return points;
	}

	public int getSavedPoints(Player player) {
		return player.getPersistentDataContainer().getOrDefault(balanceKey, PersistentDataType.INTEGER, 0);
	}

	public void setPoints(Player player, int points) {
		player.getPersistentDataContainer().set(balanceKey, PersistentDataType.INTEGER, points);
	}
}
