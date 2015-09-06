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
package org.jflicks.update;

/**
 * A base implementation of the ImageCache interface.
 *
 * @author Doug Barnum
 * @version 1.0
 */
public abstract class BaseUpdate implements Update {

    private String title;
    private String bundleDirectory;
    private String sourceURL;

    /**
     * Simple constructor.
     */
    public BaseUpdate() {
    }

    /**
     * {@inheritDoc}
     */
    public String getTitle() {
        return (title);
    }

    /**
     * Convenience method tp set the title property.
     *
     * @param s A given title as a String.
     */
    public void setTitle(String s) {
        title = s;
    }

    /**
     * {@inheritDoc}
     */
    public String getBundleDirectory() {
        return (bundleDirectory);
    }

    public void setBundleDirectory(String s) {
        bundleDirectory = s;
    }

    /**
     * {@inheritDoc}
     */
    public String getSourceURL() {
        return (sourceURL);
    }

    public void setSourceURL(String s) {
        sourceURL = s;
    }

}

