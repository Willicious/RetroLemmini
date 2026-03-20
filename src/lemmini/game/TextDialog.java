package lemmini.game;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import lemmini.graphics.GraphicsContext;
import lemmini.graphics.LemImage;

/*
 * Copyright 2009 Volker Oth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Class to create text screens which can be navigated with the mouse.
 * Uses {@link LemFont} as bitmap font.
 *
 * @author Volker Oth
 * Modified by Ryan Sakowski
 */
public class TextDialog {

    /** list of images */
    private final Map<String, List<TextDialogImage>> images;
    /** list of buttons */
    private final Map<String, List<Button>> buttons;

    private LemImage backgroundImage;
    private boolean tileBackground;

    /**
     * Create dialog text screen.
     */
    public TextDialog() {
        buttons = new LinkedHashMap<>();
        images = new LinkedHashMap<>();
    }

    /**
     * Clear the text screen.
     */
    public void clear() {
        synchronized (images) {
            images.clear();
        }
        synchronized (buttons) {
            buttons.clear();
        }
        backgroundImage = null;
    }

    /**
     * Draw the text screen to the given graphics object.
     */
    public void drawScreen(GraphicsContext g, int x, int y, int width, int height) {
        int widthHalf = width / 2;
        int heightHalf = height / 2;
        int centerX = widthHalf + x;
        int centerY = heightHalf + y;

        if (backgroundImage != null) {
            if (tileBackground) {
                int imageWidth = backgroundImage.getWidth();
                int imageHeight = backgroundImage.getHeight();
                int xMin = (widthHalf % imageWidth == 0) ? 0 : ((centerX - (widthHalf / imageWidth + 1) * imageWidth));
                int yMin = (heightHalf % imageHeight == 0) ? 0 : ((centerY - (heightHalf / imageHeight + 1) * imageHeight));
                for (int xa = xMin; xa < x + width; xa += imageWidth) {
                    for (int ya = yMin; ya < y + width; ya += imageHeight) {
                        g.drawImage(backgroundImage, xa, ya);
                    }
                }
            } else {
                int xa = x + (width - backgroundImage.getWidth()) / 2;
                int ya = y + (height - backgroundImage.getHeight()) / 2;
                g.drawImage(backgroundImage, xa, ya);
            }
        }
        synchronized (images) {
            images.keySet().stream().forEachOrdered(s -> {
                images.get(s).stream().forEachOrdered(img -> {
                    img.draw(g, centerX, centerY);
                });
            });
        }
        synchronized (buttons) {
            buttons.keySet().stream().forEachOrdered(s -> {
                buttons.get(s).stream().forEachOrdered(b -> {
                    b.draw(g, centerX, centerY);
                });
            });
        }
    }

    /**
     * Set Image as background.
     *              be centered.
     */
    public void setBackground(final LemImage image, final boolean tiled) {
        backgroundImage = image;
        tileBackground = true;
    }

    /**
     * Draw string.
     */
    public void addString(final String s, final String group,
            final int x0, final int y0, final LemFont.LemmColor col) {
        if (LemFont.getCharCount(s) <= 0) {
            return;
        }
        int width = LemFont.getWidth();
        int height = LemFont.getHeight();
        List<String> sa = LemFont.split(s, 0);
        synchronized (images) {
            addImageGroup(group);
            List<TextDialogImage> groupList = images.get(group);
            int x = x0 * width;
            for (ListIterator<String> lit = sa.listIterator(); lit.hasNext(); ) {
                int i = lit.nextIndex();
                String line = lit.next();
                if (LemFont.getCharCount(line) > 0) {
                    int y = (y0 + i) * (height + 4);
                    groupList.add(new TextDialogImage(line, x, y, col));
                }
            }
        }
    }

    /**
     * Draw string.
     */
    public void addString(final String s, final String group, final int x, final int y) {
        addString(s, group, x, y, LemFont.LemmColor.GREEN);
    }

