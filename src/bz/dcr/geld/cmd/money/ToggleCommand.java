package bz.dcr.geld.cmd.money;

import bz.dcr.geld.Geld;
import bz.dcr.geld.cmd.IGeldCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleCommand implements IGeldCommand {

    private Geld plugin;

    // Constructor
    public ToggleCommand(Geld plugin){
        this.plugin = plugin;
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(this.plugin.getLang().getMessage("playerCommand"));
    }

    @Override
    public void executePlayer(Player sender, String[] args) {
        if(!sender.hasPermission("money.toggle")){
            sender.sendMessage(this.plugin.getLang().getMessage("noPermission"));
            return;
        }

        // Wrong usage
        if(args.length != 2){
            this.printHelp(sender);
            return;
        }

        if(args[1].equalsIgnoreCase("an")){
            this.plugin.getExecutor().execute(() -> {
                this.plugin.getDB().setAcceptTransfers(sender.getUniqueId(), true);
                sender.sendMessage(this.plugin.getLang().getPrefixedMessage("acceptTransfersOn"));
            });
        } else if(args[1].equalsIgnoreCase("aus")){
            this.plugin.getExecutor().execute(() -> {
                this.plugin.getDB().setAcceptTransfers(sender.getUniqueId(), false);
                sender.sendMessage(this.plugin.getLang().getPrefixedMessage("acceptTransfersOff"));
            });
        }
    }

    @Override
    public void printHelp(CommandSender sender) {
        // money an|aus
        sender.sendMessage("§e/money toggle an§7|§eaus");
    }

}
