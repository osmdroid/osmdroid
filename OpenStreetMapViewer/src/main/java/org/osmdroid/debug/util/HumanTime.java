/*
 * HumanTime.java
 *
 * Created on 06.10.2008
 *
 * Copyright (c) 2008 Johann Burkard (<mailto:jb@eaio.com>) <http://eaio.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package org.osmdroid.debug.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;

/**
 * HumanTime parses and formats time deltas for easier reading by humans. It can format time information without losing
 * information but its main purpose is to generate more easily understood approximations.
 * <h3>Using HumanTime</h3>
 * <p>
 * Use HumanTime by creating an instance that contains the time delta ({@link HumanTime#HumanTime(long)}), create an
 * empty instance through ({@link HumanTime#HumanTime()}) and set the delta using the {@link #y()}, {@link #d()},
 * {@link #h()}, {@link #s()} and {@link #ms()} methods or parse a {@link CharSequence} representation ({@link #eval(CharSequence)}).
 * Parsing ignores whitespace and is case insensitive.
 * </p>
 * <h3>HumanTime format</h3>
 * <p>
 * HumanTime will format time deltas in years ("y"), days ("d"), hours ("h"), minutes ("m"), seconds ("s") and
 * milliseconds ("ms"), separated by a blank character. For approximate representations, the time delta will be round up
 * or down if necessary.
 * </p>
 * <h3>HumanTime examples</h3>
 * <ul>
 * <li>HumanTime.eval("1 d 1d 2m 3m").getExactly() = "2 d 5 m"</li>
 * <li>HumanTime.eval("2m8d2h4m").getExactly() = "8 d 2 h 6 m"</li>
 * <li>HumanTime.approximately("2 d 8 h 20 m 50 s") = "2 d 8 h"</li>
 * <li>HumanTime.approximately("55m") = "1 h"</li>
 * </ul>
 * <h3>Implementation details</h3>
 * <ul>
 * <li>The time delta can only be increased.</li>
 * <li>Instances of this class are thread safe.</li>
 * <li>Getters using the Java Beans naming conventions are provided for use in environments like JSP or with expression
 * languages like OGNL. See {@link #getApproximately()} and {@link #getExactly()}.</li>
 * <li>To keep things simple, a year consists of 365 days.</li>
 * </ul>
 *
 * @author <a href="mailto:jb@eaio.com">Johann Burkard</a>
 * @version $Id: HumanTime.java 3906 2011-05-21 13:56:05Z johann $
 * @see #eval(CharSequence)
 * @see #approximately(CharSequence)
 * @see <a href="http://johannburkard.de/blog/programming/java/date-formatting-parsing-humans-humantime.html">Date Formatting and Parsing for Humans in Java with HumanTime</a>
 */
public class HumanTime implements Externalizable, Comparable<HumanTime>, Cloneable {

    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = 5179328390732826722L;

    /**
     * One second.
     */
    private static final long SECOND = 1000;

    /**
     * One minute.
     */
    private static final long MINUTE = SECOND * 60;

    /**
     * One hour.
     */
    private static final long HOUR = MINUTE * 60;

    /**
     * One day.
     */
    private static final long DAY = HOUR * 24;

    /**
     * One year.
     */
    private static final long YEAR = DAY * 365;

    /**
     * Percentage of what is round up or down.
     */
    private static final int CEILING_PERCENTAGE = 15;

    /**
     * Parsing state.
     */
    static enum State {

        NUMBER, IGNORED, UNIT

    }

