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

import javax.servlet.ServletException;

import jenkins.model.Jenkins;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.sonymobile.gitlab.exceptions.GitLabApiException;
import com.sonymobile.gitlab.model.GitLabGroupInfo;
import com.sonymobile.jenkins.plugins.gitlabauth.GitLab;

import hudson.Extension;
import hudson.model.RootAction;
import hudson.security.Permission;

/**
 * Used to access the GitLab folder management page.
 * 
 * @author Andreas Alanko
 */
@Extension
public class GitLabManageFolderAction implements RootAction {

    /**
     * The icon used in the side menu bar.
     */
    @Override
    public String getIconFileName() {
        return "folder.png";
    }

    /**
     * The display name linked to this RootAction.
     */
    @Override
    public String getDisplayName() {
        return "GitLab Folders";
    }

    /**
     * The URL name used to accessed the index.groovy file
     * linked with this RootAction.
     */
    @Override
    public String getUrlName() {
        return "manage-folders";
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
        Jenkins.getInstance().checkPermission(Permission.READ);
        
        Map<String, Object> formData = request.getSubmittedForm();
        List<GitLabGroupInfo> groupList = new ArrayList<GitLabGroupInfo>();
        
        for (Entry<String, Object> groupSet : formData.entrySet()) {
            if (groupSet.getValue().equals(true)) {
                try {
                    groupList.add(GitLab.getGroup(Integer.parseInt(groupSet.getKey())));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } catch (GitLabApiException e) {
                    e.printStackTrace();
                }
            }
        }
        
        // Send groupList to appropriate class for folder creation.
        response.sendRedirect(".");
    }
} 