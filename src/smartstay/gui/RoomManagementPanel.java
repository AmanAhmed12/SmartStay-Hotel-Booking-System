package smartstay.gui;

import smartstay.exception.BookingException;
import smartstay.gui.theme.*;
import smartstay.model.*;
import smartstay.service.HotelService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class RoomManagementPanel extends JPanel {

    private final HotelService hotelService;

    private final DefaultTableModel tableModel;
    private final JTable roomTable;
    private final JComboBox<RoomType> roomTypeCombo = new JComboBox<>(RoomType.values());
    private final JComboBox<BedType> bedTypeCombo = new JComboBox<>(BedType.values());
    private final JComboBox<RoomStatus> statusCombo = new JComboBox<>(RoomStatus.values());

    public RoomManagementPanel(HotelService hotelService) {
        this.hotelService = hotelService;
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));

        CardPanel tableCard = new CardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));
        JLabel tableTitle = new JLabel("All Rooms");
        tableTitle.setFont(Theme.FONT_SECTION);
        tableCard.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {"Room ID", "Room Type", "Bed Type", "Daily Rate (LKR)", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        roomTable = new JTable(tableModel);
        TableStyler.style(roomTable);
        roomTable.getColumnModel().getColumn(4).setCellRenderer(new StatusBadgeRenderer());
        roomTable.getSelectionModel().addListSelectionListener(e -> onRowSelected());
        tableCard.add(new JScrollPane(roomTable), BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);

        add(buildControlSection(), BorderLayout.SOUTH);
        refreshTable();
    }

    private JPanel buildControlSection() {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.add(buildRoomFormCard());
        wrapper.add(Box.createVerticalStrut(14));
        wrapper.add(buildAvailabilityCard());
        return wrapper;
    }

    private CardPanel buildRoomFormCard() {
        CardPanel card = new CardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.anchor = GridBagConstraints.WEST;

        JLabel heading = new JLabel("Add / Edit Room");
        heading.setFont(Theme.FONT_SECTION);
        Theme.fitLabel(heading);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 4;
        card.add(heading, c);
        c.gridwidth = 1;

        JLabel roomTypeLabel = fieldLabel("Room Type:");
        JLabel bedTypeLabel = fieldLabel("Bed Type:");

        c.gridy = 1;
        c.gridx = 0;
        card.add(roomTypeLabel, c);
        c.gridx = 1;
        card.add(roomTypeCombo, c);
        c.gridx = 2;
        card.add(bedTypeLabel, c);
        c.gridx = 3;
        card.add(bedTypeCombo, c);

        RoundedButton addButton = new RoundedButton("Add Room", Icons.add(Color.WHITE), Theme.SUCCESS);
        RoundedButton updateButton = new RoundedButton("Update Selected", Icons.edit(Color.WHITE), Theme.INFO);
        RoundedButton removeButton = new RoundedButton("Remove Selected", Icons.delete(Color.WHITE), Theme.DANGER);
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(addButton);
        buttonRow.add(updateButton);
        buttonRow.add(removeButton);
        c.gridx = 0; c.gridy = 2; c.gridwidth = 4;
        c.insets = new Insets(10, 6, 5, 6);
        card.add(buttonRow, c);

        JLabel hint = new JLabel("Tip: select a row in the table to edit it, or leave nothing selected to add a brand new room.");
        hint.setForeground(Theme.TEXT_MUTED);
        hint.setFont(Theme.FONT_SUBTITLE);
        Theme.fitLabel(hint);
        c.gridy = 3;
        c.insets = new Insets(8, 6, 2, 6);
        card.add(hint, c);

        addButton.addActionListener(e -> onAddRoom());
        updateButton.addActionListener(e -> onUpdateRoom());
        removeButton.addActionListener(e -> onRemoveRoom());

        return card;
    }

    private CardPanel buildAvailabilityCard() {
        CardPanel card = new CardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.anchor = GridBagConstraints.WEST;

        JLabel heading = new JLabel("Room Availability");
        heading.setFont(Theme.FONT_SECTION);
        Theme.fitLabel(heading);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 3;
        card.add(heading, c);
        c.gridwidth = 1;

        RoundedButton setStatusButton = new RoundedButton("Apply Status", Icons.edit(Color.WHITE), Theme.NAVY);

        c.gridy = 1;
        c.gridx = 0;
        card.add(fieldLabel("Set Status:"), c);
        c.gridx = 1;
        card.add(statusCombo, c);
        c.gridx = 2;
        c.insets = new Insets(5, 12, 5, 6);
        card.add(setStatusButton, c);

        setStatusButton.addActionListener(e -> onSetStatus());

        return card;
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.FONT_BOLD);
        Theme.fitLabel(label);
        return label;
    }

    private void onRowSelected() {
        int row = roomTable.getSelectedRow();
        if (row < 0) {
            return;
        }
        Room room = hotelService.getAllRooms().get(row);
        roomTypeCombo.setSelectedItem(room.getRoomType());
        bedTypeCombo.setSelectedItem(room.getBedType());
        statusCombo.setSelectedItem(room.getStatus());
    }

    private void onAddRoom() {
        hotelService.addRoom((RoomType) roomTypeCombo.getSelectedItem(), (BedType) bedTypeCombo.getSelectedItem());
        refreshTable();
    }

    private void onUpdateRoom() {
        String roomId = getSelectedRoomId();
        if (roomId == null) {
            showMessage("Select a room to update.");
            return;
        }
        try {
            hotelService.updateRoom(roomId, (RoomType) roomTypeCombo.getSelectedItem(),
                    (BedType) bedTypeCombo.getSelectedItem());
            refreshTable();
        } catch (BookingException ex) {
            showMessage(ex.getMessage());
        }
    }

    private void onRemoveRoom() {
        String roomId = getSelectedRoomId();
        if (roomId == null) {
            showMessage("Select a room to remove.");
            return;
        }
        try {
            hotelService.removeRoom(roomId);
            refreshTable();
        } catch (BookingException ex) {
            showMessage(ex.getMessage());
        }
    }

    private void onSetStatus() {
        String roomId = getSelectedRoomId();
        if (roomId == null) {
            showMessage("Select a room to change its status.");
            return;
        }
        hotelService.setRoomStatus(roomId, (RoomStatus) statusCombo.getSelectedItem());
        refreshTable();
    }

    private String getSelectedRoomId() {
        int row = roomTable.getSelectedRow();
        return row < 0 ? null : (String) tableModel.getValueAt(row, 0);
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "SmartStay", JOptionPane.WARNING_MESSAGE);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Room> rooms = hotelService.getAllRooms();
        for (Room room : rooms) {
            tableModel.addRow(new Object[]{
                    room.getRoomId(),
                    room.getRoomType(),
                    room.getBedType(),
                    String.format("%,.2f", room.getDailyRate()),
                    room.getStatus()
            });
        }
    }
}
