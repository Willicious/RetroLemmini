package lemmini.graphics;

import javax.swing.*;
import javax.swing.plaf.basic.BasicMenuItemUI;

import lemmini.LemminiFrame.ColorPalette;
import lemmini.LemminiFrame.FrameColor;

import java.awt.*;

public class LemMenuItem extends BasicMenuItemUI {
	
    private static final Color bgColor = ColorPalette.getColor(FrameColor.DEEP_BLUE);
    private static final Color fgColor = ColorPalette.getColor(FrameColor.MENU_TEXT);
    private static final Color hkColor = ColorPalette.getColor(FrameColor.CHALK);

    private static final int ICON_X = 8;
    private static final int TEXT_X = 52;

    @Override
    protected void paintMenuItem(
            Graphics g,
            JComponent c,
            Icon checkIcon,
            Icon arrowIcon,
            Color background,
            Color foreground,
            int defaultTextIconGap
    ) {
        JMenuItem menuItem = (JMenuItem) c;

        Graphics2D g2 = (Graphics2D) g.create();

        // Background
        if (menuItem.getModel().isArmed()) {
            g2.setColor(ColorPalette.getColor(FrameColor.HIGHLIGHT_BLUE));
        } else {
            g2.setColor(bgColor);
        }

        g2.fillRect(0, 0, menuItem.getWidth(), menuItem.getHeight());

        // Icon
        Icon icon = menuItem.getIcon();
        if (icon != null) {
            int y = (menuItem.getHeight() - icon.getIconHeight()) / 2;
            icon.paintIcon(menuItem, g2, ICON_X, y);
        }

        // Text
        String text = menuItem.getText();
        if (text != null) {
            g2.setFont(menuItem.getFont());
        	FontMetrics fm = g2.getFontMetrics();

            int y = (menuItem.getHeight() - fm.getHeight()) / 2 + fm.getAscent();

            g2.setColor(fgColor);
            antialiasText(g2);
            g2.drawString(text, TEXT_X, y);
        }

        // Accelerator
        String acceleratorText = (String) menuItem.getClientProperty("HotkeyString");

        if (acceleratorText != null) {
            FontMetrics fm = g2.getFontMetrics();
            
            int x = menuItem.getWidth() - fm.stringWidth(acceleratorText) - 10;
            int y = (menuItem.getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            
            g2.setColor(hkColor);
            antialiasText(g2);
            g2.drawString(acceleratorText, x, y);
        }

        g2.dispose();
    }
    
    @Override
    protected Dimension getPreferredMenuItemSize(
            JComponent c,
            Icon checkIcon,
            Icon arrowIcon,
            int defaultTextIconGap
    ) {
        JMenuItem menuItem = (JMenuItem) c;

        FontMetrics fm = menuItem.getFontMetrics(menuItem.getFont());

        int height = Math.max(
                menuItem.getIcon() != null ? menuItem.getIcon().getIconHeight() : 0,
                fm.getHeight()
        );

        // Add vertical padding
        height += 16;

        int width = TEXT_X + fm.stringWidth(menuItem.getText());

        // Reserve space for accelerator hotkey text
        width += 48 + getHotkeyWidth(menuItem, fm);
        
        return new Dimension(width, height);
    }
    
    private int getHotkeyWidth(JMenuItem menuItem, FontMetrics fm) {
        String acceleratorText =
                (String) menuItem.getClientProperty("HotkeyString");

        if (acceleratorText == null) {
            return 0;
        }

        return fm.stringWidth(acceleratorText);
    }
    
    private void antialiasText(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
    }
}
