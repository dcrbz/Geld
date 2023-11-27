package bz.dcr.geld.cmd.pin;

import bz.dcr.geld.Geld;
import bz.dcr.geld.cmd.IGeldCommand;
import bz.dcr.geld.pin.RedeemablePin;
import bz.dcr.geld.pin.results.PinValidationResult;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GenerateCommand implements IGeldCommand {

    private Geld plugin;

    // Constructor
    public GenerateCommand(Geld plugin) {
        this.plugin = plugin;
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 2) {
            double value;

            if (!NumberUtils.isNumber(args[1])) {
                sender.sendMessage(this.plugin.getLang().getMessage("invalidNumber", args[1]));
                return;
            } else if ((value = Double.parseDouble(args[1])) < 0 || value < this.plugin.getConfig().getDouble("Pins.Min-Value")) {
                sender.sendMessage(this.plugin.getLang().getMessage("invalidNumber", args[1]));
                return;
            }

            RedeemablePin pin = new RedeemablePin(value, value, this.plugin.getConfig().getLong("Pins.Expire-Days"));
            while (this.plugin.getDB().validatePin(pin.getPinCode()) != PinValidationResult.NOT_EXISTING) {
                pin = new RedeemablePin(value, value, this.plugin.getConfig().getLong("Pins.Expire-Days"));
            }

            this.plugin.getDB().addRedeemablePin(pin);

            final String separatedPin = pin.toSeparatedString();
            sender.sendMessage("Generated PIN: " + separatedPin);
        }
    }

    @Override
    public void executePlayer(Player sender, String[] args) {
        // No permission
        if (!sender.hasPermission("pin.generate")) {
            sender.sendMessage(this.plugin.getLang().getMessage("noPermission"));
            return;
        }

        this.plugin.getExecutor().execute(() -> {
            if (args.length == 2) {
                double value;

                if (!NumberUtils.isNumber(args[1])) {
                    sender.sendMessage(this.plugin.getLang().getMessage("invalidNumber", args[1]));
                    return;
                } else if ((value = Double.parseDouble(args[1])) < 0 || value < this.plugin.getConfig().getDouble("Pins.Min-Value")) {
                    sender.sendMessage(this.plugin.getLang().getMessage("invalidNumber", args[1]));
                    return;
                }

                RedeemablePin pin = new RedeemablePin(value, value, this.plugin.getConfig().getLong("Pins.Expire-Days"));
                while (this.plugin.getDB().validatePin(pin.getPinCode()) != PinValidationResult.NOT_EXISTING) {
                    pin = new RedeemablePin(value, value, this.plugin.getConfig().getLong("Pins.Expire-Days"));
                }

                this.plugin.getDB().addRedeemablePin(pin);

                final String separatedPin = pin.toSeparatedString();
                final TextComponent text = new TextComponent(this.plugin.getLang().getMessage("pinGenerated", separatedPin, value, this.plugin.getConfig().get("Currency.Name-Plural")));
                text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.plugin.getLang().getMessage("pinGeneratedHover")).create()));
                text.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, separatedPin));
                sender.spigot().sendMessage(text);
            } else {
                this.printHelp(sender);
            }
        });
    }

    @Override
    public void printHelp(CommandSender sender) {
        sender.sendMessage(
                "§e/pin generate §o<Wert>"
        );
    }

}
