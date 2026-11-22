package smartstay.gui.theme;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

// All icons here are drawn in code with Java2D rather than loaded from image
// files, so the project has no external image assets to go missing.
public final class Icons {

    private Icons() {
    }

    private interface Painter {
        void paint(Graphics2D g, int size, Color color);
    }

    private static Icon build(int size, Color color, Painter painter) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(Math.max(1.5f, size / 8f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        painter.paint(g, size, color);
        g.dispose();
        return new ImageIcon(image);
    }

    public static Icon add(Color color) {
        return build(16, color, (g, s, c) -> {
            g.setColor(c);
            int m = s / 4;
            g.drawLine(s / 2, m, s / 2, s - m);
            g.drawLine(m, s / 2, s - m, s / 2);
        });
    }

    public static Icon edit(Color color) {
        return build(16, color, (g, s, c) -> {
            g.setColor(c);
            g.drawLine(3, s - 4, s - 5, 3);
            g.fillOval(s - 6, 1, 4, 4);
            g.drawLine(2, s - 2, 4, s - 4);
        });
    }

    public static Icon delete(Color color) {
        return build(16, color, (g, s, c) -> {
            g.setColor(c);
            g.drawRoundRect(3, 5, s - 6, s - 6, 2, 2);
            g.drawLine(2, 4, s - 2, 4);
            g.drawLine(6, 2, s - 6, 2);
            g.drawLine(s / 2, 7, s / 2, s - 4);
        });
    }

    public static Icon search(Color color) {
        return build(16, color, (g, s, c) -> {
            g.setColor(c);
            g.drawOval(2, 2, s - 7, s - 7);
            g.drawLine(s - 6, s - 6, s - 1, s - 1);
        });
    }

    public static Icon cancelMark(Color color) {
        return build(16, color, (g, s, c) -> {
            g.setColor(c);
            g.drawLine(3, 3, s - 3, s - 3);
            g.drawLine(s - 3, 3, 3, s - 3);
        });
    }

    public static Icon invoice(Color color) {
        return build(16, color, (g, s, c) -> {
            g.setColor(c);
            g.drawRoundRect(3, 1, s - 7, s - 3, 2, 2);
            g.setStroke(new BasicStroke(1f));
            for (int y = 5; y < s - 3; y += 3) {
                g.drawLine(5, y, s - 5, y);
            }
        });
    }

    public static Icon guest(Color color) {
        return build(16, color, (g, s, c) -> {
            g.setColor(c);
            g.fillOval(s / 2 - 3, 1, 6, 6);
            g.drawArc(2, s - 8, s - 4, 10, 0, 180);
        });
    }

    public static Icon bed(Color color) {
        return build(16, color, (g, s, c) -> {
            g.setColor(c);
            g.drawLine(2, s - 3, 2, 3);
            g.drawLine(2, 3, 2, 3);
            g.drawRoundRect(2, s / 2, s - 4, s / 2 - 3, 2, 2);
            g.drawLine(2, s / 2, 2, 2);
            g.drawLine(2, s / 2, s - 2, s / 2);
            g.drawLine(s - 2, s / 2, s - 2, s - 3);
        });
    }

    public static Icon calendar(Color color) {
        return build(16, color, (g, s, c) -> {
            g.setColor(c);
            g.drawRoundRect(2, 3, s - 4, s - 5, 2, 2);
            g.drawLine(2, 7, s - 2, 7);
            g.drawLine(5, 1, 5, 4);
            g.drawLine(s - 5, 1, s - 5, 4);
        });
    }

    public static Icon dashboard(Color color) {
        return build(16, color, (g, s, c) -> {
            g.setColor(c);
            int gap = 2, cell = (s - gap * 3) / 2;
            g.fillRect(gap, gap, cell, cell);
            g.fillRect(gap * 2 + cell, gap, cell, cell);
            g.fillRect(gap, gap * 2 + cell, cell, cell);
            g.fillRect(gap * 2 + cell, gap * 2 + cell, cell, cell);
        });
    }

    public static Icon checklist(Color color) {
        return build(16, color, (g, s, c) -> {
            g.setColor(c);
            g.drawRoundRect(2, 1, s - 4, s - 2, 2, 2);
            g.drawLine(5, 5, 6, 6);
            g.drawLine(6, 6, 9, 3);
            g.drawLine(5, 10, s - 4, 10);
        });
    }

    public static Icon logo(int size, Color buildingColor, Color accentColor) {
        return build(size, buildingColor, (g, s, c) -> {
            g.setColor(c);
            g.setStroke(new BasicStroke(Math.max(2f, s / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int margin = s / 6;
            g.draw(new RoundRectangle2D.Float(margin, margin * 1.4f, s - margin * 2f, s - margin * 2.1f, s / 6f, s / 6f));
            g.setColor(accentColor);
            g.fill(new Ellipse2D.Float(s / 2f - s / 10f, margin * 0.3f, s / 5f, s / 5f));
            int cols = 2, rows = 2;
            float cellW = (s - margin * 2f) / 4f;
            float cellH = (s - margin * 2.1f) / 4f;
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    float x = margin + cellW * 0.6f + col * cellW * 1.6f;
                    float y = margin * 1.4f + cellH * 0.6f + row * cellH * 1.6f;
                    g.fill(new RoundRectangle2D.Float(x, y, cellW * 0.8f, cellH * 0.8f, 2, 2));
                }
            }
        });
    }
}
