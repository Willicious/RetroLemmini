package lemmini.game;

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
 * Generic exception class for problems during resource gathering.
 *
 * @author Volker Oth
 */
public class ResourceException extends Exception {

    private static final long serialVersionUID = 0x00000001L;

    /**
     * Constructor.
     */
    public ResourceException() {
        super();
    }

    /**
     * Constructor.
     * @param s Exception string
     */
    public ResourceException(final String s) {
        super(s);
    }

    /**
     * Constructor that accepts a resource.
     * @param res resource
     */
    public ResourceException(final Resource resource) {
        super(resource.getOriginalPath());
    }
}
