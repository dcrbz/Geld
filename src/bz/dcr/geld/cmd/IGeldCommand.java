package bz.dcr.geld.cmd;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface IGeldCommand {

    void execute(CommandSender sender, String[] args);

    void executePlayer(Player sender, String[] args);

    void printHelp(CommandSender sender);

}
