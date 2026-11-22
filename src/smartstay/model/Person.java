package smartstay.model;

// Common base for anyone who uses the system. Kept abstract since a bare
// "Person" with no role never exists on its own in SmartStay.
public abstract class Person {

    private final String id;
    private String fullName;
    private String contactNumber;
    private final String username;
    private final String password;

    protected Person(String id, String fullName, String contactNumber, String username, String password) {
        this.id = id;
        this.fullName = fullName;
        this.contactNumber = contactNumber;
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public boolean matchesPassword(String attempt) {
        return password != null && password.equals(attempt);
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public abstract String getRole();

    @Override
    public String toString() {
        return id + " - " + fullName + " (" + getRole() + ")";
    }
}
