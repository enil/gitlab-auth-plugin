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

package com.sonymobile.jenkins.plugins.gitlab.gitlabauth.time;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * A time duration specified using a duration and unit.
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
     * @param unit     the time unit the time is specified in
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
     * Parses a string to an interval.
     *
     * @param input the input string
     * @return the interval
     */
    public static Interval parseInterval(String input) {
        // parse with no default unit
        return parseInterval(input, null);
    }

    /**
     * Parses a string to an interval with a default time unit.
     *
     * The default unit will be used if not specified in the input string.
     *
     * @param input       the input string
     * @param defaultUnit the unit to used if none is specified in the input string
     * @return the interval
     */
    public static Interval parseInterval(String input, TimeUnit defaultUnit) {
        Matcher matcher = Pattern.compile("\\s*(\\d+)\\s*((µ|\\w)*)\\s*").matcher(input);

        if (matcher.matches()) {
            long duration = Long.parseLong(matcher.group(1));
            String suffix = matcher.group(2);
            TimeUnit unit;

            if (isBlank(suffix) && defaultUnit != null) {
                // use the default unit if none provided
                unit = defaultUnit;
            } else if (suffix.matches("n(ano)?s(ec(onds?)?)?")) {
                unit = NANOSECONDS;
            } else if (suffix.matches("(µ|micro)s(ec(onds?)?)?")) {
                unit = MICROSECONDS;
            } else if (suffix.matches("m(illi)?s(ec(onds?)?)?")) {
                unit = MILLISECONDS;
            } else if (suffix.matches("s(ec(onds?)?)?")) {
                unit = SECONDS;
            } else if (suffix.matches("m(in(utes?)?)?")) {
                unit = MINUTES;
            } else if (suffix.matches("h(ours?)?")) {
                unit = HOURS;
            } else if (suffix.matches("d(ays?)?")) {
                unit = DAYS;
            } else {
                throw new IllegalArgumentException("Invalid interval unit: \"" + suffix + "\"");
            }

            return new Interval(duration, unit);
        }

        throw new IllegalArgumentException("Invalid interval format: \"" + input + "\"");
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
    public boolean equals(Object other) {
        return other instanceof Interval && ((Interval)other).toMilliseconds() == toMilliseconds();
    }

    @Override
    public String toString() {
        return duration + " " + unit.toString().toLowerCase();
    }
}
