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

package com.sonymobile.jenkins.plugins.gitlab.gitlabauth.helpers;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.model.TopLevelItem;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

/**
 * A builder for creating mock instances of {@link TopLevelItem} objects.
 *
 * @param <ITEM>    the type of the item to be created
 * @param <BUILDER> the type of the builder returned for method chaining
 * @author Emil Nilsson
 */
public class MockTopLevelItemBuilder<ITEM extends TopLevelItem, BUILDER extends MockTopLevelItemBuilder> {
    /** The item type class. */
    private final Class<ITEM> itemClass;

    /** The item name. */
    private String name;

    /**
     * Creates a builder object.
     *
     * @param itemClass the class of the item type
     */
    public MockTopLevelItemBuilder(Class<ITEM> itemClass) {
        this.itemClass = itemClass;
    }

    /**
     * Creates a builder for a specific class.
     *
     * @param itemType   the class for the item type
     * @param <ITEMTYPE> type parameter of the item class
     * @return a builder
     */
    public static <ITEMTYPE extends TopLevelItem> MockTopLevelItemBuilder mockItem(Class<ITEMTYPE> itemType) {
        if (itemType == Folder.class) {
            return new MockFolderBuilder();
        } else {
            return new MockTopLevelItemBuilder<ITEMTYPE, MockTopLevelItemBuilder>(itemType);
        }
    }

    /**
     * Creates a new item of a specified type.
     *
     * @param itemType   the class for the item type
     * @param name       the name of the item
     * @param <ITEMTYPE> type parameter of the item class
     * @return the top level item
     */
    public static <ITEMTYPE extends TopLevelItem> ITEMTYPE item(Class<ITEMTYPE> itemType, String name) {
        return (ITEMTYPE)mockItem(itemType).name(name).build();
    }

    /**
     * Sets the item name.
     *
     * @param name the name
     * @return this object for chaining
     */
    public final BUILDER name(String name) {
        this.name = name;
        return (BUILDER)this;
    }

    /**
     * Creates a top level item for the item type.
     *
     * @return the top level item
     */
    public final ITEM build() {
        ITEM item = mock();
        replay(item);

        return item;
    }

    /**
     * Creates a top level mock item for item type.
     *
     * The item has to be replayed before use.
     *
     * @return the top level item
     */
    public final ITEM mock() {
        try {
            return createMockItem();
        } catch (Exception e) {
            // rethrow all exceptions as runtime exceptions
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a mock for the top level item of the item type.
     *
     * This method might be overridden in subclasses to add method expectations before the mock item is replayed.
     *
     * @return the top level item
     * @throws Exception if any error happened
     */
    protected ITEM createMockItem() throws Exception {
        ITEM item = createMock(itemClass);

        if (name != null) {
            expect(item.getName()).andReturn(name);
        }

        return item;
    }
}
