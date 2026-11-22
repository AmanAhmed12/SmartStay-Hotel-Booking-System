package smartstay.gui.theme;

import javax.swing.*;
import java.awt.*;

// A flat, rounded button that lightens on hover and darkens while pressed -
// plain JButtons look dated next to the rest of the themed UI.
public class RoundedButton extends JButton {

    private final Color baseColor;

    public RoundedButton(String text, Icon icon, Color baseColor) {
        super(text, icon);
        this.baseColor = baseColor;
        setForeground(Color.WHITE);
        setFont(Theme.FONT_BOLD);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        setIconTextGap(8);
    }

    public RoundedButton(String text, Color baseColor) {
        this(text, null, baseColor);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color fill = baseColor;
        if (getModel().isPressed()) {
            fill = fill.darker();
        } else if (getModel().isRollover()) {
            fill = brighten(fill, 0.15);
        }
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        g2.dispose();
        super.paintComponent(g);
    }

    private static Color brighten(Color c, double amount) {
        int r = (int) Math.min(255, c.getRed() + 255 * amount);
        int g = (int) Math.min(255, c.getGreen() + 255 * amount);
        int b = (int) Math.min(255, c.getBlue() + 255 * amount);
        return new Color(r, g, b);
    }
}
