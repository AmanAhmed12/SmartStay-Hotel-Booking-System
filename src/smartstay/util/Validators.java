package smartstay.util;

import java.util.regex.Pattern;

public final class Validators {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern CONTACT_PATTERN =
            Pattern.compile("^\\+?[0-9]{7,15}$");
    private static final Pattern ID_DOCUMENT_PATTERN =
            Pattern.compile("^[A-Za-z0-9]{5,12}$");
    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9_]{4,20}$");

    private Validators() {
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidContactNumber(String contactNumber) {
        return contactNumber != null && CONTACT_PATTERN.matcher(contactNumber.trim()).matches();
    }

    public static boolean isValidIdDocument(String idDocumentNumber) {
        return idDocumentNumber != null && ID_DOCUMENT_PATTERN.matcher(idDocumentNumber.trim()).matches();
    }

    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 4;
    }
}
