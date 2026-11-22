package smartstay.gui;

import smartstay.exception.AuthenticationException;
import smartstay.gui.theme.*;
import smartstay.model.Person;
import smartstay.service.HotelService;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class LoginPanel extends JPanel {

    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();

    public LoginPanel(HotelService hotelService, Consumer<Person> onLoginSuccess, Runnable onRegisterRequested) {
        setOpaque(false);
        setLayout(new GridBagLayout());

        CardPanel card = new CardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel(Icons.logo(64, Theme.NAVY, Theme.GOLD));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JLabel title = new JLabel("SmartStay");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Theme.NAVY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        Theme.fitLabel(title);

        JLabel subtitle = new JLabel("Hotel Booking & Reservation System");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(2, 0, 24, 0));
        Theme.fitLabel(subtitle);

        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(Theme.FONT_BASE);
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        Theme.fitLabel(usernameLabel);

        styleField(usernameField);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(Theme.FONT_BASE);
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        Theme.fitLabel(passwordLabel);

        styleField(passwordField);

        JLabel hint = new JLabel("Admin demo login: admin / admin123");
        hint.setFont(Theme.FONT_SUBTITLE);
        hint.setForeground(Theme.TEXT_MUTED);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        hint.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        Theme.fitLabel(hint);

        RoundedButton loginButton = new RoundedButton("Login", Theme.SUCCESS);
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        RoundedButton registerButton = new RoundedButton("New guest? Register here", Theme.NAVY);
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(logo);
        card.add(title);
        card.add(subtitle);
        card.add(usernameLabel);
        card.add(usernameField);
        card.add(passwordLabel);
        card.add(passwordField);
        card.add(hint);
        card.add(Box.createVerticalStrut(18));
        card.add(loginButton);
        card.add(Box.createVerticalStrut(8));
        card.add(registerButton);

        Runnable attemptLogin = () -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            try {
                Person person = hotelService.login(username, password);
                onLoginSuccess.accept(person);
            } catch (AuthenticationException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Login failed", JOptionPane.WARNING_MESSAGE);
            }
        };
        loginButton.addActionListener(e -> attemptLogin.run());
        passwordField.addActionListener(e -> attemptLogin.run());
        registerButton.addActionListener(e -> onRegisterRequested.run());

        add(card);
    }

    public void reset() {
        usernameField.setText("");
        passwordField.setText("");
    }

    private void styleField(JTextField field) {
        field.setFont(Theme.FONT_BASE);
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setMaximumSize(new Dimension(260, 36));
        field.setPreferredSize(new Dimension(260, 36));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
    }
}
