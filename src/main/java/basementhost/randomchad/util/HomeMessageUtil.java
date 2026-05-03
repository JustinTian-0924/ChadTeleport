package basementhost.randomchad.util;

import basementhost.randomchad.model.TeleportOffer;
import basementhost.randomchad.service.LangService;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Map;

public final class HomeMessageUtil {

	private HomeMessageUtil() {
	}

	public static void sendHomeQuoteMessage(Player player, String homeName, TeleportOffer offer, LangService langService) {
		Component title = langService.get("home.quote-created", Map.of(
				"name", homeName
		));

		Component destination = langService.get("home.destination", Map.of(
				"name", homeName
		));

		QuoteMessageUtil.sendOfferQuote(player, offer, title, destination, langService);
	}
}