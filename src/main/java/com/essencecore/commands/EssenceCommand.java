package com.essencecore.commands;

import com.essencecore.EssenceCore;
import com.essencecore.data.PlayerData;
import com.essencecore.essence.Essence;
import com.essencecore.inventory.impl.EssenceMenuGUI;
import com.essencecore.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EssenceCommand implements CommandExecutor, TabCompleter {
    
    private final EssenceCore plugin;
    
    public EssenceCommand(EssenceCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ColorUtil.color("&cOnly players can use this command!"));
                return true;
            }
            
            Player player = (Player) sender;
            EssenceMenuGUI gui = new EssenceMenuGUI(plugin, 0);
            plugin.getGuiManager().openGUI(gui, player);
            return true;
        }
        
        if (!sender.hasPermission("essence.admin")) {
            sender.sendMessage(ColorUtil.color("&cYou don't have permission!"));
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "give" -> {
                if (args.length < 3) {
                    sender.sendMessage(ColorUtil.color("&cUsage: /essence give <player> <essence>"));
                    return true;
                }
                giveEssence(sender, args[1], args[2]);
            }
            case "remove" -> {
                if (args.length < 2) {
                    sender.sendMessage(ColorUtil.color("&cUsage: /essence remove <player>"));
                    return true;
                }
                removeEssence(sender, args[1]);
            }
            case "list" -> listEssences(sender);
            case "reload" -> {
                plugin.reload();
                sender.sendMessage(ColorUtil.color(plugin.getConfigManager().getPrefix() + " " + 
                    plugin.getConfigManager().getMessage("config-reloaded")));
            }
            default -> sendHelp(sender);
        }
        
        return true;
    }
    
    private void giveEssence(CommandSender sender, String playerName, String essenceId) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ColorUtil.color("&cPlayer not found!"));
            return;
        }
        
        Essence essence = plugin.getEssenceManager().getEssence(essenceId).orElse(null);
        if (essence == null) {
            sender.sendMessage(ColorUtil.color("&cEssence not found!"));
            return;
        }
        
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(target);
        
        if (data.hasEssence() && plugin.getConfigManager().isOneEssenceOnly()) {
            sender.sendMessage(ColorUtil.color("&cPlayer already has an essence!"));
            return;
        }
        
        plugin.getPlayerDataManager().setActiveEssence(target, essenceId);
        
        String message = plugin.getConfigManager().getMessage("essence-given")
            .replace("{essence}", essence.getName());
        target.sendMessage(ColorUtil.color(plugin.getConfigManager().getPrefix() + " " + message));
        sender.sendMessage(ColorUtil.color("&aGave " + essence.getName() + " &ato " + target.getName()));
    }
    
    private void removeEssence(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ColorUtil.color("&cPlayer not found!"));
            return;
        }
        
        plugin.getPlayerDataManager().clearActiveEssence(target);
        plugin.getCooldownManager().clearCooldowns(target.getUniqueId());
        
        target.sendMessage(ColorUtil.color(plugin.getConfigManager().getPrefix() + " " + 
            plugin.getConfigManager().getMessage("essence-removed")));
        sender.sendMessage(ColorUtil.color("&aRemoved essence from " + target.getName()));
    }
    
    private void listEssences(CommandSender sender) {
        sender.sendMessage(ColorUtil.color("&d&lAvailable Essences:"));
        for (Essence essence : plugin.getEssenceManager().getAllEssences()) {
            sender.sendMessage(ColorUtil.color("&7- &f" + essence.getId() + " &7(" + essence.getName() + "&7)"));
        }
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtil.color("&d&lEssence Commands:"));
        sender.sendMessage(ColorUtil.color("&7/essence &f- Open essence menu"));
        sender.sendMessage(ColorUtil.color("&7/essence give <player> <essence> &f- Give essence"));
        sender.sendMessage(ColorUtil.color("&7/essence remove <player> &f- Remove essence"));
        sender.sendMessage(ColorUtil.color("&7/essence list &f- List all essences"));
        sender.sendMessage(ColorUtil.color("&7/essence reload &f- Reload config"));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission("essence.admin")) {
            return completions;
        }
        
        if (args.length == 1) {
            completions.add("give");
            completions.add("remove");
            completions.add("list");
            completions.add("reload");
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("remove"))) {
            Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            plugin.getEssenceManager().getAllEssences().forEach(e -> completions.add(e.getId()));
        }
        
        return completions;
    }
}