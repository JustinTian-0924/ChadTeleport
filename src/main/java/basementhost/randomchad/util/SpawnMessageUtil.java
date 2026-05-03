package basementhost.randomchad.util;

import basementhost.randomchad.model.TeleportOffer;
import basementhost.randomchad.service.LangService;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public final class SpawnMessageUtil {

	private SpawnMessageUtil() {
	}

	public static void sendSpawnQuoteMessage(Player player, TeleportOffer offer, LangService langService) {
		Component title = langService.get("spawn.quote-created");
		Component destination = langService.get("spawn.destination");

		QuoteMessageUtil.sendOfferQuote(player, offer, title, destination, langService);
	}
}