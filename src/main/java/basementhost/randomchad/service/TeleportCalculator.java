package basementhost.randomchad.service;

import basementhost.randomchad.exception.LocalizedException;
import basementhost.randomchad.model.TeleportOffer;
import basementhost.randomchad.model.TeleportQuote;
import basementhost.randomchad.model.TeleportRoute;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Map;

public class TeleportCalculator {

	private final ConfigService configService;
	private final PricingService pricingService;
	private final TeleportPaymentService paymentService;
	private final TeleportHeatService heatService;

	public TeleportCalculator(ConfigService configService, PricingService pricingService,
			TeleportPaymentService paymentService, TeleportHeatService heatService) {
		this.configService = configService;
		this.pricingService = pricingService;
		this.paymentService = paymentService;
		this.heatService = heatService;
	}

	public TeleportQuote calculateTpaQuote(Player requester, Player target, Location destination) {
		TeleportOffer offer = calculateOffer("tpa", requester, target, destination);
		return new TeleportQuote(requester, target, offer.getFrom(), offer.getDestination(), offer.getRoute(),
				offer.getPayment(), offer.getWarmupSeconds(), offer.getWarmupTicks());
	}

	public TeleportOffer calculateLocationOffer(String featureName, Player requester, Location destination) {
		return calculateOffer(featureName, requester, null, destination);
	}

	private TeleportOffer calculateOffer(String featureName, Player requester, Player target, Location destination) {
		Location from = requester.getLocation();
		Location to = destination.clone();
		World fromWorld = from.getWorld();
		World toWorld = to.getWorld();
		if (fromWorld == null || toWorld == null) {
			throw new LocalizedException("calculator.world-null");
		}
		String fromKey = fromWorld.getKey().asString();
		String toKey = toWorld.getKey().asString();
		boolean crossWorld = !fromKey.equals(toKey);
		if (crossWorld && !configService.isCrossWorldEnabled()) {
			throw new LocalizedException("calculator.cross-world-disabled");
		}
		// 世界进入限制与收费分开，免费和免手续费权限也不能跳过限制。
		if (crossWorld && !configService.isWorldEnterAllowed(toKey)) {
			throw new LocalizedException("calculator.world-enter-denied", Map.of("world", toKey));
		}
		heatService.requireCoolEnough(requester);
		TeleportRoute route = crossWorld
				? new TeleportRoute(fromKey, toKey, flatDistanceToOrigin(from), flatDistanceToOrigin(to))
				: new TeleportRoute(fromKey, toKey, Math.hypot(from.getX() - to.getX(), from.getZ() - to.getZ()), 0.0);
		double price = pricingService.calculate(featureName, requester, route);
		double warmupSeconds = configService.getBaseTime(fromKey)
				+ configService.getDistanceTimePer1k(fromKey) * (route.fromDistance() / 1000.0);
		if (crossWorld) {
			warmupSeconds += configService.getDistanceTimePer1k(toKey) * (route.toDistance() / 1000.0);
		}
		if ("spawn".equals(featureName) && !configService.isSpawnUsingTpaWarmup()) {
			warmupSeconds = configService.getSpawnFixedWarmupSeconds();
		} else if ("warp".equals(featureName) && !configService.isWarpUsingTpaWarmup()) {
			warmupSeconds = configService.getWarpFixedWarmupSeconds();
		} else if ("home".equals(featureName) && configService.isHomeUsingCustomTimingFormula()) {
			warmupSeconds = configService.getHomeBaseTime()
					+ configService.getHomeDistanceTimePer1k() * (route.totalDistance() / 1000.0);
		}
		warmupSeconds = floor1(warmupSeconds);
		long warmupTicks = (long) Math.floor(warmupSeconds * 20.0);
		return new TeleportOffer(featureName, requester, target, from.clone(), to, route,
				paymentService.quote(requester, price), warmupSeconds, warmupTicks);
	}

	private double flatDistanceToOrigin(Location location) {
		return Math.hypot(location.getX(), location.getZ());
	}

	private double floor1(double value) {
		return Math.floor(value * 10.0) / 10.0;
	}
}
