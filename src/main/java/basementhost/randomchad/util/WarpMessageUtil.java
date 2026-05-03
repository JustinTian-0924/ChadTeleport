package basementhost.randomchad.util;

import basementhost.randomchad.model.TeleportOffer;
import basementhost.randomchad.service.LangService;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Map;

public final class WarpMessageUtil {

	private WarpMessageUtil() {
	}

	public static void sendWarpQuoteMessage(Player player, String warpName, TeleportOffer offer, LangService langService) {
		Component title = langService.get("warp.quote-created", Map.of(
				"name", warpName
		));

		Component destination = langService.get("warp.destination", Map.of(
				"name", warpName
		));

		QuoteMessageUtil.sendOfferQuote(player, offer, title, destination, langService);
	}
}