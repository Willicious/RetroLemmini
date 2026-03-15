package lemmini.graphics;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.Icon;

public class LemmIcon implements Icon {

    private final Image img10x;
    private final Image img15x;
    private final Image img20x;

    public LemmIcon(Image img10x, Image img15x, Image img20x) {
        this.img10x = img10x;
        this.img15x = img15x;
        this.img20x = img20x;
    }

    @Override
    public int getIconWidth() {
        return 32;
    }

    @Override
    public int getIconHeight() {
        return 32;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        double scale = g2.getTransform().getScaleX();
        Image img;
        double baseScale;

        if (scale >= 2.0 && img20x != null) {
        	img = img20x;
        	baseScale = 2.0;
        } else if (scale == 1.5 && img15x != null) {
            img = img15x;
            baseScale = 1.5;
        } else {
            img = img10x;
            baseScale = 1.0;
        }

        // Cancel Swing's scaling
        g2.scale(1.0 / scale, 1.0 / scale);

        // Compute the extra scaling needed
        double extraScale = scale / baseScale;

        int drawX = (int) Math.round(x * scale);
        int drawY = (int) Math.round(y * scale);

        int w = (int) Math.round(img.getWidth(null) * extraScale);
        int h = (int) Math.round(img.getHeight(null) * extraScale);

        g2.drawImage(img, drawX, drawY, w, h, null);

        g2.dispose();
    }
}