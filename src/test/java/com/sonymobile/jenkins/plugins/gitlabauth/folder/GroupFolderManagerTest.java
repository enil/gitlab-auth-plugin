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
import com.sonymobile.jenkins.plugins.gitlabauth.GroupFolderInfo;
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
import org.powermock.reflect.Whitebox;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link GroupFolderManager}
 *
 * @author Emil Nilsson
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ GitLabFolderAuthorization.class, GroupFolderManager.class })
public class GroupFolderManagerTest {
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

    /** The folder manager instance. */
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

        // create a folder manager with the mock item group and folder descriptor
        folderManager = Whitebox.invokeConstructor(
                GroupFolderManager.class,
                new Class[]{ ModifiableTopLevelItemGroup.class, TopLevelItemDescriptor.class },
                new Object[]{ itemGroup, folderDescriptor });
    }

    /**
     * Tests getting the existing GitLab group folders.
     */
    @Test
    public void getFolders() {
        // two GitLab folder and two misc items
        addItems(
                gitLabFolder("group1", 1),
                gitLabFolder("group2", 2),
                folder("folder"),
                freeStyleProject("item"));
        replay(itemGroup);

        Map<Integer, GroupFolderInfo> existingFolders = folderManager.getFolders();

        verify(itemGroup);

        assertThat(existingFolders.size(), is(2));
        assertThat("GitLab folder not matched", existingFolders, hasKey(1));
        assertThat("GitLab folder not matched", existingFolders, hasKey(2));

        assertThat(existingFolders.get(1).getGroupId(), is(1));
        assertThat(existingFolders.get(2).getGroupId(), is(2));
    }

    /**
     * Tests creating new GitLab group folders.
     */
    @Test
    public void createFolders() throws Exception {
        // attempt to create group1 and group2
        addGroups(
                mockGroupInfo(1, "Group 1", "group1"),
                mockGroupInfo(2, "Group 2", "group2")
        );
        // group1 is already created
        addItems(
                gitLabFolder("group1", 1),
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
        assertThat("group ID not set correctly", folderProperty.getGroupId(), is(2));
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
                gitLabFolder("group1", 1),
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
     * Tests checking if GitLab group folders already exist.
     */
    @Test
    public void folderExists() {
        // only group1 is a GitLab group
        addItems(
                gitLabFolder("group1", 1),
                folder("group2"),
                freeStyleProject("group3"));
        replay(itemGroup);

        assertThat(folderManager.folderExists(mockGroupInfo(1, "Group 1", "group1")), is(true));
        assertThat(folderManager.folderExists(mockGroupInfo(2, "Group 2", "group2")), is(false));
        assertThat(folderManager.folderExists(mockGroupInfo(3, "Group 3", "group3")), is(false));
        assertThat(folderManager.folderExists(mockGroupInfo(4, "Group 4", "group4")), is(false));

        verify(itemGroup);
    }

    /**
     * Creates a mock of a GitLab group folder
     *
     * @param name    the name of the item
     * @param groupId the group ID of the group
     * @return a GitLab group folder
     */
    private Folder gitLabFolder(String name, int groupId) {
        return mockFolder().name(name).addProperty(mockFolderAuthorization(groupId)).build();
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
}
