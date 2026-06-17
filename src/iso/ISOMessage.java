package iso;

import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;

public class ISOMessage {
    private final String mti;
    private final TreeMap<Integer, String> fields = new TreeMap<>();

    public ISOMessage(String mti) {
        if (mti == null || mti.length() != 4) {
            throw new IllegalArgumentException("MTI must be exactly 4 characters: '" + mti + "'");
        }
        this.mti = mti;
    }

    public String mti() {
        return mti;
    }

    public ISOMessage set(int id, String value) {
        if (value == null) {
            throw new IllegalArgumentException("Null value for DE" + id);
        }
        fields.put(id, value);
        return this;
    }
    public String get(int id) {
        return fields.get(id);
    }

    public boolean has(int id) {
        return fields.containsKey(id);
    }

    public void echo(ISOMessage source, int id) {
        if (source.has(id)) {
            set(id, source.get(id));
        }
    }
    public Set<Integer> fieldIds() {
        return Collections.unmodifiableSet(fields.keySet());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ISOMessage{MTI=").append(mti);
        for (var e : fields.entrySet()) {
            sb.append(", DE").append(e.getKey()).append('=').append(mask(e.getKey(), e.getValue()));
        }
        return sb.append('}').toString();
    }
    private static String mask(int id, String v) {
        if (id == 2 && v.length() > 10) {
            return v.substring(0, 6) + "*".repeat(v.length() - 10) + v.substring(v.length() - 4);
        }
        return v;
    }
}
