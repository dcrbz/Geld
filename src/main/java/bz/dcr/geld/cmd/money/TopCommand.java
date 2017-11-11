package bz.dcr.geld.cmd.money;

import bz.dcr.geld.Geld;
import bz.dcr.geld.cmd.IGeldCommand;
import bz.dcr.geld.data.PlayerData;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public class TopCommand implements IGeldCommand {

    private Geld plugin;

    // Constructor
    public TopCommand(Geld plugin){
        this.plugin = plugin;
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        this.plugin.getExecutor().execute(() -> {
            List<PlayerData> data;

            if (args.length == 1) {
                data = this.plugin.getDB().getTop(10);
            } else if (args.length == 2) {
                int amount;
                if(!NumberUtils.isNumber(args[2])){
                    sender.sendMessage(this.plugin.getLang().getMessage("invalidNumber", args[1]));
                    return;
                } else if((amount = Integer.parseInt(args[1])) < 1) {
                    sender.sendMessage(this.plugin.getLang().getMessage("invalidNumber", args[1]));
                    return;
                }
                data = this.plugin.getDB().getTop(amount);
            } else {
                this.printHelp(sender);
                return;
            }

            Optional<String> currentPlayerName;
            for(int i = 0; i < data.size(); i++){
                currentPlayerName = plugin.getIdentificationProvider().getName(data.get(i).getUniqueId());

                if(!currentPlayerName.isPresent())
                    currentPlayerName = Optional.of("Unknown");

                sender.sendMessage(
                        "§e§l" + (i + 1) + ". ) §b" + currentPlayerName.get() + " §f- §b" + data.get(i).getBalance()
                );
            }
        });
    }

    @Override
    public void executePlayer(Player sender, String[] args) {
        if(!sender.hasPermission("money.top")){
            sender.sendMessage(this.plugin.getLang().getMessage("noPermission"));
            return;
        }

        this.plugin.getExecutor().execute(() -> {
            List<PlayerData> data;

            if (args.length == 1) {
                data = this.plugin.getDB().getTop(10);
            } else if (args.length == 2) {
                int amount;
                if(!NumberUtils.isNumber(args[1])){
                    sender.sendMessage(this.plugin.getLang().getMessage("invalidNumber", args[1]));
                    return;
                } else if((amount = Integer.parseInt(args[1])) < 1) {
                    sender.sendMessage(this.plugin.getLang().getMessage("invalidNumber", args[1]));
                    return;
                }
                data = this.plugin.getDB().getTop(amount);
            } else {
                this.printHelp(sender);
                return;
            }

            Optional<String> currentPlayerName;
            for(int i = 0; i < data.size(); i++){
                currentPlayerName = plugin.getIdentificationProvider().getName(data.get(i).getUniqueId());

                if(!currentPlayerName.isPresent())
                    currentPlayerName = Optional.of("Unknown");

                sender.sendMessage(
                        "§e§l" + (i + 1) + ". ) §b" + currentPlayerName.get() + " §f- §b" + this.plugin.getLang().formatCurrency(data.get(i).getBalance())
                );
            }
        });
    }

    @Override
    public void printHelp(CommandSender sender) {

    }

}
