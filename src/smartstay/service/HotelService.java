package smartstay.service;

import smartstay.exception.AuthenticationException;
import smartstay.exception.BookingException;
import smartstay.model.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Central business-logic class. Keeps all SmartStay data in memory using
// LinkedHashMap so lookups by ID are fast while insertion order is kept
// for listing. GUI classes only talk to this class, never to each other's
// internal state directly.
public class HotelService {

    // Fixed credentials for the single built-in administrator account - there
    // is no sign-up screen for admins, this account simply exists on startup.
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    private final Map<String, Room> rooms = new LinkedHashMap<>();
    private final Map<String, Guest> guests = new LinkedHashMap<>();
    private final Map<String, Reservation> reservations = new LinkedHashMap<>();
    private final Administrator administrator =
            new Administrator("A001", "Admin", "N/A", ADMIN_USERNAME, ADMIN_PASSWORD);

    private int roomCounter = 1;
    private int guestCounter = 1;
    private int bookingCounter = 1;

    // ---- Room management (admin) ----------------------------------------

    public Room addRoom(RoomType roomType, BedType bedType) {
        return addRoom(roomType, bedType, RoomStatus.AVAILABLE);
    }

    public Room addRoom(RoomType roomType, BedType bedType, RoomStatus initialStatus) {
        String roomId = "R" + String.format("%03d", roomCounter++);
        Room room = new Room(roomId, roomType, bedType, initialStatus);
        rooms.put(roomId, room);
        return room;
    }

    public void updateRoom(String roomId, RoomType roomType, BedType bedType) {
        Room room = getRoomOrThrow(roomId);
        room.setRoomType(roomType);
        room.setBedType(bedType);
    }

    public void removeRoom(String roomId) {
        Room room = getRoomOrThrow(roomId);
        boolean hasActiveReservation = reservations.values().stream()
                .anyMatch(r -> r.getRoom().getRoomId().equals(roomId) && r.getStatus() == ReservationStatus.ACTIVE);
        if (hasActiveReservation) {
            throw new BookingException("Cannot remove room " + roomId + " - it has an active reservation.");
        }
        rooms.remove(roomId);
    }

    public void setRoomStatus(String roomId, RoomStatus status) {
        getRoomOrThrow(roomId).setStatus(status);
    }

    public List<Room> getAllRooms() {
        List<Room> list = new ArrayList<>(rooms.values());
        list.sort(Comparator.comparing(Room::getRoomId));
        return list;
    }

