package smartstay.gui.theme;

import javax.swing.*;
import java.awt.*;

// A white rounded "card" with a soft drop shadow, used to group related
// controls on an otherwise plain grey page background.
public class CardPanel extends JPanel {

    private static final int ARC = 14;

    public CardPanel() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0, 0, 0, 20));
        g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 6, ARC, ARC);
        g2.setColor(Theme.CARD_WHITE);
        g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 5, ARC, ARC);
        g2.setColor(Theme.BORDER);
        g2.drawRoundRect(0, 0, getWidth() - 4, getHeight() - 6, ARC, ARC);
        g2.dispose();
        super.paintComponent(g);
    }
}
