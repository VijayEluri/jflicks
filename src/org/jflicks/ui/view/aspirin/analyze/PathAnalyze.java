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
package org.jflicks.ui.view.aspirin.analyze;

import org.jflicks.util.Util;

/**
 * This class is an Analyze implementation that can check if a program
 * is in the users path.
 *
 * @author Doug Barnum
 * @version 1.0
 */
public class PathAnalyze extends BaseAnalyze {

    private String program;

    /**
     * Simple empty constructor.
     */
    public PathAnalyze() {
    }

    /**
     * The program to check whether it is currently in the users PATH.
     *
     * @return A program name.
     */
    public String getProgram() {
        return (program);
    }

    /**
     * The program to check whether it is currently in the users PATH.
     *
     * @param s A program name.
     */
    public void setProgram(String s) {
        program = s;
    }

    /**
     * {@inheritDoc}
     */
    public Finding analyze() {

        Finding result = null;

        String s = getProgram();
        if (s != null) {

            result = new Finding();
            String[] array = Util.getProgramPaths(s);
            if (array != null) {

                result.setPassed(true);
                StringBuilder sb = new StringBuilder();
                sb.append(s);
                sb.append(" has been found in " + array.length);
                if (array.length > 1) {
                    sb.append(" locations. They are ");
                } else {
                    sb.append(" location.  Is it ");
                }

                for (int i = 0; i < array.length; i++) {

                    sb.append(array[i]);
                    if ((i + 1) < array.length) {

                        sb.append(", ");
                    }
                }

                result.setDescription(sb.toString());

            } else {

                result.setPassed(false);
                result.setDescription(s + " was not found!");
            }

        } else {

            throw new RuntimeException("Program property cannot be NULL!");
        }

        return (result);
    }

}

