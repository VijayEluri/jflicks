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
package org.jflicks.stream.http;

import org.osgi.service.http.HttpService;

/**
 * The HttpServiceProperty is a get/set pair for classes that want to be
 * notified by an HttpTracker.
 *
 * @author Doug Barnum
 * @version 1.0
 */
public interface HttpServiceProperty {

    /**
     * The HttpService property.
     *
     * @return The HttpService instance.
     */
    HttpService getHttpService();

    /**
     * The HttpService property.
     *
     * @param hs The HttpService instance.
     */
    void setHttpService(HttpService hs);
}
