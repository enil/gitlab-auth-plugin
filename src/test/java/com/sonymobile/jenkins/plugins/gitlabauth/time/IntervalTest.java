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

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link Interval}.
 *
 * @author Emil Nilsson
 */
public class IntervalTest {
    /**
     * Tests {@link Interval#parseInterval(String)}.
     */
    @Test
    public void parseInterval() {
        assertThat(Interval.parseInterval("1 nanosecond"), is(new Interval(1, NANOSECONDS)));
        assertThat(Interval.parseInterval("10ns"), is(new Interval(10, NANOSECONDS)));
        assertThat(Interval.parseInterval("10nsec"), is(new Interval(10, NANOSECONDS)));
        assertThat(Interval.parseInterval("10 nanoseconds"), is(new Interval(10, NANOSECONDS)));

        assertThat(Interval.parseInterval("1 microsecond"), is(new Interval(1, MICROSECONDS)));
        assertThat(Interval.parseInterval("10µs"), is(new Interval(10, MICROSECONDS)));
        assertThat(Interval.parseInterval("10µsec"), is(new Interval(10, MICROSECONDS)));
        assertThat(Interval.parseInterval("10 microseconds"), is(new Interval(10, MICROSECONDS)));

        assertThat(Interval.parseInterval("1 millisecond"), is(new Interval(1, MILLISECONDS)));
        assertThat(Interval.parseInterval("10ms"), is(new Interval(10, MILLISECONDS)));
        assertThat(Interval.parseInterval("10msec"), is(new Interval(10, MILLISECONDS)));
        assertThat(Interval.parseInterval("10 milliseconds"), is(new Interval(10, MILLISECONDS)));

        assertThat(Interval.parseInterval("1 second"), is(new Interval(1, SECONDS)));
        assertThat(Interval.parseInterval("10s"), is(new Interval(10, SECONDS)));
        assertThat(Interval.parseInterval("10sec"), is(new Interval(10, SECONDS)));
        assertThat(Interval.parseInterval("10 seconds"), is(new Interval(10, SECONDS)));

        assertThat(Interval.parseInterval("1 minute"), is(new Interval(1, MINUTES)));
        assertThat(Interval.parseInterval("10m"), is(new Interval(10, MINUTES)));
        assertThat(Interval.parseInterval("10min"), is(new Interval(10, MINUTES)));
        assertThat(Interval.parseInterval("10 minutes"), is(new Interval(10, MINUTES)));

        assertThat(Interval.parseInterval("1 hour"), is(new Interval(1, HOURS)));
        assertThat(Interval.parseInterval("10h"), is(new Interval(10, HOURS)));
        assertThat(Interval.parseInterval("10 hours"), is(new Interval(10, HOURS)));

        assertThat(Interval.parseInterval("1 day"), is(new Interval(1, DAYS)));
        assertThat(Interval.parseInterval("10d"), is(new Interval(10, DAYS)));
        assertThat(Interval.parseInterval("10 days"), is(new Interval(10, DAYS)));
    }

    /**
     * Tests {@link Interval#parseInterval(String, TimeUnit)}.
     */
    @Test
    public void parseIntervalWithDefaultUnit() {
        assertThat(Interval.parseInterval("10ns"), is(new Interval(10, NANOSECONDS)));
        assertThat(Interval.parseInterval("10", NANOSECONDS), is(new Interval(10, NANOSECONDS)));

        assertThat(Interval.parseInterval("10µs"), is(new Interval(10, MICROSECONDS)));
        assertThat(Interval.parseInterval("10", MICROSECONDS), is(new Interval(10, MICROSECONDS)));

        assertThat(Interval.parseInterval("10ms"), is(new Interval(10, MILLISECONDS)));
        assertThat(Interval.parseInterval("10", MILLISECONDS), is(new Interval(10, MILLISECONDS)));

        assertThat(Interval.parseInterval("10s"), is(new Interval(10, SECONDS)));
        assertThat(Interval.parseInterval("10", SECONDS), is(new Interval(10, SECONDS)));

        assertThat(Interval.parseInterval("10m"), is(new Interval(10, MINUTES)));
        assertThat(Interval.parseInterval("10", MINUTES), is(new Interval(10, MINUTES)));

        assertThat(Interval.parseInterval("10h"), is(new Interval(10, HOURS)));
        assertThat(Interval.parseInterval("10", HOURS), is(new Interval(10, HOURS)));

        assertThat(Interval.parseInterval("10d"), is(new Interval(10, DAYS)));
        assertThat(Interval.parseInterval("10", DAYS), is(new Interval(10, DAYS)));
    }

    /**
     * Tests {@link Interval#toString()}.
     */
    @Test
    public void convertToString() {
        assertThat(new Interval(10, NANOSECONDS), hasToString("10 nanoseconds"));
        assertThat(new Interval(10, MICROSECONDS), hasToString("10 microseconds"));
        assertThat(new Interval(10, MILLISECONDS), hasToString("10 milliseconds"));
        assertThat(new Interval(10, SECONDS), hasToString("10 seconds"));
        assertThat(new Interval(10, MINUTES), hasToString("10 minutes"));
        assertThat(new Interval(10, HOURS), hasToString("10 hours"));
        assertThat(new Interval(10, DAYS), hasToString("10 days"));
    }
}
