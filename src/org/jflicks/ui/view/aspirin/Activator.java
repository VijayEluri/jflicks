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
package org.jflicks.ui.view.aspirin;

import java.util.Hashtable;

import org.jflicks.mvc.Controller;
import org.jflicks.mvc.View;
import org.jflicks.util.BaseActivator;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Simple activator that creates a AspirinView and starts it.  Also registers
 * the AspirinView so a Controller can find it.
 *
 * @author Doug Barnum
 * @version 1.0
 */
public class Activator extends BaseActivator {

    private ServiceTracker controllerServiceTracker;
    private AnalyzeTracker analyzeTracker;

    /**
     * {@inheritDoc}
     */
    public void start(BundleContext bc) {

        setBundleContext(bc);

        AspirinView v = new AspirinView();
        v.setBundleContext(bc);

        ServiceTracker cst =
            new ServiceTracker(bc, Controller.class.getName(), null);
        setControllerServiceTracker(cst);
        v.setControllerServiceTracker(cst);
        cst.open();

        AnalyzeTracker at = new AnalyzeTracker(bc, v);
        setAnalyzeTracker(at);
        at.open();

        Hashtable<String, String> dict = new Hashtable<String, String>();
        dict.put(AspirinView.TITLE_PROPERTY, "JFLICKS-ASPIRIN");

        bc.registerService(View.class.getName(), v, dict);
    }

    /**
     * {@inheritDoc}
     */
    public void stop(BundleContext context) {

        ServiceTracker cst = getControllerServiceTracker();
        if (cst != null) {
            cst.close();
        }

        AnalyzeTracker at = getAnalyzeTracker();
        if (at != null) {
            at.close();
        }
    }

    private ServiceTracker getControllerServiceTracker() {
        return (controllerServiceTracker);
    }

    private void setControllerServiceTracker(ServiceTracker cst) {
        controllerServiceTracker = cst;
    }

    private AnalyzeTracker getAnalyzeTracker() {
        return (analyzeTracker);
    }

    private void setAnalyzeTracker(AnalyzeTracker t) {
        analyzeTracker = t;
    }

}
