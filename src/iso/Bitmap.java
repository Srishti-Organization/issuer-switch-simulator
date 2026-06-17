package iso;

import util.Cursor;

public final class Bitmap {
    private final boolean[] bits= new boolean[129];

    public void setBit(int i) {
        check(i);
        bits[i] = true;
    }
    public boolean isSet(int i) {
        check(i);
        return bits[i];
    }
    private static void check(int i) {
        if (i <1 || i > 128) {
            throw new IllegalArgumentException("Bit index out of range 1...128: " + i);
        }
    }

    private void applyHex(String hex16, int bitOffset) {
        if(hex16.length() != 16) {
            throw new IllegalArgumentException("Bitmap half must be 16 hex chars, got " + hex16.length());
        }
        for(int c = 0; c < 16; c++) {
            int nibble = HexUtil.hexCharToVal(hex16.charAt(c));
            for(int b = 0; b < 4; b++) {
                boolean on = ((nibble >> (3 - b)) & 1) == 1;
                if(on) {
                    bits[bitOffset + c * 4 + b + 1] = true;
                }
            }
        }
    }

    public static Bitmap parse(Cursor cur) {
        Bitmap bm = new Bitmap();
        bm.applyHex(cur.read(16), 0);
        if (bm.isSet(1)) {
            bm.applyHex(cur.read(16), 64);
        }
        return bm;
    }

    public String toHex() {
        boolean secondary = false;
        for (int i = 65; i <= 128; i++) {
            if (bits[i]) {
                secondary = true;
                break;
            }
        }
        if (secondary) {
            bits[1] = true;
        }
        int totalBits = secondary ? 128 : 64;
        StringBuilder sb = new StringBuilder(totalBits / 4);
        for (int c = 0; c < totalBits / 4; c++) {
            int nibble = 0;
            for (int b = 0; b < 4; b++) {
                if (bits[c * 4 + b + 1]) {
                    nibble |= (1 << (3 - b));
                }
            }
            sb.append(HexUtil.valToHexChar(nibble));
        }
        return sb.toString();
    }

    public String toBinaryString() {
        boolean secondary = false;
        for (int i = 65; i <= 128; i++) {
            if (bits[i]) {
                secondary = true;
                break;
            }
        }
        int totalBits = secondary ? 128 : 64;
        StringBuilder sb = new StringBuilder(totalBits);
        for (int i = 1; i <= totalBits; i++) {
            sb.append(bits[i] ? '1' : '0');
        }
        return sb.toString();
    }
}
