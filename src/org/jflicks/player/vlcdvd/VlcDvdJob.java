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
package org.jflicks.player.vlcdvd;

import java.io.IOException;

import org.jflicks.job.AbstractJob;
import org.jflicks.job.JobContainer;
import org.jflicks.job.JobEvent;
import org.jflicks.job.JobListener;
import org.jflicks.job.JobManager;
import org.jflicks.job.SystemJob;
import org.jflicks.util.LogUtil;

/**
 * This job starts a system job that runs vlc.  It also is a conduit to
 * send vlc commands over stdin.
 *
 * @author Doug Barnum
 * @version 1.0
 */
public class VlcDvdJob extends AbstractJob implements JobListener {

    private VlcDvd vlcDvd;
    private SystemJob systemJob;
    private JobContainer jobContainer;
    private String url;

    /**
     * Constructor with one required argument.
     *
     * @param vlcDvd The player instance that created this job.
     * @param url The url to listen upon.
     */
    public VlcDvdJob(VlcDvd vlcDvd, String url) {

        setVlcDvd(vlcDvd);
        setURL(url);
    }

    private VlcDvd getVlcDvd() {
        return (vlcDvd);
    }

    private void setVlcDvd(VlcDvd p) {
        vlcDvd = p;
    }

    private SystemJob getSystemJob() {
        return (systemJob);
    }

    private void setSystemJob(SystemJob j) {
        systemJob = j;
    }

    private JobContainer getJobContainer() {
        return (jobContainer);
    }

    private void setJobContainer(JobContainer j) {
        jobContainer = j;
    }

    /**
     * When we start we start by listening on a UDP port via a URL.
     *
     * @return The URL as a String.
     */
    public String getURL() {
        return (url);
    }

    /**
     * When we start we start by listening on a UDP port via a URL.
     *
     * @param s The URL as a String.
     */
    public void setURL(String s) {
        url = s;
    }

    /**
     * {@inheritDoc}
     */
    public void start() {

        SystemJob job = SystemJob.getInstance("vlc -I dummy --key-quit q"
            + " --fullscreen " + getURL());

        LogUtil.log(LogUtil.DEBUG, "started: " + job.getCommand());
        job.addJobListener(this);
        setSystemJob(job);
        JobContainer jc = JobManager.getJobContainer(job);
        setJobContainer(jc);
        jc.start();
        setTerminate(false);
    }

    /**
     * {@inheritDoc}
     */
    public void run() {

        while (!isTerminate()) {

            JobManager.sleep(getSleepTime());
        }

        fireJobEvent(JobEvent.COMPLETE);
    }

    /**
     * {@inheritDoc}
     */
    public void stop() {

        JobContainer jc = getJobContainer();
        if (jc != null) {

            jc.stop();
        }

        setTerminate(true);
    }

    /**
     * This method will pass a command to vlc over stdin.
     *
     * @param s The given command to send.
     */
    public void command(String s) {

        SystemJob job = getSystemJob();
        if (job != null) {

            try {

                job.write(s.getBytes(), 0, s.length());

            } catch (IOException ex) {

                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void jobUpdate(JobEvent event) {

        if (event.getType() == JobEvent.COMPLETE) {

            stop();
        }

        fireJobEvent(event);
    }

}

