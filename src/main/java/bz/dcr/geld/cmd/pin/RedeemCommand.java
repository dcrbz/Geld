package bz.dcr.geld.cmd.pin;

import bz.dcr.geld.Geld;
import bz.dcr.geld.cmd.IGeldCommand;
import bz.dcr.geld.pin.RedeemablePin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class RedeemCommand implements IGeldCommand {

    private Geld plugin;

    // Constructor
    public RedeemCommand(Geld plugin) {
        this.plugin = plugin;
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(this.plugin.getLang().getMessage("playerCommand"));
    }

    @Override
    public void executePlayer(Player sender, String[] args) {
        // No permission
        if (!sender.hasPermission("pin.redeem")) {
            sender.sendMessage(this.plugin.getLang().getMessage("noPermission"));
            return;
        }

        this.plugin.getExecutor().execute(() -> {
            if (args.length == 2) {
                final Optional<RedeemablePin> pin = this.plugin.getDB().getRedeemablePin(args[1]);

                // Invalid pin
                if (!pin.isPresent()) {
                    sender.sendMessage(this.plugin.getLang().getPrefixedMessage("pinNotValid"));
                    return;
                }

                // Pin already redeemed
                if (pin.get().getValue() != pin.get().getInitialValue()) {
                    sender.sendMessage(this.plugin.getLang().getPrefixedMessage("pinAlreadyRedeemed"));
                    return;
                }

                // Send message
                this.plugin.getEconomy().increaseBalance(sender.getUniqueId(), pin.get().getValue());
                sender.sendMessage(this.plugin.getLang().getPrefixedMessage("pinRedeemed", this.plugin.getLang().formatCurrency(pin.get().getValue()), this.plugin.getConfig().get("Currency.Name-Plural")));

                // Update PIN
                pin.get().setValue(0.0D);
                this.plugin.getDB().addRedeemablePin(pin.get());
            } else {
                this.printHelp(sender);
            }
        });
    }

    @Override
    public void printHelp(CommandSender sender) {
        sender.sendMessage(
                "§e/pin redeem §o<Wert>"
        );
    }

}
