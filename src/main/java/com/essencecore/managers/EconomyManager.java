package com.essencecore.managers;

import com.essencecore.EssenceCore;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

@Getter
public class EconomyManager {
    
    private final EssenceCore plugin;
    private Economy vaultEconomy;
    private PlayerPointsAPI playerPointsAPI;
    private boolean vaultEnabled = false;
    private boolean playerPointsEnabled = false;
    
    public EconomyManager(EssenceCore plugin) {
        this.plugin = plugin;
        setupVault();
        setupPlayerPoints();
    }
    
    private void setupVault() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            vaultEconomy = rsp.getProvider();
            vaultEnabled = true;
            plugin.getLogger().info("Vault economy hooked successfully!");
        }
    }
    
    private void setupPlayerPoints() {
        if (Bukkit.getPluginManager().getPlugin("PlayerPoints") == null) {
            return;
        }
        
        PlayerPoints playerPointsPlugin = (PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints");
        if (playerPointsPlugin != null) {
            playerPointsAPI = playerPointsPlugin.getAPI();
            playerPointsEnabled = true;
            plugin.getLogger().info("PlayerPoints hooked successfully!");
        }
    }
    
    public boolean hasMoney(Player player, double amount) {
        if (!vaultEnabled || vaultEconomy == null) {
            return true;
        }
        return vaultEconomy.has(player, amount);
    }
    
    public boolean withdrawMoney(Player player, double amount) {
        if (!vaultEnabled || vaultEconomy == null) {
            return true;
        }
        return vaultEconomy.withdrawPlayer(player, amount).transactionSuccess();
    }
    
    public boolean hasPoints(Player player, int amount) {
        if (!playerPointsEnabled || playerPointsAPI == null) {
            return true;
        }
        return playerPointsAPI.look(player.getUniqueId()) >= amount;
    }
    
    public boolean withdrawPoints(Player player, int amount) {
        if (!playerPointsEnabled || playerPointsAPI == null) {
            return true;
        }
        return playerPointsAPI.take(player.getUniqueId(), amount);
    }
    
    public int getPoints(Player player) {
        if (!playerPointsEnabled || playerPointsAPI == null) {
            return 0;
        }
        return playerPointsAPI.look(player.getUniqueId());
    }
    
    public String formatMoney(double amount) {
        if (vaultEnabled && vaultEconomy != null) {
            return vaultEconomy.format(amount);
        }
        return String.valueOf(amount);
    }
}