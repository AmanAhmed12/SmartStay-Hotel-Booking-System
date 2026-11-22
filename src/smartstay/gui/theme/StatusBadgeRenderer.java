package smartstay.gui.theme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

// Draws a status value (AVAILABLE, RESERVED, CANCELLED, ...) as a small
// coloured pill instead of plain text, so status is readable at a glance.
public class StatusBadgeRenderer extends DefaultTableCellRenderer {

    private static final JLabel METRICS_SOURCE = new JLabel();

    public StatusBadgeRenderer() {
        setHorizontalAlignment(CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                     boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
        label.setBackground(row % 2 == 0 ? Theme.CARD_WHITE : new Color(0xF5, 0xF7, 0xFA));
        if (isSelected) {
            label.setBackground(table.getSelectionBackground());
        }
        Color badgeColor = Theme.statusColor(value);
        String text = Theme.statusLabel(value);
        label.setIcon(new Badge(text, badgeColor));
        label.setText(null);
        return label;
    }

    private static class Badge implements Icon {
        private final String text;
        private final Color color;
        private final Font font = Theme.FONT_BOLD;

        Badge(String text, Color color) {
            this.text = text;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int width = textWidth + 18;
            int height = getIconHeight();
            g2.setColor(color);
            g2.fillRoundRect(x, y, width, height, height, height);
            g2.setColor(Color.WHITE);
            g2.drawString(text, x + 9, y + height - fm.getDescent() - 3);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            FontMetrics fm = METRICS_SOURCE.getFontMetrics(font);
            return fm.stringWidth(text) + 18;
        }

        @Override
        public int getIconHeight() {
            return 20;
        }
    }
}
