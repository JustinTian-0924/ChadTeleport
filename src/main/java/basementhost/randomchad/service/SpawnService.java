package basementhost.randomchad.service;

import basementhost.randomchad.ChadteleportPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class SpawnService {

	private final ChadteleportPlugin plugin;
	private File file;
	private FileConfiguration config;

	public SpawnService(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	public void load() {
		if (!plugin.getDataFolder().exists()) {
			plugin.getDataFolder().mkdirs();
		}

		file = new File(plugin.getDataFolder(), "spawn.yml");

		if (!file.exists()) {
			plugin.saveResource("spawn.yml", false);
		}

		config = YamlConfiguration.loadConfiguration(file);
	}

	public void save() {
		if (config == null || file == null) {
			return;
		}

		try {
			config.save(file);
		} catch (IOException e) {
			plugin.getLogger().severe("Failed to save spawn.yml: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public boolean isSpawnSet() {
		return config != null && config.getBoolean("spawn.set", false);
	}

	public Location getSpawnLocation() {
		if (!isSpawnSet()) {
			return null;
		}

		String worldName = config.getString("spawn.world");
		if (worldName == null) {
			return null;
		}

		World world = plugin.getServer().getWorld(worldName);
		if (world == null) {
			return null;
		}

		double x = config.getDouble("spawn.x");
		double y = config.getDouble("spawn.y");
		double z = config.getDouble("spawn.z");
		float yaw = (float) config.getDouble("spawn.yaw");
		float pitch = (float) config.getDouble("spawn.pitch");

		return new Location(world, x, y, z, yaw, pitch);
	}

	public boolean setSpawnLocation(Location location) {
		if (location == null || location.getWorld() == null || config == null) {
			return false;
		}

		config.set("spawn.set", true);
		config.set("spawn.world", location.getWorld().getName());
		config.set("spawn.x", location.getX());
		config.set("spawn.y", location.getY());
		config.set("spawn.z", location.getZ());
		config.set("spawn.yaw", location.getYaw());
		config.set("spawn.pitch", location.getPitch());

		save();
		return true;
	}
}