package basementhost.randomchad.util;

import basementhost.randomchad.model.TeleportOffer;
import basementhost.randomchad.service.LangService;
import org.bukkit.entity.Player;

import java.util.Map;

public final class HomeMessageUtil {

	private HomeMessageUtil() {
	}

	public static void sendHomeQuoteMessage(Player player, String homeName, TeleportOffer offer, LangService langService) {
		player.sendMessage(langService.get("home.quote-created", Map.of(
				"name", homeName
		)));

		player.sendMessage(langService.get("home.destination", Map.of(
				"name", homeName
		)));

		QuoteMessageUtil.sendOfferDetails(player, offer, langService);
	}
}