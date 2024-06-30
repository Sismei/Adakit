package com.agevate.adakit;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Adakit extends JavaPlugin {

    private File addressFile;
    private FileConfiguration addressConfig;

    private File messagesFile;
    private FileConfiguration messagesConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        addressFile = new File(getDataFolder(), "address.yml");
        if (!addressFile.exists()) {
            try {
                addressFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        addressConfig = YamlConfiguration.loadConfiguration(addressFile);

        messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("adakit")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("adakit.reload")) {
                    reloadConfig();
                    addressConfig = YamlConfiguration.loadConfiguration(addressFile);
                    messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
                    sender.sendMessage("§aAdakit yenilendi.");
                } else {
                    sender.sendMessage("§cBu komutu kullanmak için yetkin yok!");
                }
                return true;
            }

            if (sender instanceof Player) {
                Player player = (Player) sender;
                String playerIp = player.getAddress().getAddress().getHostAddress();

                List<String> usedIps = addressConfig.getStringList("usedIps");

                if (usedIps.contains(playerIp)) {
                    player.sendMessage(messagesConfig.getString("already-received"));
                    return true;
                }

                usedIps.add(playerIp);
                addressConfig.set("usedIps", usedIps);

                try {
                    addressConfig.save(addressFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                FileConfiguration config = getConfig();
                for (String cmd : config.getStringList("kit-cmds")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
                }

                player.sendMessage(messagesConfig.getString("kit-received"));
                return true;
            }
        }
        return false;
    }
}
