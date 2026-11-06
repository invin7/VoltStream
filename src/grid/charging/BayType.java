package grid.charging;

public enum BayType {
    ULTRA_FAST("Ultra-Fast"),
    FAST_DC("Fast-DC");

    private final String displayName;

    BayType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
