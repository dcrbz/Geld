package bz.dcr.geld.api;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

public class Transaction {

    private UUID sender;
    private String senderName;
    private UUID target;
    private String targetName;
    private double value;
    private double tax;
    private Date time;

    // Constructor
    public Transaction(){}

    public Transaction(UUID sender, UUID target){
        this.sender = sender;
        this.target = target;
        this.value = 0.0D;
        this.tax = 0.0D;
        this.time = new Date(System.currentTimeMillis());
    }


    public UUID getSender(){
        return this.sender;
    }

    public String getSenderName(){
        return this.senderName;
    }

    public void setSenderName(String senderName){
        this.senderName = senderName;
    }

    public UUID getTarget(){
        return this.target;
    }

    public String getTargetName(){
        return this.targetName;
    }

    public void setTargetName(String targetName){
        this.targetName = targetName;
    }

    public double getValue(){
        return this.value;
    }

    public void setValue(double amount){
        this.value = amount;
    }

    public double getTax(){
        return tax;
    }

    public void setTax(double tax){
        this.tax = tax;
    }

    public Date getTime(){
        return this.time;
    }

    public void setTime(Timestamp timestamp){
        this.time = timestamp;
    }

}
