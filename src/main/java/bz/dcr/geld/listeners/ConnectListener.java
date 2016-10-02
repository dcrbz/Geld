package bz.dcr.geld.listeners;

import bz.dcr.geld.Geld;
import bz.dcr.geld.data.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class ConnectListener implements Listener {

    private Geld plugin;

    // Constructor
    public ConnectListener(Geld plugin){
        this.plugin = plugin;
    }


    @EventHandler
    public void onJoin(AsyncPlayerPreLoginEvent e){
        if(e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED)
            return;

        // Add player to database
        this.plugin.getDB().addPlayer(
                new PlayerData(
                        e.getUniqueId()
                )
        );
    }

}
