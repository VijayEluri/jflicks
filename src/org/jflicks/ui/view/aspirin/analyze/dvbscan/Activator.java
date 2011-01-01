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
package org.jflicks.ui.view.aspirin.analyze.dvbscan;

import java.util.Hashtable;

import org.jflicks.ui.view.aspirin.analyze.Analyze;
import org.jflicks.util.BaseActivator;

import org.osgi.framework.BundleContext;

/**
 * Simple activator that creates an Analyze that checks that if there are
 * any DVB devices, that they have an associated channel.conf file.
 *
 * @author Doug Barnum
 * @version 1.0
 */
public class Activator extends BaseActivator {

    /**
     * {@inheritDoc}
     */
    public void start(BundleContext bc) {

        setBundleContext(bc);

        DvbScanAnalyze dsa = new DvbScanAnalyze();

        Hashtable<String, String> dict = new Hashtable<String, String>();
        dict.put(Analyze.TITLE_PROPERTY, dsa.getTitle());
        bc.registerService(Analyze.class.getName(), dsa, dict);
    }

    /**
     * {@inheritDoc}
     */
    public void stop(BundleContext context) {
    }

}