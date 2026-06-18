package util;

public final class HexUtil {
    private HexUtil() {
    }

    public static int hexCharToVal(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'f') {
            return 10 + (c - 'a');
        }
        if (c >= 'A' && c <= 'F') {
            return 10 + (c - 'A');
        }
        throw new IllegalArgumentException("Not a hex character: '" + c + "'");
    }

    public static char valToHexChar(int v) {
        if (v < 0 || v > 15) {
            throw new IllegalArgumentException("Nibble out of range: " + v);
        }
        return "0123456789ABCDEF".charAt(v);
    }

    public static String bytesToHex(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte b : data) {
            int v = b & 0xFF;
            sb.append(valToHexChar(v >>> 4));
            sb.append(valToHexChar(v & 0x0F));
        }
        return sb.toString();
    }

    public static String hexToBinary(String hex) {
        StringBuilder sb = new StringBuilder(hex.length() * 4);
        for (int i = 0; i < hex.length(); i++) {
            int v = hexCharToVal(hex.charAt(i));
            for (int b = 3; b >= 0; b--) {
                sb.append(((v >> b) & 1) == 1 ? '1' : '0');
            }
        }
        return sb.toString();
    }
}
