package bz.dcr.geld.data;

import bz.dcr.geld.api.Transaction;
import bz.dcr.geld.pin.results.PinValidationResult;
import bz.dcr.geld.pin.RedeemablePin;
import com.mongodb.*;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;

import java.io.IOException;
import java.util.*;

public class MongoDB extends Database {

    private MongoClient client;
    private DB db;
    private ServerAddress serverAddress;
    private MongoClientOptions options;
    private MongoCredential credential;
    private Jongo jongo;
    private boolean auth;

    // Collections
    private MongoCollection playerCollection;
    private MongoCollection transactionCollection;
    private MongoCollection pinCollection;

    // Constructor
    public MongoDB(ServerAddress serverAddress, MongoClientOptions options, MongoCredential credential, boolean auth){
        this.serverAddress = serverAddress;
        this.options = options;
        this.credential = credential;
        this.auth = auth;
    }


    @Override
    public void init() {
        if(this.auth)
            this.client = new MongoClient(this.serverAddress, Arrays.asList(this.credential), this.options);
        else
            this.client = new MongoClient(this.serverAddress, this.options);

        this.db = this.client.getDB(this.credential.getSource());
        this.jongo = new Jongo(this.db);

        // Collections
        this.playerCollection = this.jongo.getCollection("players");
        this.transactionCollection = this.jongo.getCollection("transactions");
        this.pinCollection = this.jongo.getCollection("pins");
    }

    @Override
    public void shutdown() {
        if(client != null)
            client.close();
    }


    @Override
    public void addPlayer(PlayerData data) {
        if(playerCollection.count("{uniqueId: #}", data.getUniqueId()) == 0)
            this.playerCollection.insert(data);
    }

    @Override
    public Optional<PlayerData> getPlayerData(UUID uuid) {
        return Optional.ofNullable(this.playerCollection.findOne("{uniqueId: #}", uuid).as(PlayerData.class));
    }

    @Override
    public void setPlayerData(PlayerData data) {
        this.playerCollection.update("{uniqueId: #}", data.getUniqueId()).upsert().with(data);
    }


    @Override
    public void addTransaction(Transaction transaction) {
        this.transactionCollection.insert(transaction);
    }

    @Override
    public List<Transaction> getTransactions(UUID uuid) {
        List<Transaction> transactions = new ArrayList<>();

        try ( MongoCursor<Transaction> cursor = this.transactionCollection.find("{$or: [{sender: #}, {target: #}]}", uuid, uuid).sort("{time: -1}").as(Transaction.class) ){
            while (cursor.hasNext()){
                transactions.add(cursor.next());
            }
            return transactions;
        } catch (IOException e) {
            e.printStackTrace();
            return transactions;
        }
    }


    @Override
    public void setAllPlayerData(Collection<PlayerData> data) {
        this.playerCollection.insert(data.toArray(new PlayerData[data.size()]));
    }

    @Override
    public void setAcceptTransfers(UUID player, boolean status) {
        this.playerCollection.update("{uniqueId: #", player).with("{$set {acceptTransfer: #}}", status);
    }

    @Override
    public boolean getAcceptTransfers(UUID player) {
        final Optional<PlayerData> data = this.getPlayerData(player);

        return data.isPresent() && data.get().doesAcceptTransfers();
    }

    @Override
    public boolean hasPlayer(UUID uuid) {
        return this.playerCollection.count("{uniqueId: #", uuid) > 0;
    }

    @Override
    public List<PlayerData> getTop(int amount) {
        List<PlayerData> data = new ArrayList<>();
        try ( MongoCursor<PlayerData> cursor = this.playerCollection.find().limit(amount).sort("{balance: -1}").as(PlayerData.class) ){
            while (cursor.hasNext()){
                data.add(cursor.next());
            }
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return data;
        }
    }


    /* PINs */

    @Override
    public void addRedeemablePin(RedeemablePin pin) {
        this.pinCollection.update("{pinCode: #}", pin.getPinCode()).upsert().with(pin);
    }

    @Override
    public Optional<RedeemablePin> getRedeemablePin(String pinCode) {
        return Optional.ofNullable( this.pinCollection.findOne("{pinCode: #}", pinCode.replace("-", "")).as(RedeemablePin.class) );
    }



    @Override
    public PinValidationResult validatePin(String pinCode) {
        final Optional<RedeemablePin> redeemablePin = this.getRedeemablePin(pinCode);

        if(redeemablePin.isPresent()){
            if(redeemablePin.get().getValue() != redeemablePin.get().getInitialValue())
                return PinValidationResult.EXISTING_REDEEMED;
            else
                return PinValidationResult.EXISTING_NOT_REDEEMED;
        } else {
            return PinValidationResult.NOT_EXISTING;
        }
    }

}
