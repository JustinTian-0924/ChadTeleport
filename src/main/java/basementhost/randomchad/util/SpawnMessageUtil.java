package basementhost.randomchad.util;

import basementhost.randomchad.model.TeleportOffer;
import basementhost.randomchad.service.LangService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Map;

public final class SpawnMessageUtil {

	private SpawnMessageUtil() {
	}

	public static void sendSpawnQuoteMessage(Player player, TeleportOffer offer, LangService langService) {
		player.sendMessage(langService.get("spawn.quote-created"));
		player.sendMessage(langService.get("spawn.destination"));

		player.sendMessage(langService.get("quote.distance", Map.of(
				"distance", String.valueOf(offer.getDistance())
		)));

		player.sendMessage(langService.get("quote.fee", Map.of(
				"price", String.valueOf(offer.getPrice())
		)));

		player.sendMessage(langService.get("quote.warmup", Map.of(
				"seconds", String.valueOf(offer.getWarmupSeconds()),
				"ticks", String.valueOf(offer.getWarmupTicks())
		)));

		String crossWorldText = offer.isCrossWorld()
				? langService.getRaw("general.text-yes")
				: langService.getRaw("general.text-no");

		player.sendMessage(langService.get("quote.cross-world", Map.of(
				"cross_world", crossWorldText
		)));

		Component confirmButton = Component.text(
						langService.getRaw("buttons.confirm"),
						NamedTextColor.GREEN
				)
				.clickEvent(ClickEvent.runCommand("/teleport confirm"))
				.hoverEvent(HoverEvent.showText(langService.get("quote.confirm-hover")));

		Component cancelButton = Component.text(
						langService.getRaw("buttons.cancel"),
						NamedTextColor.RED
				)
				.clickEvent(ClickEvent.runCommand("/teleport cancel"))
				.hoverEvent(HoverEvent.showText(langService.get("quote.cancel-hover")));

		Component buttonsLine = confirmButton
				.append(Component.text(" | ", NamedTextColor.DARK_GRAY))
				.append(cancelButton);

		player.sendMessage(buttonsLine);
	}
}