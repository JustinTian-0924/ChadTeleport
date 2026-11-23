package basementhost.randomchad.util;

import basementhost.randomchad.model.TeleportOffer;
import basementhost.randomchad.model.TeleportPayment;
import basementhost.randomchad.model.TeleportQuote;
import basementhost.randomchad.model.TeleportRoute;
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
				offer.getRoute(),
				offer.getPayment(),
				offer.getWarmupSeconds(),
				offer.getWarmupTicks(),
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
				quote.getRoute(),
				quote.getPayment(),
				quote.getWarmupSeconds(),
				quote.getWarmupTicks(),
				langService
		);
	}

	private static void sendCommonQuoteDetails(
			Player player,
			TeleportRoute route,
			TeleportPayment payment,
			double warmupSeconds,
			long warmupTicks,
			LangService langService
	) {
		player.sendMessage(langService.get("quote.distance", Map.of(
				"distance", String.valueOf(Math.floor(route.totalDistance() * 10.0) / 10.0)
		)));

		String coins = String.valueOf(payment.coins());
		if (payment.points() > 0) {
			langService.send(player, "balance.quote", Map.of(
					"name", payment.balanceName(), "points", String.valueOf(payment.points()), "price", coins));
		} else {
			langService.send(player, "quote.fee", Map.of("price", coins));
		}

		player.sendMessage(langService.get("quote.warmup", Map.of(
				"seconds", String.valueOf(warmupSeconds),
				"ticks", String.valueOf(warmupTicks)
		)));

		String crossWorldText = route.isCrossWorld()
				? langService.getRaw("general.text-yes")
				: langService.getRaw("general.text-no");

		player.sendMessage(langService.get("quote.cross-world", Map.of(
				"cross_world", crossWorldText
		)));

		sendConfirmCancelButtons(player, langService);
	}

	public static void sendConfirmCancelButtons(Player player, LangService langService) {
		Component confirmButton = langService.get("buttons.confirm")
				.colorIfAbsent(NamedTextColor.GREEN)
				.clickEvent(ClickEvent.runCommand("/teleport confirm"))
				.hoverEvent(HoverEvent.showText(langService.get("quote.confirm-hover")));

		Component cancelButton = langService.get("buttons.cancel")
				.colorIfAbsent(NamedTextColor.RED)
				.clickEvent(ClickEvent.runCommand("/teleport cancel"))
				.hoverEvent(HoverEvent.showText(langService.get("quote.cancel-hover")));

		Component buttonsLine = Component.empty()
				.append(confirmButton)
				.append(Component.text(" | ", NamedTextColor.DARK_GRAY))
				.append(cancelButton);

		player.sendMessage(buttonsLine);
	}
}
