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
package org.jflicks.ui.view.scheduler;

import java.util.Hashtable;

import org.jflicks.mvc.Controller;
import org.jflicks.mvc.View;
import org.jflicks.util.BaseActivator;
import org.jflicks.util.EventSender;

import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Simple activator that creates a SchedulerView and starts it.  Also registers
 * the SchedulerView so a Controller can find it.
 *
 * @author Doug Barnum
 * @version 1.0
 */
public class Activator extends BaseActivator {

    private ServiceTracker controllerServiceTracker;

    /**
     * {@inheritDoc}
     */
    public void start(BundleContext bc) {

        setBundleContext(bc);

        SchedulerView v = new SchedulerView();
        v.setBundleContext(bc);

        controllerServiceTracker =
            new ServiceTracker(bc, Controller.class.getName(), null);
        v.setControllerServiceTracker(controllerServiceTracker);
        controllerServiceTracker.open();

        Hashtable<String, String> dict = new Hashtable<String, String>();
        dict.put(SchedulerView.TITLE_PROPERTY, "JFLICKS-SCHEDULERCLIENT");

        bc.registerService(View.class.getName(), v, dict);

        String[] topics = new String[] {
            EventSender.MESSAGE_TOPIC_PATH
        };

        Hashtable<String, String[]> eprops = new Hashtable<String, String[]>();
        eprops.put(EventConstants.EVENT_TOPIC, topics);
        bc.registerService(EventHandler.class.getName(), v, eprops);
    }

    /**
     * {@inheritDoc}
     */
    public void stop(BundleContext context) {

        if (controllerServiceTracker != null) {

            controllerServiceTracker.close();
            controllerServiceTracker = null;
        }
    }

}
