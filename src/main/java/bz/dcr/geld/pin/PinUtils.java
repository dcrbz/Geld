package bz.dcr.geld.pin;

public class PinUtils {

    public static short[] pinFromString(String s, int digitsPerGroup) {
        // Remove separators
        if (s.contains("-"))
            s = s.replace("-", "");

        final char[] chars = s.toCharArray();
        short[] pin = new short[chars.length / digitsPerGroup];

        String group = "";
        int digitNum = 1;
        int groupNum = 0;
        for (int i = 1; i < chars.length; i++) {
            group += String.valueOf(chars[i]);
            digitNum++;

            if (digitNum == digitsPerGroup) {
                pin[groupNum] = Short.valueOf(group);
                digitNum = 1;
                groupNum++;
            }
        }

        return pin;
    }

}
