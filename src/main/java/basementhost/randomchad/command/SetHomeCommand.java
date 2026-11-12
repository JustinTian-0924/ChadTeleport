package basementhost.randomchad.command;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.util.HomeNameValidator;
import org.bukkit.Location;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class SetHomeCommand implements BasicCommand {

	private final ChadteleportPlugin plugin;

	public SetHomeCommand(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSourceStack source, String[] args) {
		CommandSender sender = source.getSender();
		if (!(sender instanceof Player player)) {
			plugin.getLangService().send(sender, "home.player-only-sethome");
			return;
		}

		if (args.length > 1) {
			plugin.getLangService().send(player, "general.usage.sethome");
			return;
		}

		String homeName;
		if (args.length == 0) {
			homeName = "default";
		} else {
			if (!HomeNameValidator.isValid(args[0])) {
				plugin.getLangService().send(player, "home.invalid-name", Map.of(
						"min", String.valueOf(HomeNameValidator.getMinLength()),
						"max", String.valueOf(HomeNameValidator.getMaxLength())
				));
				return;
			}
			homeName = HomeNameValidator.normalize(args[0]);
		}

		boolean alreadyExists = plugin.getHomeService().hasHome(player, homeName);

		if (!alreadyExists) {
			int currentCount = plugin.getHomeService().getHomeCount(player);
			int maxHomes = plugin.getHomeLimitService().getMaxHomes(player);

			if (currentCount >= maxHomes) {
				plugin.getLangService().send(player, "home.limit-reached", Map.of(
						"max", String.valueOf(maxHomes)
				));
				return;
			}
		}

		Location location = player.getLocation().clone();
		plugin.getHomeService().setHome(player, homeName, location);

		plugin.getLangService().send(player,
				alreadyExists ? "home.updated" : "home.created",
				Map.of("name", homeName)
		);

	}
}
