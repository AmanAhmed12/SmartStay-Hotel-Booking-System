package smartstay.model;

public class Administrator extends Person {

    public Administrator(String id, String fullName, String contactNumber, String username, String password) {
        super(id, fullName, contactNumber, username, password);
    }

    @Override
    public String getRole() {
        return "Administrator";
    }
}
