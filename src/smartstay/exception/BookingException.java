package smartstay.exception;

// Thrown when a business rule (not a plain input-format error) is violated,
// e.g. booking an unavailable room or cancelling too close to check-in.
public class BookingException extends RuntimeException {

    public BookingException(String message) {
        super(message);
    }
}
