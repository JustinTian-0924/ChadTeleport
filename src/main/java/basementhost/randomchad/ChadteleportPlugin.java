package basementhost.randomchad;

import basementhost.randomchad.command.*;
import basementhost.randomchad.listener.PendingOfferMoveInvalidationListener;
import basementhost.randomchad.listener.QuoteInvalidationListener;
import basementhost.randomchad.listener.RequestTargetMoveInvalidationListener;
import basementhost.randomchad.listener.WarmupInterruptListener;
import basementhost.randomchad.manager.TeleportManager;
import basementhost.randomchad.service.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.List;

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
		HomeTabCompleter homeTabCompleter = new HomeTabCompleter(this);
		WarpTabCompleter warpTabCompleter = new WarpTabCompleter(this);
		AdminTeleportTabCompleter adminTeleportTabCompleter = new AdminTeleportTabCompleter(this);
		TeleportCommand teleportCommand = new TeleportCommand(this);
		TeleportCommand confirmCommand = new TeleportCommand(this, "confirm");
		TeleportCommand cancelCommand = new TeleportCommand(this, "cancel");
		AdminTpPosCommand tpPosCommand = new AdminTpPosCommand(this);

		registerPluginCommand("tpa", "Request teleport to another player", null, new TpaCommand(this), null);
		registerPluginCommand("tpaccept", "Accept a teleport request", null, new TpAcceptCommand(this), null);
		registerPluginCommand("tpdeny", "Deny a teleport request", null, new TpDenyCommand(this), null);
		registerPluginCommand("teleport", "Teleport confirmation command", null,
				teleportCommand, teleportCommand::suggest);
		registerPluginCommand("tpconfirm", "Confirm a teleport quote", null, confirmCommand, confirmCommand::suggest);
		registerPluginCommand("tpcancel", "Cancel a teleport quote or warmup", null, cancelCommand, cancelCommand::suggest);
		registerPluginCommand("setspawn", "Set the server spawn location", "chadteleport.setspawn",
				new SetSpawnCommand(this), null);
		registerPluginCommand("spawn", "Teleport to the server spawn", "chadteleport.spawn",
				new SpawnCommand(this), null);
		registerPluginCommand("setwarp", "Set a public warp point", "chadteleport.setwarp",
				new SetWarpCommand(this), warpTabCompleter);
		registerPluginCommand("warp", "Teleport to a public warp", "chadteleport.warp",
				new WarpCommand(this), warpTabCompleter);
		registerPluginCommand("delwarp", "Delete a public warp", "chadteleport.delwarp",
				new DelWarpCommand(this), warpTabCompleter);
		registerPluginCommand("warps", "List all public warps", "chadteleport.warp", new WarpsCommand(this), null);
		registerPluginCommand("sethome", "Set one of your homes", null, new SetHomeCommand(this), homeTabCompleter);
		registerPluginCommand("home", "Teleport to one of your homes", null, new HomeCommand(this), homeTabCompleter);
		registerPluginCommand("delhome", "Delete one of your homes", null, new DelHomeCommand(this), homeTabCompleter);
		registerPluginCommand("homes", "List your homes", null, new HomesCommand(this), null);
		registerPluginCommand("tp", "Teleport to a player instantly", "chadteleport.admin.tp",
				new AdminTpCommand(this), adminTeleportTabCompleter);
		registerPluginCommand("tphere", "Teleport a player to your location instantly", "chadteleport.admin.tphere",
				new AdminTpHereCommand(this), adminTeleportTabCompleter);
		registerPluginCommand("tpall", "Teleport all players to a player or to yourself", "chadteleport.admin.tpall",
				new AdminTpAllCommand(this), adminTeleportTabCompleter);
		registerPluginCommand("tppos", "Teleport to coordinates in your current world", "chadteleport.admin.tppos",
				tpPosCommand, tpPosCommand::suggest);

		AdminCommand adminCommand = new AdminCommand(this);
		registerPluginCommand("chadteleport", "Chadteleport admin command", "chadteleport.admin",
				adminCommand, adminCommand::suggest, "ct");
	}

	private void registerListeners() {
		getServer().getPluginManager().registerEvents(new QuoteInvalidationListener(this), this);
		getServer().getPluginManager().registerEvents(new WarmupInterruptListener(this), this);
		getServer().getPluginManager().registerEvents(new RequestTargetMoveInvalidationListener(this), this);
		getServer().getPluginManager().registerEvents(new PendingOfferMoveInvalidationListener(this), this);
	}

	private void registerPluginCommand(String name, String description, String permission,
			BasicCommand executor, CommandSuggestions suggestions, String... aliases) {
		// 保留原来的权限和补全规则，统一交给 Paper 的新指令接口注册。
		CommandSuggestions completer = suggestions != null ? suggestions : new PlayerNameTabCompleter(this);
		registerCommand(name, description, List.of(aliases), new BasicCommand() {
			@Override
			public void execute(CommandSourceStack source, String[] args) throws CommandSyntaxException {
				executor.execute(source, args);
			}

			@Override
			public Collection<String> suggest(CommandSourceStack source, String[] args) {
				return completer.suggest(source, args);
			}

			@Override
			public String permission() {
				return permission;
			}
		});
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

	public void reloadPluginResources() {
		reloadConfig();
		if (langService != null) {
			langService.load();
		}
		if (spawnService != null) {
			spawnService.load();
		}
		if (warpService != null) {
			warpService.load();
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
