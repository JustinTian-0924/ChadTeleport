package basementhost.randomchad.util;

import basementhost.randomchad.model.TeleportOffer;
import basementhost.randomchad.service.LangService;
import org.bukkit.entity.Player;

public final class SpawnMessageUtil {

	private SpawnMessageUtil() {
	}

	public static void sendSpawnQuoteMessage(Player player, TeleportOffer offer, LangService langService) {
		player.sendMessage(langService.get("spawn.quote-created"));
		player.sendMessage(langService.get("spawn.destination"));

		QuoteMessageUtil.sendOfferDetails(player, offer, langService);
	}
}