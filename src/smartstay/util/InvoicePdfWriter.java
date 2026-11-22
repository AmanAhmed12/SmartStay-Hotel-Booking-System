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
import java.util.Locale;

// Draws a one-page invoice (header band, billed-to/stay-details, a bordered
// and striped charges table, a totals banner) straight into the raw PDF file
// format - no external PDF library needed for a layout this simple.
public final class InvoicePdfWriter {

    private static final float PAGE_WIDTH = 612;
    private static final float PAGE_HEIGHT = 792;
    private static final float MARGIN = 50;
    private static final float RIGHT = PAGE_WIDTH - MARGIN;

    private static final float[] NAVY = rgb(0x10, 0x2A, 0x43);
    private static final float[] GOLD = rgb(0xD4, 0xAF, 0x6A);
    private static final float[] TEXT_DARK = rgb(0x1E, 0x24, 0x2B);
    private static final float[] TEXT_MUTED = rgb(0x6B, 0x73, 0x80);
    private static final float[] SUCCESS = rgb(0x2E, 0x8B, 0x57);
    private static final float[] DANGER = rgb(0xC0, 0x39, 0x2B);
    private static final float[] BORDER = rgb(0xDD, 0xE1, 0xE8);
    private static final float[] ROW_ALT = rgb(0xF5, 0xF7, 0xFA);
    private static final float[] WHITE = {1f, 1f, 1f};

    private InvoicePdfWriter() {
    }

    public static void write(Invoice invoice, Path destination) throws IOException {
        Files.write(destination, buildPdf(invoice));
    }

    private static float[] rgb(int r, int g, int b) {
        return new float[]{r / 255f, g / 255f, b / 255f};
    }

    private static String money(double amount) {
        return String.format(Locale.US, "%,.2f", amount);
    }

    // ---- Layout ---------------------------------------------------------

    private static void drawInvoice(Canvas c, Invoice invoice) {
        Reservation reservation = invoice.getReservation();
        Room room = reservation.getRoom();
        Guest guest = reservation.getGuest();

        c.fillRect(0, 692, PAGE_WIDTH, 100, NAVY);
        c.fillRect(0, 689, PAGE_WIDTH, 3, GOLD);
        c.text(MARGIN, 750, Font.BOLD, 24, WHITE, "SmartStay");
        c.text(MARGIN, 731, Font.REGULAR, 10, WHITE, "Hotel Booking & Reservation System");
        c.textRight(RIGHT, 750, Font.BOLD, 18, GOLD, "INVOICE");
        c.textRight(RIGHT, 731, Font.REGULAR, 10, WHITE, "Booking ID: " + reservation.getBookingId());

        float leftColX = MARGIN;
        float rightColX = 320;
        float y = 655;
        c.text(leftColX, y, Font.BOLD, 9, TEXT_MUTED, "BILLED TO");
        c.text(rightColX, y, Font.BOLD, 9, TEXT_MUTED, "STAY DETAILS");

        y -= 18;
        c.text(leftColX, y, Font.BOLD, 13, TEXT_DARK, guest.getFullName());
        c.text(rightColX, y, Font.BOLD, 13, TEXT_DARK, room.getRoomId() + " - " + room.getRoomType());

        y -= 16;
        c.text(leftColX, y, Font.REGULAR, 10, TEXT_DARK, "NIC/Passport: " + guest.getIdDocumentNumber());
        c.text(rightColX, y, Font.REGULAR, 10, TEXT_DARK,
                "Bed Type: " + room.getBedType() + "   Rate: LKR " + money(room.getDailyRate()));

        y -= 14;
        c.text(leftColX, y, Font.REGULAR, 10, TEXT_DARK, "Contact: " + guest.getContactNumber());
        c.text(rightColX, y, Font.REGULAR, 10, TEXT_DARK,
                "Check-in: " + reservation.getCheckInDate() + "   Check-out: " + reservation.getCheckOutDate());

        y -= 14;
        c.text(leftColX, y, Font.REGULAR, 10, TEXT_DARK, "Email: " + guest.getEmail());
        c.text(rightColX, y, Font.REGULAR, 10, TEXT_DARK,
                "Nights: " + reservation.getNumberOfNights() + "   Guests: " + reservation.getNumberOfGuests());

        y -= 22;
        c.line(MARGIN, y, RIGHT, y, BORDER, 1);

        float tableWidth = RIGHT - MARGIN;
        float headerHeight = 22;
        float rowHeight = 22;
        float tableTop = y - 20;
        float dividerX = 400;

        c.fillRect(MARGIN, tableTop - headerHeight, tableWidth, headerHeight, NAVY);
        c.text(MARGIN + 10, tableTop - headerHeight + 7, Font.BOLD, 10, WHITE, "DESCRIPTION");
        c.textRight(RIGHT - 10, tableTop - headerHeight + 7, Font.BOLD, 10, WHITE, "AMOUNT (LKR)");

        List<Row> rows = new ArrayList<>();
        rows.add(new Row("Base Room Charge", invoice.getBaseRoomCharge(), TEXT_DARK));
        rows.add(new Row("Extra Guest Charge", invoice.getExtraGuestCharge(), TEXT_DARK));
        rows.add(new Row("Discount", -invoice.getDiscount() + 0.0, SUCCESS));
        rows.add(new Row("Tax", invoice.getTax(), TEXT_DARK));
        rows.add(new Row("Advance Deducted", -invoice.getBookingAdvance(), DANGER));

        float rowTop = tableTop - headerHeight;
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            float rowBottom = rowTop - rowHeight;
            if (i % 2 == 0) {
                c.fillRect(MARGIN, rowBottom, tableWidth, rowHeight, ROW_ALT);
            }
            float textY = rowBottom + 7;
            c.text(MARGIN + 10, textY, Font.REGULAR, 10, TEXT_DARK, row.label);
            String sign = row.amount < 0 ? "-" : "";
            c.textRight(RIGHT - 10, textY, Font.REGULAR, 10, row.color,
                    sign + "LKR " + money(Math.abs(row.amount)));
            rowTop = rowBottom;
        }
        float tableBottom = rowTop;

