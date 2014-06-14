/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Andreas Alanko, Emil Nilsson, Sony Mobile Communications AB. 
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.sonymobile.jenkins.plugins.gitlab.gitlabauth.acl;

/**
 * Enum class for Jenkins access levels.
 * 
 * @author Andreas Alanko
 */
public enum JenkinsAccessLevel {
    /** Anonumous access level. */
    ANONYMOUS("Anonymous"),
    
    /** Logged in access level. */
    LOGGED_IN("Logged In"),
    
    /** Administrator access level. */
    ADMIN("Admin");
    
    /** Displayable name */
    public final String displayName;
    
    /**
     * Creates a Jenkins access level enum with the given display name.
     * 
     * @param displayName the display name
     */
    private JenkinsAccessLevel(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Returns the access level with the given name.
     * 
     * The method doesn't take upper or lower case into consideration.
     * 
     * @param name the name
     * @return a Jenkins access level
     */
    public static JenkinsAccessLevel getAccessLevelWithName(String name) {
        name = name.toUpperCase();
        for (JenkinsAccessLevel accessLevel : JenkinsAccessLevel.values()) {
            if (name.equals(accessLevel.name())) {
                return accessLevel;
            }
        }
        throw new IllegalArgumentException("Invalid access level name"); 
    }
}
