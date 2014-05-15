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

import hudson.Extension;
import hudson.ExtensionList;

import java.util.concurrent.TimeUnit;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;

/**
 * Configuration page used to configure settings such as
 * period duration and if folders should be created automatically
 * or manually by users.
 * 
 * @author Andreas Alanko
 */
@Extension
public class GitLabAuthConfiguration extends GlobalConfiguration {
    private final static long DEFAULT_PERIOD_DURATION = 10;
    private final static TimeUnit DEFAUTL_PERIOD_TIMEUNIT = TimeUnit.MINUTES;
    
    private long periodDuration = DEFAULT_PERIOD_DURATION;
    private TimeUnit periodUnit = DEFAUTL_PERIOD_TIMEUNIT;
    
    private boolean autoCreateFolders = false;
    
    /**
     * Creates a configuration page and loads any previous settings
     * saved by Jenkins to this object.
     */
    public GitLabAuthConfiguration() {
        load();
    }
    
    /**
     * Checks if folders should be created automatically.
     * 
     * If this is false, users will have to choose which GitLab groups
     * they want to create folders for in Jenkins manually.
     * 
     * @return true if folders should be created automatically
     */
    public boolean getAutoCreateFolders() {
        return autoCreateFolders;
    }
    
    /**
     * Sets if folders should be created automatically.
     * 
     * @param autoCreateFolders if folders should be created automatically
     */
    private void setAutoCreateFolders(boolean autoCreateFolders) {
        this.autoCreateFolders = autoCreateFolders;
    }
    
    /**
     * Gets the value of the period duration between synchronizations
     * with the configured GitLab server.
     * 
     * @return the period duration
     */
    public long getPeriodDuration() {
        return periodDuration;
    }
    
    /**
     * Sets the period duration between synchronizations with the
     * configured GitLab server.
     * 
     * @param periodDuration the period duration
     */
    private void setPeriodDuration(long periodDuration) {
        this.periodDuration = periodDuration;
    }
    
    /**
     * Saves the configured values from the submitted form.
     *
     * @return true if configuration succeeded else false
     */
    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) {
        setPeriodDuration(formData.getLong("periodDuration"));
        setAutoCreateFolders(formData.getBoolean("autoCreateFolders"));
        
        save();
        return true;
    }
    
    /**
     * Gets the configured recurrence period or the default one if a
     * configuration hasn't been made.
     * 
     * Default value is 10 minutes in milliseconds.
     * 
     * @return the recurrence period
     */
    public static long getRecurrencePeriod() {
        GitLabAuthConfiguration instance = getInstance();
        
        if (instance != null) {
            return instance.periodUnit.toMillis(instance.periodDuration);
        }
        return DEFAUTL_PERIOD_TIMEUNIT.toMillis(DEFAULT_PERIOD_DURATION);
    }
    
    /**
     * Checks if folders should be created automatically.
     * 
     * If this is false, users will have to choose which GitLab groups
     * they want to create folders for in Jenkins manually.
     * 
     * @return true if folders should be created automatically
     */
    public static boolean useAutoCreationOfFolders() {
        GitLabAuthConfiguration instance = getInstance();
        
        if (instance != null) {
            return instance.getAutoCreateFolders();
        }
        return false;
    }
    
    /**
     * Returns the singleton instance of this class.
     *
     * @return the instance or null if Jenkins misbehaves
     */
    private static GitLabAuthConfiguration getInstance() {
        ExtensionList<GitLabAuthConfiguration> list = Jenkins.getInstance().getExtensionList(GitLabAuthConfiguration.class);

        // return the singleton instance if available from the extension list
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }
}
