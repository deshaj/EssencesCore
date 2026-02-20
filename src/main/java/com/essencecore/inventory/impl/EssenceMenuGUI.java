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
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class EssenceMenuGUI extends InventoryGUI {
    
    private final EssenceCore plugin;
    private final int page;
    private final int essencesPerPage = 9;
    private final boolean papiEnabled;
    
    public EssenceMenuGUI(EssenceCore plugin, int page) {
        this.plugin = plugin;
        this.page = page;
        this.papiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }
    
    @Override
    protected Inventory createInventory() {
        List<Essence> essences = new ArrayList<>(plugin.getEssenceManager().getAllEssences());
        int totalPages = (int) Math.ceil((double) essences.size() / essencesPerPage);
        String title = ColorUtil.color("&8Origin " + (page + 1) + "/" + totalPages);
        return Bukkit.createInventory(null, 54, title);
    }
    
    @Override
    public void decorate(Player player) {
        addFillerGlass(player);
        addEssenceItems(player);
        addNavigationButtons(player);
        super.decorate(player);
    }
    
    private void addFillerGlass(Player player) {
        Material material = XMaterial.matchXMaterial("BLACK_STAINED_GLASS_PANE")
            .map(XMaterial::parseMaterial)
            .orElse(Material.BLACK_STAINED_GLASS_PANE);
        
        ItemStack filler = new ItemStack(material);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);
        }
        
        int[] fillerSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};
        for (int slot : fillerSlots) {
            this.addButton(slot, new InventoryButton()
                .creator(p -> filler.clone())
                .consumer(event -> {})
            );
        }
    }
    
    private void addEssenceItems(Player player) {
        List<Essence> essences = new ArrayList<>(plugin.getEssenceManager().getAllEssences());
        int startIndex = page * essencesPerPage;
        int endIndex = Math.min(startIndex + essencesPerPage, essences.size());
        
        int[] essenceSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20};
        int slotIndex = 0;
        
        for (int i = startIndex; i < endIndex && slotIndex < essenceSlots.length; i++, slotIndex++) {
            Essence essence = essences.get(i);
            this.addButton(essenceSlots[slotIndex], new InventoryButton()
                .creator(p -> createEssenceItem(essence, p))
                .consumer(event -> handleEssenceClick((Player) event.getWhoClicked(), essence))
            );
        }
    }
    
    private void addNavigationButtons(Player player) {
        List<Essence> essences = new ArrayList<>(plugin.getEssenceManager().getAllEssences());
        int totalPages = (int) Math.ceil((double) essences.size() / essencesPerPage);
        
        if (page > 0) {
            Material prevMat = XMaterial.matchXMaterial("LIME_STAINED_GLASS_PANE")
                .map(XMaterial::parseMaterial)
                .orElse(Material.LIME_STAINED_GLASS_PANE);
            
            ItemStack prevPage = new ItemStack(prevMat);
            ItemMeta prevMeta = prevPage.getItemMeta();
            if (prevMeta != null) {
                prevMeta.setDisplayName(ColorUtil.color("&aPrevious Page"));
                prevPage.setItemMeta(prevMeta);
            }
            
            this.addButton(28, new InventoryButton()
                .creator(p -> prevPage.clone())
                .consumer(event -> {
                    event.setCancelled(true);
                    EssenceMenuGUI newGui = new EssenceMenuGUI(plugin, page - 1);
                    plugin.getGuiManager().openGUI(newGui, (Player) event.getWhoClicked());
                })
            );
        }
        
        if (page < totalPages - 1) {
            Material nextMat = XMaterial.matchXMaterial("LIME_STAINED_GLASS_PANE")
                .map(XMaterial::parseMaterial)
                .orElse(Material.LIME_STAINED_GLASS_PANE);
            
            ItemStack nextPage = new ItemStack(nextMat);
            ItemMeta nextMeta = nextPage.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName(ColorUtil.color("&aNext Page"));
                nextPage.setItemMeta(nextMeta);
            }
            
            this.addButton(34, new InventoryButton()
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
            meta.setDisplayName(hex(essence.getName()));
            
            List<String> lore = new ArrayList<>();
            PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
            int playerPoints = plugin.getEconomyManager().getPoints(player);
            
            if (data.isOnTrial() && data.getTrialEssence().equals(essence.getId())) {
                long remaining = (data.getTrialEndTime() - System.currentTimeMillis()) / 1000 / 60;
                lore.add(hex("&#ecff00&l⭐ &#ecff00FREE TRIAL - " + remaining + " minutes left"));
            } else if (!data.isHasUsedTrial()) {
                lore.add(hex("&#ecff00&l⭐ &#ecff00Try for FREE for 1 hour"));
            } else {
                lore.add(hex("&#ecff00&l⭐ &#ecff00Buy for " + essence.getCost() + " Credits"));
            }
            
            lore.add(hex("&#4887FA&l⭐ &#4887FAYou have " + playerPoints + " Credits"));
            lore.add("");
            lore.add(hex(essence.getName() + " Perks:"));
            
            for (String line : essence.getDescription()) {
                String processedLine = line;
                if (papiEnabled) {
                    processedLine = PlaceholderAPI.setPlaceholders(player, processedLine);
                }
                lore.add(hex(processedLine));
            }
            
            lore.add("");
            
            if (data.getActiveEssence() != null && data.getActiveEssence().equals(essence.getId())) {
                lore.add(hex("&#42FF71✓ &#5FFF8AACTIVE"));
            } else if (!data.isHasUsedTrial()) {
                lore.add(hex("&#4887FA→ &fClick to try for free"));
            } else {
                lore.add(hex("&#4887FA→ &fClick to purchase"));
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
        
        if (data.hasEssence() && data.getActiveEssence().equals(essence.getId())) {
            XSound.matchXSound("ENTITY_VILLAGER_NO").ifPresent(s -> s.play(player));
            player.sendMessage(ColorUtil.color(plugin.getConfigManager().getPrefix() + " " + 
                plugin.getConfigManager().getMessage("already-has-essence")));
            return;
        }
        
        if (!data.isHasUsedTrial() && !data.isOnTrial()) {
            data.setTrialEssence(essence.getId());
            data.setTrialEndTime(System.currentTimeMillis() + (60 * 60 * 1000));
            plugin.getPlayerDataManager().setActiveEssence(player, essence.getId());
            
            String message = plugin.getConfigManager().getMessage("trial-started")
                .replace("{essence}", essence.getName());
            
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
        
        XSound.matchXSound("ENTITY_PLAYER_LEVELUP").ifPresent(s -> s.play(player));
        player.sendMessage(ColorUtil.color(plugin.getConfigManager().getPrefix() + " " + message));
        player.closeInventory();
    }
}