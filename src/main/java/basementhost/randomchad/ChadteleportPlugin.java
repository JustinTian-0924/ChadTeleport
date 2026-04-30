package basementhost.randomchad;

import basementhost.randomchad.command.*;
import basementhost.randomchad.listener.QuoteInvalidationListener;
import basementhost.randomchad.listener.RequestTargetMoveInvalidationListener;
import basementhost.randomchad.listener.WarmupInterruptListener;
import basementhost.randomchad.manager.TeleportManager;
import basementhost.randomchad.service.*;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChadteleportPlugin extends JavaPlugin {

	private EconomyService economyService;
	private TeleportManager teleportManager;
	private ConfigService configService;
	private TeleportCalculator teleportCalculator;
	private WarmupService warmupService;
	private ExpirationService expirationService;
	private LangService langService;
	private SpawnService spawnService;
	private HomeService homeService;
	private HomeLimitService homeLimitService;
	private WarpService warpService;

	@Override
	public void onEnable() {
		saveDefaultConfig();

		this.economyService = new EconomyService(this);
		this.teleportManager = new TeleportManager(this);
		this.configService = new ConfigService(this);
		this.teleportCalculator = new TeleportCalculator(configService);
		this.warmupService = new WarmupService(this);
		this.spawnService = new SpawnService(this);
		this.spawnService.load();
		this.expirationService = new ExpirationService(this);
		this.homeService = new HomeService(this);
		this.homeLimitService = new HomeLimitService();
		this.warpService = new WarpService(this);
		this.langService = new LangService(this);
		this.langService.load();


		registerCommands();
		registerListeners();
		scheduleEconomyRetry();

		expirationService.start();

		getLogger().info("Chadteleport Plugin has been enabled!");	// do not think I need to translate this shit too
	}

	@Override
	public void onDisable() {
		if (expirationService != null) {
			expirationService.stop();
		}

		getLogger().info("Chadteleport Plugin has been disabled!"); // do not think I need to translate this shit too #
	}

	private void registerCommands() {
		registerCommand("tpa", new TpaCommand(this));
		registerCommand("tpaccept", new TpAcceptCommand(this));
		registerCommand("tpdeny", new TpDenyCommand(this));
		registerCommand("teleport", new TeleportCommand(this));
		registerCommand("setspawn", new SetSpawnCommand(this));
		registerCommand("spawn", new SpawnCommand(this));
		registerCommand("setwarp", new SetWarpCommand(this));
		registerCommand("warp", new WarpCommand(this));
		registerCommand("delwarp", new DelWarpCommand(this));
		registerCommand("warps", new WarpsCommand(this));
		registerCommand("sethome", new SetHomeCommand(this));
		registerCommand("home", new HomeCommand(this));
		registerCommand("delhome", new DelHomeCommand(this));
		registerCommand("homes", new HomesCommand(this));
		HomeTabCompleter homeTabCompleter = new HomeTabCompleter(this);
		registerTabCompleter("sethome", homeTabCompleter);
		registerTabCompleter("home", homeTabCompleter);
		registerTabCompleter("delhome", homeTabCompleter);
		WarpTabCompleter warpTabCompleter = new WarpTabCompleter(this);
		registerTabCompleter("setwarp", warpTabCompleter);
		registerTabCompleter("warp", warpTabCompleter);
		registerTabCompleter("delwarp", warpTabCompleter);

		AdminCommand adminCommand = new AdminCommand(this);
		registerCommand("chadteleport", adminCommand);
		registerTabCompleter("chadteleport", adminCommand);
	}

	private void registerListeners() {
		getServer().getPluginManager().registerEvents(new QuoteInvalidationListener(this), this);
		getServer().getPluginManager().registerEvents(new WarmupInterruptListener(this), this);
		getServer().getPluginManager().registerEvents(new RequestTargetMoveInvalidationListener(this), this);
	}

	private void registerCommand(String name, org.bukkit.command.CommandExecutor executor) {
		PluginCommand command = getCommand(name);
		if (command == null) {
			getLogger().warning("Command '" + name + "' is missing in plugin.yml");
			return;
		}
		command.setExecutor(executor);
	}

	private void scheduleEconomyRetry() {
		getServer().getScheduler().runTaskLater(this, () -> {
			if (!economyService.isAvailable()) {
				boolean hooked = economyService.tryHook();
				if (!hooked) {
					getLogger().warning("Still no economy provider found after delayed retry, maybe it is just not installed.");
				}
			}
		}, 40L);
	}

	private void registerTabCompleter(String name, TabCompleter completer) {
		PluginCommand command = getCommand(name);
		if (command != null) {
			command.setTabCompleter(completer);
		}
	}

	public void reloadPluginResources() {
		reloadConfig();
		if (langService != null) {
			langService.load();
		}
		if (spawnService != null) {
			spawnService.load();
		}
	}

	public EconomyService getEconomyService() {
		return economyService;
	}

	public TeleportManager getTeleportManager() {
		return teleportManager;
	}

	public ConfigService getConfigService() {
		return configService;
	}

	public TeleportCalculator getTeleportCalculator() {
		return teleportCalculator;
	}

	public WarmupService getWarmupService() {
		return warmupService;
	}

	public ExpirationService getExpirationService() {
		return expirationService;
	}

	public SpawnService getSpawnService() {
		return spawnService;
	}

	public HomeService getHomeService() {
		return homeService;
	}

	public HomeLimitService getHomeLimitService() {
		return homeLimitService;
	}

	public WarpService getWarpService() {
		return warpService;
	}

	public LangService getLangService() {
		return langService;
	}

}