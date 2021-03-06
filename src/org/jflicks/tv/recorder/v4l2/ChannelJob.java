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
package org.jflicks.tv.recorder.v4l2;

import org.jflicks.job.JobContainer;
import org.jflicks.job.JobEvent;
import org.jflicks.job.JobManager;
import org.jflicks.job.SystemJob;
import org.jflicks.tv.recorder.BaseDeviceJob;

/**
 * This job will change the channel for a V4l2 device.  It can use the
 * v4l2-ctl program to set the frequency or optionally use an external
 * channel changing script.  This latter choice will be common for V4l2
 * devices hooked up to externel set top boxes.  Also required for the
 * HD-PVR which does not have a tuner.
 *
 * @author Doug Barnum
 * @version 1.0
 */
public class ChannelJob extends BaseDeviceJob {

    private String channel;
    private String frequencyTable;
    private String script;

    /**
     * Simple no argument constructor.
     */
    public ChannelJob() {
    }

    /**
     * The channel value is the channel number.
     *
     * @return The channel as a String.
     */
    public String getChannel() {
        return (channel);
    }

    /**
     * The channel value is the channel number.
     *
     * @param s The channel as a String.
     */
    public void setChannel(String s) {
        channel = s;
    }

    /**
     * The frequency table to use for older analog tuners.
     *
     * @return The frequency table name as a String.
     */
    public String getFrequencyTable() {
        return (frequencyTable);
    }

    /**
     * The frequency table to use for older analog tuners.
     *
     * @param s The frequency table name as a String.
     */
    public void setFrequencyTable(String s) {
        frequencyTable = s;
    }

    /**
     * Optionally a channel can be set by running an external script with
     * the Channel property as an argument.  Otherwise the channel will be
     * set using v4l2-ctl.
     *
     * @return A path to a script.
     */
    public String getScript() {
        return (script);
    }

    /**
     * Optionally a channel can be set by running an external script with
     * the Channel property as an argument.  Otherwise the channel will be
     * set using v4l2-ctl.
     *
     * @param s A path to a script.
     */
    public void setScript(String s) {
        script = s;
    }

    /**
     * {@inheritDoc}
     */
    public void start() {

        setTerminate(false);
    }

    /**
     * {@inheritDoc}
     */
    public void run() {

        SystemJob job = null;
        String scr = getScript();
        if (scr != null) {

            job = SystemJob.getInstance(scr + " " + getChannel());

        } else {

            String chan = getChannel();
            if (chan != null) {

                job = SystemJob.getInstance("ivtv-tune -d " + getDevice()
                    + " --freqtable=" + getFrequencyTable()
                    + " --channel=" + getChannel());
            }
        }

        if (job != null) {

            fireJobEvent(JobEvent.UPDATE, "command: <" + job.getCommand()
                + ">");
            setSystemJob(job);
            job.addJobListener(this);
            JobContainer jc = JobManager.getJobContainer(job);
            setJobContainer(jc);
            jc.start();

            while (!isTerminate()) {

                JobManager.sleep(getSleepTime());
            }
        }


        // We sleep here because it might take some time to have
        // the v4l2  device "sync-up" with it's source.  This
        // was found when testing the HD-PVR as just a bit
        // previously the channel was changed by a script.
        // Obviously hard-wiring a value here is not the best
        // solution.
        JobManager.sleep(2000);

        fireJobEvent(JobEvent.COMPLETE);
    }

    /**
     * {@inheritDoc}
     */
    public void stop() {

        setTerminate(true);
        JobContainer jc = getJobContainer();
        if (jc != null) {

            jc.stop();
            setJobContainer(null);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void jobUpdate(JobEvent event) {

        if (event.getType() == JobEvent.COMPLETE) {

            SystemJob job = getSystemJob();
            if (job != null) {

                fireJobEvent(JobEvent.UPDATE, "ProgramJob: exit: "
                    + job.getExitValue());
                stop();
            }
        }
    }

}