    /**
     * Draw string horizontally centered.
     */
    public void addStringCentered(final String s, final String group,
            final int y0, final LemFont.LemmColor col) {
        if (LemFont.getCharCount(s) <= 0) {
            return;
        }
        int width = LemFont.getWidth();
        int height = LemFont.getHeight();
        List<String> sa = LemFont.split(s, Math.max(1, 800 / width));
        synchronized (images) {
            addImageGroup(group);
            List<TextDialogImage> groupList = images.get(group);
            for (ListIterator<String> lit = sa.listIterator(); lit.hasNext(); ) {
                int i = lit.nextIndex();
                String line = lit.next();
                int charCount = LemFont.getCharCount(line);
                if (charCount > 0) {
                    if (charCount % 2 > 0) {
                        charCount = (charCount + 2) - charCount % 2;
                    }
                    int y = (y0 + i) * (height + 4);
                    int x = -(charCount * width / 2);
                    groupList.add(new TextDialogImage(line, x, y, col));
                }
            }
        }
    }

    /**
     * Draw string horizontally centered.
     */
    public void addStringCentered(final String s, final String group, final int y) {
        addStringCentered(s, group, y, LemFont.LemmColor.GREEN);
    }

    /**
     * Add an image.
     */
    public void addImage(final LemImage img, final String group, final int x, final int y) {
        synchronized (images) {
            addImageGroup(group);
            images.get(group).add(new TextDialogImage(img, x, y));
        }
    }

    /**
     * Add a horizontally centered image.
     */
    public void addImage(final LemImage img, final String group, final int y) {
        int x = -(img.getWidth() / 2);
        synchronized (images) {
            addImageGroup(group);
            images.get(group).add(new TextDialogImage(img, x, y));
        }
    }

    /**
     * Add Button.
     */
    public void addButton(final LemImage img, final LemImage imgSelected, LemImage imgPressed, final String group,
            final int x, final int y, final TextScreen.Button type) {
        Button b = new Button(x, y, type);
        b.SetImage(img);
        b.SetImageSelected(imgSelected);
        b.SetImagePressed(imgPressed);
        synchronized (buttons) {
            addButtonGroup(group);
            buttons.get(group).add(b);
        }
    }

    /**
     * Add text button.
     */
    public void addTextButton(final String t, final String ts, final String group,
            final int x0, final int y0, final TextScreen.Button type,
            final LemFont.LemmColor textCol, final LemFont.LemmColor selectedCol) {
        int x = x0 * LemFont.getWidth();
        int y = y0 * (LemFont.getHeight() + 4);
        TextButton b = new TextButton(x, y, type);
        b.setText(t, textCol);
        b.setTextSelected(ts, selectedCol);
        synchronized (buttons) {
            addButtonGroup(group);
            buttons.get(group).add(b);
        }
    }

    /**
     * React on left click.
     */
    public TextScreen.Button handleLeftClick(final int x, final int y) {
        synchronized (buttons) {
            for (List<Button> bl : buttons.values()) {
                for (Button b : bl) {
                    if (b.inside(x, y)) {
                    	b.pressed = b.inside(x, y);
                        return b.type;
                    }
                }
            }
        }
        return TextScreen.Button.NONE;
    }
    
    public void handleMouseReleased() {
        synchronized (buttons) {
            for (List<Button> bl : buttons.values()) {
                for (Button b : bl) {
                    	b.pressed = false;
                }
            }
        }
    }

    /**
     * React on mouse hover.
     */
    public void handleMouseMove(final int x, final int y) {
        synchronized (buttons) {
            buttons.values().stream().forEach(bl -> {
                bl.stream().forEach(b -> {
                    b.selected = b.inside(x, y);
                });
            });
        }
    }

    public void clearGroup(String group) {
        synchronized (images) {
            if (images.containsKey(group)) {
                images.get(group).clear();
            }
        }
        synchronized (buttons) {
            if (buttons.containsKey(group)) {
                buttons.get(group).clear();
            }
        }
    }

    private void addImageGroup(String group) {
        if (!images.containsKey(group)) {
            images.put(group, new ArrayList<>(16));
        }
    }

    private void addButtonGroup(String group) {
        if (!buttons.containsKey(group)) {
            buttons.put(group, new ArrayList<>(16));
        }
    }
}

/**
 * Button class for TextDialog.
 * @author Volker Oth
 */
