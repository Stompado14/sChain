package chain.Commands;

import chain.Utils.ActionBarAPI;
import chain.Utils.LocationAPI;
import chain.Utils.T_Config;
import chain.sChain;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Chain implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lb, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cO console não executa esse comando.");
            return true;
        }

        Player p = (Player) sender;
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("sair")) {
                if (!sChain.onChain.contains(p.getName())) {
                    p.sendMessage(sChain.Config.ForaChain);
                    return true;
                }

                if (hasChainSaidaLocation(p)) { return true; }

                playerTeleportExitChain(p);
                p.getInventory().clear();
                p.getInventory().setArmorContents(null);
                sChain.onChain.remove(p.getName());
                for (Player exit : Bukkit.getOnlinePlayers())
                    ActionBarAPI.sendActionBar(sChain.Config.ChainMessageExit.replace("{jogador}", p.getName()), exit);

            } else if (args[0].equalsIgnoreCase("setspawn") || args[0].equalsIgnoreCase("setentrada") || args[0].equalsIgnoreCase("setarena")) {
                if (!p.hasPermission("schain.admin")) {
                    p.sendMessage(sChain.Config.semPerm);
                    return true;
                }
                String locationSerialized = LocationAPI.locationSerializer(p.getLocation());

                sChain.Instance.locations.getConfig().set("Locais.Chain.Entrada", locationSerialized);
                sChain.Instance.locations.saveConfig();

                p.sendMessage("§c[sChain] §aA §cChain §afoi definida com sucesso.");

            } else if (args[0].equalsIgnoreCase("setsaida") || args[0].equalsIgnoreCase("setexit")) {
                if (!p.hasPermission("schain.admin")) {
                    p.sendMessage(sChain.Config.semPerm);
                    return true;
                }
                String locationSerialized = LocationAPI.locationSerializer(p.getLocation());

                sChain.Instance.locations.getConfig().set("Locais.Chain.Saida", locationSerialized);
                sChain.Instance.locations.saveConfig();

                p.sendMessage("§c[sChain] §aO local de §cSaída §ada §cChain §afoi definido com sucesso.");

            } else if (args[0].equalsIgnoreCase("setkit") || args[0].equalsIgnoreCase("setarkit")) {
                if (!p.hasPermission("schain.admin")) {
                    p.sendMessage(sChain.Config.semPerm);
                    return true;

                }

                if (isInventoryEmpty(p)) {
                    p.sendMessage("§c[sChain] §cVoce não possui itens no inventário para setar o Kit.");
                    return true;
                }

                int loop;

                for (loop = 0; loop <= 35; loop++)
                    sChain.Instance.kit.set("Itens.Slot." + loop, p.getInventory().getItem(loop));

                for (loop = 36; loop <= 39; loop++)
                    sChain.Instance.kit.set("Armadura.Slot." + loop, p.getInventory().getItem(loop));

                sChain.Instance.kit.saveConfig();

                p.getInventory().clear();
                p.getInventory().setArmorContents(null);
                p.sendMessage("§c[sChain] §aO kit do §cChain §afoi definido com sucesso.");

            } else if (args[0].equalsIgnoreCase("ajuda") || args[0].equalsIgnoreCase("help")) {

                if (!p.hasPermission("sChain.admin")) {
                    p.sendMessage("");
                    p.sendMessage("§c§lsChain §7§l>> §7Comandos:");
                    p.sendMessage("");
                    p.sendMessage("§e/chain §7- §eEntre na §cChain");
                    p.sendMessage("§e/chain sair §7- §eSaida da §cChain");
                    p.sendMessage("§e/chain ajuda §7- §eVeja essa mensagem");
                }
                p.sendMessage("§e/chain §7- §eEntre na §cChain");
                p.sendMessage("§e/chain sair §7- §eSaida da §cChain");
                p.sendMessage("§e/chain setkit/setarkit §7- §eDefina o kit da §cChain");
                p.sendMessage("§e/chain setentrada/setspawn/setarena §7- §eDefina o local de entrada da §cChain");
                p.sendMessage("§e/chain setsaida/setexit §7- §eDefina o local de saida da §cChain");
                p.sendMessage("§e/chain ajuda §7- §eVeja esta mensagem");
                p.sendMessage("");

            } else {
                p.sendMessage("§c[sChain] §7Comando incorreto, use /chain ajuda");
            }

        } else {

            if (sChain.onChain.contains(p.getName())) {
                p.sendMessage(sChain.Config.PlayerChain);
                return true;
            }

            if (sChain.Instance.kit.getConfig().get("Itens.Slot") == null) {
                p.sendMessage("O kit ainda não foi definido.");
                return true;
            }
            if (sChain.Instance.kit.getConfig().get("Armadura.Slot") == null) {
                p.sendMessage("O kit ainda não foi definido.");
                return true;
            }

            if (hasChainLocation(p)) { return true; }

            if (hasChainSaidaLocation(p)) { return true; }

            if (p.getInventory().getHelmet() != null || p.getInventory().getChestplate() != null || p.getInventory().getLeggings() != null || p.getInventory().getBoots() != null) {
                p.sendMessage(sChain.Config.ItemsNoInv);
                return true;

            }

            if (!isInventoryEmpty(p)) {
                p.sendMessage(sChain.Config.ItemsNoInv);
                return true;
            }

            if (sChain.onChain.contains(p.getName())) {
                p.sendMessage(sChain.Config.PlayerChain);
                return true;
            }

            playerTeleportChain(p);

            sChain.onChain.add(p.getName());

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "effect " + p.getName() + " clear");

            int loop;

            for (loop = 0; loop <= 35; loop++)
                p.getInventory().setItem(loop, sChain.Instance.kit.getConfig().getItemStack("Itens.Slot." + loop));

            for (loop = 36; loop <= 39; loop++)
                p.getInventory().setItem(loop, sChain.Instance.kit.getConfig().getItemStack("Armadura.Slot." + loop));

            p.updateInventory();

            for (Player action : Bukkit.getOnlinePlayers())
                ActionBarAPI.sendActionBar(sChain.Config.ChainMessage.replace("{jogador}", p.getName()), action);

        }
        return false;
    }

    private boolean isInventoryEmpty(Player p){
        for(ItemStack item : p.getInventory().getContents())
        {
            if(item != null)
                return false;
        }
        return true;
    }

    private void playerTeleportChain(Player p) {
        T_Config locationFile = sChain.Instance.locations;
        FileConfiguration locationConfig = locationFile.getConfig();

        Location locationChain = LocationAPI.locationUnserializer(locationConfig.getString("Locais.Chain.Entrada"));

        p.teleport(locationChain);
    }

    public void playerTeleportExitChain(Player p) {
        T_Config locationFile = sChain.Instance.locations;
        FileConfiguration locationConfig = locationFile.getConfig();

        Location locationExitChain = LocationAPI.locationUnserializer(locationConfig.getString("Locais.Chain.Saida"));

        p.teleport(locationExitChain);
    }

    private boolean hasChainLocation(Player p) {
        T_Config locationFile = sChain.Instance.locations;
        FileConfiguration locationConfig = locationFile.getConfig();

        String location = locationConfig.getString("Locais.Chain.Entrada");

        if (location == null) {
            p.sendMessage("§c[sChain] A chain ainda não foi definida.");
            return  true;
        }
        return false;
    }

    private boolean hasChainSaidaLocation(Player p) {
        T_Config locationFile = sChain.Instance.locations;
        FileConfiguration locationConfig = locationFile.getConfig();

        String location = locationConfig.getString("Locais.Chain.Saida");

        if (location == null) {
            p.sendMessage("§c[sChain] A saida da chain ainda não foi definida.");
            return  true;
        }
        return false;
    }
}