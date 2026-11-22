package smartstay.gui.theme;

import javax.swing.*;
import java.awt.*;

// Sidebar navigation entry: flat, full-width, highlights a gold accent bar
// on the left edge when it is the active section.
public class NavButton extends JToggleButton {

    public NavButton(String text, Icon icon) {
        super(text, icon);
        setHorizontalAlignment(LEFT);
        setForeground(Color.WHITE);
        setFont(Theme.FONT_BOLD);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setIconTextGap(12);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 12));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        if (isSelected()) {
            g2.setColor(new Color(255, 255, 255, 25));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(Theme.GOLD);
            g2.fillRect(0, 0, 4, getHeight());
        } else if (getModel().isRollover()) {
            g2.setColor(new Color(255, 255, 255, 12));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
