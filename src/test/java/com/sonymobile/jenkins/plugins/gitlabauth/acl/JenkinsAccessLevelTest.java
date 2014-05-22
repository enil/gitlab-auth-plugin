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

package com.sonymobile.jenkins.plugins.gitlabauth.acl;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link JenkinsAccessLevel}.
 * 
 * @author Andreas Alanko
 */
public class JenkinsAccessLevelTest {
    private JenkinsAccessLevel[] accessLevels;
    
    @Before
    public void setUp() {
        accessLevels = JenkinsAccessLevel.values();
    }
    
    @Test
    public void testExistenceOfAccessLevels() {
        // We only have three access levels
        assertEquals(3, accessLevels.length);
        
        assertEquals(JenkinsAccessLevel.ANONYMOUS, accessLevels[0]);
        assertEquals(JenkinsAccessLevel.LOGGED_IN, accessLevels[1]);
        assertEquals(JenkinsAccessLevel.ADMIN, accessLevels[2]);
    }
    
    @Test
    public void testGetAccessLevelWithName() {
        assertEquals(JenkinsAccessLevel.ADMIN, JenkinsAccessLevel.getAccessLevelWithName("ADMIN"));
        assertEquals(JenkinsAccessLevel.ADMIN, JenkinsAccessLevel.getAccessLevelWithName("admin"));
        assertEquals(JenkinsAccessLevel.ADMIN, JenkinsAccessLevel.getAccessLevelWithName("AdMiN"));
        
        assertEquals(JenkinsAccessLevel.LOGGED_IN, JenkinsAccessLevel.getAccessLevelWithName("LOGGED_IN"));
        assertEquals(JenkinsAccessLevel.LOGGED_IN, JenkinsAccessLevel.getAccessLevelWithName("logged_in"));
        assertEquals(JenkinsAccessLevel.LOGGED_IN, JenkinsAccessLevel.getAccessLevelWithName("loGgEd_IN"));
        
        assertEquals(JenkinsAccessLevel.ANONYMOUS, JenkinsAccessLevel.getAccessLevelWithName("ANONYMOUS"));
        assertEquals(JenkinsAccessLevel.ANONYMOUS, JenkinsAccessLevel.getAccessLevelWithName("anonymous"));
        assertEquals(JenkinsAccessLevel.ANONYMOUS, JenkinsAccessLevel.getAccessLevelWithName("AnOnYmoUS"));
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void testGetAccessLevelWithNameException() {
        JenkinsAccessLevel.getAccessLevelWithName("non existing access level");
    }
    
    @Test
    public void testDisplayNames() {
        assertEquals("Admin", JenkinsAccessLevel.ADMIN.displayName);
        assertEquals("Logged In", JenkinsAccessLevel.LOGGED_IN.displayName);
        assertEquals("Anonymous", JenkinsAccessLevel.ANONYMOUS.displayName);
    }
}
