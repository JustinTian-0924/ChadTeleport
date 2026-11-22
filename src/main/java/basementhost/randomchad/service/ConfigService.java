package basementhost.randomchad.service;

import basementhost.randomchad.model.PricingMode;
import basementhost.randomchad.model.PricingGroup;
import basementhost.randomchad.model.PricingPolicy;
import basementhost.randomchad.model.PricingRule;
import basementhost.randomchad.model.PricingRule.DistanceRate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ConfigService {

	private final JavaPlugin plugin;
	private FileConfiguration activeConfig;
	private PricingPolicy pricingPolicy;
	private BalanceSettings balanceSettings;
	private Map<String, WorldTransferRule> worldTransferRules;
	private final Map<String, Permission> ownedGroupPermissions = new HashMap<>();

	public ConfigService(JavaPlugin plugin) {
		this.plugin = plugin;
		reload();
	}

	public void reload() {
		YamlConfiguration candidate = new YamlConfiguration();
		// 世界键允许包含小数点，因此内部路径使用世界键中不会出现的分隔符。
		candidate.options().pathSeparator('|');
		try {
			candidate.load(new File(plugin.getDataFolder(), "config.yml"));
		} catch (IOException | InvalidConfigurationException e) {
			throw new IllegalArgumentException("Cannot load config.yml: " + e.getMessage(), e);
		}
		Map<String, PricingRule> newPricingRules = new HashMap<>();
		for (String feature : PricingPolicy.COMMANDS) {
			newPricingRules.put(feature, readPricingRule(requiredSection(candidate, "features." + feature + ".pricing")));
		}
		PricingPolicy newPolicy = new PricingPolicy(newPricingRules, readPricingGroups(candidate));
		BalanceSettings newBalanceSettings = readBalanceSettings(candidate);
		ConfigurationSection transfers = requiredSection(candidate, "world-transfers");
		WorldTransferRule defaults = readWorldTransferRule(requiredSection(transfers, "default"), null);
		Map<String, WorldTransferRule> newTransferRules = new HashMap<>();
		newTransferRules.put("default", defaults);
		for (var entry : transfers.getValues(false).entrySet()) {
			if (!entry.getKey().equals("default")) {
				validateWorldKey(entry.getKey());
				if (!(entry.getValue() instanceof ConfigurationSection section)) {
					throw new IllegalArgumentException("world-transfers." + entry.getKey() + " must be a section.");
				}
				newTransferRules.put(entry.getKey(), readWorldTransferRule(section, defaults));
			}
		}
		// 所有价格检查通过后才启用新配置，重载失败时继续使用原来的有效配置。
		registerGroupPermissions(newPolicy.groups());
		pricingPolicy = newPolicy;
		balanceSettings = newBalanceSettings;
		worldTransferRules = Map.copyOf(newTransferRules);
		activeConfig = candidate;
	}

	private BalanceSettings readBalanceSettings(ConfigurationSection candidate) {
		if (!candidate.contains("teleport-balance")) {
			return new BalanceSettings(false, 30, BigDecimal.ONE, "Teleport Points");
		}
		ConfigurationSection section = requiredSection(candidate, "teleport-balance");
		int startingPoints = integer(section, "starting-points", 30);
		if (startingPoints < 0) {
			throw new IllegalArgumentException("teleport-balance.starting-points must be zero or greater.");
		}
		BigDecimal coinValue = BigDecimal.valueOf(number(section, "coins-per-point", 1.0)).stripTrailingZeros();
		if (coinValue.signum() <= 0 || coinValue.scale() > 2) {
			throw new IllegalArgumentException("teleport-balance.coins-per-point must be positive with at most two decimal places.");
		}
		Object rawName = section.get("display-name", "Teleport Points");
		if (!(rawName instanceof String name) || name.isBlank() || name.length() > 48
				|| name.chars().anyMatch(c -> Character.isISOControl(c) || "<>{}\\".indexOf(c) >= 0)) {
			throw new IllegalArgumentException("teleport-balance.display-name must be plain text between 1 and 48 characters.");
		}
		return new BalanceSettings(bool(section, "enabled", false), startingPoints, coinValue, name.strip());
	}

	public BalanceSettings getBalanceSettings() {
		return balanceSettings;
	}

	public record BalanceSettings(boolean enabled, int startingPoints, BigDecimal coinValue, String displayName) {
	}

	private List<PricingGroup> readPricingGroups(ConfigurationSection candidate) {
		List<PricingGroup> groups = new ArrayList<>();
		if (!candidate.contains("pricing-groups")) {
			return groups;
		}
		ConfigurationSection configuredGroups = requiredSection(candidate, "pricing-groups");
		for (var entry : configuredGroups.getValues(false).entrySet()) {
			if (!(entry.getValue() instanceof ConfigurationSection group)) {
				throw new IllegalArgumentException("pricing-groups." + entry.getKey() + " must be a section.");
			}
			Object rawPermission = group.get("permission");
			if (!(rawPermission instanceof String permission)) {
				throw new IllegalArgumentException(group.getCurrentPath() + ".permission must be a permission node.");
			}
			Map<String, PricingRule> rules = new HashMap<>();
			ConfigurationSection commands = requiredSection(group, "commands");
			for (var command : commands.getValues(false).entrySet()) {
				if (!PricingPolicy.COMMANDS.contains(command.getKey())) {
					throw new IllegalArgumentException(commands.getCurrentPath() + ": unsupported command " + command.getKey());
				}
				if (!(command.getValue() instanceof ConfigurationSection pricing)) {
					throw new IllegalArgumentException(commands.getCurrentPath() + "." + command.getKey() + " must be a pricing section.");
				}
				rules.put(command.getKey(), readPricingRule(pricing));
			}
			groups.add(new PricingGroup(entry.getKey(), permission, integer(group, "priority", 0), rules));
		}
		return groups;
	}

	private int integer(ConfigurationSection section, String key, int fallback) {
		Object raw = section.get(key);
		if (raw == null) {
			return fallback;
		}
		if (raw instanceof Byte || raw instanceof Short || raw instanceof Integer || raw instanceof Long) {
			long value = ((Number) raw).longValue();
			if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
				return (int) value;
			}
		}
		throw new IllegalArgumentException(section.getCurrentPath() + "." + key + " must be a 32-bit integer.");
	}

	private void registerGroupPermissions(List<PricingGroup> groups) {
		var manager = plugin.getServer().getPluginManager();
		List<Permission> added = new ArrayList<>();
		try {
			for (PricingGroup group : groups) {
				if (manager.getPermission(group.permission()) == null) {
					Permission permission = new Permission(group.permission(), "Selects ChadTeleport group pricing", PermissionDefault.FALSE);
					manager.addPermission(permission);
					added.add(permission);
					permission.recalculatePermissibles();
				}
			}
		} catch (RuntimeException e) {
			for (Permission permission : added) {
				manager.removePermission(permission);
				permission.recalculatePermissibles();
			}
			throw e;
		}
		for (Permission permission : added) {
			ownedGroupPermissions.put(permission.getName(), permission);
		}
	}

	public void close() {
		var manager = plugin.getServer().getPluginManager();
		for (Permission permission : ownedGroupPermissions.values()) {
			// Only unregister nodes created by this plugin, leaving other plugins' definitions intact.
			if (manager.getPermission(permission.getName()) == permission) {
				manager.removePermission(permission);
				permission.recalculatePermissibles();
			}
		}
		ownedGroupPermissions.clear();
	}

	private PricingRule readPricingRule(ConfigurationSection section) {
		PricingMode mode;
		try {
			mode = PricingMode.parse(section.getString("mode"));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(section.getCurrentPath() + ".mode: " + e.getMessage(), e);
		}
		double fixed = number(section, "fixed-price", mode == PricingMode.FIXED ? null : 0.0);
		double base = number(section, "base-price", mode == PricingMode.DISTANCE ? null : 0.0);
		double per1k = number(section, "distance-price-1k", mode == PricingMode.DISTANCE ? null : 0.0);
		DistanceRate defaults = new DistanceRate(base, per1k);
		Map<String, DistanceRate> overrides = new HashMap<>();
		if (section.contains("world-overrides")) {
			for (var entry : requiredSection(section, "world-overrides").getValues(false).entrySet()) {
				validateWorldKey(entry.getKey());
				if (!(entry.getValue() instanceof ConfigurationSection world)) {
					throw new IllegalArgumentException(section.getCurrentPath() + ".world-overrides must contain sections.");
				}
				overrides.put(entry.getKey(), new DistanceRate(number(world, "base-price", base),
						number(world, "distance-price-1k", per1k)));
			}
		}
		return new PricingRule(mode, fixed, defaults, overrides, bool(section, "include-world-transfer-fees", true));
	}

	private WorldTransferRule readWorldTransferRule(ConfigurationSection section, WorldTransferRule fallback) {
		return new WorldTransferRule(bool(section, "allow-enter", fallback == null ? null : fallback.allowEnter()),
				number(section, "enter", fallback == null ? null : fallback.enter()),
				number(section, "leave", fallback == null ? null : fallback.leave()));
	}

	private ConfigurationSection requiredSection(ConfigurationSection parent, String key) {
		ConfigurationSection result = parent.getConfigurationSection(configPath(key));
		if (result == null) {
			throw new IllegalArgumentException(parent.getCurrentPath() + "." + key + " must be a section.");
		}
		return result;
	}

	private double number(ConfigurationSection section, String key, Double fallback) {
		Object raw = section.get(key);
		if (raw == null && fallback != null) {
			return fallback;
		}
		String path = section.getCurrentPath() + "." + key;
		if (!(raw instanceof Number value)) {
			throw new IllegalArgumentException(path + " must be a number.");
		}
		return PricingRule.requireNonNegative(value.doubleValue(), path);
	}

	private boolean bool(ConfigurationSection section, String key, Boolean fallback) {
		Object raw = section.get(key);
		if (raw == null && fallback != null) {
			return fallback;
		}
		if (!(raw instanceof Boolean value)) {
			throw new IllegalArgumentException(section.getCurrentPath() + "." + key + " must be true or false.");
		}
		return value;
	}

	private void validateWorldKey(String key) {
		if (!key.matches("[a-z0-9._-]+:[a-z0-9/._-]+")) {
			throw new IllegalArgumentException("Use a world key such as minecraft:overworld, received: " + key);
		}
	}

	private String configPath(String path) {
		return path.replace('.', '|');
	}

	public PricingRule getPricingRule(String featureName) {
		PricingRule rule = pricingPolicy.defaults().get(featureName);
		if (rule == null) {
			throw new IllegalArgumentException("No pricing rule for feature: " + featureName);
		}
		return rule;
	}

	public PricingRule resolvePricingRule(String featureName, Predicate<String> hasPermission) {
		return pricingPolicy.resolve(featureName, hasPermission);
	}

	private WorldTransferRule transferRule(String worldKey) {
		return worldTransferRules.getOrDefault(worldKey, worldTransferRules.get("default"));
	}

	public boolean isWorldEnterAllowed(String worldKey) {
		return transferRule(worldKey).allowEnter();
	}

	public double getEnterPrice(String worldKey) {
		return transferRule(worldKey).enter();
	}

	public double getLeavePrice(String worldKey) {
		return transferRule(worldKey).leave();
	}

	public String getDefaultLocale() {
		return activeConfig.getString(configPath("language.default-locale"), "en_us");
	}

	public boolean isFeatureEnabled(String featureName) {
		return activeConfig.getBoolean(configPath("features." + featureName + ".enable"), false);
	}

	public boolean isWarmupEnabled(String featureName) {
		return activeConfig.getBoolean(configPath("features." + featureName + ".warmup"), false);
	}

	public boolean isCrossWorldEnabled() {
		return activeConfig.getBoolean(configPath("cross-world.enable"), true);
	}

	private double worldTiming(String worldKey, String field, double fallback) {
		ConfigurationSection worlds = activeConfig.getConfigurationSection("worlds");
		if (worlds == null) {
			return fallback;
		}
		double defaultValue = worlds.getDouble(configPath("default." + field), fallback);
		// 直接读取键，避免自定义世界键中的小数点被当成配置路径分隔符。
		Object raw = worlds.getValues(false).get(worldKey);
		return raw instanceof ConfigurationSection world ? world.getDouble(field, defaultValue) : defaultValue;
	}

	public double getBaseTime(String worldKey) {
		return worldTiming(worldKey, "base_time", 3.0);
	}

	public double getDistanceTimePer1k(String worldKey) {
		return worldTiming(worldKey, "distance_time_1k", 1.0);
	}

	public double getQuoteMaxMoveDistance() {
		return activeConfig.getDouble(configPath("quote.max-move-distance"), 4.0);
	}

	public int getQuoteExpireSeconds() {
		return activeConfig.getInt(configPath("quote.expire-seconds"), 30);
	}

	public int getRequestExpireSeconds() {
		return activeConfig.getInt(configPath("request.expire-seconds"), 60);
	}

	public boolean isRequestCancelledOnTargetMove() {
		return activeConfig.getBoolean(configPath("request.cancel-on-target-move"), true);
	}

	public double getRequestTargetMaxMoveDistance() {
		return activeConfig.getDouble(configPath("request.target-max-move-distance"), 64.0);
	}

	public boolean isQuoteCancelledOnTargetMove() {
		return activeConfig.getBoolean(configPath("quote.cancel-on-target-move"), true);
	}

	public double getQuoteTargetMaxMoveDistance() {
		return activeConfig.getDouble(configPath("quote.target-max-move-distance"), 64.0);
	}

	public boolean isSpawnEnabled() {
		return isFeatureEnabled("spawn");
	}

	public boolean isSpawnPermissionRequired() {
		return activeConfig.getBoolean(configPath("features.spawn.require-permission"), false);
	}

	public String getSpawnPermission() {
		return activeConfig.getString(configPath("features.spawn.permission"), "chadteleport.spawn");
	}

	public boolean isSpawnUsingTpaWarmup() {
		return activeConfig.getBoolean(configPath("features.spawn.use-tpa-warmup"), true);
	}

	public double getSpawnFixedWarmupSeconds() {
		return activeConfig.getDouble(configPath("features.spawn.fixed-warmup-seconds"), 0.0);
	}

	public boolean isHomeEnabled() {
		return isFeatureEnabled("home");
	}

	public boolean isHomeUsingCustomTimingFormula() {
		return activeConfig.getBoolean(configPath("features.home.timing.use-custom-formula"), false);
	}

	public double getHomeBaseTime() {
		return activeConfig.getDouble(configPath("features.home.timing.base-time"), 3.0);
	}

	public double getHomeDistanceTimePer1k() {
		return activeConfig.getDouble(configPath("features.home.timing.distance-time-1k"), 1.0);
	}

	public boolean isWarpEnabled() {
		return isFeatureEnabled("warp");
	}

	public boolean isWarpUsingTpaWarmup() {
		return activeConfig.getBoolean(configPath("features.warp.use-tpa-warmup"), true);
	}

	public double getWarpFixedWarmupSeconds() {
		return activeConfig.getDouble(configPath("features.warp.fixed-warmup-seconds"), 0.0);
	}

	public boolean isQuoteCancelledOnMove() {
		return activeConfig.getBoolean(configPath("teleport.quote-cancel-on-move"), true);
	}

	private record WorldTransferRule(boolean allowEnter, double enter, double leave) {
	}
}
