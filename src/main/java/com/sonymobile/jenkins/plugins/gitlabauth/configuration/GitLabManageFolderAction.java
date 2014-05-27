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

package com.sonymobile.jenkins.plugins.gitlabauth.configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import jenkins.model.Jenkins;

import org.acegisecurity.Authentication;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.sonymobile.gitlab.exceptions.GitLabApiException;
import com.sonymobile.gitlab.model.GitLabGroupInfo;
import com.sonymobile.jenkins.plugins.gitlabauth.GitLab;
import com.sonymobile.jenkins.plugins.gitlabauth.GroupFolderInfo;
import com.sonymobile.jenkins.plugins.gitlabauth.authorization.GitLabAuthorization;
import com.sonymobile.jenkins.plugins.gitlabauth.exceptions.ItemNameCollisionException;
import com.sonymobile.jenkins.plugins.gitlabauth.folder.UserCreatedGroupFolder;
import com.sonymobile.jenkins.plugins.gitlabauth.security.GitLabUserDetails;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.RootAction;
import hudson.security.Permission;

/**
 * Used to access the GitLab folder management page.
 * 
 * @author Andreas Alanko
 */
@Extension
public class GitLabManageFolderAction implements RootAction {
    /** The logger for this class. */
    private final Logger LOGGER = Logger.getLogger(GitLabManageFolderAction.class.getName());
    
    /**
     * The icon used in the side menu bar.
     */
    public String getIconFileName() {
        if (Jenkins.getInstance().getAuthorizationStrategy() instanceof GitLabAuthorization) {
            return "folder.png";
        }
        return null;
    }

    /**
     * The display name linked to this RootAction.
     */
    public String getDisplayName() {
        return "GitLab Folders";
    }

    /**
     * The URL name used to accessed the index.groovy file
     * linked with this RootAction.
     */
    public String getUrlName() {
        return "manage-folders";
    }
    
    /**
     * Gets a list of {@link GroupFolderInfo} for GitLab groups that does not
     * have an associated folder already created.
     * 
     * @return list of {@link GroupFolderInfo}
     */
    public List<GroupFolderInfo> getNonExistingFolders() {
        int userId = getCurrentUserId();
        
        if (userId != -1) {
            try {
                return UserCreatedGroupFolder.getNonExistingFolders(userId);
            } catch (GitLabApiException e) {
                LOGGER.warning(e.getMessage());
            }
        }
        return new ArrayList<GroupFolderInfo>();
    }
    
    /**
     * Gets a list of {@link GroupFolderInfo} for GitLab groups that does have an 
     * associated folder created.
     * 
     * @return list of {@link GroupFolderInfo}
     */
    public List<GroupFolderInfo> getExistingFolders() {
        try {
            return UserCreatedGroupFolder.getExistingFolders(Jenkins.getAuthentication());
        } catch (GitLabApiException e) {
            LOGGER.warning(e.getMessage());
        }
        return new ArrayList<GroupFolderInfo>();
    }
    
    public String getGitLabAccessLevel(int groupId) {
        int userId = getCurrentUserId();
        
        if (userId != -1) {
            try {
                return GitLab.getAccessLevelInGroup(userId, groupId).toString();
            } catch (GitLabApiException e) {
                LOGGER.warning(e.getMessage());
            }
        }
        return "N/A";
    }
    
    private int getCurrentUserId() {
        Authentication auth = Jenkins.getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof GitLabUserDetails) {
            return ((GitLabUserDetails) Jenkins.getAuthentication().getPrincipal()).getId();
        }
        return -1;
    }
    
    /**
     * Handles form submit from the create folder form.
     * 
     * @param request  the stapler request
     * @param response the stapler response
     * @throws ServletException
     * @throws IOException
     */
    public void doCreateFolders(StaplerRequest request, StaplerResponse response) throws ServletException, IOException {
        Map<String, Object> formData = request.getSubmittedForm();
        List<GitLabGroupInfo> groups = new ArrayList<GitLabGroupInfo>();
        
        for (Entry<String, Object> groupSet : formData.entrySet()) {
            if (groupSet.getValue().equals(true)) {
                try {
                    GitLabGroupInfo group = GitLab.getGroup(Integer.parseInt(groupSet.getKey()));
                    if(group != null) {
                        groups.add(group);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } catch (GitLabApiException e) {
                    e.printStackTrace();
                }
            }
        }
        
        int userId = getCurrentUserId();
        
        if (userId != -1) {
            try {
                UserCreatedGroupFolder.createFolders(groups, userId);
            } catch (GitLabApiException e) {
                LOGGER.warning(e.getMessage());
            } catch (ItemNameCollisionException e) {
                LOGGER.warning(e.getMessage());
            }
        } else {
            LOGGER.warning("Error processing the currently logged in users user ID");
        }
        response.sendRedirect(".");
    }
} 