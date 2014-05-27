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
import com.sonymobile.gitlab.exceptions.GitLabApiException;
import com.sonymobile.gitlab.model.GitLabGroupInfo;
import com.sonymobile.jenkins.plugins.gitlabauth.authorization.GitLabFolderAuthorization;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import jenkins.model.ModifiableTopLevelItemGroup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.LinkedList;
import java.util.List;

import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.MockDataCreators.expectNewFolderAuthorization;
import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.MockDataCreators.mockFolderAuthorization;
import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.MockDataCreators.mockGroupInfo;
import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.MockFolderBuilder.folder;
import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.MockFolderBuilder.mockFolder;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link GroupFolderSynchronizer}.
 *
 * @author Emil Nilsson
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ GroupFolderManager.class, GitLabFolderAuthorization.class })
public class GroupFolderSynchronizerTest {
    /** The descriptor for folder items. */
    private static final TopLevelItemDescriptor folderDescriptor = new Folder.DescriptorImpl();

    /** A mock item group to create folders in. */
    private ModifiableTopLevelItemGroup itemGroup;

    /** Items in the mock item group. */
    private List<TopLevelItem> items;

    /** A list of groups to be created. */
    private List<GitLabGroupInfo> groups;

    /** The folder synchronizer. */
    private FolderSynchronizerImplementation synchronizer;

    @Before
    public void setUp() throws Exception {
        // clear lists
        groups = new LinkedList<GitLabGroupInfo>();
        items = new LinkedList<TopLevelItem>();

        // mock itemGroup#getGetItems() returning the items list
        itemGroup = createMock(ModifiableTopLevelItemGroup.class);
        expect(itemGroup.getItems()).andReturn(items).anyTimes();

        // mock creating new GitLabFolderAuthorization folder properties
        expectNewFolderAuthorization();

        synchronizer = new FolderSynchronizerImplementation(itemGroup, folderDescriptor);
    }

    /**
     * Tests the synchronizer with an empty item group.
     */
    @Test
    public void withNoExistingItems() throws Exception {
        groups.add(mockGroupInfo(1, "Folder Name", "folder"));

        Folder folder = folder("folder");
        expect(itemGroup.createProject(folderDescriptor, "folder", true)).andReturn(folder);

        replay(itemGroup);

        synchronizer.synchronize();

        // verify that itemGroup#createProject was called
        verify(itemGroup);

        // see if the GitLab folder property has been set
        GitLabFolderAuthorization folderProperty = folder.getProperties().get(GitLabFolderAuthorization.class);
        assertThat("folder property not set", folderProperty, is(notNullValue()));
        assertThat("group ID not set correctly", folderProperty.getGroupId(), is(1));
    }

    /**
     * Tests the synchronizer with an item group with existing GitLab folders.
     */
    @Test
    public void withExistingGitLabFolders() throws Exception {
        groups.add(mockGroupInfo(1, "folder", "Folder Name"));

        items.add(mockFolder()
                .name("folder")
                .addProperty(mockFolderAuthorization(/* groupId */ 1))
                .build());

        replay(itemGroup);

        synchronizer.synchronize();

        // verify itemGroup#createProject wasn't called
        verify(itemGroup);
    }

    /**
     * Tests the synchronizer with multiple groups.
     */
    @Test
    public void withMultipleGroups() throws Exception {
        groups.add(mockGroupInfo(1, "Folder 1", "folder1"));
        groups.add(mockGroupInfo(2, "Folder 2", "folder2"));

        Folder folder1 = folder("folder1");
        Folder folder2 = folder("folder2");
        expect(itemGroup.createProject(folderDescriptor, "folder1", true)).andReturn(folder1);
        expect(itemGroup.createProject(folderDescriptor, "folder2", true)).andReturn(folder2);

        replay(itemGroup);

        synchronizer.synchronize();

        // verify that itemGroup#createProject was called for both folders
        verify(itemGroup);

        // see if the GitLab folder properties have been set
        GitLabFolderAuthorization folder1Property = folder1.getProperties().get(GitLabFolderAuthorization.class);
        GitLabFolderAuthorization folder2Property = folder2.getProperties().get(GitLabFolderAuthorization.class);

        assertThat("folder property not set", folder1Property, is(notNullValue()));
        assertThat("folder property not set", folder2Property, is(notNullValue()));
        assertThat("group ID not set correctly", folder1Property.getGroupId(), is(1));
        assertThat("group ID not set correctly", folder2Property.getGroupId(), is(2));
    }

    /**
     * Tests the synchronizer with an excluded group.
     */
    @Test
    public void withExcludedGroup() throws Exception {
        groups.add(mockGroupInfo(1, "Folder 1", "folder1"));
        groups.add(mockGroupInfo(10, "Folder 2", "folder2"));

        Folder folder = folder("folder1");
        expect(itemGroup.createProject(folderDescriptor, "folder1", true)).andReturn(folder);

        replay(itemGroup);

        synchronizer.synchronize();

        // verify that itemGroup#createProject was called for only the first folder
        verify(itemGroup);
    }

    /**
     * Concrete implementation of {@link GroupFolderSynchronizer} for testing.
     */
    private class FolderSynchronizerImplementation extends GroupFolderSynchronizer {
        public FolderSynchronizerImplementation(
                ModifiableTopLevelItemGroup itemGroup,
                TopLevelItemDescriptor folderDescriptor) {
            super(itemGroup, folderDescriptor);
        }

        /**
         * Synchronizes the group from the groups list.
         *
         * @throws GitLabApiException if the connection against GitLab failed
         */
        public void synchronize() throws GitLabApiException {
            synchronizeGroupFolders(groups);
        }

        /**
         * Only allows creating groups with a single digit group ID.
         */
        public boolean shouldManageGroup(GitLabGroupInfo group) throws GitLabApiException {
            return group.getId() < 10;
        }
    }
}
