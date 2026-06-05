/*
 * Copyright 2026 Will James.
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
package lemmini.game;

import lemmini.tools.ToolBox;

/**
 * Checks currently-loaded replay for result
 * @author Will James
 */
public final class ReplayChecker {

    public enum ReplayResult {
        PASS,
        FAIL,
        UNDETERMINED
    }

    public static final int MAX_FRAMES = 200_000;

    public static ReplayResult check() {
    	ReplayResult result = ReplayResult.UNDETERMINED;    	
        try {
            prepareSimulation();
            for (int frame = 0; frame < MAX_FRAMES; frame++) {
                LemGame.update();

                result = LemGame.getReplayResult();

                if (result == ReplayResult.PASS) return ReplayResult.PASS;
                if (result == ReplayResult.FAIL) return ReplayResult.FAIL;
            }
            return ReplayResult.UNDETERMINED; // Simulation timed out
        } catch (Exception ex) {
        	ToolBox.showException(ex); // TODO: Add the exception message to the output
            return ReplayResult.UNDETERMINED; // Some error occurred
        } finally {
            // ALWAYS restore normal mode
            LemGame.setGameMode(LemGame.GameMode.NORMAL);
        }
    }

    private static void prepareSimulation() {
    	LemGame.setGameMode(LemGame.GameMode.REPLAY_CHECK);
        LemGame.setReplayResult(ReplayResult.UNDETERMINED);
        LemGame.setReplayMode(true);
    }
}