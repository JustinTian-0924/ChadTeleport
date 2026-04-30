package basementhost.randomchad.util;

import basementhost.randomchad.model.TeleportOffer;
import basementhost.randomchad.service.LangService;
import org.bukkit.entity.Player;

import java.util.Map;

public final class WarpMessageUtil {

	private WarpMessageUtil() {
	}

	public static void sendWarpQuoteMessage(Player player, String warpName, TeleportOffer offer, LangService langService) {
		langService.send(player, "warp.quote-created", Map.of(
				"name", warpName
		));

		langService.send(player, "teleport.quote-distance", Map.of(
				"distance", String.valueOf(offer.getDistance())
		));

		langService.send(player, "teleport.quote-price", Map.of(
				"price", String.valueOf(offer.getPrice())
		));

		langService.send(player, "teleport.quote-warmup", Map.of(
				"seconds", String.valueOf(offer.getWarmupSeconds())
		));

		langService.send(player, "teleport.quote-cross-world", Map.of(
				"value", String.valueOf(offer.isCrossWorld())
		));

		langService.send(player, "warp.destination", Map.of(
				"name", warpName
		));

		langService.send(player, "teleport.quote-confirm-hint");
	}
}