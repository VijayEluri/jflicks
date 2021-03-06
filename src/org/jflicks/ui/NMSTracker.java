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
    along with JFLICKS.  If not, see <remote://www.gnu.org/licenses/>.
*/
package org.jflicks.ui;

import java.util.ArrayList;

import org.jflicks.nms.NMS;
import org.jflicks.util.BaseTracker;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * A tracker for the remote NMS service.
 *
 * @author Doug Barnum
 * @version 1.0
 */
public class NMSTracker extends BaseTracker {

    private JFlicksModel ltmsModel;

    /**
     * Contructor with BundleContext and model.
     *
     * @param bc A given BundleContext needed to communicate with OSGi.
     * @param m Our Model.
     */
    public NMSTracker(BundleContext bc, JFlicksModel m) {

        super(bc, NMS.class.getName());
        setJFlicksModel(m);
    }

    private JFlicksModel getJFlicksModel() {
        return (ltmsModel);
    }

    private void setJFlicksModel(JFlicksModel m) {
        ltmsModel = m;
    }

    /**
     * A new NMS has come online.
     *
     * @param sr The ServiceReference object.
     * @return The instantiation.
     */
    public Object addingService(ServiceReference sr) {

        Object result = null;

        BundleContext bc = getBundleContext();
        String title = (String) sr.getProperty(NMS.TITLE_PROPERTY);
        if (bc != null) {

            add(title, sr);
            NMS nms = (NMS) bc.getService(sr);
            addNMS(nms);
            result = nms;
        }

        return (result);
    }

    /**
     * A remote service has been modified.
     *
     * @param sr The Stream ServiceReference.
     * @param svc The Stream instance.
     */
    public void modifiedService(ServiceReference sr, Object svc) {
    }

    /**
     * A remote service has gone away.  Bye-bye.
     *
     * @param sr The ServiceReference.
     * @param svc The instance.
     */
    public void removedService(ServiceReference sr, Object svc) {

        removeNMS((NMS) svc);
        String title = (String) sr.getProperty(NMS.TITLE_PROPERTY);
        dispose(title);
    }

    private void addNMS(NMS nms) {

        JFlicksModel m = getJFlicksModel();
        if (m != null) {

            NMS[] array = m.getNMS();
            if (array != null) {

                NMS[] newarray = new NMS[array.length + 1];
                for (int i = 0; i < array.length; i++) {
                    newarray[i] = array[i];
                }

                newarray[array.length] = nms;
                m.setNMS(newarray);

            } else {

                NMS[] newarray = new NMS[1];
                newarray[0] = nms;
                m.setNMS(newarray);
            }
        }
    }

    private void removeNMS(NMS nms) {

        JFlicksModel m = getJFlicksModel();
        if ((nms != null) && (m != null)) {

            // An NMS went away, lets make a new list of only the ones that
            // seem alive...
            NMS[] array = m.getNMS();
            if (array != null) {

                if (array.length == 1) {

                    // We only had one so just make an empty list.
                    m.setNMS(null);

                } else {

                    ArrayList<NMS> nlist = new ArrayList<NMS>();
                    for (int i = 0; i < array.length; i++) {

                        try {

                            String tmp = array[i].getTitle();
                            nlist.add(array[i]);

                        } catch (Exception ex) {

                            // This one must be dead so we don't add it.  The
                            // exception would have happened on the getTitle().
                        }
                    }

                    if (nlist.size() > 0) {

                        // Set our list to what appear to be alive...
                        m.setNMS(nlist.toArray(new NMS[nlist.size()]));

                    } else {

                        // Well apparently all are dead now...
                        m.setNMS(null);
                    }
                }
            }
        }
    }

}
