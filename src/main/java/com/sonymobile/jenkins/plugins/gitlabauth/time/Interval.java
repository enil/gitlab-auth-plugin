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

package com.sonymobile.jenkins.plugins.gitlabauth.time;

import com.sonymobile.jenkins.plugins.gitlabauth.folder.PeriodicGitLabFolderSynchronization;

import java.util.concurrent.TimeUnit;

/**
 * A time duration used by {@link PeriodicGitLabFolderSynchronization}.
 *
 * @author Emil Nilsson
 */
public final class Interval {
    /** The time specified in the time unit. */
    private final long duration;

    /** The time unit the time is specified in. */
    private final TimeUnit unit;

    /**
     * Creates a time duration
     *
     * @param duration the time specified in the time unit
     * @param unit the time unit the time is specified in
     */
    public Interval(long duration, TimeUnit unit) {
        this.duration = duration;
        this.unit = unit;
    }

    /**
     * Creates a copy of a time duration
     *
     * @param other the time duration to copy
     */
    public Interval(Interval other) {
        duration = other.getDuration();
        unit = other.getUnit();
    }

    /**
     * Gets the time specified in the time unit.
     *
     * @return the time specified in the time unit
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Gets the time unit the time is specified in.
     *
     * @return the time unit the time is specified in
     */
    public TimeUnit getUnit() {
        return unit;
    }

    /**
     * Converts the time duration to milliseconds.
     *
     * @return the time duration in milliseconds.
     */
    public long toMilliseconds() {
        return unit.toMillis(duration);
    }

    @Override
    public String toString() {
        return duration + " " + unit.toString().toLowerCase();
    }
}
