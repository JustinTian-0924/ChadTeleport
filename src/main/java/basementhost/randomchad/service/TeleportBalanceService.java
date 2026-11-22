package basementhost.randomchad.service;

import basementhost.randomchad.model.TeleportBalance;
import basementhost.randomchad.service.ConfigService.BalanceSettings;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class TeleportBalanceService {

	private final NamespacedKey balanceKey;
	private final NamespacedKey recoveryKey;
	private final ConfigService configService;

	public TeleportBalanceService(JavaPlugin plugin, ConfigService configService) {
		this.balanceKey = new NamespacedKey(plugin, "teleport_balance");
		this.recoveryKey = new NamespacedKey(plugin, "teleport_balance_recovery_since");
		this.configService = configService;
	}

	public TeleportBalance getOrCreate(Player player, long now) {
		BalanceSettings settings = configService.getBalanceSettings();
		TeleportBalance balance = refresh(player, now, settings);
		if (balance == null) {
			int startingPoints = Math.min(settings.startingPoints(), settings.recovery().maxPoints());
			balance = new TeleportBalance(startingPoints, 0).recover(now, settings.recovery());
			save(player, balance);
		}
		return balance;
	}

	public int getAvailablePoints(Player player) {
		TeleportBalance balance = refresh(player, System.currentTimeMillis(), configService.getBalanceSettings());
		return balance == null ? 0 : balance.points();
	}

	public TeleportBalance reserve(Player player, int points) {
		long now = System.currentTimeMillis();
		BalanceSettings settings = configService.getBalanceSettings();
		TeleportBalance before = refresh(player, now, settings);
		if (before == null || before.points() < points) {
			return null;
		}
		TeleportBalance after = new TeleportBalance(before.points() - points, before.recoverySinceMillis());
		if (settings.enabled()) {
			// Spending from a full balance starts a new interval. Spending below the cap keeps progress.
			after = after.recover(now, settings.recovery());
		}
		save(player, after);
		return before;
	}

	private TeleportBalance refresh(Player player, long now, BalanceSettings settings) {
		var data = player.getPersistentDataContainer();
		Integer points = data.get(balanceKey, PersistentDataType.INTEGER);
		if (points == null) {
			return null;
		}
		long recoverySince = data.getOrDefault(recoveryKey, PersistentDataType.LONG, 0L);
		TeleportBalance stored = new TeleportBalance(points, recoverySince);
		TeleportBalance current = settings.enabled() ? stored.recover(now, settings.recovery()) : stored;
		if (!current.equals(stored)) {
			save(player, current);
		}
		return current;
	}

	public void save(Player player, TeleportBalance balance) {
		var data = player.getPersistentDataContainer();
		// Keep zero balances too, so a depleted account never receives another starter grant.
		data.set(balanceKey, PersistentDataType.INTEGER, balance.points());
		data.set(recoveryKey, PersistentDataType.LONG, balance.recoverySinceMillis());
	}
}
