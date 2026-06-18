package util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public final class ByteUtil {
    private ByteUtil() {
    }

    public static boolean readFully(InputStream in, byte[] buf) throws IOException {
        int total = 0;
        while (total < buf.length) {
            int n = in.read(buf, total, buf.length - total);
            if (n < 0) {
                if (total == 0) {
                    return false; // peer closed between messages: normal shutdown
                }
                throw new EOFException("Stream closed after reading " + total
                        + " of " + buf.length + " expected bytes");
            }
            total += n;
        }
        return true;
    }
}
