package lemmini.game;

import java.awt.Color;

import lemmini.graphics.GraphicsContext;
import lemmini.graphics.LemmImage;

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
 * Handle the nuke/bomber particle explosion.
 * @author Volker Oth
 */
public class Explosion {

    /** number of particles per explosion */
    private static final int PARTICLE_NUM = 24;
    /** maximum step width (velocity) in X direction (pixels per step) */
    private static final double MAX_DX =  1.5;
    /** minimum step width (velocity) in X direction (pixels per step) */
    private static final double MIN_DX = -1.5;
    /** maximum step width (velocity) in Y direction (pixels per step) */
    private static final double MAX_DY = 1;
    /** minimum step width (velocity) in Y direction (pixels per step) */
    private static final double MIN_DY = -4;
    /** mean life time of a particle (in animation frames) */
    private static final int LIFE_COUNTER = 64;
    /** life time variance of a particle (in animation frames) */
    private static final int LIFE_VARIANCE = 16;
    /** factor used to simulate gravity (drags particles down) */
    private static final double GRAVITY = 0.1;
    /** Remove the explosion bitmaps after REMOVE_IMAGE_CTR animation steps */
    private static final int REMOVE_IMAGE_CTR = 2;
    private static final int PARTICLE_SIZE = 2;

    /** array of particles */
    private final Particle[] particles;
    /** time/frame counter for explosion */
    private int counter;
    /** x position in pixels */
    private final int xExp;
    /** y position in pixels */
    private final int yExp;
    /** time/frame position when all particles are vanished */
    private int maxCounter;
    /** flag: explosion is finished */
    private boolean finished;
    /** explosion image used for the first few frames */
    private static LemmImage expImg;

    /**
     * Load explosion image as static resource.
     * Mainly outside constructor for easier handling of ResourceException.
     * @throws ResourceException
     */
    static void init() throws ResourceException {
        Resource resource = Core.findResource("gfx/misc/explode.png", Core.IMAGE_EXTENSIONS);
        expImg = Core.loadLemmImage(resource);
    }

    /**
     * Constructor.
     * @param x x position in pixels.
     * @param y y position in pixels.
     */
    public Explosion(final int x, final int y) {
        xExp = x - expImg.getWidth() / 2;
        yExp = y - expImg.getHeight() / 2;
        maxCounter = 0;
        particles = new Particle[PARTICLE_NUM];
        for (int i = 0; i < particles.length; i++) {
            double dx = (Math.random() * (MAX_DX - MIN_DX) + MIN_DX);
            double dy = (Math.random() * (MAX_DY - MIN_DY) + MIN_DY);
            int color = GameController.getLevel().getParticleCol()[(int) (Math.random() * GameController.getLevel().getParticleCol().length)];
            int lifeCtr = LIFE_COUNTER + (int) (Math.random() * 2 * LIFE_VARIANCE) - LIFE_VARIANCE;
            if (lifeCtr > maxCounter) {
                maxCounter = lifeCtr;
            }
            particles[i] = new Particle(x, y, dx, dy, color, lifeCtr);
        }
        counter = 0;
        finished = false;
    }

    /**
     * Update explosion (move particles etc.).
     */
    public void update() {
        if (counter > REMOVE_IMAGE_CTR) {
            for (int i = 0; i < particles.length; i++) {
                Particle p = particles[i];
                if (p != null) {
                    // calculate new position
                    p.x += p.dx;
                    p.y += p.dy + counter * GRAVITY;
                    // check life counter
                    if (p.lifeCtr > 0) {
                        p.lifeCtr--;
                    } else {
                        particles[i] = null;
                    }
                }
            }
        }
        if (++counter > maxCounter) {
            finished = true;
        }
    }

    /**
     * Draw explosion on graphics object.
     * @param g
     * @param width
     * @param height
     * @param xOfs
     * @param yOfs
     */
    public void draw(final GraphicsContext g, final int width, final int height, final int xOfs, final int yOfs) {
        if (!finished) {
            int maxY = height - 1;
            int maxX = width - 1;
            // draw explosion bitmap
            if (counter <= REMOVE_IMAGE_CTR) {
                int x = xExp - xOfs;
                int y = yExp - yOfs;
                if (x + expImg.getWidth() > 0 && x <= maxX && y + expImg.getHeight() > 0 && y <= maxY) {
                    g.drawImage(expImg, x, y);
                }
            } else {
                // draw particles
                for (Particle p : particles) {
                    if (p != null) {
                        // draw
                        int x = (int) p.x - xOfs;
                        int y = (int) p.y - yOfs;
                        if (x + PARTICLE_SIZE > 0 && x <= maxX && y + PARTICLE_SIZE > 0 && y <= maxY) {
                            g.setColor(p.color);
                            g.fillRect(x, y, 2, 2);
                        }
                    }
                }
            }
        }
    }

    /**
     * Get finished state.
     * @return true if the explosion is over, false otherwise
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Storage class for a particle.
     * @author Volker Oth
     */
    private class Particle {
        /** x position in pixels */
        double x;
        /** y position in pixels */
        double y;
        /** x step width (velocity) in pixels per step */
        double dx;
        /** y step width (velocity) in pixels per step */
        double dy;
        /** particle color */
        Color color;
        /** life counter in steps (counting down) */
        int lifeCtr;

        /**
         * Constructor
         * @param x0 initial x position in pixels
         * @param y0 initial y position in pixels
         * @param dx0 x step width (velocity) in pixels per step
         * @param dy0 y step width (velocity) in pixels per step
         * @param col particle color
         * @param lCtr life counter in steps (counting down)
         */
        Particle(double x0, double y0, double dx0, double dy0, int col, int lCtr) {
            x = x0;
            y = y0;
            dx = dx0;
            dy = dy0;
            color = new Color(col);
            lifeCtr = lCtr;
        }
    }
}
