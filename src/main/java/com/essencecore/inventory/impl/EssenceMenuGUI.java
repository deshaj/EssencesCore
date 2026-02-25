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
import java.util.stream.Collectors;

public class EssenceMenuGUI extends InventoryGUI {
    
    private final EssenceCore plugin;
    private final int page;
    private final boolean papiEnabled;
    
    public EssenceMenuGUI(EssenceCore plugin, int page) {
        this.plugin = plugin;
        this.page = page;
        this.papiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }
    
    @Override
    protected Inventory createInventory() {
        ConfigurationSection menuConfig = plugin.getConfigManager().getInventoryConfig().getConfigurationSection("essence-menu");
        List<Essence> essences = new ArrayList<>(plugin.getEssenceManager().getAllEssences());
        
        int size = menuConfig.getInt("size", 54);
        List<Integer> essenceSlots = menuConfig.getIntegerList("essence-slots");
        int essencesPerPage = essenceSlots.size();
        int totalPages = (int) Math.ceil((double) essences.size() / essencesPerPage);
        
        String titleTemplate = menuConfig.getString("title", "&8Origin {page}/{total}");
        String title = ColorUtil.color(titleTemplate
            .replace("{page}", String.valueOf(page + 1))
            .replace("{total}", String.valueOf(totalPages)));
        
        return Bukkit.createInventory(null, size, title);
    }
    
    @Override
    public void decorate(Player player) {
        addFillerGlass(player);
        addEssenceItems(player);
        addNavigationButtons(player);
        super.decorate(player);
    }
    
