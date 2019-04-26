package bz.dcr.geld.data;

import bz.dcr.dccore.commons.db.MongoDB;
import bz.dcr.geld.api.Transaction;
import bz.dcr.geld.pin.RedeemablePin;
import bz.dcr.geld.pin.results.PinValidationResult;
import com.mongodb.MongoClientURI;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GeldDatabase extends Database {

    private ClassLoader classLoader;
    private MongoClientURI uri;

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
                .createQuery(PlayerData.class)
                .field("uniqueId").equal(uuid)
                .get();

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
        final Query<Transaction> query = mongoDB.getDatastore()
                .createQuery(Transaction.class);

        query.or(
                query.criteria("sender").equal(uuid),
                query.criteria("target").equal(uuid)
        );

        return query.asList();
    }

    @Override
    public void setAllPlayerData(Collection<PlayerData> data) {
        mongoDB.getDatastore().save(data);
    }

    @Override
    public void setAcceptTransfers(UUID player, boolean status) {
        final Optional<PlayerData> playerData = getPlayerData(player);

        // No PlayerData found
        if (!playerData.isPresent()) {
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
        if (!playerData.isPresent()) {
            return false;
        }

        return playerData.get().doesAcceptTransfers();
    }

    @Override
    public boolean hasPlayer(UUID uuid) {
        return mongoDB.getDatastore().getCount(PlayerData.class) > 0;
    }

    @Override
    public List<PlayerData> getTop(int amount) {
        return mongoDB.getDatastore()
                .createQuery(PlayerData.class)
                .order("-balance")
                .asList(new FindOptions().limit(amount));
    }

    @Override
    public void addRedeemablePin(RedeemablePin pin) {
        mongoDB.getDatastore().save(pin);
    }

    @Override
    public Optional<RedeemablePin> getRedeemablePin(String pinCode) {
        final RedeemablePin pin = mongoDB.getDatastore()
                .createQuery(RedeemablePin.class)
                .field("pinCode").equal(pinCode.replace("-", ""))
                .get();

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
