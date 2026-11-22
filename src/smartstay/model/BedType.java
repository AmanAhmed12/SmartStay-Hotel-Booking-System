package smartstay.model;

public enum BedType {
    SINGLE, DOUBLE, QUEEN, KING;

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
