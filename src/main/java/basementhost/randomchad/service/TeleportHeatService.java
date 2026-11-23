package basementhost.randomchad.service;

import basementhost.randomchad.exception.LocalizedException;
import basementhost.randomchad.model.TeleportHeat;
import basementhost.randomchad.model.TeleportOffer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.util.Map;

public class TeleportHeatService {

	private final NamespacedKey heatKey;
	private final ConfigService configService;

	public TeleportHeatService(JavaPlugin plugin, ConfigService configService) {
		this.heatKey = new NamespacedKey(plugin, "teleport_heat");
		this.configService = configService;
	}

	public void requireCoolEnough(Player player) {
		var settings = configService.getHeatSettings();
		if (!settings.enabled()) {
			return;
		}
		long seconds = read(player).secondsUntilAllowed(System.currentTimeMillis(), settings);
		if (seconds > 0) {
			throw new LocalizedException("overheat.blocked", Map.of("seconds", String.valueOf(seconds)));
		}
	}

	public void recordSuccessfulTeleport(TeleportOffer offer) {
		var settings = configService.getHeatSettings();
		if (!settings.enabled()) {
			return;
		}
		Player player = offer.getRequester();
		var before = read(player);
		var updated = before.afterTeleport(System.currentTimeMillis(), settings, offer.getFeatureName(),
				offer.getRoute(), offer.getPayment().quotedPrice());
		if (updated.equals(before)) {
			return;
		}
		// Store heat and its timestamp together so the next read uses a matching pair.
		player.getPersistentDataContainer().set(heatKey, PersistentDataType.LONG_ARRAY,
				new long[]{updated.milliHeat(), updated.measuredAtMillis()});
	}

	public void sendStatus(Player player, LangService messages) {
		var settings = configService.getHeatSettings();
		if (!settings.enabled()) {
			messages.send(player, "overheat.disabled");
			return;
		}
		long now = System.currentTimeMillis();
		var heat = read(player);
		String current = BigDecimal.valueOf(heat.heatAt(now, settings), 3).stripTrailingZeros().toPlainString();
		messages.send(player, "overheat.current", Map.of(
				"heat", current, "threshold", String.valueOf(settings.threshold())));
		long seconds = heat.secondsUntilAllowed(now, settings);
		if (seconds > 0) {
			messages.send(player, "overheat.blocked", Map.of("seconds", String.valueOf(seconds)));
		} else {
			messages.send(player, "overheat.ready");
		}
	}

	private TeleportHeat read(Player player) {
		long[] saved = player.getPersistentDataContainer().get(heatKey, PersistentDataType.LONG_ARRAY);
		if (saved == null || saved.length != 2 || saved[0] < 0 || saved[1] < 0) {
			return new TeleportHeat(0, 0);
		}
		return new TeleportHeat(saved[0], saved[1]);
	}
}
