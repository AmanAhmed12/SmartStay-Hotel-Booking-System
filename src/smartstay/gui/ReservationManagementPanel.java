package smartstay.gui;

import smartstay.exception.BookingException;
import smartstay.gui.theme.*;
import smartstay.model.Guest;
import smartstay.model.Reservation;
import smartstay.service.HotelService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReservationManagementPanel extends JPanel {

    private final HotelService hotelService;
    private final Guest guest;

    private final JTextField searchField = new JTextField(16);
    private final JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());

    private final DefaultTableModel tableModel;
    private final JTable reservationTable;

    public ReservationManagementPanel(HotelService hotelService, Guest guest) {
        this.hotelService = hotelService;
        this.guest = guest;
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        dateSpinner.setPreferredSize(new Dimension(110, dateSpinner.getPreferredSize().height));
        searchField.setPreferredSize(new Dimension(150, searchField.getPreferredSize().height));

        add(buildSearchPanel(), BorderLayout.NORTH);

        CardPanel tableCard = new CardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));
        JLabel tableTitle = new JLabel("My Reservations");
        tableTitle.setFont(Theme.FONT_SECTION);
        tableCard.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {"Booking ID", "Room ID", "Check-in", "Check-out",
                "Nights", "Guests", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reservationTable = new JTable(tableModel);
        TableStyler.style(reservationTable);
        reservationTable.getColumnModel().getColumn(6).setCellRenderer(new StatusBadgeRenderer());
        tableCard.add(new JScrollPane(reservationTable), BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);

        add(buildActionPanel(), BorderLayout.SOUTH);
        showReservations(hotelService.getAllReservations());
    }

    private CardPanel buildSearchPanel() {
        CardPanel card = new CardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel heading = new JLabel("Find Reservations");
        heading.setFont(Theme.FONT_SECTION);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        Theme.fitLabel(heading);
        card.add(heading);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row1.setOpaque(false);
        row1.add(new JLabel("Guest name or Booking ID:"));
        row1.add(searchField);
        RoundedButton searchButton = new RoundedButton("Search", Icons.search(Color.WHITE), Theme.NAVY);
        searchButton.addActionListener(e -> onSearch());
        row1.add(searchButton);
        card.add(row1);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.setOpaque(false);
        row2.add(new JLabel("Check-in date:"));
        row2.add(dateSpinner);
        RoundedButton byDateButton = new RoundedButton("View by Date", Icons.calendar(Color.WHITE), Theme.TEAL);
        byDateButton.addActionListener(e -> onViewByDate());
        row2.add(byDateButton);

        RoundedButton showAllButton = new RoundedButton("Show All", Theme.NEUTRAL);
        showAllButton.addActionListener(e -> showReservations(hotelService.getAllReservations()));
        row2.add(showAllButton);
        card.add(row2);

        return card;
    }

    private CardPanel buildActionPanel() {
        CardPanel card = new CardPanel();
        RoundedButton cancelButton = new RoundedButton("Cancel Selected Reservation", Icons.cancelMark(Color.WHITE), Theme.DANGER);
        RoundedButton invoiceButton = new RoundedButton("View Invoice", Icons.invoice(Color.WHITE), Theme.INFO);
        cancelButton.addActionListener(e -> onCancel());
        invoiceButton.addActionListener(e -> onViewInvoice());
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        card.add(cancelButton);
        card.add(invoiceButton);
        return card;
    }

    private void onSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            showReservations(hotelService.getAllReservations());
            return;
        }
        Reservation byId = hotelService.findReservationById(query.toUpperCase());
        if (byId != null) {
            List<Reservation> result = new ArrayList<>();
            result.add(byId);
            showReservations(result);
        } else {
            showReservations(hotelService.searchReservationsByGuestName(query));
        }
    }

    private void onViewByDate() {
        LocalDate date = ((Date) dateSpinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        showReservations(hotelService.getReservationsByCheckInDate(date));
    }

    private void onCancel() {
        String bookingId = getSelectedBookingId();
        if (bookingId == null) {
            JOptionPane.showMessageDialog(this, "Select a reservation to cancel.",
                    "SmartStay", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            hotelService.cancelReservation(bookingId);
            JOptionPane.showMessageDialog(this, "Reservation " + bookingId + " cancelled.",
                    "SmartStay", JOptionPane.INFORMATION_MESSAGE);
            showReservations(hotelService.getAllReservations());
        } catch (BookingException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cancellation failed", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onViewInvoice() {
        String bookingId = getSelectedBookingId();
        if (bookingId == null) {
            JOptionPane.showMessageDialog(this, "Select a reservation first.",
                    "SmartStay", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            new InvoiceDialog(SwingUtilities.getWindowAncestor(this),
                    hotelService.generateInvoice(bookingId)).setVisible(true);
        } catch (BookingException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "SmartStay", JOptionPane.WARNING_MESSAGE);
        }
    }

    private String getSelectedBookingId() {
        int row = reservationTable.getSelectedRow();
        return row < 0 ? null : (String) tableModel.getValueAt(row, 0);
    }

    private void showReservations(List<Reservation> reservations) {
        tableModel.setRowCount(0);
        for (Reservation r : reservations) {
            // Each guest only ever sees and manages their own bookings.
            if (!r.getGuest().getId().equals(guest.getId())) {
                continue;
            }
            tableModel.addRow(new Object[]{
                    r.getBookingId(), r.getRoom().getRoomId(),
                    r.getCheckInDate(), r.getCheckOutDate(), r.getNumberOfNights(),
                    r.getNumberOfGuests(), r.getStatus()
            });
        }
    }
}
