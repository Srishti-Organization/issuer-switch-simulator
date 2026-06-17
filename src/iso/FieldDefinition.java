package iso;

public final class FieldDefinition {
    private final int id;
    private final FieldType type;
    private final int length;
    private final String label;

    public FieldDefinition(int id, FieldType type, int length, String label) {
        this.id = id;
        this.type = type;
        this.length = length;
        this.label = label;
    }

    public int id() {
        return id;
    }

    public FieldType type() {
        return type;
    }

    public int length() {
        return length;
    }

    public String label() {
        return label;
    }

    @Override
    public String toString() {
        return "DE" + id + "(" + label + "," + type + "," + length + ")";
    }
}
