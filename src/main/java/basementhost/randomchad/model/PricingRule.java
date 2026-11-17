package basementhost.randomchad.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;

public record PricingRule(PricingMode mode, double fixedPrice, DistanceRate defaultRate,
		Map<String, DistanceRate> worldOverrides, boolean includeWorldTransferFees) {

	public PricingRule {
		Objects.requireNonNull(mode, "mode");
		requireNonNegative(fixedPrice, "fixed-price");
		Objects.requireNonNull(defaultRate, "defaultRate");
		worldOverrides = Map.copyOf(worldOverrides);
	}

	public double calculate(TeleportRoute route, double leaveFee, double enterFee) {
		requireNonNegative(leaveFee, "leave fee");
		requireNonNegative(enterFee, "enter fee");
		BigDecimal total = switch (mode) {
			case FREE -> BigDecimal.ZERO;
			case FIXED -> BigDecimal.valueOf(fixedPrice);
			case DISTANCE -> distancePrice(route, leaveFee, enterFee);
		};
		// 金额只在最终合计时四舍五入，报价和扣费共用这一个结果。
		double rounded = total.setScale(2, RoundingMode.HALF_UP).doubleValue();
		requireNonNegative(rounded, "calculated price");
		return rounded;
	}

	private BigDecimal distancePrice(TeleportRoute route, double leaveFee, double enterFee) {
		DistanceRate fromRate = worldOverrides.getOrDefault(route.fromWorld(), defaultRate);
		BigDecimal total = BigDecimal.valueOf(fromRate.basePrice()).add(fromRate.distanceCost(route.fromDistance()));
		if (route.isCrossWorld()) {
			DistanceRate toRate = worldOverrides.getOrDefault(route.toWorld(), defaultRate);
			total = total.add(toRate.distanceCost(route.toDistance()));
			if (includeWorldTransferFees) {
				total = total.add(BigDecimal.valueOf(leaveFee)).add(BigDecimal.valueOf(enterFee));
			}
		}
		return total;
	}

	public static double requireNonNegative(double value, String name) {
		if (!Double.isFinite(value) || value < 0.0) {
			throw new IllegalArgumentException(name + " must be a finite number greater than or equal to zero.");
		}
		return value;
	}

	public record DistanceRate(double basePrice, double distancePricePer1k) {

		public DistanceRate {
			requireNonNegative(basePrice, "base-price");
			requireNonNegative(distancePricePer1k, "distance-price-1k");
		}

		private BigDecimal distanceCost(double distance) {
			return BigDecimal.valueOf(distancePricePer1k).multiply(BigDecimal.valueOf(distance)).movePointLeft(3);
		}
	}
}
