package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.model.PendingTeleportOffer;
import basementhost.randomchad.model.PendingTeleportQuote;
import basementhost.randomchad.model.PendingTeleportRequest;
import basementhost.randomchad.model.TeleportOffer;
import basementhost.randomchad.model.TeleportQuote;
import basementhost.randomchad.util.TeleportMessageUtil;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TeleportCommand implements BasicCommand {

	private final ChadteleportPlugin plugin;
	private final String shortcutAction;

	public TeleportCommand(ChadteleportPlugin plugin) {
		this(plugin, null);
	}

	public TeleportCommand(ChadteleportPlugin plugin, String shortcutAction) {
		this.plugin = plugin;
		this.shortcutAction = shortcutAction;
	}

	@Override
	public void execute(CommandSourceStack source, String[] args) {
		CommandSender sender = source.getSender();
		if (!(sender instanceof Player player)) {
			plugin.getLangService().send(sender, "general.player-only");
			return;
		}

		if (shortcutAction != null) {
			// Shortcuts require no arguments, while legacy syntax—such as `/tpconfirm confirm`—remains supported.
			if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase(shortcutAction))) {
				args = new String[]{shortcutAction};
			} else {
				plugin.getLangService().send(player, "general.usage.teleport");
				return;
			}
		}

		if (args.length != 1) {
			plugin.getLangService().send(player, "general.usage.teleport");
			return;
		}

		if (args[0].equalsIgnoreCase("confirm")) {
			PendingTeleportOffer pendingOffer = plugin.getTeleportManager().getPendingOffer(player);
			if (pendingOffer != null) {
				long ageMillis = System.currentTimeMillis() - pendingOffer.getCreatedAt();
				long maxAgeMillis = plugin.getConfigService().getQuoteExpireSeconds() * 1000L;

				if (ageMillis > maxAgeMillis) {
					plugin.getTeleportManager().removePendingOffer(player);
					plugin.getLangService().send(player, "quote.expired");
					return;
				}

				TeleportOffer offer = pendingOffer.getOffer();
				plugin.getTeleportManager().removePendingOffer(player);
				plugin.getWarmupService().startOfferWarmup(offer);
				return;
			}

			PendingTeleportQuote pending = plugin.getTeleportManager().getPendingQuote(player);
			if (pending == null) {
				plugin.getLangService().send(player, "quote.none");
				return;
			}

			long ageMillis = System.currentTimeMillis() - pending.getCreatedAt();
			long maxAgeMillis = plugin.getConfigService().getQuoteExpireSeconds() * 1000L;
			if (ageMillis > maxAgeMillis) {
				plugin.getTeleportManager().removePendingQuote(player);
				plugin.getLangService().send(player, "quote.expired");
				return;
			}

			TeleportQuote quote = pending.getQuote();
			Player target = quote.getTarget();

			if (target == null || !target.isOnline()) {
				plugin.getTeleportManager().removePendingQuote(player);
				plugin.getLangService().send(player, "general.target-offline");
				return;
			}

			plugin.getTeleportManager().removePendingQuote(player);

			PendingTeleportRequest request = new PendingTeleportRequest(quote);
			plugin.getTeleportManager().setPendingRequest(target, request);

			int requestWindow = plugin.getConfigService().getRequestExpireSeconds();

			plugin.getLangService().send(player, "request.sent", Map.of(
					"target", target.getName()
			));
			plugin.getLangService().send(player, "request.waiting");

			TeleportMessageUtil.sendIncomingRequestMessage(
					target,
					player.getName(),
					requestWindow,
					plugin.getLangService()
			);

			int count = plugin.getTeleportManager().getPendingRequestCount(target);
			if (count > 1) {
				plugin.getLangService().send(target, "request.multiple-hint-count", Map.of(
						"count", String.valueOf(count)
				));
				plugin.getLangService().send(target, "request.multiple-hint-usage");
			}

			return;
		}

		if (args[0].equalsIgnoreCase("cancel")) {
			PendingTeleportOffer removedOffer = plugin.getTeleportManager().removePendingOffer(player);
			PendingTeleportQuote removedQuote = plugin.getTeleportManager().removePendingQuote(player);

			boolean cancelledWarmup = plugin.getWarmupService().cancelWarmup(
					player,
					"warmup.cancel-command"
			);

			if (removedOffer == null && removedQuote == null && !cancelledWarmup) {
				plugin.getLangService().send(player, "teleport.no-pending-or-warmup");
				return;
			}

			if (removedOffer != null || removedQuote != null) {
				plugin.getLangService().send(player, "quote.cancelled");
			}

			return;
		}

		plugin.getLangService().send(player, "general.usage.teleport");
	}

	@Override
	public List<String> suggest(CommandSourceStack source, String[] args) {
		if (shortcutAction != null || args.length != 1) {
			return List.of();
		}

		String input = args[0].toLowerCase(Locale.ROOT);
		return List.of("confirm", "cancel").stream()
				.filter(action -> action.startsWith(input))
				.toList();
	}
}
