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
package org.jflicks.nms.system;

import org.jflicks.videomanager.VideoManager;
import org.jflicks.util.BaseTracker;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * A tracker for the video service.
 *
 * @author Doug Barnum
 * @version 1.0
 */
public class VideoManagerTracker extends BaseTracker {

    private SystemNMS systemNMS;

    /**
     * Contructor with BundleContext and NMS instance.
     *
     * @param bc A given BundleContext needed to communicate with OSGi.
     * @param nms Our NMS implementation.
     */
    public VideoManagerTracker(BundleContext bc, SystemNMS nms) {

        super(bc, VideoManager.class.getName());
        setSystemNMS(nms);
    }

    private SystemNMS getSystemNMS() {
        return (systemNMS);
    }

    private void setSystemNMS(SystemNMS s) {
        systemNMS = s;
    }

    /**
     * A new Video service has come online.
     *
     * @param sr The ServiceReference object.
     * @return The instantiation.
     */
    public Object addingService(ServiceReference sr) {

        Object result = null;

        BundleContext bc = getBundleContext();
        SystemNMS s = getSystemNMS();
        if ((bc != null) && (s != null)) {

            VideoManager service = (VideoManager) bc.getService(sr);
            s.setVideoManager(service);
            result = service;
        }

        return (result);
    }

    /**
     * A recorder service has been modified.
     *
     * @param sr The Video ServiceReference.
     * @param svc The Video instance.
     */
    public void modifiedService(ServiceReference sr, Object svc) {
    }

    /**
     * A recorder service has gone away.  Bye-bye.
     *
     * @param sr The ServiceReference.
     * @param svc The instance.
     */
    public void removedService(ServiceReference sr, Object svc) {

        SystemNMS s = getSystemNMS();
        if (s != null) {

            s.setVideoManager(null);
        }
    }

}
