package basementhost.randomchad.util;

import basementhost.randomchad.model.TeleportOffer;
import basementhost.randomchad.model.TeleportQuote;
import basementhost.randomchad.service.LangService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Map;

public final class QuoteMessageUtil {

	private QuoteMessageUtil() {
	}

	public static void sendOfferQuote(
			Player player,
			TeleportOffer offer,
			Component title,
			Component destination,
			LangService langService
	) {
		player.sendMessage(title);
		player.sendMessage(destination);

		sendCommonQuoteDetails(
				player,
				offer.getDistance(),
				offer.getPrice(),
				offer.getWarmupSeconds(),
				offer.getWarmupTicks(),
				offer.isCrossWorld(),
				langService
		);
	}

	public static void sendPlayerQuote(
			Player player,
			TeleportQuote quote,
			Component title,
			Component destination,
			LangService langService
	) {
		player.sendMessage(title);
		player.sendMessage(destination);

		sendCommonQuoteDetails(
				player,
				quote.getDistance(),
				quote.getPrice(),
				quote.getWarmupSeconds(),
				quote.getWarmupTicks(),
				quote.isCrossWorld(),
				langService
		);
	}

	private static void sendCommonQuoteDetails(
			Player player,
			double distance,
			double price,
			double warmupSeconds,
			long warmupTicks,
			boolean crossWorld,
			LangService langService
	) {
		player.sendMessage(langService.get("quote.distance", Map.of(
				"distance", String.valueOf(distance)
		)));

		player.sendMessage(langService.get("quote.fee", Map.of(
				"price", String.valueOf(price)
		)));

		player.sendMessage(langService.get("quote.warmup", Map.of(
				"seconds", String.valueOf(warmupSeconds),
				"ticks", String.valueOf(warmupTicks)
		)));

		String crossWorldText = crossWorld
				? langService.getRaw("general.text-yes")
				: langService.getRaw("general.text-no");

		player.sendMessage(langService.get("quote.cross-world", Map.of(
				"cross_world", crossWorldText
		)));

		sendConfirmCancelButtons(player, langService);
	}

	public static void sendConfirmCancelButtons(Player player, LangService langService) {
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