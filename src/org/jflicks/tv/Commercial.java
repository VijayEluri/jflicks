/*
    This file is part of JFLICKS.

    JFLICKS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    JFLICKS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with JFLICKS.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.jflicks.tv;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import org.jflicks.util.Util;
import org.jflicks.util.LogUtil;

/**
 * This class contains all the properties representing a Commercial.  A
 * Commercial is just a span of time in a recording.
 *
 * @author Doug Barnum
 * @version 1.0
 */
public class Commercial implements Serializable, Comparable<Commercial> {

    private int start;
    private int end;

    /**
     * Simple empty constructor.
     */
    public Commercial() {
    }

    /**
     * The number of seconds from the start of the recording that this
     * commercial starts.
     *
     * @return An int value.
     */
    public int getStart() {
        return (start);
    }

    /**
     * The number of seconds from the start of the recording that this
     * commercial starts.
     *
     * @param i An int value.
     */
    public void setStart(int i) {
        start = i;
    }

    /**
     * The number of seconds from the start of the recording that this
     * commercial ends.
     *
     * @return An int value.
     */
    public int getEnd() {
        return (end);
    }

    /**
     * The number of seconds from the start of the recording that this
     * commercial ends.
     *
     * @param i An int value.
     */
    public void setEnd(int i) {
        end = i;
    }

    /**
     * Override the hashcode.
     *
     * @return An int value.
     */
    public int hashCode() {

        Integer obj = Integer.valueOf(getStart());
        return (obj.hashCode());
    }

    /**
     * The equals override method.
     *
     * @param o A gven object to check.
     * @return True if the objects are equal.
     */
    public boolean equals(Object o) {

        boolean result = false;

        if (o == this) {

            result = true;

        } else if (!(o instanceof Commercial)) {

            result = false;

        } else {

            Commercial c = (Commercial) o;

            Integer start0 = Integer.valueOf(getStart());
            Integer start1 = Integer.valueOf(c.getStart());
            if ((start0 != null) && (start1 != null)) {

                result = start0.equals(start1);
            }
        }

        return (result);
    }

    /**
     * The comparable interface.
     *
     * @param c The given Commercial instance to compare.
     * @throws ClassCastException on the input argument.
     * @return An int representing their "equality".
     */
    public int compareTo(Commercial c) throws ClassCastException {

        int result = 0;

        if (c == null) {

            throw new NullPointerException();
        }

        if (c == this) {

            result = 0;

        } else {

            Integer sort0 = Integer.valueOf(getStart());
            Integer sort1 = Integer.valueOf(c.getStart());
            if ((sort0 != null) && (sort1 != null)) {

                result = sort0.compareTo(sort1);
            }
        }

        return (result);
    }

    /**
     * Convenience method to turn an EDL file to an array of Commercial
     * instances.  Any commercial skip program that outputs an EDL file
     * can be parsed by this method.
     *
     * @param f A given file instance of an EDL output file.
     * @return An array of Commercial instances generated by parsing the EDL.
     */
    public static Commercial[] fromEDL(File f) {

        Commercial[] result = null;

        if ((f != null) && (f.exists()) && (!f.isDirectory())) {

            String[] array = Util.readTextFile(f);
            if (array != null) {

                ArrayList<Commercial> l = new ArrayList<Commercial>();
                for (int i = 0; i < array.length; i++) {

                    int first = array[i].indexOf("\t");
                    int second = array[i].lastIndexOf("\t");
                    if ((first != -1) && (second != -1)) {

                        String stmp = array[i].substring(0, first);
                        stmp = stmp.trim();
                        int start = (int) Util.str2double(stmp, 0.0);
                        stmp = array[i].substring(first + 1, second);
                        stmp = stmp.trim();
                        int end = (int) Util.str2double(stmp, 0.0);
                        LogUtil.log(LogUtil.DEBUG, "fromEDL: start " + start);
                        LogUtil.log(LogUtil.DEBUG, "fromEDL: end " + end);
                        Commercial tmp = new Commercial();
                        tmp.setStart(start);
                        tmp.setEnd(end);
                        l.add(tmp);
                    }
                }

                if (l.size() > 0) {

                    result = l.toArray(new Commercial[l.size()]);
                }
            }
        }

        return (result);
    }

    /**
     * A convenience method to build a "timeline" of commercial start/end
     * times.
     *
     * @param array The defined Commercial instances.
     * @return An array of Integer instances.
     */
    public static Integer[] timeline(Commercial[] array) {

        Integer[] result = null;

        if (array != null) {

            ArrayList<Integer> l = new ArrayList<Integer>();
            for (int i = 0; i < array.length; i++) {

                l.add(Integer.valueOf(array[i].getStart()));
                l.add(Integer.valueOf(array[i].getEnd()));
            }

            if (l.size() > 0) {
                result = l.toArray(new Integer[l.size()]);
            }
        }

        return (result);
    }

    /**
     * Given a timeline and the current position, figure where the next
     * forward location should be.
     *
     * @param array A given array of Integer instances.
     * @param current The current position in the timeline.
     * @return The seconds value of where to "jump".
     */
    public static int[] whereNextTwo(Integer[] array, int current) {

        int[] result = new int[2];

        result[0] = current;
        result[1] = current;

        if (array != null) {

            for (int i = 0; i < array.length; i++) {

                int tmp = array[i].intValue();
                if (current < tmp) {

                    result[0] = tmp;
                    if ((i + 1) < array.length) {

                        result[1] = array[i + 1].intValue();
                    }

                    break;
                }
            }
        }

        return (result);
    }

    /**
     * Given a timeline and the current position, figure where the next
     * forward location should be.
     *
     * @param array A given array of Integer instances.
     * @param current The current position in the timeline.
     * @return The seconds value of where to "jump".
     */
    public static int whereNext(Integer[] array, int current) {

        int result = current;

        if (array != null) {

            for (int i = 0; i < array.length; i++) {

                int tmp = array[i].intValue();
                if (current < tmp) {

                    result = tmp;
                    break;
                }
            }
        }

        return (result);
    }

    /**
     * Given a timeline and the current position, figure where the next
     * backward location should be.
     *
     * @param array A given array of Integer instances.
     * @param current The current position in the timeline.
     * @return The seconds value of where to "jump".
     */
    public static int wherePrevious(Integer[] array, int current) {

        int result = current;

        if (array != null) {

            for (int i = array.length - 1; i >= 0; i--) {

                int tmp = array[i].intValue();
                if (tmp < current) {

                    result = tmp;
                    break;
                }
            }
        }

        return (result);
    }

}

