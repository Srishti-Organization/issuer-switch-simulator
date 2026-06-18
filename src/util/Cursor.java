package util;

public final class Cursor {
    private final String data;
    private int pos;

    public Cursor(String data) {
        this.data = data;
    }

    public String read(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Negative read length: " + n);
        }
        if (pos + n > data.length()) {
            throw new IllegalStateException(
                    "Buffer underflow: need " + n + " chars at offset " + pos
                            + " but only " + (data.length() - pos) + " remain");
        }
        String slice = data.substring(pos, pos + n);
        pos += n;
        return slice;
    }

    public int readInt(int n) {
        String raw = read(n);
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Expected numeric length field, got '" + raw + "'");
        }
    }

    public int remaining() {
        return data.length() - pos;
    }

    public int position() {
        return pos;
    }
}
