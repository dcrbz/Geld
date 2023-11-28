package bz.dcr.geld.data;

import bz.dcr.geld.api.Transaction;
import bz.dcr.geld.pin.RedeemablePin;
import bz.dcr.geld.pin.results.PinValidationResult;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class Database {

    public abstract void init();

    public abstract void shutdown();


    public abstract void addPlayer(PlayerData data);

    public abstract Optional<PlayerData> getPlayerData(UUID uuid);

    public abstract void setPlayerData(PlayerData data);

    public abstract void addTransaction(Transaction transaction);

    public abstract List<Transaction> getTransactions(UUID uuid);

    public abstract void setAllPlayerData(List<PlayerData> data);

    public abstract void setAcceptTransfers(UUID player, boolean status);

    public abstract boolean getAcceptTransfers(UUID player);

    public abstract boolean hasPlayer(UUID uuid);

    public abstract List<PlayerData> getTop(int amount);


    public abstract void addRedeemablePin(RedeemablePin pin);

    public abstract Optional<RedeemablePin> getRedeemablePin(String pinCode);

    public abstract PinValidationResult validatePin(String pinCode);

}
