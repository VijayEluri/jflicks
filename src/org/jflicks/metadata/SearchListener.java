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
package org.jflicks.metadata;

import java.util.EventListener;

/**
 * Classes can listen for events from searches.  The status of these
 * searches can be monitored this way and action can be taken.
 *
 * @author Doug Barnum
 * @version 1.0
 */
public interface SearchListener extends EventListener {

    /**
     * Some update from a search has occured.
     *
     * @param event The SearchEvent instance.
     */
    void searchUpdate(SearchEvent event);
}
