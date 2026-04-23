package basementhost.randomchad.service;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigService {

	private final JavaPlugin plugin;

	public ConfigService(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	private FileConfiguration config() {
		return plugin.getConfig();
	}

	public boolean isFeatureEnabled(String featureName) {
		return config().getBoolean("features." + featureName + ".enable", false);
	}

	public boolean isMoneyEnabled(String featureName) {
		return config().getBoolean("features." + featureName + ".money", false);
	}

	public boolean isWarmupEnabled(String featureName) {
		return config().getBoolean("features." + featureName + ".warmup", false);
	}

	public boolean isCrossWorldEnabled() {
		return config().getBoolean("cross-world.enable", true);
	}

	public double getBaseTime(String worldName) {
		return config().getDouble("worlds." + worldName + ".base_time",
				config().getDouble("worlds.default.base_time", 3.0));
	}

	public double getBasePrice(String worldName) {
		return config().getDouble("worlds." + worldName + ".base_price",
				config().getDouble("worlds.default.base_price", 10.0));
	}

	public double getDistanceTimePer1k(String worldName) {
		return config().getDouble("worlds." + worldName + ".distance_time_1k",
				config().getDouble("worlds.default.distance_time_1k", 1.0));
	}

	public double getDistancePricePer1k(String worldName) {
		return config().getDouble("worlds." + worldName + ".distance_price_1k",
				config().getDouble("worlds.default.distance_price_1k", 10.0));
	}

	public double getEnterPrice(String worldName) {
		return config().getDouble("world-transfers." + worldName + ".enter", 0.0);
	}

	public double getLeavePrice(String worldName) {
		return config().getDouble("world-transfers." + worldName + ".leave", 0.0);
	}

	public double getQuoteMaxMoveDistance() {
		return config().getDouble("quote.max-move-distance", 4.0);
	}

	public int getQuoteExpireSeconds() {
		return config().getInt("quote.expire-seconds", 30);
	}

	public int getRequestExpireSeconds() {
		return config().getInt("request.expire-seconds", 60);
	}

	public boolean isRequestCancelledOnTargetMove() {
		return config().getBoolean("request.cancel-on-target-move", true);
	}

	public double getRequestTargetMaxMoveDistance() {
		return config().getDouble("request.target-max-move-distance", 64.0);
	}

	public boolean isQuoteCancelledOnTargetMove() {
		return config().getBoolean("quote.cancel-on-target-move", true);
	}

	public double getQuoteTargetMaxMoveDistance() {
		return config().getDouble("quote.target-max-move-distance", 64.0);
	}
}