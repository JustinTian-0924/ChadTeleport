package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.service.TeleportBalanceService;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class TeleportBalanceCommand implements BasicCommand {

	private final ChadteleportPlugin plugin;
	private final TeleportBalanceService balanceService;

	public TeleportBalanceCommand(ChadteleportPlugin plugin, TeleportBalanceService balanceService) {
		this.plugin = plugin;
		this.balanceService = balanceService;
	}

	@Override
	public void execute(CommandSourceStack source, String[] args) {
		CommandSender sender = source.getSender();
		if (!(sender instanceof Player player)) {
			plugin.getLangService().send(sender, "general.player-only");
			return;
		}
		if (args.length != 0) {
			plugin.getLangService().send(player, "balance.usage");
			return;
		}
		var settings = plugin.getConfigService().getBalanceSettings();
		if (!settings.enabled()) {
			plugin.getLangService().send(player, "balance.disabled");
			return;
		}
		long now = System.currentTimeMillis();
		var balance = balanceService.getOrCreate(player, now);
		var recovery = settings.recovery();
		plugin.getLangService().send(player, "balance.current", Map.of(
				"name", settings.displayName(), "points", String.valueOf(balance.points())));
		String cap = String.valueOf(recovery.maxPoints());
		if (balance.points() >= recovery.maxPoints()) {
			plugin.getLangService().send(player, "balance.recovery-full", Map.of("cap", cap));
		} else {
			plugin.getLangService().send(player, "balance.recovery-next", Map.of("cap", cap,
					"seconds", String.valueOf(balance.secondsUntilRecovery(now, recovery))));
		}
	}
}
