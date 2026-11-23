package basementhost.randomchad.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public sealed interface HeatRule {

	BigDecimal MAX_HEAT = BigDecimal.valueOf(Integer.MAX_VALUE);

	default long gainMilliHeat(TeleportRoute route, double quotedPrice) {
		BigDecimal heat = switch (this) {
			case Fixed(var points) -> BigDecimal.valueOf(points);
			case Distance(var baseHeat, var heatPer1k) -> BigDecimal.valueOf(route.fromDistance())
					.add(BigDecimal.valueOf(route.toDistance()))
					.movePointLeft(3)
					.multiply(BigDecimal.valueOf(heatPer1k))
					.add(BigDecimal.valueOf(baseHeat));
			case CrossWorld(var sameWorldHeat, var crossWorldHeat) ->
					BigDecimal.valueOf(route.isCrossWorld() ? crossWorldHeat : sameWorldHeat);
			case Price(var heatPerCoin) -> BigDecimal.valueOf(quotedPrice).multiply(BigDecimal.valueOf(heatPerCoin));
		};
		// Bound extreme quotes before converting to the stored integer unit.
		return heat.min(MAX_HEAT).movePointRight(3).setScale(0, RoundingMode.HALF_UP).longValueExact();
	}

	record Fixed(double heat) implements HeatRule {
	}

	record Distance(double baseHeat, double heatPer1k) implements HeatRule {
	}

	record CrossWorld(double sameWorldHeat, double crossWorldHeat) implements HeatRule {
	}

	record Price(double heatPerCoin) implements HeatRule {
	}
}
