package util;

import java.util.Random;
import java.util.UUID;

public class AddressGenerator {
    private static final Random random = new Random();
    private static final String HEX_CHARS = "0123456789abcdef";
    private static final String BASE58_CHARS = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";

    public static String ethereumAddress(){
        StringBuilder sb = new StringBuilder("0x");
        for(int i = 0; i < 40; i++){
            sb.append(HEX_CHARS.charAt(random.nextInt(HEX_CHARS.length())));
        }
        return sb.toString();
    }

    public static String bitcoinAdress(){
        String prefix;
        int type = random.nextInt(3);
        if (type == 0) {
            prefix = "1";
        } else if (type == 1) {
            prefix = "3";
        } else {
            prefix = "bc1";
        }
        StringBuilder sb = new StringBuilder(prefix);
        int length = (prefix.equals("bc1")) ? 39 : 40;
        for(int i = 0; i < length; i++){
            sb.append(BASE58_CHARS.charAt(random.nextInt(BASE58_CHARS.length())));
        }
        return sb.toString();
    }

}
