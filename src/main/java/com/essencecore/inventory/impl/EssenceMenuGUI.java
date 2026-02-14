package com.essencecore.inventory.impl;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.essencecore.EssenceCore;
import com.essencecore.data.PlayerData;
import com.essencecore.essence.Essence;
import com.essencecore.inventory.InventoryButton;
import com.essencecore.inventory.InventoryGUI;
import com.essencecore.utils.ColorUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class EssenceMenuGUI extends InventoryGUI {
    
    private final EssenceCore plugin;
    private final ConfigurationSection config;
    private final boolean papiEnabled;
    
    public EssenceMenuGUI(EssenceCore plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager().getInventoryConfig().getConfigurationSection("essence-menu");
        this.papiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }
    
    @Override
    protected Inventory createInventory() {
        int size = config.getInt("size", 27);
        String title = ColorUtil.color(config.getString("title", "&d&lEssence Menu"));
        return Bukkit.createInventory(null, size, title);
    }
    
    @Override
    public void decorate(Player player) {
        addFillerGlass(player);
        addEssenceItems(player);
        addInfoItem(player);
        super.decorate(player);
    }
    
    private void addFillerGlass(Player player) {
        ConfigurationSection fillerSection = config.getConfigurationSection("filler");
        if (fillerSection == null || !fillerSection.getBoolean("enabled", true)) return;
        
        String materialName = fillerSection.getString("material", "BLACK_STAINED_GLASS_PANE");
        Material material = XMaterial.matchXMaterial(materialName)
            .map(XMaterial::parseMaterial)
            .orElse(Material.BLACK_STAINED_GLASS_PANE);
        
        ItemStack filler = new ItemStack(material);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);
        }
        
        for (int slot : fillerSection.getIntegerList("slots")) {
            this.addButton(slot, new InventoryButton()
                .creator(p -> filler.clone())
                .consumer(event -> {})
            );
        }
    }
    
    private void addEssenceItems(Player player) {
        ConfigurationSection essenceSlots = config.getConfigurationSection("essence-slots");
        if (essenceSlots == null) return;
        
        List<Essence> essences = new ArrayList<>(plugin.getEssenceManager().getAllEssences());
        
        int index = 0;
        for (String slotKey : essenceSlots.getKeys(false)) {
            int slot = essenceSlots.getInt(slotKey);
            if (index >= essences.size()) break;
            
            Essence essence = essences.get(index);
            this.addButton(slot, new InventoryButton()
                .creator(p -> createEssenceItem(essence, p))
                .consumer(event -> handleEssenceClick((Player) event.getWhoClicked(), essence))
            );
            index++;
        }
    }
    
    private void addInfoItem(Player player) {
        ConfigurationSection infoSection = config.getConfigurationSection("info-item");
        if (infoSection == null) return;
        
        int slot = infoSection.getInt("slot", 22);
        
        this.addButton(slot, new InventoryButton()
            .creator(p -> createInfoItem(p))
            .consumer(event -> {})
        );
    }
    
    private ItemStack createEssenceItem(Essence essence, Player player) {
        String iconName = essence.getIcon();
        Material material = XMaterial.matchXMaterial(iconName)
            .map(XMaterial::parseMaterial)
            .orElse(Material.PAPER);
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(hex(essence.getName()));
            
            List<String> lore = new ArrayList<>();
            
            for (String line : essence.getDescription()) {
                String processedLine = line;
                if (papiEnabled) {
                    processedLine = PlaceholderAPI.setPlaceholders(player, processedLine);
                }
                lore.add(hex(processedLine));
            }
            
            lore.add("");
            
            PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
            if (data.getActiveEssence() != null && data.getActiveEssence().equals(essence.getId())) {
                lore.add(hex("&#42FF71✓ &#5FFF8AACTIVE"));
            } else if (player.hasPermission("essence.use." + essence.getId())) {
                if (data.hasEssence() && plugin.getConfigManager().isSwitchCostEnabled()) {
                    String costType = plugin.getConfigManager().getSwitchCostType();
                    double amount = plugin.getConfigManager().getSwitchCostAmount();
                    String costDisplay = costType.equalsIgnoreCase("playerpoints") 
                        ? (int) amount + " Points" 
                        : plugin.getEconomyManager().formatMoney(amount);
                    lore.add(hex("&#FFD93DCost: &#FFEB99" + costDisplay));
                }
                lore.add(hex("&#71FF71Click to select!"));
            } else {
                lore.add(hex("&#FF4757✗ &#FF6B79NO PERMISSION"));
            }
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    private ItemStack createInfoItem(Player player) {
        ConfigurationSection infoSection = config.getConfigurationSection("info-item");
        
        String materialName = infoSection.getString("material", "BOOK");
        Material material = XMaterial.matchXMaterial(materialName)
            .map(XMaterial::parseMaterial)
            .orElse(Material.BOOK);
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(hex(infoSection.getString("name", "&d&lEssence Info")));
            
            List<String> lore = new ArrayList<>();
            for (String line : infoSection.getStringList("lore")) {
                PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
                String currentEssence = data.hasEssence()
                    ? plugin.getEssenceManager().getEssence(data.getActiveEssence())
                        .map(Essence::getName)
                        .orElse("&7None")
                    : "&7None";
                
                String processedLine = line.replace("{current}", currentEssence);
                if (papiEnabled) {
                    processedLine = PlaceholderAPI.setPlaceholders(player, processedLine);
                }
                lore.add(hex(processedLine));
            }
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    private String hex(String text) {
        return ChatColor.translateAlternateColorCodes('&', 
            java.util.regex.Pattern.compile("&#([A-Fa-f0-9]{6})")
                .matcher(text)
                .replaceAll(match -> ChatColor.of("#" + match.group(1)).toString())
        );
    }
    
    private void handleEssenceClick(Player player, Essence essence) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        
        if (!player.hasPermission("essence.use." + essence.getId())) {
            XSound.matchXSound("ENTITY_VILLAGER_NO").ifPresent(s -> s.play(player));
            player.sendMessage(ColorUtil.color(plugin.getConfigManager().getPrefix() + " " + 
                plugin.getConfigManager().getMessage("no-permission")));
            return;
        }
        
        if (data.hasEssence() && data.getActiveEssence().equals(essence.getId())) {
            XSound.matchXSound("ENTITY_VILLAGER_NO").ifPresent(s -> s.play(player));
            player.sendMessage(ColorUtil.color(plugin.getConfigManager().getPrefix() + " " + 
                plugin.getConfigManager().getMessage("already-has-essence")));
            return;
        }
        
        if (data.hasEssence() && plugin.getConfigManager().isOneEssenceOnly()) {
            if (plugin.getConfigManager().isSwitchCostEnabled()) {
                if (!handleSwitchCost(player)) {
                    XSound.matchXSound("ENTITY_VILLAGER_NO").ifPresent(s -> s.play(player));
                    return;
                }
                
                plugin.getPlayerDataManager().setActiveEssence(player, essence.getId());
                
                String costType = plugin.getConfigManager().getSwitchCostType();
                double amount = plugin.getConfigManager().getSwitchCostAmount();
                String costDisplay = costType.equalsIgnoreCase("playerpoints") 
                    ? (int) amount + " Points" 
                    : plugin.getEconomyManager().formatMoney(amount);
                
                String message = plugin.getConfigManager().getMessage("essence-switched")
                    .replace("{essence}", essence.getName())
                    .replace("{cost}", costDisplay);
                
                XSound.matchXSound("ENTITY_PLAYER_LEVELUP").ifPresent(s -> s.play(player));
                player.sendMessage(ColorUtil.color(plugin.getConfigManager().getPrefix() + " " + message));
                
                player.closeInventory();
                return;
            } else {
                XSound.matchXSound("ENTITY_VILLAGER_NO").ifPresent(s -> s.play(player));
                player.sendMessage(ColorUtil.color(plugin.getConfigManager().getPrefix() + " " + 
                    plugin.getConfigManager().getMessage("one-essence-only")));
                return;
            }
        }
        
        plugin.getPlayerDataManager().setActiveEssence(player, essence.getId());
        
        String message = plugin.getConfigManager().getMessage("essence-given")
            .replace("{essence}", essence.getName());
        
        XSound.matchXSound("ENTITY_PLAYER_LEVELUP").ifPresent(s -> s.play(player));
        player.sendMessage(ColorUtil.color(plugin.getConfigManager().getPrefix() + " " + message));
        
        player.closeInventory();
    }
    
    private boolean handleSwitchCost(Player player) {
        String costType = plugin.getConfigManager().getSwitchCostType();
        double amount = plugin.getConfigManager().getSwitchCostAmount();
        
        if (costType.equalsIgnoreCase("vault")) {
            if (!plugin.getEconomyManager().hasMoney(player, amount)) {
                String currency = "money";
                String costDisplay = plugin.getEconomyManager().formatMoney(amount);
                String message = plugin.getConfigManager().getMessage("insufficient-funds")
                    .replace("{currency}", currency)
                    .replace("{cost}", costDisplay);
                player.sendMessage(ColorUtil.color(plugin.getConfigManager().getPrefix() + " " + message));
                return false;
            }
            return plugin.getEconomyManager().withdrawMoney(player, amount);
        } else if (costType.equalsIgnoreCase("playerpoints")) {
            int points = (int) amount;
            if (!plugin.getEconomyManager().hasPoints(player, points)) {
                String currency = "PlayerPoints";
                String costDisplay = points + " Points";
                String message = plugin.getConfigManager().getMessage("insufficient-funds")
                    .replace("{currency}", currency)
                    .replace("{cost}", costDisplay);
                player.sendMessage(ColorUtil.color(plugin.getConfigManager().getPrefix() + " " + message));
                return false;
            }
            return plugin.getEconomyManager().withdrawPoints(player, points);
        }
        
        return true;
    }
}