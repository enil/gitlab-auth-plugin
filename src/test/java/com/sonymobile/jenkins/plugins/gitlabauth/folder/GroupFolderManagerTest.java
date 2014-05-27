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
import com.sonymobile.jenkins.plugins.gitlabauth.GitLab;
import com.sonymobile.jenkins.plugins.gitlabauth.authorization.GitLabFolderAuthorization;
import com.sonymobile.jenkins.plugins.gitlabauth.exceptions.ItemNameCollisionException;

import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import jenkins.model.ModifiableTopLevelItemGroup;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.GitLabMatchers.hasGroupId;
import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.MockDataCreators.expectNewFolderAuthorization;
import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.MockDataCreators.mockFolderAuthorization;
import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.MockDataCreators.mockGroupInfo;
import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.MockFolderBuilder.folder;
import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.MockFolderBuilder.mockFolder;
import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.MockFreeStyleProjectBuilder.freeStyleProject;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.verify;


/**
 * Tests for {@link GroupFolderManager}.
 *
 * @author Emil Nilsson
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ GitLabFolderAuthorization.class, GroupFolderManager.class, GitLab.class })
public class GroupFolderManagerTest implements GroupFolderManager.ManagesGroupPredicate {
    /** A rule for catching expected exceptions. */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /** The descriptor for a folder item. */
    private static final TopLevelItemDescriptor folderDescriptor = new Folder.DescriptorImpl();

    /** The target item group. */
    private ModifiableTopLevelItemGroup itemGroup;

    /** Items in the target item group. */
    private List<TopLevelItem> items;

    /** The GitLab groups. */
    private List<GitLabGroupInfo> groups;

    /**
     * The folder manager instance.
     *
     * This folder manager only manages group with a group ID less than 10.
     *
     * @see #shouldManageGroup(GitLabGroupInfo)
     */
    private GroupFolderManager folderManager;

    @Before
    public void setUp() throws Exception {
        items = new LinkedList<TopLevelItem>();
        groups = new LinkedList<GitLabGroupInfo>();

        // mock itemGroup#getGetItems() returning the items list
        itemGroup = createMock(ModifiableTopLevelItemGroup.class);
        expect(itemGroup.getItems()).andReturn(items).anyTimes();

        // mock creating new GitLabFolderAuthorization folder properties
        expectNewFolderAuthorization();

        mockStatic(GitLab.class);

        folderManager = new GroupFolderManager(this, itemGroup, folderDescriptor);
    }

    /**
     * Tests getting the existing GitLab group folders.
     */
    @Test
    public void getFolders() throws Exception {
        // group1 and group2 are managed by the folder manager, everything else should be excluded
        addItems(
                gitLabFolder(1, "Group 1", "group1"),
                gitLabFolder(2, "Group 2", "group2"),
                gitLabFolder(10, "Group 10", "group10"),
                folder("folder"),
                freeStyleProject("item"));
        replay(itemGroup);

        List<GroupFolderInfo> existingFolders = new ArrayList<GroupFolderInfo>(folderManager.getFolders());

        verify(itemGroup);

        assertThat(existingFolders, contains(hasGroupId(1), hasGroupId(2)));
    }

    /**
     * Tests getting all the existing GitLab group folders.
     */
    @Test
    public void getAllFolders() throws Exception {
        // group1 and group2 are managed by the folder manager, group10 should still be included
        addItems(
                gitLabFolder(1, "Group 1", "group1"),
                gitLabFolder(2, "Group 2", "group2"),
                gitLabFolder(10, "Group 10", "group10"),
                folder("folder"),
                freeStyleProject("item"));
        replay(itemGroup);

        List<GroupFolderInfo> existingFolders = new ArrayList<GroupFolderInfo>(folderManager.getAllFolders());

        verify(itemGroup);

        assertThat(existingFolders, contains(hasGroupId(1), hasGroupId(2), hasGroupId(10)));
    }

    /**
     * Tests getting available GitLab groups.
     */
    @Test
    public void getAvailableGroups() throws Exception {
        // existing groups are group1, group2 and group 10 (group 10 is not managed)
        addGroups(
                mockGroupInfo(1, "Group 1", "group1"),
                mockGroupInfo(2, "Group 2", "group2"),
                mockGroupInfo(10, "Group 10", "group10"));
        // group1 is already created
        addItems(
                gitLabFolder(1, "Group 1", "group1"),
                folder("folder"),
                freeStyleProject("item"));
        replay(itemGroup);

        Collection<GitLabGroupInfo> availableGroups = folderManager.getAvailableGroups(groups);
        assertThat(availableGroups, contains(hasGroupId(2)));

        verify(itemGroup);
    }

    /**
     * Tests creating new GitLab group folders.
     */
    @Test
    public void createFolders() throws Exception {
        // attempt to create group1, group2 and group 10 (group 10 is not managed)
        addGroups(
                mockGroupInfo(1, "Group 1", "group1"),
                mockGroupInfo(2, "Group 2", "group2"),
                mockGroupInfo(10, "Group 10", "group10")
        );
        // group1 is already created
        addItems(
                gitLabFolder(1, "Group 1", "group1"),
                folder("folder"),
                freeStyleProject("item"));

        // return group2 when creating a new folder
        Folder group2 = folder("group2");
        expect(itemGroup.createProject(folderDescriptor, "group2", true))
                .andReturn(group2);
        replay(itemGroup);

        folderManager.createFolders(groups);

        verify(itemGroup);

        // get property for group2
        GitLabFolderAuthorization folderProperty = group2.getProperties().get(GitLabFolderAuthorization.class);

        assertThat("name not set correctly", group2.getName(), is("group2"));
        assertThat("folder property not set", folderProperty, is(notNullValue()));
        assertThat("group ID not set correctly", folderProperty, hasGroupId(2));
    }

    /**
     * Tests creating new GitLab group folders when the item name collides with another item.
     */
    @Test
    public void createFoldersWithNameCollisions() throws Exception {
        // attempt to create group1, group2, group3 and group4
        addGroups(
                mockGroupInfo(1, "Group 1", "group1"),
                mockGroupInfo(2, "Group 2", "group2"),
                mockGroupInfo(3, "Group 3", "group3"),
                mockGroupInfo(3, "Group 4", "group4")
        );
        // group1 is already created, group3 and group4 collides with existing items
        addItems(
                gitLabFolder(1, "Group 1", "group1"),
                freeStyleProject("group3"),
                folder("group4"));

        expect(itemGroup.createProject(folderDescriptor, "group2", true))
                .andReturn(folder("group2"));
        expect(itemGroup.createProject(folderDescriptor, "group3", true))
                .andThrow(new IllegalArgumentException(EMPTY));
        expect(itemGroup.createProject(folderDescriptor, "group4", true))
                .andThrow(new IllegalArgumentException(EMPTY));
        replay(itemGroup);

        // should throw an exception because the item names collides
        thrown.expect(ItemNameCollisionException.class);
        thrown.expectMessage(containsString("group3, group4"));

        folderManager.createFolders(groups);

        verify(itemGroup);
    }

    /**
     * Creates a mock of a GitLab group folder
     *
     * @param name    the name of the item
     * @param groupId the group ID of the group
     * @return a GitLab group folder
     */
    private Folder gitLabFolder(int groupId, String name, String path) {
        return mockFolder().name(name).addProperty(mockFolderAuthorization(groupId, name, path)).build();
    }

    /**
     * Adds items to the target item group.
     *
     * @param items the items
     */
    private void addItems(TopLevelItem... items) {
        this.items.addAll(asList(items));
    }

    private void addGroups(GitLabGroupInfo... groups) {
        this.groups.addAll(asList(groups));
    }

    /**
     * Checks whether a group should be included by the folder manager.
     *
     * Used to exclude groups with a group ID greater than 9.
     *
     * @param group the group
     * @return true if the group should be included
     * @throws GitLabApiException
     */
    public boolean shouldManageGroup(GitLabGroupInfo group) throws GitLabApiException {
        return group.getId() < 10;
    }
}
