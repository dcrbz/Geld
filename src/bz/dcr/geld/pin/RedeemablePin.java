package bz.dcr.geld.pin;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class RedeemablePin {

    public static final int DIGITS_PER_GROUP = 4;

    private String pinCode;
    private double initialValue;
    private double value;
    private Date expiresAt;

    // Constructors
    public RedeemablePin(){}

    public RedeemablePin(double initialValue, double value, long expireDays){
        this.pinCode = PinCodeBuilder.builder().groups(6).build();
        this.initialValue = initialValue;
        this.value = value;
        this.expiresAt = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(expireDays));
    }


    public String getPinCode(){
        return this.pinCode;
    }

    public double getValue(){
        return this.value;
    }

    public double getInitialValue(){
        return this.initialValue;
    }

    public String toSeparatedString() {
        final String str = this.pinCode.replaceAll("(.{" + String.valueOf(DIGITS_PER_GROUP) + "})", "$1-");
        return (str.endsWith("-")) ? str.subSequence(0, str.length() - 1).toString() : str;
    }


    /* Setters */

    public void setValue(double value){
        this.value = value;
    }

}
