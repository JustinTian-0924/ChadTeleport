package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.exception.LocalizedException;
import basementhost.randomchad.model.PendingTeleportQuote;
import basementhost.randomchad.model.TeleportQuote;
import basementhost.randomchad.util.TeleportMessageUtil;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpaCommand implements BasicCommand {

	private final ChadteleportPlugin plugin;

	public TpaCommand(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSourceStack source, String[] args) {
		CommandSender sender = source.getSender();
		if (!(sender instanceof Player player)) {
			plugin.getLangService().send(sender, "general.player-only");
			return;
		}

		if (!plugin.getConfigService().isFeatureEnabled("tpa")) {
			plugin.getLangService().send(player, "feature.tpa-disabled");
			return;
		}

		if (args.length != 1) {
			plugin.getLangService().send(player, "general.usage.tpa");
			return;
		}

		Player target = Bukkit.getPlayerExact(args[0]);
		if (target == null || !target.isOnline()) {
			plugin.getLangService().send(player, "general.target-offline");
			return;
		}

		if (target.equals(player)) {
			plugin.getLangService().send(player, "feature.self-request");
			return;
		}

		try {
			TeleportQuote quote = plugin.getTeleportCalculator().calculateTpaQuote(player, target);

			if (!plugin.getTeleportPaymentService().checkCanAfford(player, quote.getPayment())) {
				return;
			}

			plugin.getTeleportManager().setPendingQuote(player, new PendingTeleportQuote(quote));
			TeleportMessageUtil.sendQuoteMessage(player, quote, plugin.getLangService());

		} catch (LocalizedException e) {
			plugin.getLangService().send(player, e.getMessageKey(), e.getPlaceholders());
		} catch (IllegalArgumentException e) {
			player.sendPlainMessage(e.getMessage());
		} catch (Exception e) {
			plugin.getLangService().send(player, "quote.calculate-failed");
			plugin.getLogger().warning("Failed to calculate TPA quote: " + e.getMessage());
			e.printStackTrace();
		}

	}
}
