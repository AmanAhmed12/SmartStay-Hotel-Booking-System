package smartstay.gui.theme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

// Applies one consistent look (header colour, striped rows, row height,
// selection colour) to every JTable in the app instead of repeating it.
public final class TableStyler {

    private TableStyler() {
    }

    public static void style(JTable table) {
        table.setRowHeight(28);
        table.setFont(Theme.FONT_BASE);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(0x1B, 0x6E, 0x7A, 60));
        table.setSelectionForeground(Theme.TEXT_DARK);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(Theme.FONT_BOLD);
        header.setBackground(Theme.NAVY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 32));
        header.setDefaultRenderer(new HeaderRenderer());

        table.setDefaultRenderer(Object.class, new StripedRenderer());
    }

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer() {
            setHorizontalAlignment(LEFT);
            setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBackground(Theme.NAVY);
            setForeground(Color.WHITE);
            setFont(Theme.FONT_BOLD);
            return this;
        }
    }

    private static class StripedRenderer extends DefaultTableCellRenderer {
        StripedRenderer() {
            setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setFont(Theme.FONT_BASE);
            if (isSelected) {
                label.setBackground(table.getSelectionBackground());
            } else {
                label.setBackground(row % 2 == 0 ? Theme.CARD_WHITE : new Color(0xF5, 0xF7, 0xFA));
            }
            label.setForeground(Theme.TEXT_DARK);
            return label;
        }
    }
}
