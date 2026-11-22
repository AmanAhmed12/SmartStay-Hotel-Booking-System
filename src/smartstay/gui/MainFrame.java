package smartstay.gui;

import smartstay.gui.theme.*;
import smartstay.model.Administrator;
import smartstay.model.Guest;
import smartstay.model.Person;
import smartstay.service.HotelService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    private final HotelService hotelService = new HotelService();

    private final CardLayout rootLayout = new CardLayout();
    private final JPanel rootPanel = new JPanel(rootLayout);

    private LoginPanel loginPanel;
    private JPanel currentAppShell;

    public MainFrame() {
        super("SmartStay Hotel Booking System");
        hotelService.seedDefaultRooms();

        rootPanel.add(buildLoginScreen(), "login");
        rootPanel.add(buildRegisterScreen(), "register");
        setContentPane(rootPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 640));
        setSize(1140, 720);
        setLocationRelativeTo(null);
    }

    // ---- Auth screens -------------------------------------------------------

    private JPanel buildLoginScreen() {
        GradientPanel background = new GradientPanel(Theme.NAVY, Theme.TEAL, true);
        background.setLayout(new BorderLayout());
        loginPanel = new LoginPanel(hotelService, this::onLoginSuccess, this::showRegisterScreen);
        background.add(loginPanel, BorderLayout.CENTER);
        return background;
    }

    private JPanel buildRegisterScreen() {
        GradientPanel background = new GradientPanel(Theme.NAVY, Theme.TEAL, true);
        background.setLayout(new BorderLayout());
        background.add(new GuestRegistrationPanel(hotelService, this::showLoginScreen), BorderLayout.CENTER);
        return background;
    }

    private void showLoginScreen() {
        loginPanel.reset();
        rootLayout.show(rootPanel, "login");
    }

    private void showRegisterScreen() {
        rootLayout.show(rootPanel, "register");
    }

    private void onLoginSuccess(Person person) {
        if (currentAppShell != null) {
            rootPanel.remove(currentAppShell);
        }
        currentAppShell = person instanceof Administrator
                ? buildAdminShell((Administrator) person)
                : buildGuestShell((Guest) person);
        rootPanel.add(currentAppShell, "app");
        rootLayout.show(rootPanel, "app");
    }

    // ---- Role-specific shells -------------------------------------------------

    private JPanel buildAdminShell(Administrator admin) {
        DashboardPanel dashboardPanel = new DashboardPanel(hotelService);
        List<NavEntry> items = new ArrayList<>();
        items.add(new NavEntry("dashboard", "Dashboard", Icons.dashboard(Color.WHITE), dashboardPanel));
        items.add(new NavEntry("rooms", "Room Management", Icons.bed(Color.WHITE), new RoomManagementPanel(hotelService)));
        return buildRoleShell(admin, items, dashboardPanel::refresh);
    }

    private JPanel buildGuestShell(Guest guest) {
        List<NavEntry> items = new ArrayList<>();
        items.add(new NavEntry("booking", "Book a Room", Icons.calendar(Color.WHITE),
                new BookingPanel(hotelService, guest)));
        items.add(new NavEntry("reservations", "Manage Reservations", Icons.checklist(Color.WHITE),
                new ReservationManagementPanel(hotelService, guest)));
        return buildRoleShell(guest, items, null);
    }

    private static class NavEntry {
        final String key;
        final String label;
        final Icon icon;
        final JPanel panel;

        NavEntry(String key, String label, Icon icon, JPanel panel) {
            this.key = key;
            this.label = label;
            this.icon = icon;
            this.panel = panel;
        }
    }

    private JPanel buildRoleShell(Person person, List<NavEntry> items, Runnable onDashboardShown) {
        CardLayout contentLayout = new CardLayout();
        JPanel contentPanel = new JPanel(contentLayout);
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JLabel sectionTitleLabel = new JLabel(items.get(0).label);
        sectionTitleLabel.setFont(Theme.FONT_TITLE);
        sectionTitleLabel.setForeground(Theme.TEXT_DARK);
        sectionTitleLabel.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        for (NavEntry item : items) {
            JPanel outer = new JPanel(new BorderLayout());
            outer.setOpaque(false);
            outer.add(item.panel, BorderLayout.CENTER);
            contentPanel.add(outer, item.key);
        }

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.CARD_WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));
        header.add(sectionTitleLabel, BorderLayout.WEST);

        JPanel area = new JPanel(new BorderLayout());
        area.setBackground(Theme.PAGE_BACKGROUND);
        area.add(header, BorderLayout.NORTH);
        area.add(contentPanel, BorderLayout.CENTER);

        GradientPanel sidebar = new GradientPanel(Theme.NAVY, Theme.NAVY_DARK, false);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(230, 10));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 0, 16, 0));

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setBorder(BorderFactory.createEmptyBorder(0, 18, 20, 18));
        JLabel brandIcon = new JLabel(Icons.logo(40, Color.WHITE, Theme.GOLD));
        brandIcon.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel brandName = new JLabel("SmartStay");
        brandName.setForeground(Color.WHITE);
        brandName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandName.setAlignmentX(Component.LEFT_ALIGNMENT);
        String displayName = Theme.truncateToWidth(person.getFullName(), Theme.FONT_BOLD, 185);
        JLabel nameLabel = new JLabel(displayName);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(Theme.FONT_BOLD);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel roleLabel = new JLabel(person.getRole());
        roleLabel.setForeground(new Color(255, 255, 255, 170));
        roleLabel.setFont(Theme.FONT_SUBTITLE);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        Theme.fitLabel(brandName);
        Theme.fitLabel(nameLabel);
        Theme.fitLabel(roleLabel);
        brand.add(brandIcon);
        brand.add(Box.createVerticalStrut(8));
        brand.add(brandName);
        brand.add(Box.createVerticalStrut(6));
        brand.add(nameLabel);
        brand.add(roleLabel);
        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(10));

        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < items.size(); i++) {
            NavEntry item = items.get(i);
            boolean selected = i == 0;
            NavButton button = new NavButton(item.label, item.icon);
            group.add(button);
            button.setSelected(selected);
            button.addActionListener(e -> {
                sectionTitleLabel.setText(item.label);
                if ("dashboard".equals(item.key) && onDashboardShown != null) {
                    onDashboardShown.run();
                }
                contentLayout.show(contentPanel, item.key);
            });
            sidebar.add(button);
        }
        sidebar.add(Box.createVerticalGlue());

        NavButton logoutButton = new NavButton("Log Out", Icons.cancelMark(Color.WHITE));
        logoutButton.addActionListener(e -> showLoginScreen());
        sidebar.add(logoutButton);

        JPanel shell = new JPanel(new BorderLayout());
        shell.add(sidebar, BorderLayout.WEST);
        shell.add(area, BorderLayout.CENTER);
        return shell;
    }
}
