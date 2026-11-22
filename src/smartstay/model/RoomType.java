package smartstay.model;

// Daily rate, breakfast, extra guest charge and tax rate are fixed per room type,
// as given in the SmartStay room package table.
public enum RoomType {
    STANDARD("Standard", 6000.0, false, 1000.0, 0.10, 2),
    DELUXE("Deluxe", 9000.0, true, 1500.0, 0.12, 2),
    SUITE("Suite", 14000.0, true, 2000.0, 0.15, 3),
    FAMILY_SUITE("Family Suite", 18000.0, true, 2500.0, 0.15, 4);

    private final String label;
    private final double dailyRate;
    private final boolean breakfastIncluded;
    private final double extraGuestChargePerNight;
    private final double taxRate;
    private final int baseCapacity; // assumption: guests included before extra charges apply

    RoomType(String label, double dailyRate, boolean breakfastIncluded,
             double extraGuestChargePerNight, double taxRate, int baseCapacity) {
        this.label = label;
        this.dailyRate = dailyRate;
        this.breakfastIncluded = breakfastIncluded;
        this.extraGuestChargePerNight = extraGuestChargePerNight;
        this.taxRate = taxRate;
        this.baseCapacity = baseCapacity;
    }

    public double getDailyRate() {
        return dailyRate;
    }

    public boolean isBreakfastIncluded() {
        return breakfastIncluded;
    }

    public double getExtraGuestChargePerNight() {
        return extraGuestChargePerNight;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public int getBaseCapacity() {
        return baseCapacity;
    }

    @Override
    public String toString() {
        return label;
    }
}
