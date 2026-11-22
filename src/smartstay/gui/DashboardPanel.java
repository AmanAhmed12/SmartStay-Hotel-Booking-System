package smartstay.gui;

import smartstay.gui.theme.CardPanel;
import smartstay.gui.theme.Icons;
import smartstay.gui.theme.Theme;
import smartstay.model.Reservation;
import smartstay.model.ReservationStatus;
import smartstay.model.Room;
import smartstay.model.RoomStatus;
import smartstay.service.HotelService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Landing screen: a quick live snapshot of the hotel, built from the same
// HotelService queries the other tabs use - no separate data source.
public class DashboardPanel extends JPanel {

    private final HotelService hotelService;
    private final JLabel totalRoomsValue = new JLabel("0");
    private final JLabel availableRoomsValue = new JLabel("0");
    private final JLabel activeReservationsValue = new JLabel("0");
    private final JLabel registeredGuestsValue = new JLabel("0");
    private final JPanel upcomingList = new JPanel();

    public DashboardPanel(HotelService hotelService) {
        this.hotelService = hotelService;
        setOpaque(false);
        setLayout(new BorderLayout(0, 18));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel welcome = new JLabel("Welcome back - here is today's snapshot");
        welcome.setFont(Theme.FONT_SECTION);
        welcome.setForeground(Theme.TEXT_DARK);
        add(welcome, BorderLayout.NORTH);

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        statsRow.add(statCard("Total Rooms", totalRoomsValue, Icons.bed(Theme.NAVY), Theme.NAVY));
        statsRow.add(statCard("Available Now", availableRoomsValue, Icons.checklist(Theme.SUCCESS), Theme.SUCCESS));
        statsRow.add(statCard("Active Reservations", activeReservationsValue, Icons.invoice(Theme.WARNING), Theme.WARNING));
        statsRow.add(statCard("Registered Guests", registeredGuestsValue, Icons.guest(Theme.TEAL), Theme.TEAL));

        JPanel north = new JPanel(new BorderLayout(0, 12));
        north.setOpaque(false);
        north.add(statsRow, BorderLayout.NORTH);
        add(north, BorderLayout.CENTER);

        CardPanel upcomingCard = new CardPanel();
        upcomingCard.setLayout(new BorderLayout(0, 8));
        JLabel upcomingTitle = new JLabel("Upcoming check-ins (next 7 days)");
        upcomingTitle.setFont(Theme.FONT_SECTION);
        upcomingCard.add(upcomingTitle, BorderLayout.NORTH);
        upcomingList.setLayout(new BoxLayout(upcomingList, BoxLayout.Y_AXIS));
        upcomingList.setOpaque(false);
        upcomingCard.add(upcomingList, BorderLayout.CENTER);
        add(upcomingCard, BorderLayout.SOUTH);

        refresh();
    }

    private JPanel statCard(String label, JLabel valueLabel, Icon icon, Color accent) {
        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout(10, 4));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 10));
        card.add(iconLabel, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        valueLabel.setFont(Theme.FONT_STAT);
        valueLabel.setForeground(accent);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel captionLabel = new JLabel(label);
        captionLabel.setFont(Theme.FONT_SUBTITLE);
        captionLabel.setForeground(Theme.TEXT_MUTED);
        captionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        Theme.fitLabel(captionLabel);
        textPanel.add(valueLabel);
        textPanel.add(captionLabel);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    public void refresh() {
        List<Room> rooms = hotelService.getAllRooms();
        long available = rooms.stream().filter(r -> r.getStatus() == RoomStatus.AVAILABLE).count();
        List<Reservation> reservations = hotelService.getAllReservations();
        long active = reservations.stream().filter(r -> r.getStatus() == ReservationStatus.ACTIVE).count();

        totalRoomsValue.setText(String.valueOf(rooms.size()));
        availableRoomsValue.setText(String.valueOf(available));
        activeReservationsValue.setText(String.valueOf(active));
        registeredGuestsValue.setText(String.valueOf(hotelService.getAllGuests().size()));
        Theme.fitLabel(totalRoomsValue);
        Theme.fitLabel(availableRoomsValue);
        Theme.fitLabel(activeReservationsValue);
        Theme.fitLabel(registeredGuestsValue);

        upcomingList.removeAll();
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy");
        boolean any = false;
        for (int i = 0; i <= 7; i++) {
            LocalDate date = today.plusDays(i);
            for (Reservation r : hotelService.getReservationsByCheckInDate(date)) {
                if (r.getStatus() != ReservationStatus.ACTIVE) {
                    continue;
                }
                any = true;
                JLabel row = new JLabel(String.format("%s   -   %s in Room %s (%s)   -   check-in %s",
                        r.getBookingId(), r.getGuest().getFullName(), r.getRoom().getRoomId(),
                        r.getRoom().getRoomType(), date.format(fmt)));
                row.setFont(Theme.FONT_BASE);
                row.setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2));
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                Theme.fitLabel(row);
                upcomingList.add(row);
            }
        }
        if (!any) {
            JLabel empty = new JLabel("No upcoming check-ins in the next 7 days.");
            empty.setForeground(Theme.TEXT_MUTED);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            Theme.fitLabel(empty);
            upcomingList.add(empty);
        }
        upcomingList.revalidate();
        upcomingList.repaint();
    }
}
