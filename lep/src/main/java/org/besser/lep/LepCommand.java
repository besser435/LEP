package org.besser.lep;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.logging.Level.*;
import static org.besser.lep.LepLogger.log;

public class LepCommand implements CommandExecutor, TabCompleter {
    /*
        TODO: Make list command more appealing
        TODO: Ensure only items that are craftable can be sold
        TODO: CRITICAL BUG: Only opped players can use the /lep command
     */

    private final Lep plugin;
    private final SendBuyTelemetry sendBuyTelemetry;


    public LepCommand(Lep plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();

        this.sendBuyTelemetry = new SendBuyTelemetry(config);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // switch to switch statements maybe, or do something to make less stupid
        // TODO: add maintenance/debug commands and assign permissions.

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("list")) {
                return handleListCommand(sender);
            } else if (args[0].equalsIgnoreCase("buy") && args.length == 2) {
                return handleBuyCommand(sender, args[1]);
            } else if (args[0].equalsIgnoreCase("about")) {
                return handleAboutCommand(sender);
            } else if (args[0].equalsIgnoreCase("fish")) {
                return handleFishingLeaderboardCommand(sender);
            } else {
                sender.sendMessage(ChatColor.RED + "Unknown command. Usage: /lep <list|buy|about>");
                return false;
            }
        //} else if (args.length == 0) {
            //return handleListCommand(sender);
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /lep <list|buy|about>");    // TODO this should maybe just be the about command
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if ("list".startsWith(args[0].toLowerCase())) {
                completions.add("list");
            }
            if ("buy".startsWith(args[0].toLowerCase())) {
                completions.add("buy");
            }
            if ("about".startsWith(args[0].toLowerCase())) {
                completions.add("about");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("buy")) {
            List<Map<?, ?>> sellItems = PriceManager.getSellItems();
            for (int i = 1; i <= sellItems.size(); i++) {
                completions.add(String.valueOf(i));
            }
        }

