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

/**
 * This job will change setup all seetings for the v4l2 device.
 *
 * @author Doug Barnum
 * @version 1.0
 */
public class ControlJob extends BaseV4l2Job {

    private int audioInput;
    private int videoInput;

    /**
     * Simple no argument constructor.
     */
    public ControlJob() {
    }

    /**
     * The audio input index.
     *
     * @return An int value.
     */
    public int getAudioInput() {
        return (audioInput);
    }

    /**
     * The audio input index.
     *
     * @param i An int value.
     */
    public void setAudioInput(int i) {
        audioInput = i;
    }

    /**
     * The video input index.
     *
     * @return An int value.
     */
    public int getVideoInput() {
        return (videoInput);
    }

    /**
     * The video input index.
     *
     * @param i An int value.
     */
    public void setVideoInput(int i) {
        videoInput = i;
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

        SystemJob job = SystemJob.getInstance("v4l2-ctl -d " + getDevice()
            + " --set-input=" + getVideoInput() + " --set-audio-input="
            + getAudioInput());
        System.out.println("command: <" + job.getCommand() + ">");
        setSystemJob(job);
        job.addJobListener(this);
        JobContainer jc = JobManager.getJobContainer(job);
        setJobContainer(jc);
        jc.start();

        while (!isTerminate()) {

            JobManager.sleep(getSleepTime());
        }

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

                System.out.println("ProgramJob: exit: " + job.getExitValue());
                stop();
            }
        }
    }

}
