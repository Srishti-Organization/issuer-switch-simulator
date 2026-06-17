package iso;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class FieldCatalog {
    private static final Map<Integer, FieldDefinition> DEFS;

    static {
        Map<Integer, FieldDefinition> m = new LinkedHashMap<>();
        put(m, 2, FieldType.LLVAR, 19, "Primary Account Number");
        put(m, 3, FieldType.FIXED, 6, "Processing Code");
        put(m, 4, FieldType.FIXED, 12, "Amount, Transaction");
        put(m, 7, FieldType.FIXED, 10, "Transmission Date/Time");
        put(m, 11, FieldType.FIXED, 6, "System Trace Audit Number");
        put(m, 12, FieldType.FIXED, 6, "Local Transaction Time");
        put(m, 13, FieldType.FIXED, 4, "Local Transaction Date");
        put(m, 37, FieldType.FIXED, 12, "Retrieval Reference Number");
        put(m, 38, FieldType.FIXED, 6, "Authorization Identification Response");
        put(m, 39, FieldType.FIXED, 2, "Response Code");
        put(m, 41, FieldType.FIXED, 8, "Card Acceptor Terminal ID");
        put(m, 49, FieldType.FIXED, 3, "Currency Code, Transaction");
        put(m, 54, FieldType.LLLVAR, 120, "Additional Amounts");
        DEFS = Collections.unmodifiableMap(m);
    }

    private FieldCatalog() {
    }

    private static void put(Map<Integer, FieldDefinition> m, int id, FieldType t, int len, String label) {
        m.put(id, new FieldDefinition(id, t, len, label));
    }

    public static FieldDefinition get(int id) {
        FieldDefinition def = DEFS.get(id);
        if (def == null) {
            throw new IllegalArgumentException("No catalog definition for DE" + id);
        }
        return def;
    }

    public static boolean isDefined(int id)
    {
        return DEFS.containsKey(id);
    }
}
