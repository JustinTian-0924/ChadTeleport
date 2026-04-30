package basementhost.randomchad.service;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.model.WarpEntry;
import basementhost.randomchad.util.HomeNameValidator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class WarpService {

	private final ChadteleportPlugin plugin;
	private final File warpsFile;
	private YamlConfiguration config;

	public WarpService(ChadteleportPlugin plugin) {
		this.plugin = plugin;
		this.warpsFile = new File(plugin.getDataFolder(), "warps.yml");
		load();
	}

	public void load() {
		if (!warpsFile.exists()) {
			try {
				if (warpsFile.getParentFile() != null) {
					warpsFile.getParentFile().mkdirs();
				}
				warpsFile.createNewFile();
			} catch (IOException e) {
				plugin.getLogger().warning("Failed to create warps.yml: " + e.getMessage());
			}
		}

		this.config = YamlConfiguration.loadConfiguration(warpsFile);
	}

	public boolean setWarp(String name, Location location) {
		if (location == null || location.getWorld() == null) {
			return false;
		}

		String normalizedName = normalizeWarpName(name);
		String basePath = "warps." + normalizedName;

		config.set(basePath + ".world", location.getWorld().getName());
		config.set(basePath + ".x", location.getX());
		config.set(basePath + ".y", location.getY());
		config.set(basePath + ".z", location.getZ());
		config.set(basePath + ".yaw", location.getYaw());
		config.set(basePath + ".pitch", location.getPitch());

		return save();
	}

	public boolean deleteWarp(String name) {
		String normalizedName = normalizeWarpName(name);
		String basePath = "warps." + normalizedName;

		if (!config.contains(basePath)) {
			return false;
		}

		config.set(basePath, null);
		return save();
	}

	public boolean hasWarp(String name) {
		String normalizedName = normalizeWarpName(name);
		return config.contains("warps." + normalizedName);
	}

	public WarpEntry getWarp(String name) {
		String normalizedName = normalizeWarpName(name);
		String basePath = "warps." + normalizedName;

		if (!config.contains(basePath)) {
			return null;
		}

		String worldName = config.getString(basePath + ".world");
		if (worldName == null) {
			return null;
		}

		World world = Bukkit.getWorld(worldName);
		if (world == null) {
			return null;
		}

		double x = config.getDouble(basePath + ".x");
		double y = config.getDouble(basePath + ".y");
		double z = config.getDouble(basePath + ".z");
		float yaw = (float) config.getDouble(basePath + ".yaw");
		float pitch = (float) config.getDouble(basePath + ".pitch");

		Location location = new Location(world, x, y, z, yaw, pitch);
		return new WarpEntry(normalizedName, location);
	}

	public Map<String, WarpEntry> getWarps() {
		Map<String, WarpEntry> result = new LinkedHashMap<>();

		if (!config.contains("warps")) {
			return result;
		}

		for (String name : config.getConfigurationSection("warps").getKeys(false)) {
			WarpEntry entry = getWarp(name);
			if (entry != null) {
				result.put(name, entry);
			}
		}

		return result;
	}

	private boolean save() {
		try {
			config.save(warpsFile);
			return true;
		} catch (IOException e) {
			plugin.getLogger().warning("Failed to save warps.yml: " + e.getMessage());
			return false;
		}
	}

	private String normalizeWarpName(String name) {
		return HomeNameValidator.normalize(name);
	}
}