class Button {
	/** x coordinate in pixels */
    private final int x;
    /** y coordinate in pixels */
    private final int y;
    /** width in pixels */
    protected int width;
    /** height in pixels */
    protected int height;
    /** button type */
    protected TextScreen.Button type;
    /** true if button is selected */
    protected boolean selected;
    /** true if button is pressed */
    public boolean pressed;
    /** normal button image */
    protected LemImage image;
    /** selected button image */
    protected LemImage imgSelected;
    /** pressed button image */
    protected LemImage imgPressed;

    /**
     * Constructor
     */
    Button(final int xi, final int yi, final TextScreen.Button typei) {
        x = xi;
        y = yi;
        type = typei;
        width = 0;
        height = 0;
        selected = false;
        pressed = false;
        image = null;
        imgSelected = null;
        imgSelected = null;
    }

    /**
     * Set normal button image.
     */
    void SetImage(final LemImage img) {
        image = img;
        if (image.getHeight() > height) {
            height = image.getHeight();
        }
        if (image.getWidth() > width) {
            width = image.getWidth();
        }
    }

    /**
     * Set selected button image.
     */
    void SetImageSelected(final LemImage img) {
        imgSelected = img;
        if (imgSelected.getHeight() > height) {
            height = imgSelected.getHeight();
        }
        if (imgSelected.getWidth() > width) {
            width = imgSelected.getWidth();
        }
    }
    
    /**
     * Set selected button image.
     */
    void SetImagePressed(final LemImage img) {
        imgPressed = img;
        if (imgPressed.getHeight() > height) {
            height = imgPressed.getHeight();
        }
        if (imgPressed.getWidth() > width) {
            width = imgPressed.getWidth();
        }
    }

    /**
     * Return current button image (normal or selected, depending on state).
     */
    LemImage getImage() {
    	if (pressed) {
    		return imgPressed;
    	} else if (selected) {
            return imgSelected;
        } else {
            return image;
        }
    }

    /**
     * Draw the button.
     */
    void draw(final GraphicsContext g, final int cx, final int cy) {
        LemImage img = getImage();
        if (img != null) {
            g.drawImage(getImage(), cx + x, cy + y);
        }
    }

    /**
     * Check if a (mouse) position is inside this button.
     */
    boolean inside(final int xi, final int yi) {
        return (xi >= x && xi < x + width && yi >= y && yi < y + height);
    }
}

/**
 * Button class for TextDialog.
 * @author Volker Oth
 */
class TextButton extends Button {
    /**
     * Constructor
     */
    TextButton(final int xi, final int yi, final TextScreen.Button typei) {
        super(xi, yi, typei);
    }

    /**
     * Set text which is used as button.
     */
    void setText(final String s, final LemFont.LemmColor color) {
        if (LemFont.getCharCount(s) <= 0) {
            return;
        }
        image = LemFont.strImage(s, color);
        if (image.getHeight() > height) {
            height = image.getHeight();
        }
        if (image.getWidth() > width) {
            width = image.getWidth();
        }
    }

    /**
     * Set text for selected button.
     */
    void setTextSelected(final String s, final LemFont.LemmColor color) {
        if (LemFont.getCharCount(s) <= 0) {
            return;
        }
        imgSelected = LemFont.strImage(s, color);
        if (imgSelected.getHeight() > height) {
            height = imgSelected.getHeight();
        }
        if (imgSelected.getWidth() > width) {
            width = imgSelected.getWidth();
        }
    }
}

class TextDialogImage {

    /** x coordinate in pixels */
    private final int x;
    /** y coordinate in pixels */
    private final int y;
    /** string image */
    protected LemImage image;

    TextDialogImage(final LemImage img, final int xi, final int yi) {
        x = xi;
        y = yi;
        image = img;
    }

    TextDialogImage(final String text, final int xi, final int yi, final LemFont.LemmColor col) {
        x = xi;
        y = yi;
        image = LemFont.strImage(text, col);
    }

    /**
     * Draw the image.
     */
    void draw(final GraphicsContext g, final int cx, final int cy) {
        g.drawImage(image, cx + x, cy + y);
    }
}