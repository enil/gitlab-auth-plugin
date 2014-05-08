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
import com.cloudbees.hudson.plugins.folder.FolderProperty;
import com.sonymobile.gitlab.model.GitLabGroupInfo;
import com.sonymobile.jenkins.plugins.gitlabauth.authorization.GitLabFolderAuthorization;
import com.sonymobile.jenkins.plugins.gitlabauth.exceptions.ItemNameCollisionException;
import hudson.model.FreeStyleProject;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import jenkins.model.ModifiableTopLevelItemGroup;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;

import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.GitLabModelDataCreator.createGroupInfo;
import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.MockDataCreators.mockFolderAuthorization;
import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.MockFolderBuilder.mockFolder;
import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.MockTopLevelItemBuilder.mockItem;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
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

    /** A GitLab folder. */
    private final Folder gitLabFolder = mockFolder()
            .name("foldername")
            // GitLab folder property with groupID 1
            .addProperty(mockFolderAuthorization(1))
            .build();

    /** A miscellaneous folder. */
    private final Folder folder = mockFolder()
            .name("foldername")
            .build();

    /** A miscellaneous top level item. */
    private final TopLevelItem item = mockItem(FreeStyleProject.class)
            .name("foldername")
            .build();

    /** A GitLab group. */
    private final GitLabGroupInfo group = createGroupInfo(1, "Folder Name", "foldername");

    /** The descriptor for a Folder item. */
    private final TopLevelItemDescriptor folderDescriptor = new Folder.DescriptorImpl();

    /** The map of existing folders. */
    Map<Integer, Folder> existingFolders;

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

        existingFolders = new HashMap<Integer, Folder>();
    }

    /**
     * Tests creating a folder when the folder isn't already present in the item group.
     */
    @Test
    public void createWhenNotPresent() throws Exception {
        // mock no already existing item with the same name
        expect(mockItemGroup.createProject(folderDescriptor, "foldername", true)).
                andReturn(folder);
        replay(mockItemGroup);

        // should create a new folder
        Folder returnedFolder = folderCreator.createOrGetGitLabGroupFolder(group, existingFolders);

        assertThat("Name should be set to the group path", returnedFolder.getName(), is("foldername"));

        verify(mockItemGroup);
    }

    /**
     * Tests creating a folder when the folder already is present in the item group.
     */
    @Test
    public void createWhenAlreadyPresent() throws Exception {
        // mock an already existing folder with the same name
        Folder gitLabFolder = mockFolder().name("foldername").build();
        existingFolders.put(1, gitLabFolder);

        replay(mockItemGroup);

        // should return existing folder
        Folder returnedFolder = folderCreator.createOrGetGitLabGroupFolder(group, existingFolders);

        assertThat("Existing folder should be returned", returnedFolder, is(sameInstance(gitLabFolder)));

        verify(mockItemGroup);
    }

    /**
     * Tests creating a folder when the folder name collides with an item present in the item group.
     */
    @Test
    public void createWhenNameCollides() throws Exception {
        expect(mockItemGroup.createProject(folderDescriptor, "foldername", true)).
                andThrow(new IllegalArgumentException(EMPTY));
        replay(mockItemGroup);

        // should throw exception
        thrown.expect(ItemNameCollisionException.class);
        folderCreator.createOrGetGitLabGroupFolder(group, existingFolders);

        verify(mockItemGroup);
    }

    /**
     * Tests getting the existing GitLab folders from an item group with a single GitLab folder.
     */
    @Test
    public void getWithGitLabFolder() {
        expect(mockItemGroup.getItems()).andReturn(singletonList((TopLevelItem)gitLabFolder));
        replay(mockItemGroup);

        Map<Integer, Folder> existingFolders = folderCreator.getExistingGitLabGroupFolders();

        assertThat("Should match the GitLab folder", existingFolders, hasEntry(1, gitLabFolder));

        verify(mockItemGroup);
    }

    /**
     * Tests getting the existing GitLab folders from an item group with a single folder.
     */
    @Test
    public void getWithFolder() {
        expect(mockItemGroup.getItems()).andReturn(singletonList((TopLevelItem)folder));
        replay(mockItemGroup);

        Map<Integer, Folder> existingFolders = folderCreator.getExistingGitLabGroupFolders();

        assertThat("Should not match the folder", existingFolders.isEmpty(), is(true));

        verify(mockItemGroup);
    }

    /**
     * Tests getting the existing GitLab folders from an item group with a single item.
     */
    @Test
    public void getWithItem() {
        expect(mockItemGroup.getItems()).andReturn(singletonList(item));
        replay(mockItemGroup);

        Map<Integer, Folder> existingFolders = folderCreator.getExistingGitLabGroupFolders();

        assertThat("Should not match the item", existingFolders.isEmpty(), is(true));

        verify(mockItemGroup);
    }
}
