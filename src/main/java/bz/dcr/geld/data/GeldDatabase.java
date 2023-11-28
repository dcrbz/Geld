package bz.dcr.geld.data;

import bz.dcr.dccore.commons.db.MongoDB;
import bz.dcr.geld.api.Transaction;
import bz.dcr.geld.pin.RedeemablePin;
import bz.dcr.geld.pin.results.PinValidationResult;
import com.mongodb.MongoClientURI;
import dev.morphia.query.FindOptions;
import dev.morphia.query.filters.Filters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static dev.morphia.query.Sort.descending;

public class GeldDatabase extends Database {

    private final ClassLoader classLoader;
    private final MongoClientURI uri;

    private MongoDB mongoDB;


    public GeldDatabase(ClassLoader classLoader, MongoClientURI uri) {
        this.classLoader = classLoader;
        this.uri = uri;
    }


    @Override
    public void init() {
        mongoDB = new MongoDB(uri, classLoader);
    }

    @Override
    public void shutdown() {
        if (mongoDB != null) {
            mongoDB.close();
        }
    }


    @Override
    public void addPlayer(PlayerData data) {
        mongoDB.getDatastore().save(data);
    }

    @Override
    public Optional<PlayerData> getPlayerData(UUID uuid) {
        final PlayerData playerData = mongoDB.getDatastore()
                .find(PlayerData.class)
                .filter(Filters.eq("uniqueId", uuid))
                .first();

        return Optional.ofNullable(playerData);
    }

    @Override
    public void setPlayerData(PlayerData data) {
        mongoDB.getDatastore().save(data);
    }

    @Override
    public void addTransaction(Transaction transaction) {
        mongoDB.getDatastore().save(transaction);
    }

    @Override
    public List<Transaction> getTransactions(UUID uuid) {
        return mongoDB.getDatastore()
                .find(Transaction.class)
                .filter(Filters.or(
                        Filters.eq("sender", uuid),
                        Filters.eq("target", uuid)
                ))
                .stream()
                .toList();
    }

    @Override
    public void setAllPlayerData(List<PlayerData> data) {
        mongoDB.getDatastore().save(data);
    }

    @Override
    public void setAcceptTransfers(UUID player, boolean status) {
        final Optional<PlayerData> playerData = getPlayerData(player);

        // No PlayerData found
        if (playerData.isEmpty()) {
            return;
        }

        // Change status
        playerData.get().setAcceptTransfer(status);

        // Save PlayerData
        setPlayerData(playerData.get());
    }

    @Override
    public boolean getAcceptTransfers(UUID player) {
        final Optional<PlayerData> playerData = getPlayerData(player);

        // No PlayerData found
        return playerData.map(PlayerData::doesAcceptTransfers).orElse(false);

    }

    @Override
    public boolean hasPlayer(UUID uuid) {
        return mongoDB.getDatastore()
                .find(PlayerData.class)
                .filter(Filters.eq("uniqueId", uuid))
                .count() > 0;
    }

    @Override
    public List<PlayerData> getTop(int amount) {
        var iterator = mongoDB.getDatastore()
                .find(PlayerData.class)
                .iterator(new FindOptions()
                        .sort(descending("balance"))
                        .limit(amount));
        try (iterator) {
            return iterator.toList();
        }
    }

    @Override
    public void addRedeemablePin(RedeemablePin pin) {
        mongoDB.getDatastore().save(pin);
    }

    @Override
    public Optional<RedeemablePin> getRedeemablePin(String pinCode) {
        final RedeemablePin pin = mongoDB.getDatastore()
                .find(RedeemablePin.class)
                .filter(Filters.eq("pinCode", pinCode.replace("-", "")))
                .first();
        return Optional.ofNullable(pin);
    }

    @Override
    public PinValidationResult validatePin(String pinCode) {
        final Optional<RedeemablePin> redeemablePin = this.getRedeemablePin(pinCode);

        if (redeemablePin.isPresent()) {
            if (redeemablePin.get().getValue() != redeemablePin.get().getInitialValue())
                return PinValidationResult.EXISTING_REDEEMED;
            else
                return PinValidationResult.EXISTING_NOT_REDEEMED;
        } else {
            return PinValidationResult.NOT_EXISTING;
        }
    }

}
