package basementhost.randomchad.service;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class EconomyService {

	private final JavaPlugin plugin;
	private volatile Economy economy;

	public EconomyService(JavaPlugin plugin) {
		this.plugin = plugin;
		tryHook();
	}

	public synchronized boolean tryHook() {
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
	public boolean has(Player player, double amount) {
		Economy provider = economy;
		return provider != null && provider.has(player, amount);
	}

	public CashDebit withdraw(Player player, double amount) {
		Economy provider = economy;
		if (provider == null) {
			return null;
		}

		EconomyResponse response = provider.withdrawPlayer(player, amount);
		if (!response.transactionSuccess()) {
			return null;
		}
		return new CashDebit(provider, response.amount);
	}

	public record CashDebit(Economy provider, double amount) {
		public boolean refund(Player player) {
			if (!Double.isFinite(amount) || amount < 0.0) {
				return false;
			}
			if (amount == 0.0) {
				return true;
			}
			EconomyResponse response = provider.depositPlayer(player, amount);
			return response.transactionSuccess() && Double.compare(response.amount, amount) == 0;
		}
	}
}
