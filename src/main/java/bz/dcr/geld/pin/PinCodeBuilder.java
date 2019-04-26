package bz.dcr.geld.pin;

import java.util.Random;

public class PinCodeBuilder {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private int groups;
    private int digits;
    private String pin;

    // Constructors
    public PinCodeBuilder() {
        this.groups = 6;
        this.digits = 4;
        this.pin = generate();
    }


    public PinCodeBuilder groups(int groups) {
        this.groups = groups;
        return this;
    }

    private PinCodeBuilder digits(int digits) {
        this.digits = digits;
        return this;
    }

    public final String build() {
        this.pin = generate();
        return this.pin;
    }


    // Generate pin
    private String generate() {
        Random random;
        String pinCode = "";

        for (int i = 0; i < this.groups * this.digits; i++) {
            random = new Random();
            pinCode += CHARS.charAt(random.nextInt(CHARS.length()));
        }

        return pinCode;
    }


    // Get builder
    public static PinCodeBuilder builder() {
        return new PinCodeBuilder();
    }

}
