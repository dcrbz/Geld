package bz.dcr.geld.data;

import bz.dcr.geld.Geld;
import bz.dcr.geld.api.Transaction;
import bz.dcr.geld.logging.GeldLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class MoneyManager {

    private Geld plugin;

    // Constructor
    public MoneyManager(Geld plugin){
        this.plugin = plugin;
    }


    /**
     * Returns an Optional with a players data
     * @param uuid UUID of the target player
     * @return An Optional with the targets data
     */
    public Optional<PlayerData> getPlayerData(UUID uuid){
        return this.plugin.getDB().getPlayerData(uuid);
    }

    /**
     * Set player data in cache or database
     * @param data PlayerData object
     * @return Result of setting the data
     */
    public Result setPlayerData(PlayerData data){
        this.plugin.getDB().setPlayerData(data);
        return Result.SUCCESS;
    }

    /**
     * Set player balance in cache or database
     * @param uuid UUID of the target player
     * @param balance New balance of target player
     * @return Result of setting the data
     */
    public Result setBalance(UUID uuid, double balance){
        final Optional<PlayerData> data = this.plugin.getDB().getPlayerData(uuid);

        if(!data.isPresent())
            return Result.UNKNOWN_PLAYER;

        data.get().setBalance(balance);
        this.plugin.getDB().setPlayerData(data.get());
        return Result.SUCCESS;
    }

    /**
     * Increase player balance in cache or database
     * @param uuid UUID of the target player
     * @param balance Value that gets added to the targets balance
     * @return Result of setting the data
     */
    public Result increaseBalance(UUID uuid, double balance){
        final Optional<PlayerData> data = this.plugin.getDB().getPlayerData(uuid);

        if(!data.isPresent())
            return Result.UNKNOWN_PLAYER;

        data.get().setBalance(data.get().getBalance() + balance);
        this.plugin.getDB().setPlayerData(data.get());
        return Result.SUCCESS;
    }

    /**
     * Decrease player balance in cache or database
     * @param uuid UUID of the target player
     * @param balance Value that gets removed from the targets balance
     * @return Result of setting the data
     */
    public Result decreaseBalance(UUID uuid, double balance){
        final Optional<PlayerData> data = this.plugin.getDB().getPlayerData(uuid);

        if(!data.isPresent())
            return Result.UNKNOWN_PLAYER;

        data.get().setBalance(data.get().getBalance() - balance);
        this.plugin.getDB().setPlayerData(data.get());
        return Result.SUCCESS;
    }

    /**
     * Set the transaction status of a player
     * @param uuid UUID of the target player
     * @param status New status
     * @return The result of setting the player's status
     */
    public Result setTransactionStatus(UUID uuid, boolean status){
        final Optional<PlayerData> data = this.plugin.getDB().getPlayerData(uuid);

        if(!data.isPresent())
            return Result.UNKNOWN_PLAYER;

        data.get().setAcceptTransfer(status);
        this.plugin.getDB().setPlayerData(data.get());
        return Result.SUCCESS;
    }



    /**
     * Do a given transfer
     * @param transaction The Transaction object
     * @return Result of the transaction
     */
    public TransferResult doTransfer(Transaction transaction){
        // Subtract from sender
        final Result decrease = this.decreaseBalance(transaction.getSender(), transaction.getValue() + transaction.getTax());
        if(decrease != Result.SUCCESS)
            return TransferResult.SENDER_ERR;

        // Add to target
        final Result increase = this.increaseBalance(transaction.getTarget(), transaction.getValue());
        if(increase != Result.SUCCESS)
            return TransferResult.TARGET_ERR;

        // Log (critical) transaction
        if(transaction.getValue() >= this.plugin.getConfig().getDouble("Logging.Transfers.Critical-Thresold")){
            this.plugin.getAlertManager().handleCriticalTransaction(transaction);
            this.plugin.getGeldLogger().log(
                    this.plugin.getLang().getMessage("logTransaction", transaction.getSenderName(), transaction.getTargetName(), this.plugin.getLang().formatCurrency(transaction.getValue())),
                    GeldLogger.LogLevel.CRITICAL_TRANSFER
            );
        } else {
            this.plugin.getGeldLogger().log(
                    this.plugin.getLang().getMessage("logTransaction", transaction.getSenderName(), transaction.getTargetName(), this.plugin.getLang().formatCurrency(transaction.getValue())),
                    GeldLogger.LogLevel.TRANSFER
            );
        }

        // Send receive message
        final String receiveMessage = this.plugin.getLang().getPrefixedMessage("transactionReceived", transaction.getSenderName(), this.plugin.getLang().formatCurrency(transaction.getValue()), this.plugin.getConfig().getString("Currency.Name-Plural"));
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            final Player targetPlayer = Bukkit.getPlayer(transaction.getTarget());

            if(targetPlayer != null)
                targetPlayer.sendMessage(receiveMessage);
            else {
                final Collection<? extends Player> players = Bukkit.getOnlinePlayers();

                if(players.size() > 0)
                    plugin.getPluginMessageManager().sendTransactionMessage(players.iterator().next(), transaction);
            }
        });

        // Save to database
        this.plugin.getDB().addTransaction(transaction);

        return TransferResult.SUCCESS;
    }


    public enum Result {
        INEXISTENT_PLAYER,
        UNKNOWN_PLAYER,
        SUCCESS
    }

    public enum TransferResult {
        SUCCESS,
        SENDER_ERR,
        TARGET_ERR
    }
}
