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

package com.sonymobile.jenkins.plugins.gitlabauth.helpers;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.model.FreeStyleProject;
import hudson.model.TopLevelItem;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

/**
 * A helper class for creating mock instances of {@link TopLevelItem} objects for tests.
 *
 * @author Emil Nilsson
 */
public class MockTopLevelItemCreator {
    private MockTopLevelItemCreator() { /* empty */ }

    /**
     * Creates a mock {@link Folder} object.
     *
     * @param name the name of the folder
     * @return a mock folder
     */
    public static Folder createMockFolder(String name) {
        return createMockTopLevelItem(Folder.class, name);
    }

    /**
     * Creates a mock {@link FreeStyleProject} object.
     *
     * @param name the name of the project
     * @return a mock project
     */
    public static FreeStyleProject createMockFreeStyleProject(String name) {
        return createMockTopLevelItem(FreeStyleProject.class, name);
    }

    /**
     * Creates a mock object for a top level item.
     *
     * The item is capable of return its own name but not much more.
     *
     * @param type the class for the item
     * @param name the name of the item
     * @param <T>  the type
     * @return a mock top level item
     */
    public static <T extends TopLevelItem> T createMockTopLevelItem(Class<T> type, String name) {
        T item = createMock(type);
        // it must be able to return its name
        expect(item.getName()).andReturn(name).anyTimes();
        replay(item);

        return item;
    }
}