    private void addFillerGlass(Player player) {
        ConfigurationSection fillerConfig = plugin.getConfigManager().getInventoryConfig().getConfigurationSection("essence-menu.filler");
        
        if (fillerConfig == null || !fillerConfig.getBoolean("enabled", true)) {
            return;
        }
        
        String materialName = fillerConfig.getString("material", "BLACK_STAINED_GLASS_PANE");
        String fillerName = fillerConfig.getString("name", " ");
        List<Integer> slots = fillerConfig.getIntegerList("slots");
        
        Material material = XMaterial.matchXMaterial(materialName)
            .map(XMaterial::parseMaterial)
            .orElse(Material.BLACK_STAINED_GLASS_PANE);
        
        ItemStack filler = new ItemStack(material);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.color(fillerName));
            filler.setItemMeta(meta);
        }
        
        for (int slot : slots) {
            this.addButton(slot, new InventoryButton()
                .creator(p -> filler.clone())
                .consumer(event -> {})
            );
        }
    }
    
    private void addEssenceItems(Player player) {
        ConfigurationSection menuConfig = plugin.getConfigManager().getInventoryConfig().getConfigurationSection("essence-menu");
        List<Integer> essenceSlots = menuConfig.getIntegerList("essence-slots");
        
        List<Essence> essences = new ArrayList<>(plugin.getEssenceManager().getAllEssences());
        int essencesPerPage = essenceSlots.size();
        int startIndex = page * essencesPerPage;
        int endIndex = Math.min(startIndex + essencesPerPage, essences.size());
        
        int slotIndex = 0;
        
        for (int i = startIndex; i < endIndex && slotIndex < essenceSlots.size(); i++, slotIndex++) {
            Essence essence = essences.get(i);
            this.addButton(essenceSlots.get(slotIndex), new InventoryButton()
                .creator(p -> createEssenceItem(essence, p))
                .consumer(event -> handleEssenceClick((Player) event.getWhoClicked(), essence))
            );
        }
    }
    
    private void addNavigationButtons(Player player) {
        ConfigurationSection menuConfig = plugin.getConfigManager().getInventoryConfig().getConfigurationSection("essence-menu");
        List<Integer> essenceSlots = menuConfig.getIntegerList("essence-slots");
        List<Essence> essences = new ArrayList<>(plugin.getEssenceManager().getAllEssences());
        
        int essencesPerPage = essenceSlots.size();
        int totalPages = (int) Math.ceil((double) essences.size() / essencesPerPage);
        
        ConfigurationSection paginationConfig = menuConfig.getConfigurationSection("pagination");
        
        if (page > 0) {
            ConfigurationSection prevConfig = paginationConfig.getConfigurationSection("previous");
            int prevSlot = prevConfig.getInt("slot", 48);
            String prevMaterial = prevConfig.getString("material", "RED_SHULKER_BOX");
            String prevName = prevConfig.getString("name", "&cPrevious Page");
            
            Material prevMat = XMaterial.matchXMaterial(prevMaterial)
                .map(XMaterial::parseMaterial)
                .orElse(Material.RED_SHULKER_BOX);
            
            ItemStack prevPage = new ItemStack(prevMat);
            ItemMeta prevMeta = prevPage.getItemMeta();
            if (prevMeta != null) {
                prevMeta.setDisplayName(ColorUtil.color(prevName));
                prevPage.setItemMeta(prevMeta);
            }
            
            this.addButton(prevSlot, new InventoryButton()
                .creator(p -> prevPage.clone())
                .consumer(event -> {
                    event.setCancelled(true);
                    EssenceMenuGUI newGui = new EssenceMenuGUI(plugin, page - 1);
                    plugin.getGuiManager().openGUI(newGui, (Player) event.getWhoClicked());
                })
            );
        }
        
        if (page < totalPages - 1) {
            ConfigurationSection nextConfig = paginationConfig.getConfigurationSection("next");
            int nextSlot = nextConfig.getInt("slot", 50);
            String nextMaterial = nextConfig.getString("material", "GREEN_SHULKER_BOX");
            String nextName = nextConfig.getString("name", "&aNext Page");
            
            Material nextMat = XMaterial.matchXMaterial(nextMaterial)
                .map(XMaterial::parseMaterial)
                .orElse(Material.GREEN_SHULKER_BOX);
            
            ItemStack nextPage = new ItemStack(nextMat);
            ItemMeta nextMeta = nextPage.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName(ColorUtil.color(nextName));
                nextPage.setItemMeta(nextMeta);
            }
            
            this.addButton(nextSlot, new InventoryButton()
                .creator(p -> nextPage.clone())
                .consumer(event -> {
                    event.setCancelled(true);
                    EssenceMenuGUI newGui = new EssenceMenuGUI(plugin, page + 1);
                    plugin.getGuiManager().openGUI(newGui, (Player) event.getWhoClicked());
                })
            );
        }
    }
    
    private ItemStack createEssenceItem(Essence essence, Player player) {
        String iconName = essence.getIcon();
        Material material = XMaterial.matchXMaterial(iconName)
            .map(XMaterial::parseMaterial)
            .orElse(Material.PAPER);
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            String displayName = essence.getName();
            if (papiEnabled) {
                displayName = PlaceholderAPI.setPlaceholders(player, displayName);
            }
            meta.setDisplayName(hex(displayName));
            
            List<String> lore = buildLore(essence, player);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    private List<String> buildLore(Essence essence, Player player) {
        ConfigurationSection loreConfig = plugin.getConfigManager().getInventoryConfig().getConfigurationSection("essence-menu.lore");
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        int playerPoints = plugin.getEconomyManager().getPoints(player);
        
        List<String> lore = new ArrayList<>();
        
        if (data.isOnTrial() && data.getTrialEssence() != null && data.getTrialEssence().equals(essence.getId())) {
            long remaining = (data.getTrialEndTime() - System.currentTimeMillis()) / 1000 / 60;
            String trialLine = loreConfig.getString("trial-active", "&#ecff00&l⭐ &#ecff00FREE TRIAL - {remaining} minutes left")
                .replace("{remaining}", String.valueOf(remaining));
            lore.add(hex(applyPlaceholders(player, trialLine)));
        } else if (!data.isUsedTrial()) {
            String trialAvailableLine = loreConfig.getString("trial-available", "&#ecff00&l⭐ &#ecff00Try for FREE for 1 hour");
            lore.add(hex(applyPlaceholders(player, trialAvailableLine)));
        } else {
            String costLine = loreConfig.getString("purchase-cost", "&#ecff00&l⭐ &#ecff00Buy for {cost} Credits")
                .replace("{cost}", String.valueOf(essence.getCost()));
            lore.add(hex(applyPlaceholders(player, costLine)));
        }
        
        String creditsLine = loreConfig.getString("current-credits", "&#4887FA&l⭐ &#4887FAYou have {current} Credits")
            .replace("{current}", String.valueOf(playerPoints));
        lore.add(hex(applyPlaceholders(player, creditsLine)));
        
        lore.add("");
        
        String perksHeader = loreConfig.getString("perks-header", "{essence} Perks:")
            .replace("{essence}", hex(essence.getName()));
        lore.add(hex(applyPlaceholders(player, perksHeader)));
        
        for (String line : essence.getDescription()) {
            lore.add(hex(applyPlaceholders(player, line)));
        }
        
        lore.add("");
        
        if (data.getActiveEssence() != null && data.getActiveEssence().equals(essence.getId())) {
            String activeLine = loreConfig.getString("active-status", "&#42FF71✓ &#5FFF8AACTIVE");
            lore.add(hex(applyPlaceholders(player, activeLine)));
        } else if (!data.isUsedTrial()) {
            String clickTrialLine = loreConfig.getString("click-trial", "&#4887FA→ &fClick to try for free");
            lore.add(hex(applyPlaceholders(player, clickTrialLine)));
        } else {
            String clickPurchaseLine = loreConfig.getString("click-purchase", "&#4887FA→ &fClick to purchase");
            lore.add(hex(applyPlaceholders(player, clickPurchaseLine)));
        }
        
        return lore;
    }
    
    private String applyPlaceholders(Player player, String text) {
        if (papiEnabled) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
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
        
        if (data.hasEssence() && data.getActiveEssence().equals(essence.getId())) {
            XSound.matchXSound("ENTITY_VILLAGER_NO").ifPresent(s -> s.play(player));
            player.sendMessage(ColorUtil.color(plugin.getConfigManager().getPrefix() + " " + 
                plugin.getConfigManager().getMessage("already-has-essence")));
            return;
        }
        
        if (data.isOnTrial() && data.getTrialEssence() != null) {
            plugin.getPlayerDataManager().setActiveEssence(player, essence.getId());
            data.setTrialEssence(essence.getId());
            
            String message = plugin.getConfigManager().getMessage("essence-switched-trial")
                .replace("{essence}", essence.getName());
            
            if (papiEnabled) {
                message = PlaceholderAPI.setPlaceholders(player, message);
            }
            
            XSound.matchXSound("ENTITY_PLAYER_LEVELUP").ifPresent(s -> s.play(player));
            player.sendMessage(ColorUtil.color(plugin.getConfigManager().getPrefix() + " " + message));
            player.closeInventory();
            return;
        }
        
        if (!data.isUsedTrial() && !data.isOnTrial()) {
            data.setUsedTrial(true);
            data.setTrialEssence(essence.getId());
            data.setTrialEndTime(System.currentTimeMillis() + (60 * 60 * 1000));
            plugin.getPlayerDataManager().setActiveEssence(player, essence.getId());
            
            String message = plugin.getConfigManager().getMessage("trial-started")
                .replace("{essence}", essence.getName());
            
            if (papiEnabled) {
                message = PlaceholderAPI.setPlaceholders(player, message);
            }
            
            XSound.matchXSound("ENTITY_PLAYER_LEVELUP").ifPresent(s -> s.play(player));
            player.sendMessage(ColorUtil.color(plugin.getConfigManager().getPrefix() + " " + message));
            player.closeInventory();
            return;
        }
        
        int playerPoints = plugin.getEconomyManager().getPoints(player);
        if (playerPoints < essence.getCost()) {
            String message = plugin.getConfigManager().getMessage("insufficient-credits")
                .replace("{cost}", String.valueOf(essence.getCost()))
                .replace("{current}", String.valueOf(playerPoints));
            
            if (papiEnabled) {
                message = PlaceholderAPI.setPlaceholders(player, message);
            }
            
            XSound.matchXSound("ENTITY_VILLAGER_NO").ifPresent(s -> s.play(player));
            player.sendMessage(ColorUtil.color(plugin.getConfigManager().getPrefix() + " " + message));
            return;
        }
        
        if (!plugin.getEconomyManager().withdrawPoints(player, essence.getCost())) {
            XSound.matchXSound("ENTITY_VILLAGER_NO").ifPresent(s -> s.play(player));
            player.sendMessage(ColorUtil.color(plugin.getConfigManager().getPrefix() + " &cFailed to process payment!"));
            return;
        }
        
        plugin.getPlayerDataManager().setActiveEssence(player, essence.getId());
        
        String message = plugin.getConfigManager().getMessage("essence-purchased")
            .replace("{essence}", essence.getName())
            .replace("{cost}", String.valueOf(essence.getCost()));
        
        if (papiEnabled) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }
        
        XSound.matchXSound("ENTITY_PLAYER_LEVELUP").ifPresent(s -> s.play(player));
        player.sendMessage(ColorUtil.color(plugin.getConfigManager().getPrefix() + " " + message));
        player.closeInventory();
    }
}