package bz.dcr.geld.cmd.money;

import bz.dcr.geld.Geld;
import bz.dcr.geld.cmd.IGeldCommand;
import bz.dcr.geld.data.MoneyManager;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class GiveCommand implements IGeldCommand {

    private Geld plugin;

    // Constructor
    public GiveCommand(Geld plugin) {
        this.plugin = plugin;
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 3) {
            // Player has no permission
            if (!sender.hasPermission("money.give.others")) {
                sender.sendMessage(this.plugin.getLang().getMessage("noPermission"));
                return;
            }

            this.plugin.getExecutor().execute(() -> {
                final Optional<UUID> target = plugin.getIdentificationProvider().getUUID(args[1]);

                // Target does not exist
                if (!target.isPresent()) {
                    sender.sendMessage(this.plugin.getLang().getMessage("inexistentNamedPlayer", args[1]));
                    return;
                }

                // Invalid number
                if (!NumberUtils.isNumber(args[2])) {
                    sender.sendMessage(this.plugin.getLang().getMessage("invalidNumber", args[2]));
                    return;
                }

                final double value = Double.parseDouble(args[2]);
                final MoneyManager.Result result = this.plugin.getEconomy().increaseBalance(target.get(), value);

                switch (result) {
                    case SUCCESS:
                        sender.sendMessage(this.plugin.getLang().getPrefixedMessage("balanceGiveOthers", args[1], this.plugin.getLang().formatCurrency(value), this.plugin.getConfig().getString("Currency.Name-Plural")));
                        break;
                    case UNKNOWN_PLAYER:
                        sender.sendMessage(this.plugin.getLang().getMessage("unknownNamedPlayer", args[1]));
                        break;
                    case INEXISTENT_PLAYER:
                        sender.sendMessage(this.plugin.getLang().getMessage("inexistentNamedPlayer", args[1]));
                        break;
                }
            });

            return;
        }

        this.printHelp(sender);
    }

    @Override
    public void executePlayer(Player sender, String[] args) {
        // Player has no permission
        if (!sender.hasPermission("money.give"))
            return;

        if (args.length == 3) {
            this.plugin.getExecutor().execute(() -> {
                final Optional<UUID> target = plugin.getIdentificationProvider().getUUID(args[1]);

                // Target does not exist
                if (!target.isPresent()) {
                    sender.sendMessage(this.plugin.getLang().getMessage("inexistentNamedPlayer", args[1]));
                    return;
                }

                // Invalid number
                if (!NumberUtils.isNumber(args[2])) {
                    sender.sendMessage(this.plugin.getLang().getMessage("invalidNumber", args[2]));
                    return;
                }

                final double value = Double.parseDouble(args[2]);
                final MoneyManager.Result result = this.plugin.getEconomy().increaseBalance(target.get(), value);

                switch (result) {
                    case SUCCESS:
                        sender.sendMessage(this.plugin.getLang().getPrefixedMessage("balanceGiveOthers", args[1], this.plugin.getLang().formatCurrency(value), this.plugin.getConfig().getString("Currency.Name-Plural")));
                        break;
                    case UNKNOWN_PLAYER:
                        sender.sendMessage(this.plugin.getLang().getMessage("unknownNamedPlayer", args[1]));
                        break;
                    case INEXISTENT_PLAYER:
                        sender.sendMessage(this.plugin.getLang().getMessage("inexistentNamedPlayer", args[1]));
                        break;
                }
            });
        } else if (args.length == 2) {
            this.plugin.getExecutor().execute(() -> {
                // Invalid number
                if (!NumberUtils.isNumber(args[1])) {
                    sender.sendMessage(this.plugin.getLang().getMessage("invalidNumber", args[1]));
                    return;
                }

                final double value = Double.parseDouble(args[1]);

                this.plugin.getEconomy().increaseBalance(sender.getUniqueId(), value);
                sender.sendMessage(this.plugin.getLang().getPrefixedMessage("balanceGive", this.plugin.getLang().formatCurrency(value), this.plugin.getConfig().get("Currency.Name-Plural")));
            });
        } else
            this.execute(sender, args);
    }

    @Override
    public void printHelp(CommandSender sender) {
        sender.sendMessage(
                "§e/money give §o<Spieler> <Wert>\n" + "§e/money give §o<Wert>"
        );
    }

}
