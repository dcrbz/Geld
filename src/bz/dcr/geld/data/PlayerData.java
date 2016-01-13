package bz.dcr.geld.data;

import java.util.UUID;

public class PlayerData {

    private UUID uniqueId;
    private String uniqueIdString;
    private double balance;
    private boolean acceptTransfer;
    private long joinTime;

    // Constructor
    public PlayerData(){}

    public PlayerData(UUID uniqueId){
        this.uniqueId = uniqueId;
        this.uniqueIdString = uniqueId.toString().replace("-", "");
        this.balance = 0.0D;
        this.acceptTransfer = true;
        this.joinTime = System.currentTimeMillis();
    }


    public double getBalance(){
        return this.balance;
    }

    public void setBalance(double balance){
        this.balance = balance;
    }


    public UUID getUniqueId(){
        return this.uniqueId;
    }

    public void setUniqueId(UUID uniqueId){
        this.uniqueId = uniqueId;
        this.uniqueIdString = uniqueId.toString().replace("-", "");
    }


    public boolean doesAcceptTransfers(){
        return this.acceptTransfer;
    }

    public void setAcceptTransfer(boolean acceptTransfer){
        this.acceptTransfer = acceptTransfer;
    }


    public long getJoinTime(){
        return this.joinTime;
    }

    public void setJoinTime(long joinTime){
        this.joinTime = joinTime;
    }

}
