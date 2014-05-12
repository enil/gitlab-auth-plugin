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

package com.sonymobile.jenkins.plugins.gitlabauth.folder;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.sonymobile.gitlab.model.GitLabGroupInfo;
import com.sonymobile.jenkins.plugins.gitlabauth.authorization.GitLabFolderAuthorization;
import com.sonymobile.jenkins.plugins.gitlabauth.exceptions.ItemNameCollisionException;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import jenkins.model.ModifiableTopLevelItemGroup;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.MockDataCreators.mockFolderAuthorization;
import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.MockDataCreators.mockGroupInfo;
import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.MockFolderBuilder.folder;
import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.MockFolderBuilder.mockFolder;
import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.MockFreeStyleProjectBuilder.freeStyleProject;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.hasEntry;
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

    /** The descriptor for a Folder item. */
    private static final TopLevelItemDescriptor folderDescriptor = new Folder.DescriptorImpl();

    /** A GitLab group. */
    private static final GitLabGroupInfo group = mockGroupInfo(1, "Folder Name", "foldername");

    /** A GitLab folder. */
    private Folder gitLabFolder;

    /** A miscellaneous folder. */
    private Folder folder;

    /** A miscellaneous top level item. */
    private TopLevelItem item;

    /** The map of existing folders (as returned from {@link GitLabFolderCreator#getExistingGitLabGroupFolders()}). */
    Map<Integer, Folder> existingFolders;

    /** The folder creator. */
    private GitLabFolderCreator folderCreator;

    /** A mock item group to create folders in. */
    private ModifiableTopLevelItemGroup itemGroup;

    /** Items in the mock item group. */
    private List<TopLevelItem> items;

    /**
     * Prepare the folder creator for testing.
     */
    @Before
    public void setUp() {
        // clear items list
        items = new LinkedList<TopLevelItem>();
        // clear map with existing GitLab folders
        existingFolders = new HashMap<Integer, Folder>();

        // create mock items
        gitLabFolder = mockFolder()
                .name("foldername")
                .addProperty(mockFolderAuthorization(/* groupId */ 1))
                .build();

        folder = folder("foldername");

        item = freeStyleProject("foldername");

        // mock itemGroup#getGetItems() returning the items list
        itemGroup = createMock(ModifiableTopLevelItemGroup.class);
        expect(itemGroup.getItems()).andReturn(items).anyTimes();

        folderCreator = new GitLabFolderCreator(itemGroup, folderDescriptor);
    }

    /**
     * Tests creating a folder when the folder isn't already present in the item group.
     */
    @Test
    public void createWhenNotPresent() throws Exception {
        expect(itemGroup.createProject(folderDescriptor, "foldername", true)).
                andReturn(folder);
        replay(itemGroup);

        Folder returnedFolder = folderCreator.createOrGetGitLabGroupFolder(group, existingFolders);

        // verify that itemGroup#createProject was called
        verify(itemGroup);

        // get property for the GitLab group
        GitLabFolderAuthorization folderProperty = returnedFolder.getProperties().get(GitLabFolderAuthorization.class);

        assertThat("name not set correctly", returnedFolder.getName(), is("foldername"));
        assertThat("folder property not set", folderProperty, is(notNullValue()));
        assertThat("group ID not set correctly", folderProperty.getGroupId(), is(1));
    }

    /**
     * Tests creating a folder when the folder already is present in the item group.
     */
    @Test
    public void createWhenAlreadyPresent() throws Exception {
        existingFolders.put(/* groupId */ 1, gitLabFolder);

        replay(itemGroup);

        Folder returnedFolder = folderCreator.createOrGetGitLabGroupFolder(group, existingFolders);

        // verify that itemGroup#createProject wasn't called
        verify(itemGroup);

        assertThat("not returning existing folder", returnedFolder, is(sameInstance(gitLabFolder)));
    }

    /**
     * Tests creating a folder when the folder name collides with an item present in the item group.
     */
    @Test
    public void createWhenNameCollides() throws Exception {
        expect(itemGroup.createProject(folderDescriptor, "foldername", true)).
                andThrow(new IllegalArgumentException(EMPTY));
        replay(itemGroup);

        // should throw exception
        thrown.expect(ItemNameCollisionException.class);
        folderCreator.createOrGetGitLabGroupFolder(group, existingFolders);

        // verify that itemGroup#createProject wasn't called
        verify(itemGroup);
    }

    /**
     * Tests getting the existing GitLab folders from an item group with a single GitLab folder.
     */
    @Test
    public void getWithGitLabFolder() {
        // item group has one existing GitLab folder
        items.add(gitLabFolder);
        replay(itemGroup);

        Map<Integer, Folder> existingFolders = folderCreator.getExistingGitLabGroupFolders();

        verify(itemGroup);

        assertThat("existing GitLab folder not matched", existingFolders, hasEntry(1, gitLabFolder));
    }

    /**
     * Tests getting the existing GitLab folders from an item group with a single folder.
     */
    @Test
    public void getWithFolder() {
        // item group has one existing folder
        items.add(item);
        replay(itemGroup);

        Map<Integer, Folder> existingFolders = folderCreator.getExistingGitLabGroupFolders();

        verify(itemGroup);

        assertThat("matched normal folder as GitLab folder", existingFolders.isEmpty(), is(true));
    }

    /**
     * Tests getting the existing GitLab folders from an item group with a single item.
     */
    @Test
    public void getWithItem() {
        // item group has one existing item
        items.add(item);
        replay(itemGroup);

        Map<Integer, Folder> existingFolders = folderCreator.getExistingGitLabGroupFolders();

        verify(itemGroup);

        assertThat("matched item as GitLab folder", existingFolders.isEmpty(), is(true));
    }
}
