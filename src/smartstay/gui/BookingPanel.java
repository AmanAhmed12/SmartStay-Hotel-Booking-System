package smartstay.gui;

import smartstay.exception.BookingException;
import smartstay.gui.theme.*;
import smartstay.model.Guest;
import smartstay.model.Reservation;
import smartstay.model.Room;
import smartstay.model.RoomType;
import smartstay.service.HotelService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BookingPanel extends JPanel {

    private static final String ANY_TYPE = "Any Type";

    private final HotelService hotelService;
    private final Guest guest;

    private final JComboBox<Object> typeCombo = new JComboBox<>();
    private final JSpinner checkInSpinner;
    private final JSpinner checkOutSpinner;

    private final DefaultTableModel resultsModel;
    private final JTable resultsTable;

    private final JSpinner guestsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));

    public BookingPanel(HotelService hotelService, Guest guest) {
        this.hotelService = hotelService;
        this.guest = guest;
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));

        typeCombo.addItem(ANY_TYPE);
        for (RoomType type : RoomType.values()) {
            typeCombo.addItem(type);
        }

        checkInSpinner = new JSpinner(new SpinnerDateModel());
        checkOutSpinner = new JSpinner(new SpinnerDateModel());
        checkInSpinner.setEditor(new JSpinner.DateEditor(checkInSpinner, "yyyy-MM-dd"));
        checkOutSpinner.setEditor(new JSpinner.DateEditor(checkOutSpinner, "yyyy-MM-dd"));
        checkInSpinner.setValue(addDays(new Date(), 2));
        checkOutSpinner.setValue(addDays(new Date(), 3));
        checkInSpinner.setPreferredSize(new Dimension(110, checkInSpinner.getPreferredSize().height));
        checkOutSpinner.setPreferredSize(new Dimension(110, checkOutSpinner.getPreferredSize().height));

        add(buildSearchPanel(), BorderLayout.NORTH);

        CardPanel resultsCard = new CardPanel();
        resultsCard.setLayout(new BorderLayout(0, 10));
        JLabel resultsTitle = new JLabel("Available Rooms");
        resultsTitle.setFont(Theme.FONT_SECTION);
        resultsCard.add(resultsTitle, BorderLayout.NORTH);

        String[] columns = {"Room ID", "Room Type", "Bed Type", "Daily Rate (LKR)", "Status"};
        resultsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultsTable = new JTable(resultsModel);
        TableStyler.style(resultsTable);
        resultsTable.getColumnModel().getColumn(4).setCellRenderer(new StatusBadgeRenderer());
        resultsCard.add(new JScrollPane(resultsTable), BorderLayout.CENTER);
        add(resultsCard, BorderLayout.CENTER);

        add(buildBookingPanel(), BorderLayout.SOUTH);
    }

    private CardPanel buildSearchPanel() {
        CardPanel card = new CardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel heading = new JLabel("Search Available Rooms");
        heading.setFont(Theme.FONT_SECTION);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        Theme.fitLabel(heading);
        card.add(heading);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setOpaque(false);
        row.add(new JLabel("Room Type:"));
        row.add(typeCombo);
        row.add(new JLabel("Check-in:"));
        row.add(checkInSpinner);
        row.add(new JLabel("Check-out:"));
        row.add(checkOutSpinner);
        RoundedButton searchButton = new RoundedButton("Search", Icons.search(Color.WHITE), Theme.NAVY);
        searchButton.addActionListener(e -> onSearch());
        row.add(searchButton);
        card.add(row);

        return card;
    }

    private CardPanel buildBookingPanel() {
        CardPanel card = new CardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel heading = new JLabel("Book Selected Room");
        heading.setFont(Theme.FONT_SECTION);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        Theme.fitLabel(heading);
        card.add(heading);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setOpaque(false);
        JLabel guestLabel = new JLabel("Booking as: " + guest.getFullName() + " (" + guest.getId() + ")");
        guestLabel.setFont(Theme.FONT_BOLD);
        Theme.fitLabel(guestLabel);
        row.add(guestLabel);
        row.add(new JLabel("Number of Guests:"));
        row.add(guestsSpinner);
        RoundedButton bookButton = new RoundedButton("Book Selected Room", Icons.calendar(Color.WHITE), Theme.SUCCESS);
        bookButton.addActionListener(e -> onBook());
        row.add(bookButton);
        card.add(row);

        return card;
    }

    private void onSearch() {
        try {
            RoomType typeFilter = selectedType();
            LocalDate checkIn = toLocalDate((Date) checkInSpinner.getValue());
            LocalDate checkOut = toLocalDate((Date) checkOutSpinner.getValue());

            List<Room> rooms = hotelService.searchAvailableRooms(typeFilter, checkIn, checkOut);
            resultsModel.setRowCount(0);
            for (Room room : rooms) {
                resultsModel.addRow(new Object[]{
                        room.getRoomId(), room.getRoomType(), room.getBedType(),
                        String.format("%,.2f", room.getDailyRate()), room.getStatus()
                });
            }
            if (rooms.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No available rooms match your search.",
                        "SmartStay", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (BookingException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Search error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onBook() {
        int row = resultsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Search and select a room first.",
                    "SmartStay", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String roomId = (String) resultsModel.getValueAt(row, 0);
        int numberOfGuests = (Integer) guestsSpinner.getValue();
        LocalDate checkIn = toLocalDate((Date) checkInSpinner.getValue());
        LocalDate checkOut = toLocalDate((Date) checkOutSpinner.getValue());

        try {
            Reservation reservation = hotelService.createReservation(roomId, guest.getId(), checkIn, checkOut, numberOfGuests);
            JOptionPane.showMessageDialog(this, "Booking confirmed. Booking ID: " + reservation.getBookingId(),
                    "SmartStay", JOptionPane.INFORMATION_MESSAGE);
            new InvoiceDialog(SwingUtilities.getWindowAncestor(this),
                    hotelService.generateInvoice(reservation.getBookingId())).setVisible(true);
            onSearch();
        } catch (BookingException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Booking failed", JOptionPane.WARNING_MESSAGE);
        }
    }

    private RoomType selectedType() {
        Object selected = typeCombo.getSelectedItem();
        return selected instanceof RoomType ? (RoomType) selected : null;
    }

    private static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }
}
