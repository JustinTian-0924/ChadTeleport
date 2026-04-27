package basementhost.randomchad.util;

import basementhost.randomchad.model.TeleportOffer;
import basementhost.randomchad.service.LangService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
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