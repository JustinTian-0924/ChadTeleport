package basementhost.randomchad.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record TeleportPayment(int points, double coins, String balanceName, double quotedPrice) {

	public static TeleportPayment coinsOnly(double price) {
		PricingRule.requireNonNegative(price, "quoted price");
		return new TeleportPayment(0, price, "", price);
	}

	public static TeleportPayment split(double price, int availablePoints, BigDecimal coinValue, String balanceName) {
		BigDecimal total = BigDecimal.valueOf(PricingRule.requireNonNegative(price, "quoted price"));
		BigDecimal pointsNeeded = total.divide(coinValue, 0, RoundingMode.CEILING);
		int points = pointsNeeded.min(BigDecimal.valueOf(availablePoints)).intValueExact();
		BigDecimal covered = coinValue.multiply(BigDecimal.valueOf(points));
		double coins = total.subtract(covered).max(BigDecimal.ZERO).doubleValue();
		return new TeleportPayment(points, coins, balanceName, price);
	}
}
