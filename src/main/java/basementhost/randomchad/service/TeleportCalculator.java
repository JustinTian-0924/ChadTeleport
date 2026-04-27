package basementhost.randomchad.service;

import basementhost.randomchad.exception.LocalizedException;
import basementhost.randomchad.model.TeleportOffer;
import basementhost.randomchad.model.TeleportQuote;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Map;

public class TeleportCalculator {

	private final ConfigService configService;

	public TeleportCalculator(ConfigService configService) {
		this.configService = configService;
	}

	public TeleportQuote calculateTpaQuote(Player requester, Player target) {
		Location from = requester.getLocation();
		Location to = target.getLocation();

		World fromWorld = from.getWorld();
		World toWorld = to.getWorld();

		if (fromWorld == null || toWorld == null) {
			throw new LocalizedException("calculator.world-null");
		}

		boolean crossWorld = !fromWorld.getName().equals(toWorld.getName());

		if (crossWorld && !configService.isCrossWorldEnabled()) {
			throw new LocalizedException("calculator.cross-world-disabled");
		}

		double distance;
		double price;
		double warmupSeconds;

		if (!crossWorld) {
			distance = flatDistance(from, to);

			String worldName = fromWorld.getName();

			price = configService.getBasePrice(worldName)
					+ configService.getDistancePricePer1k(worldName) * (distance / 1000.0);

			warmupSeconds = configService.getBaseTime(worldName)
					+ configService.getDistanceTimePer1k(worldName) * (distance / 1000.0);
		} else {
			String fromWorldName = fromWorld.getName();
			String toWorldName = toWorld.getName();

			double enterPrice = configService.getEnterPrice(toWorldName);
			if (enterPrice < 0) {
				throw new LocalizedException("calculator.world-enter-denied", Map.of(
						"world", toWorldName
				));
			}

			double fromDistance = flatDistanceToOrigin(from);
			double toDistance = flatDistanceToOrigin(to);

			distance = fromDistance + toDistance;

			price = configService.getLeavePrice(fromWorldName)
					+ enterPrice
					+ configService.getBasePrice(fromWorldName)
					+ configService.getDistancePricePer1k(fromWorldName) * (fromDistance / 1000.0)
					+ configService.getDistancePricePer1k(toWorldName) * (toDistance / 1000.0);

			warmupSeconds = configService.getBaseTime(fromWorldName)
					+ configService.getDistanceTimePer1k(fromWorldName) * (fromDistance / 1000.0)
					+ configService.getDistanceTimePer1k(toWorldName) * (toDistance / 1000.0);
		}

		distance = floor1(distance);
		price = floor1(price);
		warmupSeconds = floor1(warmupSeconds);

		long warmupTicks = (long) Math.floor(warmupSeconds * 20.0);

		return new TeleportQuote(
				requester,
				target,
				from,
				to,
				distance,
				price,
				warmupSeconds,
				warmupTicks,
				crossWorld
		);
	}

	public TeleportOffer calculateLocationOffer(String featureName, Player requester, Location destination) {
		Location from = requester.getLocation();
		Location to = destination;

		World fromWorld = from.getWorld();
		World toWorld = to.getWorld();

		if (fromWorld == null || toWorld == null) {
			throw new LocalizedException("calculator.world-null");
		}

		boolean crossWorld = !fromWorld.getName().equals(toWorld.getName());

		if (crossWorld && !configService.isCrossWorldEnabled()) {
			throw new LocalizedException("calculator.cross-world-disabled");
		}

		double distance;
		double price;
		double warmupSeconds;

		if (!crossWorld) {
			distance = flatDistance(from, to);

			String worldName = fromWorld.getName();

			price = configService.getBasePrice(worldName)
					+ configService.getDistancePricePer1k(worldName) * (distance / 1000.0);

			warmupSeconds = configService.getBaseTime(worldName)
					+ configService.getDistanceTimePer1k(worldName) * (distance / 1000.0);
		} else {
			String fromWorldName = fromWorld.getName();
			String toWorldName = toWorld.getName();

			double enterPrice = configService.getEnterPrice(toWorldName);
			if (enterPrice < 0) {
				throw new LocalizedException("calculator.world-enter-denied", Map.of(
						"world", toWorldName
				));
			}

			double fromDistance = flatDistanceToOrigin(from);
			double toDistance = flatDistanceToOrigin(to);

			distance = fromDistance + toDistance;

			price = configService.getLeavePrice(fromWorldName)
					+ enterPrice
					+ configService.getBasePrice(fromWorldName)
					+ configService.getDistancePricePer1k(fromWorldName) * (fromDistance / 1000.0)
					+ configService.getDistancePricePer1k(toWorldName) * (toDistance / 1000.0);

			warmupSeconds = configService.getBaseTime(fromWorldName)
					+ configService.getDistanceTimePer1k(fromWorldName) * (fromDistance / 1000.0)
					+ configService.getDistanceTimePer1k(toWorldName) * (toDistance / 1000.0);
		}

		if ("spawn".equalsIgnoreCase(featureName)) {
			if (!configService.isSpawnUsingTpaPricing()) {
				price = configService.getSpawnFixedPrice();
			}

			if (!configService.isSpawnUsingTpaWarmup()) {
				warmupSeconds = configService.getSpawnFixedWarmupSeconds();
			}
		}

		if ("home".equalsIgnoreCase(featureName)) {
			if (configService.isHomeUsingCustomPricingFormula()) {
				price = configService.getHomeBasePrice()
						+ configService.getHomeDistancePricePer1k() * (distance / 1000.0);
			}

			if (configService.isHomeUsingCustomTimingFormula()) {
				warmupSeconds = configService.getHomeBaseTime()
						+ configService.getHomeDistanceTimePer1k() * (distance / 1000.0);
			}
		}

		distance = floor1(distance);
		price = floor1(price);
		warmupSeconds = floor1(warmupSeconds);

		long warmupTicks = (long) Math.floor(warmupSeconds * 20.0);

		return new TeleportOffer(
				featureName,
				requester,
				null,
				from.clone(),
				to.clone(),
				distance,
				price,
				warmupSeconds,
				warmupTicks,
				crossWorld
		);
	}

	private double flatDistance(Location a, Location b) {
		double dx = a.getX() - b.getX();
		double dz = a.getZ() - b.getZ();
		return Math.sqrt(dx * dx + dz * dz);
	}

	private double flatDistanceToOrigin(Location location) {
		double x = location.getX();
		double z = location.getZ();
		return Math.sqrt(x * x + z * z);
	}

	private double floor1(double value) {
		return Math.floor(value * 10.0) / 10.0;
	}
}