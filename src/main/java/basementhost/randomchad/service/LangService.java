package basementhost.randomchad.service;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LangService {

	private final JavaPlugin plugin;
	private final MiniMessage miniMessage = MiniMessage.miniMessage();
	private final Map<String, YamlConfiguration> loadedLocales = new HashMap<>();
	private String defaultLocale;

	public LangService(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	public void load() {
		defaultLocale = plugin.getConfig().getString("language.default-locale", "en_us");

		saveLocaleIfMissing("en_us");
		saveLocaleIfMissing("zh_cn");

		loadedLocales.clear();
		loadLocale("en_us");
		loadLocale("zh_cn");

		if (!loadedLocales.containsKey(defaultLocale)) {
			plugin.getLogger().warning("Locale '" + defaultLocale + "' not found, falling back to en_us.");
			defaultLocale = "en_us";
		}
	}

	private void saveLocaleIfMissing(String locale) {
		File file = new File(plugin.getDataFolder(), "lang/" + locale + ".yml");
		if (!file.exists()) {
			plugin.saveResource("lang/" + locale + ".yml", false);
		}
	}

	private void loadLocale(String locale) {
		File file = new File(plugin.getDataFolder(), "lang/" + locale + ".yml");
		if (!file.exists()) {
			return;
		}

		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		// 保留服务器自定义文案，旧文件缺少的新条目从插件内置语言文件补足。
		try (InputStream resource = plugin.getResource("lang/" + locale + ".yml")) {
			if (resource != null) {
				yaml.setDefaults(YamlConfiguration.loadConfiguration(
						new InputStreamReader(resource, StandardCharsets.UTF_8)));
			}
		} catch (IOException e) {
			plugin.getLogger().warning("Failed to load bundled locale '" + locale + "': " + e.getMessage());
		}
		loadedLocales.put(locale, yaml);
	}

	public String getDefaultLocale() {
		return defaultLocale;
	}

	public String getRaw(String key) {
		return getRaw(defaultLocale, key);
	}

	public String getRaw(String locale, String key) {
		YamlConfiguration yaml = loadedLocales.get(locale);
		if (yaml == null) {
			yaml = loadedLocales.get(defaultLocale);
		}
		if (yaml == null) {
			return key;
		}

		String value = yaml.getString(key);
		if (value != null) {
			return value;
		}

		YamlConfiguration fallback = loadedLocales.get("en_us");
		if (fallback != null) {
			String fallbackValue = fallback.getString(key);
			if (fallbackValue != null) {
				return fallbackValue;
			}
		}

		return key;
	}

	public Component get(String key) {
		return get(defaultLocale, key, Map.of());
	}

	public Component get(String key, Map<String, String> placeholders) {
		return get(defaultLocale, key, placeholders);
	}

	public Component get(String locale, String key, Map<String, String> placeholders) {
		String raw = getRaw(locale, key);
		String resolved = applyPlaceholders(raw, placeholders);
		return miniMessage.deserialize(resolved);
	}

	public void send(CommandSender sender, String key) {
		sender.sendMessage(get(key));
	}

	public void send(CommandSender sender, String key, Map<String, String> placeholders) {
		sender.sendMessage(get(key, placeholders));
	}

	private String applyPlaceholders(String raw, Map<String, String> placeholders) {
		String result = raw;
		for (Map.Entry<String, String> entry : placeholders.entrySet()) {
			result = result.replace("{" + entry.getKey() + "}", entry.getValue());
		}
		return result;
	}
}
