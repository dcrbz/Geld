package bz.dcr.geld.cmd;

import bz.dcr.geld.Geld;
import bz.dcr.geld.data.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class MoneyCommand implements CommandExecutor {

    private Geld plugin;

    // Constructor
    public MoneyCommand(Geld plugin){
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(args.length == 0){
            if(!(sender instanceof Player)){
                sender.sendMessage(this.plugin.getLang().getMessage("playerCommand"));
                return true;
            }

            if(!sender.hasPermission("money.balance")){
                sender.sendMessage(this.plugin.getLang().getMessage("noPermission"));
                return true;
            }

            final Player player = (Player) sender;

            this.plugin.getExecutor().execute(() -> {
                final Optional<PlayerData> playerData = this.plugin.getEconomy().getPlayerData(player.getUniqueId());

                // Player has not played before
                if(!playerData.isPresent()){
                    sender.sendMessage(this.plugin.getLang().getPrefixedMessage("unknownNamedPlayer", args[1]));
                    return;
                }

                // Send balance
                sender.sendMessage(this.plugin.getLang().getPrefixedMessage("currentBalance", this.plugin.getLang().formatCurrency(playerData.get().getBalance()), this.plugin.getConfig().getString("Currency.Name-Plural")));
            });
        } else {
            IGeldCommand command = this.plugin.getMoneyCommandManager().getCommand(args[0].toLowerCase());

            if(command != null){
                this.plugin.getMoneyCommandManager().executeCommand(command, sender, args);
                return true;
            } else {
                return false;
            }
        }

        return true;
    }

}
