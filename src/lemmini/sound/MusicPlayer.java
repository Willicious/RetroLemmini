/*
 * Copyright 2014 Ryan Sakowski.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package lemmini.sound;

import lemmini.game.LemmException;
import lemmini.game.Resource;
import lemmini.game.ResourceException;

public interface MusicPlayer {

    /**
     * Load file, initialize player.
     * @param res resource
     * @param loop
     * @throws ResourceException
     * @throws LemmException
     */
    void load(Resource resource, boolean loop) throws ResourceException, LemmException;

    void stop();
    void play();
    void close();
    void setGain(double gain);
}
