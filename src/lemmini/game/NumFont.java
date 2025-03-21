package lemmini.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lemmini.graphics.GraphicsContext;
import lemmini.graphics.LemmImage;
import lemmini.tools.ToolBox;

/*
 * FILE MODIFIED BY RYAN SAKOWSKI
 *
 *
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
 * Handle small number font.
 * Meant to print out values between 0 and 99.
 *
 * @author Volker Oth
 */
public class NumFont {

    /** width in pixels */
    private static int width;
    /** height in pixels */
    private static int height;
    /** list of images - one for each digit 0-9 */
    private static final List<LemmImage> NUM_IMG = new ArrayList<>(15);
    private static final Map<Integer, LemmImage> NUM_IMG_MAP = new HashMap<>();

    /**
     * Load and initialize the font.
     * @throws ResourceException
     */
    public static void init() throws ResourceException {
        Resource resource = Core.findResource("gfx/misc/numfont.png", Core.IMAGE_EXTENSIONS);
        NUM_IMG.clear();
        LemmImage sourceImg = Core.loadLemmImage(resource);
        width = sourceImg.getWidth();
        height = sourceImg.getHeight() / 10;
        List<LemmImage> numImgTemp = ToolBox.getAnimation(sourceImg, 10);
        NUM_IMG.addAll(numImgTemp);
        resource = Core.findResource("gfx/misc/numfont2.png", Core.IMAGE_EXTENSIONS);
        sourceImg = Core.loadLemmImage(resource);
        numImgTemp = ToolBox.getAnimation(sourceImg, 5);
        NUM_IMG.addAll(numImgTemp);

        NUM_IMG_MAP.clear();
        LemmImage numImgTemp2;
        GraphicsContext g = null;
        for (int i = 0; i < 100; i++) {
            numImgTemp2 = ToolBox.createLemmImage(width * 2, height);
            try {
                g = numImgTemp2.createGraphicsContext();
                g.drawImage(NUM_IMG.get(i / 10), 0, 0);
                g.drawImage(NUM_IMG.get(i % 10), width, 0);
            } finally {
                if (g != null) {
                    g.dispose();
                }
            }
            NUM_IMG_MAP.put(i, numImgTemp2);
        }
        numImgTemp2 = ToolBox.createLemmImage(width * 2, height);
        try {
            g = numImgTemp2.createGraphicsContext();
            g.drawImage(NUM_IMG.get(13), 0, 0);
            g.drawImage(NUM_IMG.get(14), width, 0);
        } finally {
            if (g != null) {
                g.dispose();
            }
        }
        NUM_IMG_MAP.put(Integer.MAX_VALUE, numImgTemp2);
        numImgTemp2 = ToolBox.createLemmImage(width * 3, height);
        try {
            g = numImgTemp2.createGraphicsContext();
            g.drawImage(NUM_IMG.get(10), 0, 0);
            g.drawImage(NUM_IMG.get(13), width, 0);
            g.drawImage(NUM_IMG.get(14), width * 2, 0);
        } finally {
            if (g != null) {
                g.dispose();
            }
        }
        NUM_IMG_MAP.put(Integer.MIN_VALUE, numImgTemp2);
        numImgTemp2 = ToolBox.createLemmImage(width * 2, height);
        try {
            g = numImgTemp2.createGraphicsContext();
            g.drawImage(NUM_IMG.get(10), 0, 0);
            g.drawImage(NUM_IMG.get(10), width, 0);
        } finally {
            if (g != null) {
                g.dispose();
            }
        }
        NUM_IMG_MAP.put(null, numImgTemp2);
    }

    /**
     * Get an image for a number
     * @param n number
     * @return image of the number
     */
    public static LemmImage numImage(Integer n) {
        LemmImage numImgTemp = NUM_IMG_MAP.get(n);
        if (numImgTemp != null) {
            return numImgTemp;
        } else {
            String numString = n.toString();
            numImgTemp = ToolBox.createLemmImage(width * numString.length(), height);
            GraphicsContext g = null;
            try {
                g = numImgTemp.createGraphicsContext();
                for (int i = 0; i < numString.length(); i++) {
                    int numIndex = -1;
                    switch (numString.charAt(i)) {
                        case '-':
                            numIndex = 10;
                            break;
                        case '0':
                            numIndex = 0;
                            break;
                        case '1':
                            numIndex = 1;
                            break;
                        case '2':
                            numIndex = 2;
                            break;
                        case '3':
                            numIndex = 3;
                            break;
                        case '4':
                            numIndex = 4;
                            break;
                        case '5':
                            numIndex = 5;
                            break;
                        case '6':
                            numIndex = 6;
                            break;
                        case '7':
                            numIndex = 7;
                            break;
                        case '8':
                            numIndex = 8;
                            break;
                        case '9':
                            numIndex = 9;
                            break;
                    }
                    if (numIndex >= 0) {
                        g.drawImage(NUM_IMG.get(numIndex), width * i, 0);
                    }
                }
            } finally {
                if (g != null) {
                    g.dispose();
                }
            }
            NUM_IMG_MAP.put(n, numImgTemp);
            return numImgTemp;
        }
    }
}
