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
import com.cloudbees.hudson.plugins.folder.FolderProperty;
import hudson.model.Describable;
import hudson.util.DescribableList;
import org.easymock.IAnswer;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.replay;

/**
 * A builder for creating mock instances of {@link Folder} objects.
 *
 * @author Emil Nilsson
 */
public class MockFolderBuilder extends MockTopLevelItemBuilder<Folder, MockFolderBuilder> {
    /** The properties of the folder. */
    private final List<FolderProperty> folderProperties = new ArrayList<FolderProperty>();

    /**
     * Creates a builder object.
     */
    public MockFolderBuilder() {
        super(Folder.class);
    }

    /**
     * Creates a builder for the {@link Folder} class.
     *
     * @return a builder
     */
    public static MockFolderBuilder mockFolder() {
        return new MockFolderBuilder();
    }

    /**
     * Creates a new folder.
     *
     * @param name the name of the folder
     * @return the folder
     */
    public static Folder folder(String name) {
        return mockFolder().name(name).build();
    }

    /**
     * Adds a property to the property list.
     *
     * @param property the property
     * @return this object for chaining
     */
    public MockFolderBuilder addProperty(FolderProperty property) {
        folderProperties.add(property);
        return this;
    }

    /**
     * Gets the property matching a class from the property list.
     *
     * The method returns the first property which class inherits from the class.
     *
     * @param propertyClass the class to match
     * @return a property or null if the property isn't set
     */
    private FolderProperty getProperty(Class<FolderProperty> propertyClass) {
        for (final FolderProperty property : folderProperties) {
            if (propertyClass.isAssignableFrom(property.getClass())) {
                return property;
            }
        }
        return null;
    }

    @Override
    protected final Folder createMockItem() throws Exception {
        // mock the common item methods
        Folder folder = super.createMockItem();

        // mock the property list
        DescribableList propertyList = createMock(DescribableList.class);
        expect(folder.getProperties()).andReturn(propertyList);

        // mock adding properties
        folder.addProperty(anyObject(FolderProperty.class));
        expectLastCall().andAnswer(new IAnswer<Void>() {
            public Void answer() throws Throwable {
                addProperty((FolderProperty)getCurrentArguments()[0]);
                return null;
            }
        });

        // mock getting properties
        expect(propertyList.get(anyObject(Class.class))).andAnswer(new IAnswer<Describable>() {
            public Describable answer() throws Throwable {
                return getProperty((Class)getCurrentArguments()[0]);
            }
        });

        replay(propertyList);

        return folder;
    }
}
