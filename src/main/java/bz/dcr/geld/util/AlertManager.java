package bz.dcr.geld.util;

import bz.dcr.geld.Geld;
import bz.dcr.geld.api.Transaction;
import org.bukkit.Bukkit;

public class AlertManager {

    private Geld plugin;

    // Constructor
    public AlertManager(Geld plugin){
        this.plugin = plugin;
    }


    public void handleCriticalTransaction(Transaction transaction){
        Bukkit.getOnlinePlayers()
                .stream()
                .filter(p -> p.hasPermission("money.alert"))
                .parallel()
                .forEach(p -> p.sendMessage( this.plugin.getLang().getPrefixedMessage("criticalTransaction", transaction.getSenderName(), this.plugin.getLang().formatCurrency(transaction.getValue()), transaction.getTargetName()) ));
    }

}
