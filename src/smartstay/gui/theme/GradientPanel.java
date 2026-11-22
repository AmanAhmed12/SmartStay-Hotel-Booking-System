package smartstay.gui.theme;

import javax.swing.*;
import java.awt.*;

public class GradientPanel extends JPanel {

    private final Color top;
    private final Color bottom;
    private final boolean horizontal;

    public GradientPanel(Color top, Color bottom, boolean horizontal) {
        this.top = top;
        this.bottom = bottom;
        this.horizontal = horizontal;
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        if (horizontal) {
            g2.setPaint(new GradientPaint(0, 0, top, getWidth(), 0, bottom));
        } else {
            g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
        }
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        // deliberately skip super.paintComponent() - for an opaque JPanel it
        // would repaint the plain L&F background straight over our gradient
    }
}
