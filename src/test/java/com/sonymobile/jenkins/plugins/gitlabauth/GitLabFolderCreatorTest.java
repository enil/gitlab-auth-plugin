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

package com.sonymobile.jenkins.plugins.gitlabauth;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.sonymobile.gitlab.model.GitLabGroupInfo;
import com.sonymobile.jenkins.plugins.gitlabauth.exceptions.ItemNameCollisionException;
import hudson.model.FreeStyleProject;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import jenkins.model.ModifiableTopLevelItemGroup;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.GitLabModelDataCreator.createGroupInfo;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * Test class for {@link GitLabFolderCreator}.
 *
 * @author Emil Nilsson
 */
public class GitLabFolderCreatorTest {
    /** A rule for catching expected exceptions. */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /** A GitLab folder. */
    private final Folder folder = createMockTopLevelItem(Folder.class, "foldername");

    /** A item. */
    private final TopLevelItem item = createMockTopLevelItem(FreeStyleProject.class, "foldername");

    /** A GitLab group. */
    private final GitLabGroupInfo group = createGroupInfo(1, "Folder Name", "foldername");

    /** The descriptor for a Folder item. */
    private final TopLevelItemDescriptor folderDescriptor = new Folder.DescriptorImpl();

    /** The folder creator. */
    private GitLabFolderCreator folderCreator;

    /** A mock item group to create folders in. */
    private ModifiableTopLevelItemGroup mockItemGroup;

    /**
     * Prepare the folder creator for testing.
     */
    @Before
    public void setUp() {
        // create mock for the item group
        mockItemGroup = createMock(ModifiableTopLevelItemGroup.class);

        // create the test target using the mock item group
        folderCreator = new GitLabFolderCreator(mockItemGroup, folderDescriptor);
    }

    /**
     * Tests creating a folder when the folder isn't already present in the item group.
     */
    @Test
    public void whenNotPresent() throws Exception {
        // mock no already existing item with the same name
        expect(mockItemGroup.getItem("foldername")).andReturn(null).anyTimes();
        expect(mockItemGroup.createProject(folderDescriptor, "foldername", true)).
                andReturn(folder);
        replay(mockItemGroup);

        // should create a new group
        Folder returnedFolder = folderCreator.createOrGetGitLabGroup(group);

        assertThat("Name should be set to the group path", returnedFolder.getName(), is("foldername"));

        verify(mockItemGroup);
    }

    /**
     * Tests creating a folder when the folder already is present in the item group.
     */
    @Test
    public void whenAlreadyPresent() throws Exception {
        // mock an already existing folder with the same name
        Folder folder = createMockTopLevelItem(Folder.class, "foldername");

        expect(mockItemGroup.getItem("foldername")).
                andReturn(folder).anyTimes();
        replay(mockItemGroup);

        // should return existing group
        Folder returnedFolder = folderCreator.createOrGetGitLabGroup(group);

        assertThat("Existing folder should be returned", returnedFolder, is(sameInstance(folder)));

        verify(mockItemGroup);
    }

    /**
     * Tests creating a folder when the folder name collides with an item present in the item group.
     */
    @Test
    public void whenNameCollides() throws Exception {
        // mock an already existing item with the same name
        expect(mockItemGroup.getItem("foldername")).
                andReturn(item).anyTimes();
        replay(mockItemGroup);

        // should throw exception
        thrown.expect(ItemNameCollisionException.class);
        folderCreator.createOrGetGitLabGroup(group);

        verify(mockItemGroup);
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
    private static <T extends TopLevelItem> T createMockTopLevelItem(Class<T> type, String name) {
        T item = createMock(type);
        // it must be able to return its name
        expect(item.getName()).andReturn(name).anyTimes();
        replay(item);

        return item;
    }
}
