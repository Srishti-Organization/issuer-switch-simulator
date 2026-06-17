package iso;

import java.nio.charset.StandardCharsets;

public final class ISO8583Builder {
    public byte[] build(ISOMessage msg){
        Bitmap bitmap = new Bitmap();
        for(int id: msg.fieldIds()){
            bitmap.setBit(id);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(msg.mti());
        sb.append(bitmap.toHex());

        for(int id: msg.fieldIds()) {
            FieldDefinition def = FieldCatalog.get(id);
            String value = msg.get(id);
            sb.append(encodeField(def, value));
        }
        return sb.toString().getBytes(StandardCharsets.US_ASCII);
    }

    private String encodeField(FieldDefinition def, String value){
        switch(def.type()){
            case FIXED:
                if(value.length()!=def.length()){
                    throw new IllegalStateException(
                            def+ " requires exactly "+ def.length()
                            + " chars but got " + value.length() + "('" + value + "')");
                }
                return value;
            case LLVAR:
                requireMax(def,value);
                return String.format("%02d", value.length()) + value;
            case LLLVAR:
                requireMax(def, value);
                return String.format("%03d", value.length()) + value;
            default:
                throw new IllegalStateException("Unhandled field type: " + def.type());
        }
    }

    private void requireMax(FieldDefinition def, String value) {
        if(value.length() > def.length()) {
            throw new IllegalStateException(
                    def + " value length " + value.length() + " exceeds maximum " + def.length());
        }
    }
}

