package smartstay.util;

import smartstay.model.Guest;
import smartstay.model.Invoice;
import smartstay.model.Reservation;
import smartstay.model.Room;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// Writes a one-page PDF invoice directly in the raw PDF file format. The
// layout is plain text, so there is no need to pull in an external PDF
// library just to produce it.
public final class InvoicePdfWriter {

    private InvoicePdfWriter() {
    }

    public static void write(Invoice invoice, Path destination) throws IOException {
        Files.write(destination, buildPdf(buildLines(invoice)));
    }

    private static List<String> buildLines(Invoice invoice) {
        Reservation reservation = invoice.getReservation();
        Room room = reservation.getRoom();
        Guest guest = reservation.getGuest();

        List<String> lines = new ArrayList<>();
        lines.add("SmartStay Hotel Booking System");
        lines.add("Invoice - Booking " + reservation.getBookingId());
        lines.add("");
        lines.add("Room: " + room.getRoomId() + " - " + room.getRoomType());
        lines.add("Bed Type: " + room.getBedType());
        lines.add("Daily Rate: LKR " + money(room.getDailyRate()));
        lines.add("Guest: " + guest.getFullName() + " (" + guest.getId() + ")");
        lines.add("NIC/Passport: " + guest.getIdDocumentNumber());
        lines.add("Contact: " + guest.getContactNumber());
        lines.add("Email: " + guest.getEmail());
        lines.add("Check-in: " + reservation.getCheckInDate());
        lines.add("Check-out: " + reservation.getCheckOutDate());
        lines.add("Nights: " + reservation.getNumberOfNights());
        lines.add("Guests: " + reservation.getNumberOfGuests());
        lines.add("");
        lines.add("Charges");
        lines.add("Base Room Charge: LKR " + money(invoice.getBaseRoomCharge()));
        lines.add("Extra Guest Charge: LKR " + money(invoice.getExtraGuestCharge()));
        lines.add("Discount: LKR " + money(invoice.getDiscount()));
        lines.add("Tax: LKR " + money(invoice.getTax()));
        lines.add("Advance Deducted: LKR " + money(invoice.getBookingAdvance()));
        lines.add("------------------------------------------");
        if (invoice.isRefundDue()) {
            lines.add("Refund Due to Guest: LKR " + money(Math.abs(invoice.getFinalPayableAmount())));
        } else {
            lines.add("Final Payable Amount: LKR " + money(invoice.getFinalPayableAmount()));
        }
        return lines;
    }

    private static String money(double amount) {
        return String.format("%,.2f", amount);
    }

    private static byte[] buildPdf(List<String> lines) throws IOException {
        StringBuilder content = new StringBuilder();
        content.append("BT\n/F1 11 Tf\n50 760 Td\n16 TL\n");
        boolean first = true;
        for (String line : lines) {
            if (!first) {
                content.append("T*\n");
            }
            first = false;
            content.append("(").append(escape(line)).append(") Tj\n");
        }
        content.append("ET");
        byte[] contentBytes = content.toString().getBytes(StandardCharsets.ISO_8859_1);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);

        writeRaw(out, "%PDF-1.4\n");

        offsets.add(out.size());
        writeRaw(out, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        offsets.add(out.size());
        writeRaw(out, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

        offsets.add(out.size());
        writeRaw(out, "3 0 obj\n<< /Type /Page /Parent 2 0 R /Resources << /Font << /F1 4 0 R >> >> "
                + "/MediaBox [0 0 612 792] /Contents 5 0 R >>\nendobj\n");

        offsets.add(out.size());
        writeRaw(out, "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

        offsets.add(out.size());
        writeRaw(out, "5 0 obj\n<< /Length " + contentBytes.length + " >>\nstream\n");
        out.write(contentBytes);
        writeRaw(out, "\nendstream\nendobj\n");

        int xrefStart = out.size();
        writeRaw(out, "xref\n0 6\n0000000000 65535 f \n");
        for (int i = 1; i <= 5; i++) {
            writeRaw(out, String.format("%010d 00000 n \n", offsets.get(i)));
        }
        writeRaw(out, "trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n" + xrefStart + "\n%%EOF");

        return out.toByteArray();
    }

    private static void writeRaw(OutputStream out, String text) throws IOException {
        out.write(text.getBytes(StandardCharsets.ISO_8859_1));
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