        c.rectStroke(MARGIN, tableBottom, tableWidth, tableTop - tableBottom, BORDER, 1);
        c.line(dividerX, tableTop, dividerX, tableBottom, BORDER, 1);

        boolean refundDue = invoice.isRefundDue();
        double totalAmount = Math.abs(invoice.getFinalPayableAmount());
        float[] bandColor = refundDue ? SUCCESS : NAVY;
        float bandHeight = 36;
        float bandTop = tableBottom - 10;

        c.fillRect(MARGIN, bandTop - bandHeight, tableWidth, bandHeight, bandColor);
        String totalLabel = refundDue ? "REFUND DUE TO GUEST" : "FINAL PAYABLE AMOUNT";
        c.text(MARGIN + 10, bandTop - bandHeight + 13, Font.BOLD, 13, WHITE, totalLabel);
        c.textRight(RIGHT - 10, bandTop - bandHeight + 13, Font.BOLD, 15, WHITE, "LKR " + money(totalAmount));

        float footerY = bandTop - bandHeight - 30;
        c.textCentered(PAGE_WIDTH / 2, footerY, Font.REGULAR, 9, TEXT_MUTED,
                "Thank you for choosing SmartStay. We hope to welcome you again soon!");
        c.textCentered(PAGE_WIDTH / 2, footerY - 14, Font.REGULAR, 8, TEXT_MUTED,
                "This is a system-generated invoice and does not require a signature.");
    }

    private static class Row {
        final String label;
        final double amount;
        final float[] color;

        Row(String label, double amount, float[] color) {
            this.label = label;
            this.amount = amount;
            this.color = color;
        }
    }

    private enum Font {
        REGULAR("F1"), BOLD("F2");

        final String resourceName;

        Font(String resourceName) {
            this.resourceName = resourceName;
        }
    }

    // ---- Drawing canvas, backed by a PDF content-stream string builder ----

    private static class Canvas {
        private final StringBuilder content = new StringBuilder();

        void fillRect(float x, float y, float w, float h, float[] color) {
            setColor(color, "rg");
            content.append(f(x)).append(' ').append(f(y)).append(' ')
                    .append(f(w)).append(' ').append(f(h)).append(" re f\n");
        }

        void rectStroke(float x, float y, float w, float h, float[] color, float lineWidth) {
            setColor(color, "RG");
            content.append(f(lineWidth)).append(" w\n");
            content.append(f(x)).append(' ').append(f(y)).append(' ')
                    .append(f(w)).append(' ').append(f(h)).append(" re S\n");
        }

        void line(float x1, float y1, float x2, float y2, float[] color, float lineWidth) {
            setColor(color, "RG");
            content.append(f(lineWidth)).append(" w\n");
            content.append(f(x1)).append(' ').append(f(y1)).append(" m\n");
            content.append(f(x2)).append(' ').append(f(y2)).append(" l\nS\n");
        }

        void text(float x, float y, Font font, float size, float[] color, String str) {
            setColor(color, "rg");
            content.append("BT\n/").append(font.resourceName).append(' ').append(f(size)).append(" Tf\n")
                    .append(f(x)).append(' ').append(f(y)).append(" Td\n(")
                    .append(escape(str)).append(") Tj\nET\n");
        }

        void textRight(float xRight, float y, Font font, float size, float[] color, String str) {
            text(xRight - stringWidth(str, font, size), y, font, size, color, str);
        }

        void textCentered(float xCenter, float y, Font font, float size, float[] color, String str) {
            text(xCenter - stringWidth(str, font, size) / 2f, y, font, size, color, str);
        }

        String getContent() {
            return content.toString();
        }

        private void setColor(float[] color, String op) {
            content.append(f3(color[0])).append(' ').append(f3(color[1])).append(' ')
                    .append(f3(color[2])).append(' ').append(op).append('\n');
        }

        private String f(float v) {
            return String.format(Locale.US, "%.2f", v);
        }

        private String f3(float v) {
            return String.format(Locale.US, "%.3f", v);
        }

        private String escape(String text) {
            return text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
        }
    }

    // ---- Standard Helvetica / Helvetica-Bold metrics (1/1000 em units,
    // codes 32-126) - used to right-align and centre text precisely without
    // needing a font-rendering library. These are the standard published
    // AFM widths for the 14 base PDF fonts. ----

    private static final int[] HELVETICA_WIDTHS = {
            278, 278, 355, 556, 556, 889, 667, 191, 333, 333, 389, 584, 278, 333, 278, 278,
            556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 278, 278, 584, 584, 584, 556,
            1015, 667, 667, 722, 722, 667, 611, 778, 722, 278, 500, 667, 556, 833, 722, 778,
            667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 278, 278, 278, 469, 556,
            333, 556, 556, 500, 556, 556, 278, 556, 556, 222, 222, 500, 222, 833, 556, 556,
            556, 556, 333, 500, 278, 556, 500, 722, 500, 500, 500, 334, 260, 334, 584
    };

    private static final int[] HELVETICA_BOLD_WIDTHS = {
            278, 333, 474, 556, 556, 889, 722, 238, 333, 333, 389, 584, 278, 333, 278, 278,
            556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 333, 333, 584, 584, 584, 611,
            975, 722, 722, 722, 722, 667, 611, 778, 722, 278, 556, 722, 611, 833, 722, 778,
            667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 333, 278, 333, 584, 556,
            333, 556, 611, 556, 611, 556, 333, 611, 611, 278, 278, 556, 278, 889, 611, 611,
            611, 611, 389, 556, 333, 611, 556, 778, 556, 556, 500, 389, 280, 389, 584
    };

    private static float stringWidth(String text, Font font, float size) {
        int[] table = font == Font.BOLD ? HELVETICA_BOLD_WIDTHS : HELVETICA_WIDTHS;
        int total = 0;
        for (int i = 0; i < text.length(); i++) {
            int idx = text.charAt(i) - 32;
            total += (idx >= 0 && idx < table.length) ? table[idx] : 556;
        }
        return total * size / 1000f;
    }

    // ---- PDF object/file assembly ------------------------------------------

    private static byte[] buildPdf(Invoice invoice) throws IOException {
        Canvas canvas = new Canvas();
        drawInvoice(canvas, invoice);
        byte[] contentBytes = canvas.getContent().getBytes(StandardCharsets.ISO_8859_1);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);

        writeRaw(out, "%PDF-1.4\n");

        offsets.add(out.size());
        writeRaw(out, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        offsets.add(out.size());
        writeRaw(out, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

        offsets.add(out.size());
        writeRaw(out, "3 0 obj\n<< /Type /Page /Parent 2 0 R "
                + "/Resources << /Font << /F1 4 0 R /F2 5 0 R >> >> "
                + "/MediaBox [0 0 " + (int) PAGE_WIDTH + " " + (int) PAGE_HEIGHT + "] "
                + "/Contents 6 0 R >>\nendobj\n");

        offsets.add(out.size());
        writeRaw(out, "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

        offsets.add(out.size());
        writeRaw(out, "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n");

        offsets.add(out.size());
        writeRaw(out, "6 0 obj\n<< /Length " + contentBytes.length + " >>\nstream\n");
        out.write(contentBytes);
        writeRaw(out, "\nendstream\nendobj\n");

        int xrefStart = out.size();
        int objectCount = offsets.size(); // includes the unused 0th entry
        writeRaw(out, "xref\n0 " + objectCount + "\n0000000000 65535 f \n");
        for (int i = 1; i < objectCount; i++) {
            writeRaw(out, String.format("%010d 00000 n \n", offsets.get(i)));
        }
        writeRaw(out, "trailer\n<< /Size " + objectCount + " /Root 1 0 R >>\nstartxref\n" + xrefStart + "\n%%EOF");

        return out.toByteArray();
    }

    private static void writeRaw(OutputStream out, String text) throws IOException {
        out.write(text.getBytes(StandardCharsets.ISO_8859_1));
    }
}
