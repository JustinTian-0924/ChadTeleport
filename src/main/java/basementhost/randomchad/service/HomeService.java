package basementhost.randomchad.service;

import basementhost.randomchad.model.HomeEntry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class HomeService {

	private final JavaPlugin plugin;
	private final File homesFolder;

	public HomeService(JavaPlugin plugin) {
		this.plugin = plugin;
		this.homesFolder = new File(plugin.getDataFolder(), "homes");

		if (!homesFolder.exists()) {
			homesFolder.mkdirs();
		}
	}

	public Map<String, HomeEntry> getHomes(Player player) {
		Map<String, HomeEntry> homes = new LinkedHashMap<>();

		YamlConfiguration yaml = loadPlayerFile(player.getUniqueId());
		if (!yaml.isConfigurationSection("homes")) {
			return homes;
		}

		for (String homeName : yaml.getConfigurationSection("homes").getKeys(false)) {
			HomeEntry entry = readHome(yaml, homeName);
			if (entry != null) {
				homes.put(homeName, entry);
			}
		}

		return homes;
	}

	public HomeEntry getHome(Player player, String homeName) {
		String normalized = normalizeHomeName(homeName);

		YamlConfiguration yaml = loadPlayerFile(player.getUniqueId());
		return readHome(yaml, normalized);
	}

	public boolean hasHome(Player player, String homeName) {
		return getHome(player, homeName) != null;
	}

	public void setHome(Player player, String homeName, Location location) {
		String normalized = normalizeHomeName(homeName);

		File file = getPlayerFile(player.getUniqueId());
		YamlConfiguration yaml = loadPlayerFile(player.getUniqueId());

		yaml.set("player-name", player.getName());

		String path = "homes." + normalized;
		yaml.set(path + ".world", location.getWorld() == null ? null : location.getWorld().getName());
		yaml.set(path + ".x", location.getX());
		yaml.set(path + ".y", location.getY());
		yaml.set(path + ".z", location.getZ());
		yaml.set(path + ".yaw", location.getYaw());
		yaml.set(path + ".pitch", location.getPitch());

		saveYaml(file, yaml);
	}

	public boolean deleteHome(Player player, String homeName) {
		String normalized = normalizeHomeName(homeName);

		File file = getPlayerFile(player.getUniqueId());
		YamlConfiguration yaml = loadPlayerFile(player.getUniqueId());

		String path = "homes." + normalized;
		if (!yaml.contains(path)) {
			return false;
		}

		yaml.set(path, null);
		saveYaml(file, yaml);
		return true;
	}

	public int getHomeCount(Player player) {
		return getHomes(player).size();
	}

	private HomeEntry readHome(YamlConfiguration yaml, String homeName) {
		String normalized = normalizeHomeName(homeName);
		String path = "homes." + normalized;

		String worldName = yaml.getString(path + ".world");
		if (worldName == null || worldName.isBlank()) {
			return null;
		}

		World world = Bukkit.getWorld(worldName);
		if (world == null) {
			return null;
		}

		double x = yaml.getDouble(path + ".x");
		double y = yaml.getDouble(path + ".y");
		double z = yaml.getDouble(path + ".z");
		float yaw = (float) yaml.getDouble(path + ".yaw");
		float pitch = (float) yaml.getDouble(path + ".pitch");

		Location location = new Location(world, x, y, z, yaw, pitch);
		return new HomeEntry(normalized, location);
	}

	private File getPlayerFile(UUID uuid) {
		return new File(homesFolder, uuid.toString() + ".yml");
	}

	private YamlConfiguration loadPlayerFile(UUID uuid) {
		File file = getPlayerFile(uuid);
		return YamlConfiguration.loadConfiguration(file);
	}

	private void saveYaml(File file, YamlConfiguration yaml) {
		try {
			yaml.save(file);
		} catch (IOException e) {
			throw new RuntimeException("Failed to save home file: " + file.getName(), e);
		}
	}

	private String normalizeHomeName(String homeName) {
		if (homeName == null || homeName.isBlank()) {
			return "default";
		}
		return homeName.toLowerCase(Locale.ROOT);
	}
}