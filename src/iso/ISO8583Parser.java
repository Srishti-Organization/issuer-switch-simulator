package iso;

import util.Cursor;

import java.nio.charset.StandardCharsets;


public final class ISO8583Parser {
    public ISOMessage parse(byte[] body){
        String ascii= new String(body, StandardCharsets.US_ASCII);
        Cursor cur= new Cursor(ascii);

        String mti= cur.read(4);
        ISOMessage msg= new ISOMessage(mti);
        Bitmap bitmap= Bitmap.parse(cur);

        for(int id=2; id<=128; id++){
            if(!bitmap.isSet(id)){
                continue;
            }
            FieldDefinition def= FieldCatalog.get(id);
            String value= readField(cur,def);
            msg.set(id, value);
        }
        if(cur.remaining()!=0){
            throw new IllegalStateException("Trailing bytes after last field: "+cur.remaining()+ " unparsed chars");
        }
        return msg;
    }

    private String readField(Cursor cur, FieldDefinition def){
        switch(def.type()){
            case FIXED:
                return cur.read(def.length());
            case LLVAR: {
                int len= cur.readInt(2);
                guardVarLength(def,len);
                return cur.read(len);
            }
            case LLLVAR: {
                int len= cur.readInt(3);
                guardVarLength(def,len);
                return cur.read(len);
            }
            default:
                throw new IllegalStateException("Unhandled field type: " + def.type());

        }
    }
    private void guardVarLength(FieldDefinition def, int len){
        if(len<0 || len>def.length()){
            throw new IllegalStateException(def+ " declared length "+ len + " exceeds maximum "+ def.length());
        }
    }
}
