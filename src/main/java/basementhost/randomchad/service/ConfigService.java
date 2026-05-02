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

	public boolean isSpawnEnabled() {
		return config().getBoolean("features.spawn.enable", false);
	}

	public boolean isSpawnPermissionRequired() {
		return config().getBoolean("features.spawn.require-permission", false);
	}

	public String getSpawnPermission() {
		return config().getString("features.spawn.permission", "chadteleport.spawn");
	}

	public boolean isSpawnMoneyEnabled() {
		return config().getBoolean("features.spawn.money", true);
	}

	public boolean isSpawnUsingTpaPricing() {
		return config().getBoolean("features.spawn.use-tpa-pricing", true);
	}

	public double getSpawnFixedPrice() {
		return config().getDouble("features.spawn.fixed-price", 0.0);
	}

	public boolean isSpawnWarmupEnabled() {
		return config().getBoolean("features.spawn.warmup", true);
	}

	public boolean isSpawnUsingTpaWarmup() {
		return config().getBoolean("features.spawn.use-tpa-warmup", true);
	}

	public double getSpawnFixedWarmupSeconds() {
		return config().getDouble("features.spawn.fixed-warmup-seconds", 0.0);
	}

	public boolean isHomeEnabled() {
		return config().getBoolean("features.home.enable", false);
	}

	public boolean isHomeMoneyEnabled() {
		return config().getBoolean("features.home.money", true);
	}

	public boolean isHomeWarmupEnabled() {
		return config().getBoolean("features.home.warmup", true);
	}

	public boolean isHomeUsingCustomPricingFormula() {
		return config().getBoolean("features.home.pricing.use-custom-formula", false);
	}

	public double getHomeBasePrice() {
		return config().getDouble("features.home.pricing.base-price", 10.0);
	}

	public double getHomeDistancePricePer1k() {
		return config().getDouble("features.home.pricing.distance-price-1k", 10.0);
	}

	public boolean isHomeUsingCustomTimingFormula() {
		return config().getBoolean("features.home.timing.use-custom-formula", false);
	}

	public double getHomeBaseTime() {
		return config().getDouble("features.home.timing.base-time", 3.0);
	}

	public double getHomeDistanceTimePer1k() {
		return config().getDouble("features.home.timing.distance-time-1k", 1.0);
	}

	public boolean isWarpEnabled() {
		return config().getBoolean("features.warp.enable", false);
	}

	public boolean isWarpMoneyEnabled() {
		return config().getBoolean("features.warp.money", true);
	}

	public boolean isWarpUsingTpaPricing() {
		return config().getBoolean("features.warp.use-tpa-pricing", true);
	}

	public double getWarpFixedPrice() {
		return config().getDouble("features.warp.fixed-price", 0.0);
	}

	public boolean isWarpWarmupEnabled() {
		return config().getBoolean("features.warp.warmup", true);
	}

	public boolean isWarpUsingTpaWarmup() {
		return config().getBoolean("features.warp.use-tpa-warmup", true);
	}

	public double getWarpFixedWarmupSeconds() {
		return config().getDouble("features.warp.fixed-warmup-seconds", 0.0);
	}

	public boolean isQuoteCancelledOnMove() {
		return config().getBoolean("teleport.quote-cancel-on-move", true);
	}

}