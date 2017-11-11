package bz.dcr.geld.cmd.money;

import bz.dcr.geld.Geld;
import bz.dcr.geld.api.Transaction;
import bz.dcr.geld.cmd.IGeldCommand;
import bz.dcr.geld.util.Pagifier;
import bz.dcr.geld.util.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PTransactionsCommand implements IGeldCommand {

    private Geld plugin;

    // Constructor
    public PTransactionsCommand(Geld plugin){
        this.plugin = plugin;
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(this.plugin.getLang().getMessage("playerCommand"));
    }

    @Override
    public void executePlayer(Player sender, String[] args) {
        // Player has no permission
        if(!sender.hasPermission("money.ptransactions")) {
            sender.sendMessage(this.plugin.getLang().getMessage("noPermission"));
            return;
        }

        this.plugin.getExecutor().execute(() -> {
            if(args.length == 2 || args.length == 3){
                int pageNum = 1;

                // Invalid page
                if(args.length == 3){
                    if(!Utils.isInteger(args[2]) || (pageNum = Integer.parseInt(args[2])) < 1){
                        sender.sendMessage(this.plugin.getLang().getMessage("invalidNumber", args[2]));
                        return;
                    }
                }

                final Optional<UUID> target = plugin.getIdentificationProvider().getUUID(args[1]);

                // Target is not existing
                if(!target.isPresent()){
                    sender.sendMessage( this.plugin.getLang().getPrefixedMessage("unknownNamedPlayer", args[1]) );
                    return;
                }

                // Get transactions
                final Pagifier<Transaction> pages = new Pagifier<>(10);
                final List<Transaction> transactions = this.plugin.getDB().getTransactions(target.get());
                transactions.forEach(t -> pages.addItem(t));

                // Page does not exist
                if(pages.getPages().size() < pageNum){
                    sender.sendMessage(this.plugin.getLang().getMessage("invalidPage", args[2]));
                    return;
                }

                final Optional<List<Transaction>> currentPage = pages.getPage(pageNum - 1);

                // Page does not exist
                if(!currentPage.isPresent()){
                    sender.sendMessage(this.plugin.getLang().getMessage("invalidPage", args[2]));
                    return;
                }

                // Show transactions
                sender.sendMessage(this.plugin.getLang().getMessage("transactionsHeader"));
                for(Transaction t : currentPage.get()){
                    sender.sendMessage( "§e§l" + this.plugin.getLang().formatTimestamp(t.getTime()) + ": §f" + t.getSenderName() + " §7> " + this.plugin.getLang().formatCurrency(t.getValue()) + " §7> §f" + t.getTargetName());
                }
                sender.sendMessage(this.plugin.getLang().getMessage("transactionsFooter", pageNum, pages.getPages().size()));
            } else {
                this.printHelp(sender);
            }
        });
    }

    @Override
    public void printHelp(CommandSender sender) {
        sender.sendMessage(
                "§e/money ptransactions §o<Spieler> [Seite]"
        );
    }

}
