package smartstay.model;

// Holds the result of a bill calculation for one reservation. Built by
// HotelService.generateInvoice() - this class itself does no calculation.
public class Invoice {

    private final Reservation reservation;
    private final double baseRoomCharge;
    private final double extraGuestCharge;
    private final double discount;
    private final double tax;
    private final double bookingAdvance;
    private final double finalPayableAmount;

    public Invoice(Reservation reservation, double baseRoomCharge, double extraGuestCharge,
                    double discount, double tax, double bookingAdvance, double finalPayableAmount) {
        this.reservation = reservation;
        this.baseRoomCharge = baseRoomCharge;
        this.extraGuestCharge = extraGuestCharge;
        this.discount = discount;
        this.tax = tax;
        this.bookingAdvance = bookingAdvance;
        this.finalPayableAmount = finalPayableAmount;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public double getBaseRoomCharge() {
        return baseRoomCharge;
    }

    public double getExtraGuestCharge() {
        return extraGuestCharge;
    }

    public double getDiscount() {
        return discount;
    }

    public double getTax() {
        return tax;
    }

    public double getBookingAdvance() {
        return bookingAdvance;
    }

    public double getFinalPayableAmount() {
        return finalPayableAmount;
    }

    // For a short, cheap stay the flat refundable advance can exceed the
    // total charge - that is not an error, it just means the hotel owes the
    // guest a refund rather than the guest owing the hotel more money.
    public boolean isRefundDue() {
        return finalPayableAmount < 0;
    }
}
