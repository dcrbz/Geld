package bz.dcr.geld.cmd;

import bz.dcr.geld.Geld;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class CommandManager {

    private Geld plugin;
    private Map<String, IGeldCommand> commands = new HashMap<>();

    // Constructor
    public CommandManager(Geld plugin, String mainCommand, CommandExecutor commandExecutor) {
        this.plugin = plugin;
        this.plugin.getCommand(mainCommand).setExecutor(commandExecutor);
    }


    public void registerCommand(String subCommand, IGeldCommand subCommandExecutor) {
        this.commands.put(subCommand, subCommandExecutor);
    }

    public IGeldCommand getCommand(String command) {
        return this.commands.get(command);
    }

    public void executeCommand(String command, CommandSender sender, String[] args) {
        if (sender instanceof Player)
            this.getCommand(command).executePlayer((Player) sender, args);
        else
            this.getCommand(command).execute(sender, args);
    }

    public void executeCommand(IGeldCommand command, CommandSender sender, String[] args) {
        if (sender instanceof Player)
            command.executePlayer((Player) sender, args);
        else
            command.execute(sender, args);
    }
}
