package smartstay.model;

public class Guest extends Person {

    private String idDocumentNumber; // NIC or passport number
    private String email;

    public Guest(String id, String fullName, String idDocumentNumber, String contactNumber,
                 String email, String username, String password) {
        super(id, fullName, contactNumber, username, password);
        this.idDocumentNumber = idDocumentNumber;
        this.email = email;
    }

    public String getIdDocumentNumber() {
        return idDocumentNumber;
    }

    public void setIdDocumentNumber(String idDocumentNumber) {
        this.idDocumentNumber = idDocumentNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getRole() {
        return "Guest";
    }
}
