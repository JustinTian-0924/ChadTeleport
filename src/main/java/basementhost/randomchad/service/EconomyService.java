package basementhost.randomchad.service;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class EconomyService {

	private final JavaPlugin plugin;
	private Economy economy;

	public EconomyService(JavaPlugin plugin) {
		this.plugin = plugin;
		tryHook();
	}

	public boolean tryHook() {
		if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
			plugin.getLogger().warning("Vault not found. Economy features will be unavailable.");
			this.economy = null;
			return false;
		}

		RegisteredServiceProvider<Economy> rsp =
				plugin.getServer().getServicesManager().getRegistration(Economy.class);

		if (rsp == null || rsp.getProvider() == null) {
			this.economy = null;
			return false;
		}

		this.economy = rsp.getProvider();
		plugin.getLogger().info("Economy provider hooked: " + economy.getName());
		return true;
	}

	public boolean isAvailable() {
		return economy != null;
	}
	// unused yet
	public Economy getEconomy() {
		return economy;
	}

	public boolean has(Player player, double amount) {
		if (economy == null) {
			return false;
		}
		return economy.has(player, amount);
	}

	public boolean withdraw(Player player, double amount) {
		if (economy == null) {
			return false;
		}

		EconomyResponse response = economy.withdrawPlayer(player, amount);
		return response.transactionSuccess();
	}
}