    private Room getRoomOrThrow(String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new BookingException("Room " + roomId + " was not found.");
        }
        return room;
    }

    // ---- Guest registration and login --------------------------------------

    public Guest registerGuest(String fullName, String idDocumentNumber, String contactNumber,
                                String email, String username, String password) {
        if (isUsernameTaken(username)) {
            throw new AuthenticationException("That username is already in use.");
        }
        String guestId = "G" + String.format("%03d", guestCounter++);
        Guest guest = new Guest(guestId, fullName, idDocumentNumber, contactNumber, email, username, password);
        guests.put(guestId, guest);
        return guest;
    }

    public boolean isUsernameTaken(String username) {
        if (administrator.getUsername().equalsIgnoreCase(username)) {
            return true;
        }
        return guests.values().stream().anyMatch(g -> g.getUsername().equalsIgnoreCase(username));
    }

    // Returns the Administrator or Guest that owns these credentials.
    public Person login(String username, String password) {
        if (administrator.getUsername().equalsIgnoreCase(username) && administrator.matchesPassword(password)) {
            return administrator;
        }
        for (Guest guest : guests.values()) {
            if (guest.getUsername().equalsIgnoreCase(username) && guest.matchesPassword(password)) {
                return guest;
            }
        }
        throw new AuthenticationException("Invalid username or password.");
    }

    public List<Guest> getAllGuests() {
        List<Guest> list = new ArrayList<>(guests.values());
        list.sort(Comparator.comparing(Guest::getId));
        return list;
    }

    public Guest findGuestById(String guestId) {
        return guests.get(guestId);
    }

    // ---- Room search (guest) ----------------------------------------------

    // typeFilter may be null to mean "any room type".
    public List<Room> searchAvailableRooms(RoomType typeFilter, LocalDate checkIn, LocalDate checkOut) {
        validateBookingDates(checkIn, checkOut);
        List<Room> result = new ArrayList<>();
        for (Room room : rooms.values()) {
            if (room.getStatus() == RoomStatus.AVAILABLE
                    && (typeFilter == null || room.getRoomType() == typeFilter)) {
                result.add(room);
            }
        }
        result.sort(Comparator.comparing(Room::getRoomId));
        return result;
    }

    private void validateBookingDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Check-in and check-out dates are required.");
        }
        long daysAhead = ChronoUnit.DAYS.between(LocalDate.now(), checkIn);
        if (daysAhead < 2) {
            throw new BookingException("Bookings must be made at least 2 days before check-in.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new BookingException("Check-out date must be after the check-in date.");
        }
    }

    // ---- Reservations (guest) ----------------------------------------------

    public Reservation createReservation(String roomId, String guestId, LocalDate checkIn,
                                          LocalDate checkOut, int numberOfGuests) {
        Room room = getRoomOrThrow(roomId);
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new BookingException("Room " + roomId + " is not available for booking.");
        }
        Guest guest = guests.get(guestId);
        if (guest == null) {
            throw new BookingException("Guest " + guestId + " was not found. Please register first.");
        }
        if (numberOfGuests < 1) {
            throw new IllegalArgumentException("Number of guests must be at least 1.");
        }
        validateBookingDates(checkIn, checkOut);

        String bookingId = "BK" + String.format("%04d", bookingCounter++);
        Reservation reservation = new Reservation(bookingId, room, guest, LocalDate.now(),
                checkIn, checkOut, numberOfGuests);
        reservations.put(bookingId, reservation);
        room.setStatus(RoomStatus.RESERVED);
        return reservation;
    }

    public void cancelReservation(String bookingId) {
        Reservation reservation = reservations.get(bookingId);
        if (reservation == null) {
            throw new BookingException("Reservation " + bookingId + " was not found.");
        }
        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new BookingException("Only active reservations can be cancelled.");
        }
        long hoursToCheckIn = Duration.between(LocalDateTime.now(),
                reservation.getCheckInDate().atStartOfDay()).toHours();
        if (hoursToCheckIn < 24) {
            throw new BookingException("Reservations cannot be cancelled within 24 hours of check-in.");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        if (reservation.getRoom().getStatus() == RoomStatus.RESERVED) {
            reservation.getRoom().setStatus(RoomStatus.AVAILABLE);
        }
    }

    public Reservation findReservationById(String bookingId) {
        return reservations.get(bookingId);
    }

    public List<Reservation> searchReservationsByGuestName(String nameQuery) {
        String query = nameQuery == null ? "" : nameQuery.trim().toLowerCase();
        List<Reservation> result = new ArrayList<>();
        for (Reservation reservation : reservations.values()) {
            if (reservation.getGuest().getFullName().toLowerCase().contains(query)) {
                result.add(reservation);
            }
        }
        result.sort(Comparator.comparing(Reservation::getBookingId));
        return result;
    }

    public List<Reservation> getReservationsByCheckInDate(LocalDate date) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation reservation : reservations.values()) {
            if (reservation.getCheckInDate().equals(date)) {
                result.add(reservation);
            }
        }
        result.sort(Comparator.comparing(Reservation::getBookingId));
        return result;
    }

    public List<Reservation> getAllReservations() {
        List<Reservation> list = new ArrayList<>(reservations.values());
        list.sort(Comparator.comparing(Reservation::getBookingId));
        return list;
    }

    // ---- Invoicing ----------------------------------------------------------

    public Invoice generateInvoice(String bookingId) {
        Reservation reservation = reservations.get(bookingId);
        if (reservation == null) {
            throw new BookingException("Reservation " + bookingId + " was not found.");
        }
        RoomType type = reservation.getRoom().getRoomType();
        long nights = reservation.getNumberOfNights();

        // Step 1: base room charge
        double baseRoomCharge = type.getDailyRate() * nights;

        // Step 2: extra guest charge, only for guests beyond the room's base capacity
        int extraGuests = Math.max(0, reservation.getNumberOfGuests() - type.getBaseCapacity());
        double extraGuestCharge = extraGuests * type.getExtraGuestChargePerNight() * nights;

        // Step 3: 12% discount on the base charge only, for stays of 5+ nights
        double discount = nights >= 5 ? baseRoomCharge * 0.12 : 0.0;

        // Step 4: tax on the discounted base plus extra guest charges
        double taxableAmount = (baseRoomCharge - discount) + extraGuestCharge;
        double tax = taxableAmount * type.getTaxRate();

        // Step 5: deduct the refundable booking advance
        double bookingAdvance = Reservation.BOOKING_ADVANCE;
        double finalPayableAmount = taxableAmount + tax - bookingAdvance;

        return new Invoice(reservation, baseRoomCharge, extraGuestCharge, discount, tax,
                bookingAdvance, finalPayableAmount);
    }

    // ---- Seed data ------------------------------------------------------------

    public void seedDefaultRooms() {
        addRoom(RoomType.STANDARD, BedType.SINGLE);
        addRoom(RoomType.STANDARD, BedType.DOUBLE);
        addRoom(RoomType.DELUXE, BedType.DOUBLE);
        addRoom(RoomType.DELUXE, BedType.QUEEN);
        addRoom(RoomType.SUITE, BedType.QUEEN);
        addRoom(RoomType.FAMILY_SUITE, BedType.KING);
    }
}
