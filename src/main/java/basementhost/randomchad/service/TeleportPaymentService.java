package basementhost.randomchad.service;

import basementhost.randomchad.model.PricingRule;
import org.bukkit.entity.Player;

import java.util.Map;

public class TeleportPaymentService {

	private final EconomyService economyService;
	private final LangService langService;

	public TeleportPaymentService(EconomyService economyService, LangService langService) {
		this.economyService = economyService;
		this.langService = langService;
	}

	public boolean checkCanAfford(Player player, double quotedPrice) {
		PricingRule.requireNonNegative(quotedPrice, "quoted price");
		if (quotedPrice == 0.0) {
			return true;
		}
		if (!economyService.isAvailable()) {
			economyService.tryHook();
		}
		if (!economyService.isAvailable()) {
			langService.send(player, "economy.unavailable-cancelled");
			return false;
		}
		if (!economyService.has(player, quotedPrice)) {
			langService.send(player, "economy.insufficient");
			langService.send(player, "economy.required", Map.of("price", String.valueOf(quotedPrice)));
			return false;
		}
		return true;
	}

	public boolean charge(Player player, double quotedPrice) {
		PricingRule.requireNonNegative(quotedPrice, "quoted price");
		return quotedPrice == 0.0 || economyService.withdraw(player, quotedPrice);
	}
}
