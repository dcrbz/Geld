package bz.dcr.geld.cmd.money;

import bz.dcr.geld.Geld;
import bz.dcr.geld.api.Transaction;
import bz.dcr.geld.cmd.IGeldCommand;
import bz.dcr.geld.data.MoneyManager;
import bz.dcr.geld.data.PlayerData;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class PayCommand implements IGeldCommand {

    private Geld plugin;

    // Constructor
    public PayCommand(Geld plugin) {
        this.plugin = plugin;
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(this.plugin.getLang().getMessage("playerCommand"));
    }

    @Override
    public void executePlayer(Player sender, String[] args) {
        // Player has no permission
        if (!sender.hasPermission("money.pay")) {
            sender.sendMessage(this.plugin.getLang().getMessage("noPermission"));
            return;
        }

        if (args.length != 3) {
            this.printHelp(sender);
            return;
        }

        this.plugin.getExecutor().execute(() -> {
            final Optional<UUID> target = plugin.getIdentificationProvider().getUUID(args[1]);
            double value = 0.0D;

            // Value is not valid
            if (!NumberUtils.isNumber(args[2])) {
                sender.sendMessage(this.plugin.getLang().getMessage("invalidNumber", args[2]));
                return;
            } else if ((value = Double.parseDouble(args[2])) <= 1D) {
                sender.sendMessage(this.plugin.getLang().getMessage("invalidNumber", args[2]));
                return;
            }

            // Target does not exist
            if (!target.isPresent()) {
                sender.sendMessage(this.plugin.getLang().getMessage("inexistentNamedPlayer", args[1]));
                return;
            }

            final Optional<PlayerData> targetData = this.plugin.getEconomy().getPlayerData(target.get());

            // Target is not known
            if (!targetData.isPresent()) {
                sender.sendMessage(this.plugin.getLang().getMessage("unknownNamedPlayer", args[1]));
                return;
            }

            // Target does not accept transfers
            if (!targetData.get().doesAcceptTransfers()) {
                sender.sendMessage(this.plugin.getLang().getPrefixedMessage("targetTransferDisabled", args[1]));
                return;
            }

            double tax = plugin.getConfig().getBoolean("Transaction-Tax.Enabled") ? value / 100 * plugin.getConfig().getInt("Transaction-Tax.Percent") : 0.0D;

            // Sender has permission to do transactions without taxes
            if (sender.hasPermission("money.notax"))
                tax = 0.0D;

            final double balance = this.plugin.getEconomy().getPlayerData(sender.getUniqueId()).get().getBalance();

            // Sender has not enough money
            if (balance < value) {
                sender.sendMessage(this.plugin.getLang().getPrefixedMessage("noMoneyTransfer"));
                return;
            }

            // Sender has not enough money to pay tax
            if (balance < value + tax) {
                sender.sendMessage(plugin.getLang().getPrefixedMessage("noMoneyTransferTax", plugin.getLang().formatCurrency(tax), plugin.getConfig().getInt("Transaction-Tax.Percent")));
                return;
            }

            // Finally do transfer
            final Transaction transaction = new Transaction(sender.getUniqueId(), target.get());
            transaction.setSenderName(sender.getName());
            transaction.setTargetName(args[1]);
            transaction.setValue(value);
            transaction.setTax(tax);
            final MoneyManager.TransferResult transferResult = this.plugin.getEconomy().doTransfer(transaction);

            // Send response message
            switch (transferResult) {
                case SUCCESS:
                    sender.sendMessage(this.plugin.getLang().getPrefixedMessage("transactionSent", args[1], this.plugin.getLang().formatCurrency(value), this.plugin.getConfig().getString("Currency.Name-Plural")));
                    break;
                case SENDER_ERR:
                    sender.sendMessage(this.plugin.getLang().getPrefixedMessage("transactionError"));
                    break;
                case TARGET_ERR:
                    sender.sendMessage(this.plugin.getLang().getPrefixedMessage("transactionError"));
                    break;
            }
        });
    }

    @Override
    public void printHelp(CommandSender sender) {
        // money pay <Spieler> <Betrag>
        sender.sendMessage("§e/money pay §o<Spieler> <Betrag>");
    }

}
