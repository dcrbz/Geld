package bz.dcr.geld.cmd.money;

import bz.dcr.geld.Geld;
import bz.dcr.geld.cmd.IGeldCommand;
import bz.dcr.geld.data.PlayerData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class BalanceCommand implements IGeldCommand {

    private Geld plugin;

    // Constructor
    public BalanceCommand(Geld plugin){
        this.plugin = plugin;
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("money.balance.others")){
            sender.sendMessage(this.plugin.getLang().getMessage("noPermission"));
            return;
        }

        this.plugin.getExecutor().execute(() -> {
            if(args.length != 2){
                this.printHelp(sender);
                return;
            }

            final Optional<UUID> target = plugin.getIdentificationProvider().getUUID(args[1]);

            // Player does not exist
            if(!target.isPresent()){
                sender.sendMessage(this.plugin.getLang().getPrefixedMessage("inexistentNamedPlayer", args[1]));
                return;
            }

            final Optional<PlayerData> playerData = this.plugin.getEconomy().getPlayerData(target.get());

            // Player has not played before
            if(!playerData.isPresent()){
                sender.sendMessage(this.plugin.getLang().getPrefixedMessage("unknownNamedPlayer", args[1]));
                return;
            }

            // Send balance
            sender.sendMessage(this.plugin.getLang().getPrefixedMessage("currentBalanceOthers", args[1], this.plugin.getLang().formatCurrency(playerData.get().getBalance()), this.plugin.getConfig().getString("Currency.Name-Plural")));
        });
    }

    @Override
    public void executePlayer(Player sender, String[] args) {
        this.execute(sender, args);
    }

    @Override
    public void printHelp(CommandSender sender) {
        sender.sendMessage(
            "§e/money balance §o[Spieler]"
        );
    }
}
