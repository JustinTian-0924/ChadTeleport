package basementhost.randomchad.service;

import basementhost.randomchad.model.TeleportRoute;
import org.bukkit.entity.Player;

public class PricingService {

	private final ConfigService configService;

	public PricingService(ConfigService configService) {
		this.configService = configService;
	}

	public double calculate(String featureName, Player player, TeleportRoute route) {
		var rule = configService.getPricingRule(featureName);
		if (player.hasPermission("chadteleport.bypass.fee") || player.hasPermission("chadteleport.bypass.*")) {
			return 0.0;
		}
		return rule.calculate(route, configService.getLeavePrice(route.fromWorld()),
				configService.getEnterPrice(route.toWorld()));
	}
}
