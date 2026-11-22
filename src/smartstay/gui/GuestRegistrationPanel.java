package smartstay.gui;

import smartstay.exception.AuthenticationException;
import smartstay.gui.theme.*;
import smartstay.model.Guest;
import smartstay.service.HotelService;
import smartstay.util.Validators;

import javax.swing.*;
import java.awt.*;

// The guest self-registration screen, shown BEFORE login (a guest must
// register here first; only then can they log in with those credentials).
public class GuestRegistrationPanel extends JPanel {

    private final HotelService hotelService;
    private final Runnable onBackToLogin;

    private final JTextField fullNameField = new JTextField(18);
    private final JTextField idDocumentField = new JTextField(18);
    private final JTextField contactField = new JTextField(18);
    private final JTextField emailField = new JTextField(18);
    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);

    public GuestRegistrationPanel(HotelService hotelService, Runnable onBackToLogin) {
        this.hotelService = hotelService;
        this.onBackToLogin = onBackToLogin;
        setOpaque(false);
        setLayout(new GridBagLayout());
        add(buildForm());
    }

    private CardPanel buildForm() {
        CardPanel form = new CardPanel();
        form.setLayout(new GridBagLayout());
        JLabel heading = new JLabel("Register as a New Guest");
        heading.setFont(Theme.FONT_SECTION);
        Theme.fitLabel(heading);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        form.add(heading, c);
        c.gridwidth = 1;

        addRow(form, c, 1, "Full Name:", fullNameField);
        addRow(form, c, 2, "NIC/Passport:", idDocumentField);
        addRow(form, c, 3, "Contact Number:", contactField);
        addRow(form, c, 4, "Email Address:", emailField);
        addRow(form, c, 5, "Choose a Username:", usernameField);
        addRow(form, c, 6, "Choose a Password:", passwordField);

        RoundedButton registerButton = new RoundedButton("Register", Icons.add(Color.WHITE), Theme.SUCCESS);
        registerButton.addActionListener(e -> onRegister());
        c.gridx = 0; c.gridy = 7; c.gridwidth = 1;
        form.add(registerButton, c);

        RoundedButton backButton = new RoundedButton("Back to Login", Theme.NEUTRAL);
        backButton.addActionListener(e -> onBackToLogin.run());
        c.gridx = 1;
        form.add(backButton, c);

        return form;
    }

    private void addRow(JPanel form, GridBagConstraints c, int row, String label, JTextField field) {
        c.gridwidth = 1;
        c.gridx = 0; c.gridy = row;
        form.add(new JLabel(label), c);
        c.gridx = 1;
        form.add(field, c);
    }

    private void onRegister() {
        String fullName = fullNameField.getText().trim();
        String idDocument = idDocumentField.getText().trim();
        String contact = contactField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        String error = validate(fullName, idDocument, contact, email, username, password);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Invalid details", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Guest guest = hotelService.registerGuest(fullName, idDocument, contact, email, username, password);
            JOptionPane.showMessageDialog(this,
                    "Registration successful. Your Guest ID is " + guest.getId()
                            + ".\nYou can now log in with the username and password you just chose.",
                    "Registration complete", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            onBackToLogin.run();
        } catch (AuthenticationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration failed", JOptionPane.WARNING_MESSAGE);
        }
    }

    private String validate(String fullName, String idDocument, String contact, String email,
                             String username, String password) {
        if (!Validators.isNotBlank(fullName)) {
            return "Full name is required.";
        }
        if (!Validators.isValidIdDocument(idDocument)) {
            return "NIC/Passport must be 5-12 letters or digits.";
        }
        if (!Validators.isValidContactNumber(contact)) {
            return "Contact number must be 7-15 digits (an optional + prefix is allowed).";
        }
        if (!Validators.isValidEmail(email)) {
            return "Please enter a valid email address.";
        }
        if (!Validators.isValidUsername(username)) {
            return "Username must be 4-20 letters, digits or underscores.";
        }
        if (!Validators.isValidPassword(password)) {
            return "Password must be at least 4 characters.";
        }
        return null;
    }

    private void clearForm() {
        fullNameField.setText("");
        idDocumentField.setText("");
        contactField.setText("");
        emailField.setText("");
        usernameField.setText("");
        passwordField.setText("");
    }
}
