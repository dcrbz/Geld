package bz.dcr.geld.cmd;

import bz.dcr.geld.Geld;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PinCommand implements CommandExecutor {

    private Geld plugin;

    // Constructor
    public PinCommand(Geld plugin){
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length < 1)
            return false;

        IGeldCommand command = this.plugin.getPinCommandManager().getCommand(args[0].toLowerCase());

        if(command != null){
            this.plugin.getPinCommandManager().executeCommand(command, sender, args);
            return true;
        } else {
            return false;
        }
    }
}
