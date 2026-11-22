package smartstay.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Reservation {

    public static final double BOOKING_ADVANCE = 10000.0;

    private final String bookingId;
    private final Room room;
    private final Guest guest;
    private final LocalDate bookingDate;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private final int numberOfGuests;
    private ReservationStatus status;

    public Reservation(String bookingId, Room room, Guest guest, LocalDate bookingDate,
                        LocalDate checkInDate, LocalDate checkOutDate, int numberOfGuests) {
        this.bookingId = bookingId;
        this.room = room;
        this.guest = guest;
        this.bookingDate = bookingDate;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.numberOfGuests = numberOfGuests;
        this.status = ReservationStatus.ACTIVE;
    }

    public String getBookingId() {
        return bookingId;
    }

    public Room getRoom() {
        return room;
    }

    public Guest getGuest() {
        return guest;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public long getNumberOfNights() {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }
}
