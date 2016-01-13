package bz.dcr.geld.messaging;

import bz.dcr.geld.Geld;
import bz.dcr.geld.api.Transaction;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class PluginMessageManager implements PluginMessageListener {

    private static final String NEW_TRANSACTION_CHANNEL = "newTransaction";
    private Geld plugin;
    private boolean active;

    // Constructor
    public PluginMessageManager(Geld plugin, boolean active) {
        this.plugin = plugin;
        this.active = active;
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", this);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord"))
            return;

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        if (subchannel.equals(NEW_TRANSACTION_CHANNEL)) {

            byte[] msgBytesByteArray = new byte[in.readShort()];
            in.readFully(msgBytesByteArray);

            ByteArrayDataInput inMsgBytes = ByteStreams.newDataInput(msgBytesByteArray);

            final String sender = inMsgBytes.readUTF();
            final Player target = Bukkit.getPlayer(UUID.fromString(inMsgBytes.readUTF()));
            final double value = inMsgBytes.readDouble();

            if (target == null)
                return;

            final String receiveMessage = this.plugin.getLang().getPrefixedMessage("transactionReceived", sender, this.plugin.getLang().formatCurrency(value), this.plugin.getConfig().getString("Currency.Name-Plural"));
            target.sendMessage(receiveMessage);
        }
    }

    public void sendTransactionMessage(Player sender, Transaction transaction) {
        if (!active)
            return;

        new BukkitRunnable() {
            @Override
            public void run() {

                ByteArrayDataOutput out = ByteStreams.newDataOutput();

                out.writeUTF("Forward");
                out.writeUTF("ALL");
                out.writeUTF(NEW_TRANSACTION_CHANNEL);

                ByteArrayDataOutput outMsgBytes = ByteStreams.newDataOutput();
                outMsgBytes.writeUTF(transaction.getSenderName());
                outMsgBytes.writeUTF(transaction.getTarget().toString());
                outMsgBytes.writeDouble(transaction.getValue());

                byte[] msgBytesByteArray = outMsgBytes.toByteArray();
                out.writeShort(msgBytesByteArray.length);
                out.write(msgBytesByteArray);

                sender.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
            }
        }.runTaskAsynchronously(plugin);
    }

}