    static State getState(char c) {
        State out;
        switch (c) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                out = State.NUMBER;
                break;
            case 's':
            case 'm':
            case 'h':
            case 'd':
            case 'y':
            case 'S':
            case 'M':
            case 'H':
            case 'D':
            case 'Y':
                out = State.UNIT;
                break;
            default:
                out = State.IGNORED;
        }
        return out;
    }

    /**
     * Parses a {@link CharSequence} argument and returns a {@link HumanTime} instance.
     *
     * @param s the char sequence, may not be <code>null</code>
     * @return an instance, never <code>null</code>
     */
    public static HumanTime eval(final CharSequence s) {
        HumanTime out = new HumanTime(0L);

        int num = 0;

        int start = 0;
        int end = 0;

        State oldState = State.IGNORED;

        for (char c : new Iterable<Character>() {

            /**
             * @see java.lang.Iterable#iterator()
             */
            public Iterator<Character> iterator() {
                return new Iterator<Character>() {

                    private int p = 0;

                    /**
                     * @see java.util.Iterator#hasNext()
                     */
                    public boolean hasNext() {
                        return p < s.length();
                    }

                    /**
                     * @see java.util.Iterator#next()
                     */
                    public Character next() {
                        return s.charAt(p++);
                    }

                    /**
                     * @see java.util.Iterator#remove()
                     */
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }

                };
            }

        }) {
            State newState = getState(c);
            if (oldState != newState) {
                if (oldState == State.NUMBER && (newState == State.IGNORED || newState == State.UNIT)) {
                    num = Integer.parseInt(s.subSequence(start, end).toString());
                } else if (oldState == State.UNIT && (newState == State.IGNORED || newState == State.NUMBER)) {
                    out.nTimes(s.subSequence(start, end).toString(), num);
                    num = 0;
                }
                start = end;
            }
            ++end;
            oldState = newState;
        }
        if (oldState == State.UNIT) {
            out.nTimes(s.subSequence(start, end).toString(), num);
        }

        return out;
    }

    /**
     * Parses and formats the given char sequence, preserving all data.
     * <p>
     * Equivalent to <code>eval(in).getExactly()</code>
     *
     * @param in the char sequence, may not be <code>null</code>
     * @return a formatted String, never <code>null</code>
     */
    public static String exactly(CharSequence in) {
        return eval(in).getExactly();
    }

    /**
     * Formats the given time delta, preserving all data.
     * <p>
     * Equivalent to <code>new HumanTime(in).getExactly()</code>
     *
     * @param l the time delta
     * @return a formatted String, never <code>null</code>
     */
    public static String exactly(long l) {
        return new HumanTime(l).getExactly();
    }

    /**
     * Parses and formats the given char sequence, potentially removing some data to make the output easier to
     * understand.
     * <p>
     * Equivalent to <code>eval(in).getApproximately()</code>
     *
     * @param in the char sequence, may not be <code>null</code>
     * @return a formatted String, never <code>null</code>
     */
    public static String approximately(CharSequence in) {
        return eval(in).getApproximately();
    }

    /**
     * Formats the given time delta, preserving all data.
     * <p>
     * Equivalent to <code>new HumanTime(l).getApproximately()</code>
     *
     * @param l the time delta
     * @return a formatted String, never <code>null</code>
     */
    public static String approximately(long l) {
        return new HumanTime(l).getApproximately();
    }

    /**
     * The time delta.
     */
    private long delta;

    /**
     * No-argument Constructor for HumanTime.
     * <p>
     * Equivalent to calling <code>new HumanTime(0L)</code>.
     */
    public HumanTime() {
        this(0L);
    }

    /**
     * Constructor for HumanTime.
     *
     * @param delta the initial time delta, interpreted as a positive number
     */
    public HumanTime(long delta) {
        super();
        this.delta = Math.abs(delta);
    }

    private void nTimes(String unit, int n) {
        if ("ms".equalsIgnoreCase(unit)) {
            ms(n);
        } else if ("s".equalsIgnoreCase(unit)) {
            s(n);
        } else if ("m".equalsIgnoreCase(unit)) {
            m(n);
        } else if ("h".equalsIgnoreCase(unit)) {
            h(n);
        } else if ("d".equalsIgnoreCase(unit)) {
            d(n);
        } else if ("y".equalsIgnoreCase(unit)) {
            y(n);
        }
    }

    private long upperCeiling(long x) {
        return (x / 100) * (100 - CEILING_PERCENTAGE);
    }

    private long lowerCeiling(long x) {
        return (x / 100) * CEILING_PERCENTAGE;
    }

    private String ceil(long d, long n) {
        return Integer.toString((int) Math.ceil((double) d / n));
    }

    private String floor(long d, long n) {
        return Integer.toString((int) Math.floor((double) d / n));
    }

    /**
     * Adds one year to the time delta.
     *
     * @return this HumanTime object
     */
    public HumanTime y() {
        return y(1);
    }

    /**
     * Adds n years to the time delta.
     *
     * @param n n
     * @return this HumanTime object
     */
    public HumanTime y(int n) {
        delta += YEAR * Math.abs(n);
        return this;
    }

    /**
     * Adds one day to the time delta.
     *
     * @return this HumanTime object
     */
    public HumanTime d() {
        return d(1);
    }

    /**
     * Adds n days to the time delta.
     *
     * @param n n
     * @return this HumanTime object
     */
    public HumanTime d(int n) {
        delta += DAY * Math.abs(n);
        return this;
    }

    /**
     * Adds one hour to the time delta.
     *
     * @return this HumanTime object
     */
    public HumanTime h() {
        return h(1);
    }

    /**
     * Adds n hours to the time delta.
     *
     * @param n n
     * @return this HumanTime object
     */
    public HumanTime h(int n) {
        delta += HOUR * Math.abs(n);
        return this;
    }

    /**
     * Adds one month to the time delta.
     *
     * @return this HumanTime object
     */
    public HumanTime m() {
        return m(1);
    }

    /**
     * Adds n months to the time delta.
     *
     * @param n n
     * @return this HumanTime object
     */
    public HumanTime m(int n) {
        delta += MINUTE * Math.abs(n);
        return this;
    }

    /**
     * Adds one second to the time delta.
     *
     * @return this HumanTime object
     */
    public HumanTime s() {
        return s(1);
    }

    /**
     * Adds n seconds to the time delta.
     *
     * @param n seconds
     * @return this HumanTime object
     */
    public HumanTime s(int n) {
        delta += SECOND * Math.abs(n);
        return this;
    }

    /**
     * Adds one millisecond to the time delta.
     *
     * @return this HumanTime object
     */
    public HumanTime ms() {
        return ms(1);
    }

    /**
     * Adds n milliseconds to the time delta.
     *
     * @param n n
     * @return this HumanTime object
     */
    public HumanTime ms(int n) {
        delta += Math.abs(n);
        return this;
    }

    /**
     * Returns a human-formatted representation of the time delta.
     *
     * @return a formatted representation of the time delta, never <code>null</code>
     */
    public String getExactly() {
        return getExactly(new StringBuilder()).toString();
    }

    /**
     * Appends a human-formatted representation of the time delta to the given {@link Appendable} object.
     *
     * @param <T> the return type
     * @param a   the Appendable object, may not be <code>null</code>
     * @return the given Appendable object, never <code>null</code>
     */
    public <T extends Appendable> T getExactly(T a) {
        try {
            boolean prependBlank = false;
            long d = delta;
            if (d >= YEAR) {
                a.append(floor(d, YEAR));
                a.append(' ');
                a.append('y');
                prependBlank = true;
            }
            d %= YEAR;
            if (d >= DAY) {
                if (prependBlank) {
                    a.append(' ');
                }
                a.append(floor(d, DAY));
                a.append(' ');
                a.append('d');
                prependBlank = true;
            }
            d %= DAY;
            if (d >= HOUR) {
                if (prependBlank) {
                    a.append(' ');
                }
                a.append(floor(d, HOUR));
                a.append(' ');
                a.append('h');
                prependBlank = true;
            }
            d %= HOUR;
            if (d >= MINUTE) {
                if (prependBlank) {
                    a.append(' ');
                }
                a.append(floor(d, MINUTE));
                a.append(' ');
                a.append('m');
                prependBlank = true;
            }
            d %= MINUTE;
            if (d >= SECOND) {
                if (prependBlank) {
                    a.append(' ');
                }
                a.append(floor(d, SECOND));
                a.append(' ');
                a.append('s');
                prependBlank = true;
            }
            d %= SECOND;
            if (d > 0) {
                if (prependBlank) {
                    a.append(' ');
                }
                a.append(Integer.toString((int) d));
                a.append(' ');
                a.append('m');
                a.append('s');
            }
        } catch (IOException ex) {
            // What were they thinking...
        }
        return a;
    }

    /**
     * Returns an approximate, human-formatted representation of the time delta.
     *
     * @return a formatted representation of the time delta, never <code>null</code>
     */
    public String getApproximately() {
        return getApproximately(new StringBuilder()).toString();
    }

    /**
     * Appends an approximate, human-formatted representation of the time delta to the given {@link Appendable} object.
     *
     * @param <T> the return type
     * @param a   the Appendable object, may not be <code>null</code>
     * @return the given Appendable object, never <code>null</code>
     */
    public <T extends Appendable> T getApproximately(T a) {

        try {
            int parts = 0;
            boolean rounded = false;
            boolean prependBlank = false;
            long d = delta;
            long mod = d % YEAR;

            if (mod >= upperCeiling(YEAR)) {
                a.append(ceil(d, YEAR));
                a.append(' ');
                a.append('y');
                ++parts;
                rounded = true;
                prependBlank = true;
            } else if (d >= YEAR) {
                a.append(floor(d, YEAR));
                a.append(' ');
                a.append('y');
                ++parts;
                rounded = mod <= lowerCeiling(YEAR);
                prependBlank = true;
            }

            if (!rounded) {
                d %= YEAR;
                mod = d % DAY;

                if (mod >= upperCeiling(DAY)) {
                    if (prependBlank) {
                        a.append(' ');
                    }
                    a.append(ceil(d, DAY));
                    a.append(' ');
                    a.append('d');
                    ++parts;
                    rounded = true;
                    prependBlank = true;
                } else if (d >= DAY) {
                    if (prependBlank) {
                        a.append(' ');
                    }
                    a.append(floor(d, DAY));
                    a.append(' ');
                    a.append('d');
                    ++parts;
                    rounded = mod <= lowerCeiling(DAY);
                    prependBlank = true;
                }

                if (parts < 2) {
                    d %= DAY;
                    mod = d % HOUR;

                    if (mod >= upperCeiling(HOUR)) {
                        if (prependBlank) {
                            a.append(' ');
                        }
                        a.append(ceil(d, HOUR));
                        a.append(' ');
                        a.append('h');
                        ++parts;
                        rounded = true;
                        prependBlank = true;
                    } else if (d >= HOUR && !rounded) {
                        if (prependBlank) {
                            a.append(' ');
                        }
                        a.append(floor(d, HOUR));
                        a.append(' ');
                        a.append('h');
                        ++parts;
                        rounded = mod <= lowerCeiling(HOUR);
                        prependBlank = true;
                    }

                    if (parts < 2) {
                        d %= HOUR;
                        mod = d % MINUTE;

                        if (mod >= upperCeiling(MINUTE)) {
                            if (prependBlank) {
                                a.append(' ');
                            }
                            a.append(ceil(d, MINUTE));
                            a.append(' ');
                            a.append('m');
                            ++parts;
                            rounded = true;
                            prependBlank = true;
                        } else if (d >= MINUTE && !rounded) {
                            if (prependBlank) {
                                a.append(' ');
                            }
                            a.append(floor(d, MINUTE));
                            a.append(' ');
                            a.append('m');
                            ++parts;
                            rounded = mod <= lowerCeiling(MINUTE);
                            prependBlank = true;
                        }

                        if (parts < 2) {
                            d %= MINUTE;
                            mod = d % SECOND;

                            if (mod >= upperCeiling(SECOND)) {
                                if (prependBlank) {
                                    a.append(' ');
                                }
                                a.append(ceil(d, SECOND));
                                a.append(' ');
                                a.append('s');
                                ++parts;
                                rounded = true;
                                prependBlank = true;
                            } else if (d >= SECOND && !rounded) {
                                if (prependBlank) {
                                    a.append(' ');
                                }
                                a.append(floor(d, SECOND));
                                a.append(' ');
                                a.append('s');
                                ++parts;
                                rounded = mod <= lowerCeiling(SECOND);
                                prependBlank = true;
                            }

                            if (parts < 2) {
                                d %= SECOND;

                                if (d > 0 && !rounded) {
                                    if (prependBlank) {
                                        a.append(' ');
                                    }
                                    a.append(Integer.toString((int) d));
                                    a.append(' ');
                                    a.append('m');
                                    a.append('s');
                                }
                            }

                        }

                    }

                }
            }
        } catch (IOException ex) {
            // What were they thinking...
        }

        return a;
    }

    /**
     * Returns the time delta.
     *
     * @return the time delta
     */
    public long getDelta() {
        return delta;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HumanTime)) {
            return false;
        }
        return delta == ((HumanTime) obj).delta;
    }

    /**
     * Returns a 32-bit representation of the time delta.
     *
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (int) (delta ^ (delta >> 32));
    }

    /**
     * Returns a String representation of this.
     * <p>
     * The output is identical to {@link #getExactly()}.
     *
     * @return a String, never <code>null</code>
     * @see java.lang.Object#toString()
     * @see #getExactly()
     */
    public String toString() {
        return getExactly();
    }

    /**
     * Compares this HumanTime to another HumanTime.
     *
     * @param t the other instance, may not be <code>null</code>
     * @return which one is greater
     */
    public int compareTo(HumanTime t) {
        return delta == t.delta ? 0 : (delta < t.delta ? -1 : 1);
    }

    /**
     * Deep-clones this object.
     *
     * @throws CloneNotSupportedException
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException {
        delta = in.readLong();
    }

    /**
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(delta);
    }

}