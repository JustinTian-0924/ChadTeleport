package basementhost.randomchad.util;

import basementhost.randomchad.model.TeleportOffer;
import basementhost.randomchad.service.LangService;
import org.bukkit.entity.Player;

import java.util.Map;

public final class WarpMessageUtil {

	private WarpMessageUtil() {
	}

	public static void sendWarpQuoteMessage(Player player, String warpName, TeleportOffer offer, LangService langService) {
		player.sendMessage(langService.get("warp.quote-created", Map.of(
				"name", warpName
		)));

		player.sendMessage(langService.get("warp.destination", Map.of(
				"name", warpName
		)));

		QuoteMessageUtil.sendOfferDetails(player, offer, langService);
	}
}