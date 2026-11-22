package smartstay.exception;

// Thrown when a login or registration attempt fails (wrong credentials,
// username already taken, and so on).
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }
}
