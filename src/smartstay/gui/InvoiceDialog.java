package smartstay.gui;

import smartstay.gui.theme.GradientPanel;
import smartstay.gui.theme.Icons;
import smartstay.gui.theme.RoundedButton;
import smartstay.gui.theme.Theme;
import smartstay.model.Guest;
import smartstay.model.Invoice;
import smartstay.model.Reservation;
import smartstay.model.Room;

import javax.swing.*;
import java.awt.*;

public class InvoiceDialog extends JDialog {

    public InvoiceDialog(Window owner, Invoice invoice) {
        super(owner, "Invoice", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.PAGE_BACKGROUND);

        add(buildHeader(invoice.getReservation()), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setBackground(Theme.PAGE_BACKGROUND);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        body.add(detailsSection(invoice));
        body.add(Box.createVerticalStrut(14));
        body.add(amountSection(invoice));
        add(body, BorderLayout.CENTER);

        RoundedButton closeButton = new RoundedButton("Close", Theme.NAVY);
        closeButton.addActionListener(e -> dispose());
        JPanel footer = new JPanel();
        footer.setBackground(Theme.PAGE_BACKGROUND);
        footer.add(closeButton);
        add(footer, BorderLayout.SOUTH);

        setMinimumSize(new Dimension(480, 300));
        pack();
        setLocationRelativeTo(owner);
    }

    private JPanel buildHeader(Reservation reservation) {
        GradientPanel header = new GradientPanel(Theme.NAVY, Theme.TEAL, true);
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel icon = new JLabel(Icons.invoice(Color.WHITE));
        header.add(icon, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
        JLabel title = new JLabel("SmartStay Invoice");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Booking " + reservation.getBookingId());
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(new Color(255, 255, 255, 210));
        Theme.fitLabel(title);
        Theme.fitLabel(subtitle);
        textPanel.add(title);
        textPanel.add(subtitle);
        header.add(textPanel, BorderLayout.CENTER);

        return header;
    }

    private JPanel detailsSection(Invoice invoice) {
        Reservation reservation = invoice.getReservation();
        Room room = reservation.getRoom();
        Guest guest = reservation.getGuest();

        JPanel panel = sectionPanel("Stay Details");
        addDetailRow(panel, "Room", room.getRoomId() + " - " + room.getRoomType());
        addDetailRow(panel, "Bed Type", String.valueOf(room.getBedType()));
        addDetailRow(panel, "Daily Rate", "LKR " + String.format("%,.2f", room.getDailyRate()));
        addDetailRow(panel, "Guest", guest.getFullName() + " (" + guest.getId() + ")");
        addDetailRow(panel, "NIC / Passport", guest.getIdDocumentNumber());
        addDetailRow(panel, "Contact", guest.getContactNumber());
        addDetailRow(panel, "Email", guest.getEmail());
        addDetailRow(panel, "Check-in", String.valueOf(reservation.getCheckInDate()));
        addDetailRow(panel, "Check-out", String.valueOf(reservation.getCheckOutDate()));
        addDetailRow(panel, "Nights", String.valueOf(reservation.getNumberOfNights()));
        addDetailRow(panel, "Guests", String.valueOf(reservation.getNumberOfGuests()));
        return panel;
    }

    private JPanel amountSection(Invoice invoice) {
        JPanel panel = sectionPanel("Charges");
        addAmountRow(panel, "Base Room Charge", invoice.getBaseRoomCharge(), Theme.TEXT_DARK);
        addAmountRow(panel, "Extra Guest Charge", invoice.getExtraGuestCharge(), Theme.TEXT_DARK);
        addAmountRow(panel, "Discount", -invoice.getDiscount() + 0.0, Theme.SUCCESS);
        addAmountRow(panel, "Tax", invoice.getTax(), Theme.TEXT_DARK);
        addAmountRow(panel, "Advance Deducted", -invoice.getBookingAdvance(), Theme.DANGER);

        JSeparator separator = new JSeparator();
        separator.setForeground(Theme.BORDER);
        panel.add(Box.createVerticalStrut(6));
        panel.add(separator);
        panel.add(Box.createVerticalStrut(6));

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        totalRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        // A short, cheap stay can cost less than the flat refundable advance -
        // that is not an error, it just means a refund is owed instead of a
        // balance being payable, so label and colour it accordingly.
        boolean refundDue = invoice.isRefundDue();
        String totalText = refundDue ? "Refund Due to Guest" : "Final Payable Amount";
        double totalAmount = Math.abs(invoice.getFinalPayableAmount());
        JLabel totalLabel = new JLabel(totalText);
        totalLabel.setFont(Theme.FONT_SECTION);
        JLabel totalValue = new JLabel("LKR " + String.format("%,.2f", totalAmount));
        totalValue.setFont(Theme.FONT_SECTION);
        totalValue.setForeground(refundDue ? Theme.SUCCESS : Theme.NAVY);
        Theme.fitLabel(totalLabel);
        Theme.fitLabel(totalValue);
        totalRow.add(totalLabel, BorderLayout.WEST);
        totalRow.add(totalValue, BorderLayout.EAST);
        panel.add(totalRow);

        return panel;
    }

    private JPanel sectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBackground(Theme.CARD_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        JLabel heading = new JLabel(title);
        heading.setFont(Theme.FONT_SECTION);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        Theme.fitLabel(heading);
        panel.add(heading);
        panel.add(Box.createVerticalStrut(8));
        return panel;
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel labelComponent = new JLabel(label);
        labelComponent.setForeground(Theme.TEXT_MUTED);
        labelComponent.setFont(Theme.FONT_SUBTITLE);
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(Theme.FONT_BOLD);
        Theme.fitLabel(labelComponent);
        Theme.fitLabel(valueComponent);
        row.add(labelComponent, BorderLayout.WEST);
        row.add(valueComponent, BorderLayout.EAST);
        panel.add(row);
        panel.add(Box.createVerticalStrut(4));
    }

    private void addAmountRow(JPanel panel, String label, double amount, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(Theme.FONT_BASE);
        JLabel valueComponent = new JLabel("LKR " + String.format("%,.2f", amount));
        valueComponent.setFont(Theme.FONT_BASE);
        valueComponent.setForeground(valueColor);
        Theme.fitLabel(labelComponent);
        Theme.fitLabel(valueComponent);
        row.add(labelComponent, BorderLayout.WEST);
        row.add(valueComponent, BorderLayout.EAST);
        panel.add(row);
        panel.add(Box.createVerticalStrut(4));
    }
}
