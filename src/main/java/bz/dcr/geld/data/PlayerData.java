package bz.dcr.geld.data;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexed;
import org.bson.types.ObjectId;

import java.util.UUID;

@Entity
public class PlayerData {

    @Id
    private ObjectId id;

    @Indexed(options = @IndexOptions(unique = true))
    private UUID uniqueId;
    private String uniqueIdString;
    private double balance;
    private boolean acceptTransfer;
    private long joinTime;

    // Constructor
    public PlayerData() {
    }

    public PlayerData(UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.uniqueIdString = uniqueId.toString().replace("-", "");
        this.balance = 0.0D;
        this.acceptTransfer = true;
        this.joinTime = System.currentTimeMillis();
    }


    public ObjectId getId() {
        return id;
    }

    public double getBalance() {
        return this.balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }


    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.uniqueIdString = uniqueId.toString().replace("-", "");
    }


    public boolean doesAcceptTransfers() {
        return this.acceptTransfer;
    }

    public void setAcceptTransfer(boolean acceptTransfer) {
        this.acceptTransfer = acceptTransfer;
    }


    public long getJoinTime() {
        return this.joinTime;
    }

    public void setJoinTime(long joinTime) {
        this.joinTime = joinTime;
    }

}
