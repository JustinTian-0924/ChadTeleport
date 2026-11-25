package basementhost.randomchad.command;

import basementhost.randomchad.service.LangService;
import basementhost.randomchad.service.TeleportHeatService;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

public class TeleportHeatCommand implements BasicCommand {

	private final TeleportHeatService heatService;
	private final LangService langService;

	public TeleportHeatCommand(TeleportHeatService heatService, LangService langService) {
		this.heatService = heatService;
		this.langService = langService;
	}

	@Override
	public void execute(CommandSourceStack source, String[] args) {
		var sender = source.getSender();
		if (!(sender instanceof Player player)) {
			langService.send(sender, "general.player-only");
			return;
		}
		if (args.length != 0) {
			langService.send(player, "overheat.usage");
			return;
		}
		heatService.sendStatus(player, player, langService);
	}
}