        return completions;
    }

    private boolean handleListCommand(CommandSender sender) {
        List<Map<?, ?>> sellItems = PriceManager.getSellItems();

        if (sellItems == null || sellItems.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No items are available for purchase.");
            return false;
        }

        sender.sendMessage(ChatColor.AQUA + "Items available for purchase:");
        int index = 1;
        for (Map<?, ?> entry : sellItems) {
            String itemString = (String) entry.get("item");
            int itemQuantity = (int) entry.get("item_quantity");
            Map<?, ?> get = (Map<?, ?>) entry.get("get");
            String paymentItemString = (String) get.get("item");
            int paymentAmount = (int) get.get("payment_amount");

            Material itemMaterial = Material.matchMaterial(itemString);
            Material paymentMaterial = Material.matchMaterial(paymentItemString);
            String itemName = itemMaterial != null ? itemMaterial.name().replace('_', ' ').toLowerCase() : itemString;
            String paymentName = paymentMaterial != null ? paymentMaterial.name().replace('_', ' ').toLowerCase() : paymentItemString;

            itemName = itemNameRename(itemName);
            paymentName = itemNameRename(paymentName);
            // TODO: if the item has a component modifier, say so
            sender.sendMessage(ChatColor.GOLD + "" + index + ". " + ChatColor.WHITE +
                    itemQuantity + "x " + itemName + " for " +
                    paymentAmount + "x " + paymentName);
            index++;
        }
        return true;
    }

    private boolean handleBuyCommand(CommandSender sender, String indexStr) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return false;
        }

        Player player = (Player) sender;
        int index;

        try {
            index = Integer.parseInt(indexStr);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid index. Please enter a number.");
            return false;
        }

        List<Map<?, ?>> sellItems = PriceManager.getSellItems();

        if (index < 1 || index > sellItems.size()) {
            player.sendMessage(ChatColor.RED + "Invalid index. Please use /lep list to see available items.");
            return false;
        }

        Map<?, ?> selectedEntry = sellItems.get(index - 1);
        String itemString = (String) selectedEntry.get("item");
        int itemQuantity = (int) selectedEntry.get("item_quantity");
        Map<?, ?> get = (Map<?, ?>) selectedEntry.get("get");
        String paymentItemString = (String) get.get("item");
        int paymentAmount = (int) get.get("payment_amount");

        // Process payment item using Bukkit's Material. Does not support modded items as payment yet.
        Material paymentMaterial = Material.matchMaterial(paymentItemString);
        if (paymentMaterial == null) {
            player.sendMessage(ChatColor.RED + "Error processing your request. Please contact an administrator.");
            return false;
        }

        ItemStack paymentItemStack = new ItemStack(paymentMaterial, paymentAmount);

        if (!player.getInventory().containsAtLeast(paymentItemStack, paymentAmount)) {
            player.sendMessage(ChatColor.RED + "You don't have enough " + itemNameRename(paymentMaterial.name().replace('_', ' ').toLowerCase()) + ".");
            return false;
        }

        player.getInventory().removeItem(paymentItemStack);

        // TODO: Using /give is silly, and results in it being broadcast to OPs. Should find a different solution to support modded items. (Bukkit Materials dont work)
        String command = String.format("minecraft:give %s %s %d", player.getName(), itemString, itemQuantity);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

        log(INFO, "LEP traded with user. Giving: " + itemQuantity + "x " + itemString + " to " + player.getName());
        player.sendMessage(ChatColor.GREEN + "You have successfully purchased " + itemQuantity + "x " + itemNameRename(itemString) + ".");

        long timeInSeconds = System.currentTimeMillis() / 1000;

        sendBuyTelemetry.sendTelemetry(player.getName(), player.getUniqueId(), itemString, itemQuantity, paymentItemString, paymentAmount, timeInSeconds);

        return true;
    }

    private boolean handleAboutCommand(CommandSender sender) {
        String description = plugin.getDescription().getDescription();
        String version = plugin.getDescription().getVersion();
        String authors = String.join(", ", plugin.getDescription().getAuthors());
        String contributors = String.join(", ", plugin.getDescription().getContributors());
        String website = plugin.getDescription().getWebsite();

        sender.sendMessage(description);
        sender.sendMessage(ChatColor.AQUA + "LEP " + ChatColor.GOLD + "v" + version);
        sender.sendMessage(ChatColor.YELLOW + "Authors: " + ChatColor.RESET + authors);
        sender.sendMessage(ChatColor.YELLOW + "Contributors: " + ChatColor.RESET + contributors);


        TextComponent link = new TextComponent("LEP GitHub");
        link.setColor(ChatColor.BLUE.asBungee());
        link.setUnderlined(true);
        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, website));

        if (sender instanceof Player) {
            ((Player) sender).spigot().sendMessage(link);
        } else {
            sender.sendMessage(website);
        }

        return true;
    }

    private boolean handleFishingLeaderboardCommand(CommandSender sender) {
        // Kind of undocumented, do not include in tab autocomplete
        // TODO: make it so people can use any stat. Provide all of the stat enums in the tab auto complete
        Map<String, Integer> fishingStats = new HashMap<>();

        // Bukkit, why is it called offline, when it is every player?
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            int fishCaught = player.getStatistic(Statistic.FISH_CAUGHT);
            fishingStats.put(player.getName(), fishCaught);
        }

        List<Map.Entry<String, Integer>> leaderboard = new ArrayList<>(fishingStats.entrySet());
        leaderboard.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        sender.sendMessage(ChatColor.AQUA + "Fishing Leaderboard:");

        int rank = 1;
        for (Map.Entry<String, Integer> entry : leaderboard) {
            sender.sendMessage(ChatColor.GOLD + "" + rank + ". " + ChatColor.RESET + entry.getKey() + ": " + entry.getValue() + " fishe Caught");
            rank++;
        }

        return true;
    }

    private String itemNameRename(String text) {
        if (text.contains(":")) {
            text = text.split(":")[1];
        }

        text = text.replace('_', ' ');
        String[] words = text.split(" ");
        StringBuilder capitalized = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                capitalized.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return capitalized.toString().trim();
    }

}
