package bz.dcr.geld.api;

import java.util.UUID;

public class PluginTransaction {

    private String sender;
    private UUID target;
    private String targetName;
    private double value;

    // Constructor
    public PluginTransaction(String sender, UUID target){
        this.sender = sender;
        this.target = target;
        this.value = 0.0D;
    }


    public String getSender(){
        return this.sender;
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

}
