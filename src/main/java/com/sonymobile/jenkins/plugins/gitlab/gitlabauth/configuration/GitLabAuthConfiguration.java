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

package com.sonymobile.jenkins.plugins.gitlab.gitlabauth.configuration;

import com.sonymobile.jenkins.plugins.gitlab.gitlabauth.time.Interval;
import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Configuration page used to configure settings such as
 * period duration and if folders should be created automatically
 * or manually by users.
 *
 * @author Andreas Alanko
 * @author Emil Nilsson
 */
@Extension
public class GitLabAuthConfiguration extends GlobalConfiguration {
    /** The default period duration interval. */
    public static final Interval DEFAULT_PERIOD_DURATION = new Interval(10, MINUTES);

    /** The time unit used for the period duration if not explicitly stated. */
    private static final TimeUnit DEFAULT_PERIOD_TIME_UNIT = MINUTES;

    /** The period duration for automatic synchronization. */
    private Interval periodDuration = DEFAULT_PERIOD_DURATION;

    /** Whether to use automatic folder synchronization. */
    private boolean autoCreateFolders = false;

    /**
     * Creates a configuration page and loads any previous settings saved by Jenkins to this object.
     */
    public GitLabAuthConfiguration() {
        load();
    }

    /**
     * Saves the configured values from the submitted form.
     *
     * @return true if configuration succeeded
     */
    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        String periodDurationInput = formData.getString("periodDuration");
        try {
            periodDuration = Interval.parseInterval(periodDurationInput, DEFAULT_PERIOD_TIME_UNIT);
            autoCreateFolders = formData.getBoolean("autoCreateFolders");

            save();
            return true;
        } catch (IllegalArgumentException e) {
            throw new FormException("Invalid interval: \"" + periodDurationInput + "\"", e, "periodDuration");
        }
    }

    /**
     * Validates the period duration input.
     *
     * @param periodDuration the period duration input from the form
     * @return ok if the form input was valid
     */
    public FormValidation doCheckPeriodDuration(@QueryParameter String periodDuration) {
        try {
            // try to parse the input
            Interval.parseInterval(periodDuration, DEFAULT_PERIOD_TIME_UNIT);
            return FormValidation.ok();
        } catch (IllegalArgumentException e) {
            return FormValidation.error("Invalid interval: \"%s\"", periodDuration);
        }
    }

    /**
     * Returns the period duration for automatic synchronization.
     *
     * @return the period duration interval
     */
    public static Interval getPeriodDuration() {
        GitLabAuthConfiguration instance = getInstance();
        return instance != null ? instance.periodDuration : DEFAULT_PERIOD_DURATION;
    }

    /**
     * Checks if folders should be created automatically.
     *
     * If this is false, users will have to choose which GitLab groups
     * they want to create folders for in Jenkins manually.
     *
     * @return true if folders should be created automatically
     */
    public static boolean getAutoCreateFolders() {
        GitLabAuthConfiguration instance = getInstance();
        return instance != null ? instance.autoCreateFolders : false;
    }

    /**
     * Returns the singleton instance of this class.
     *
     * @return the instance or null if Jenkins misbehaves
     */
    public static GitLabAuthConfiguration getInstance() {
        return GlobalConfiguration.all().get(GitLabAuthConfiguration.class);
    }
}
