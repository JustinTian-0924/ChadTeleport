package basementhost.randomchad.util;

import basementhost.randomchad.model.TeleportQuote;
import basementhost.randomchad.service.LangService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Map;

public final class TeleportMessageUtil {

	private TeleportMessageUtil() {
	}

	public static void sendQuoteMessage(Player player, TeleportQuote quote, LangService langService) {
		player.sendMessage(langService.get("quote.created"));

		player.sendMessage(langService.get("quote.target", Map.of(
				"target", quote.getTarget().getName()
		)));

		player.sendMessage(langService.get("quote.distance", Map.of(
				"distance", String.valueOf(quote.getDistance())
		)));

		player.sendMessage(langService.get("quote.fee", Map.of(
				"price", String.valueOf(quote.getPrice())
		)));

		player.sendMessage(langService.get("quote.warmup", Map.of(
				"seconds", String.valueOf(quote.getWarmupSeconds()),
				"ticks", String.valueOf(quote.getWarmupTicks())
		)));

		String crossWorldText = quote.isCrossWorld()
				? langService.getRaw("general.yes")
				: langService.getRaw("general.no");

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

	public static void sendIncomingRequestMessage(
			Player target,
			String requesterName,
			int requestWindowSeconds,
			LangService langService
	) {
		target.sendMessage(langService.get("request.received"));
		target.sendMessage(langService.get("request.requester", Map.of(
				"requester", requesterName
		)));
		target.sendMessage(langService.get("request.time-to-respond", Map.of(
				"seconds", String.valueOf(requestWindowSeconds)
		)));

		Component acceptButton = Component.text(
						langService.getRaw("buttons.accept"),
						NamedTextColor.GREEN
				)
				.clickEvent(ClickEvent.runCommand("/tpaccept " + requesterName))
				.hoverEvent(HoverEvent.showText(
						langService.get("request.accept-hover", Map.of("requester", requesterName))
				));

		Component denyButton = Component.text(
						langService.getRaw("buttons.deny"),
						NamedTextColor.RED
				)
				.clickEvent(ClickEvent.runCommand("/tpdeny " + requesterName))
				.hoverEvent(HoverEvent.showText(
						langService.get("request.deny-hover", Map.of("requester", requesterName))
				));

		Component buttonsLine = acceptButton
				.append(Component.text(" | ", NamedTextColor.DARK_GRAY))
				.append(denyButton);

		target.sendMessage(buttonsLine);
	}
}