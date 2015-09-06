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
package org.jflicks.rc.lirc;

import java.io.File;

import org.jflicks.rc.RC;
import org.jflicks.job.JobContainer;
import org.jflicks.job.JobManager;
import org.jflicks.util.BaseActivator;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Simple activator that starts a lirc job and registers a RC
 * implementation service.
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
        LircRC rc = new LircRC();
        LircRCJob job = new LircRCJob(bc, rc, getConfPath());
        JobContainer jc = JobManager.getJobContainer(job);
        setJobContainer(jc);

        jc.start();
        bc.registerService(RC.class.getName(), rc, null);
    }

    /**
     * {@inheritDoc}
     */
    public void stop(BundleContext context) {

        JobContainer jc = getJobContainer();
        if (jc != null) {
            jc.stop();
        }
    }

    private String getConfPath() {

        File home = new File(".");

        return (home.getAbsolutePath() + "/conf/LircJob.lircrc");
    }

}
