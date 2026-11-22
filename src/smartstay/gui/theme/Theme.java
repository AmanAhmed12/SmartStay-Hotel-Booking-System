package smartstay.gui.theme;

import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;

// Single place for the app's colour palette and fonts, so every screen looks consistent.
public final class Theme {

    public static final Color NAVY_DARK = new Color(0x0B, 0x1F, 0x33);
    public static final Color NAVY = new Color(0x10, 0x2A, 0x43);
    public static final Color TEAL = new Color(0x1B, 0x6E, 0x7A);
    public static final Color GOLD = new Color(0xD4, 0xAF, 0x6A);
    public static final Color PAGE_BACKGROUND = new Color(0xF1, 0xF3, 0xF6);
    public static final Color CARD_WHITE = Color.WHITE;
    public static final Color TEXT_DARK = new Color(0x1E, 0x24, 0x2B);
    public static final Color TEXT_MUTED = new Color(0x6B, 0x73, 0x80);
    public static final Color BORDER = new Color(0xDD, 0xE1, 0xE8);

    public static final Color SUCCESS = new Color(0x2E, 0x8B, 0x57);
    public static final Color DANGER = new Color(0xC0, 0x39, 0x2B);
    public static final Color WARNING = new Color(0xD9, 0x8E, 0x04);
    public static final Color INFO = new Color(0x1B, 0x6E, 0x7A);
    public static final Color NEUTRAL = new Color(0x55, 0x61, 0x70);

    public static final Font FONT_BASE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_STAT = new Font("Segoe UI", Font.BOLD, 26);

    private Theme() {
    }

    public static Color statusColor(Object status) {
        String s = String.valueOf(status);
        switch (s) {
            case "AVAILABLE":
            case "ACTIVE":
                return SUCCESS;
            case "RESERVED":
                return WARNING;
            case "UNDER_MAINTENANCE":
                return NEUTRAL;
            case "CANCELLED":
                return DANGER;
            case "COMPLETED":
                return INFO;
            default:
                return NEUTRAL;
        }
    }

    // Swing's own size calculation can under-measure label text by a few
    // pixels once a component is realized, clipping the last character or
    // two. Pin the preferred width a little wider than the raw text metric
    // so that never happens, regardless of the surrounding layout.
    // Shortens text with a trailing ellipsis if it would not fit in maxWidthPx
    // at the given font - used for user-supplied names in fixed-width spots
    // like the sidebar, where no amount of label-sizing trickery helps.
    public static String truncateToWidth(String text, Font font, int maxWidthPx) {
        FontMetrics fm = new JLabel().getFontMetrics(font);
        if (fm.stringWidth(text) <= maxWidthPx) {
            return text;
        }
        String ellipsis = "...";
        int ellipsisWidth = fm.stringWidth(ellipsis);
        StringBuilder result = new StringBuilder();
        for (char ch : text.toCharArray()) {
            if (fm.stringWidth(result.toString() + ch) + ellipsisWidth > maxWidthPx) {
                break;
            }
            result.append(ch);
        }
        return result + ellipsis;
    }

    public static void fitLabel(JLabel label) {
        FontMetrics fm = label.getFontMetrics(label.getFont());
        int width = fm.stringWidth(label.getText()) + 14;
        int height = Math.max(label.getPreferredSize().height, fm.getHeight() + 4);
        Dimension size = new Dimension(width, height);
        label.setPreferredSize(size);
        // BoxLayout caps a child's width at getMaximumSize(), which for a plain
        // JLabel defaults to the UI delegate's own (smaller) computed size and
        // ignores setPreferredSize() above - so it must be raised too.
        label.setMaximumSize(size);
    }

    public static String statusLabel(Object status) {
        String s = String.valueOf(status).replace('_', ' ');
        return s.substring(0, 1) + s.substring(1).toLowerCase();
    }
}
