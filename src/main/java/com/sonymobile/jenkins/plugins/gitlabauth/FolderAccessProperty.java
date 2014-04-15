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

import java.util.ArrayList;
import java.util.List;

import com.cloudbees.hudson.plugins.folder.FolderProperty;
import com.cloudbees.hudson.plugins.folder.FolderPropertyDescriptor;
import com.cloudbees.hudson.plugins.folder.Folder;

import hudson.Extension;
import hudson.model.Item;
import hudson.security.Permission;
import hudson.security.PermissionGroup;

/**
 * Property used for configuring access rights for folders.
 * 
 * @author Andreas Alanko
 */
public class FolderAccessProperty extends FolderProperty<Folder> {
    
    /**
     * Returns a list of all permissions used by the interface item.
     * 
     * @return a List of permission objects
     */
    public List<Permission> getAllItemPermissions() {
        PermissionGroup items = PermissionGroup.get(Item.class);
        
        return (items != null) ? items.getPermissions() : new ArrayList<Permission>();
    }
    
    @Extension
    public static class DescriptorImpl extends FolderPropertyDescriptor {

        @Override
        public String getDisplayName() {
            return "Folder Access Permissions";
        }
    }
}
