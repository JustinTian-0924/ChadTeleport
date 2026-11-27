package basementhost.randomchad.service;

import basementhost.randomchad.exception.LocalizedException;
import basementhost.randomchad.model.TeleportHeat;
import basementhost.randomchad.model.TeleportOffer;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.util.Map;

public class TeleportHeatService {

	private static final String BYPASS_PERMISSION = "chadteleport.bypass.overheat";

	private final NamespacedKey heatKey;
	private final ConfigService configService;

	public TeleportHeatService(JavaPlugin plugin, ConfigService configService) {
		this.heatKey = new NamespacedKey(plugin, "teleport_heat");
		this.configService = configService;
	}

	public void requireCoolEnough(Player player) {
		var settings = configService.getHeatSettings();
		if (!settings.enabled() || player.hasPermission(BYPASS_PERMISSION)) {
			return;
		}
		long seconds = read(player).secondsUntilAllowed(System.currentTimeMillis(), settings);
		if (seconds > 0) {
			throw new LocalizedException("overheat.blocked", Map.of("seconds", String.valueOf(seconds)));
		}
	}

	public void recordSuccessfulTeleport(Player player, TeleportOffer offer) {
		var settings = configService.getHeatSettings();
		if (!settings.enabled() || player.hasPermission(BYPASS_PERMISSION)) {
			return;
		}
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

	public void reset(Player player) {
		player.getPersistentDataContainer().remove(heatKey);
	}

	public void sendStatus(CommandSender recipient, Player player, LangService messages) {
		var settings = configService.getHeatSettings();
		long now = System.currentTimeMillis();
		var heat = read(player);
		String current = BigDecimal.valueOf(heat.heatAt(now, settings), 3).stripTrailingZeros().toPlainString();
		messages.send(recipient, "overheat.current", Map.of(
				"heat", current, "threshold", String.valueOf(settings.threshold())));
		if (!settings.enabled()) {
			messages.send(recipient, "overheat.disabled");
			return;
		}
		if (player.hasPermission(BYPASS_PERMISSION)) {
			messages.send(recipient, "overheat.bypassed");
			return;
		}
		long seconds = heat.secondsUntilAllowed(now, settings);
		if (seconds > 0) {
			messages.send(recipient, "overheat.blocked", Map.of("seconds", String.valueOf(seconds)));
		} else {
			messages.send(recipient, "overheat.ready");
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
