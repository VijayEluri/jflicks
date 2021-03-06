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
package org.jflicks.tv.postproc.worker;

import java.io.File;

import org.jflicks.job.AbstractJob;
import org.jflicks.job.JobContainer;
import org.jflicks.job.SystemJob;
import org.jflicks.tv.Commercial;
import org.jflicks.tv.Recording;
import org.jflicks.util.LogUtil;
import org.jflicks.util.Util;

/**
 * A base worker job class that workers can extend.
 *
 * @author Doug Barnum
 * @version 1.0
 */
public abstract class BaseWorkerJob extends AbstractJob {

    private Recording recording;
    private SystemJob systemJob;
    private JobContainer jobContainer;
    private BaseWorker baseWorker;
    private String extension;
    private String nice;

    /**
     * Constructor with one required argument.
     *
     * @param r A Recording to process.
     * @param bw The Worker associated with this job.
     */
    public BaseWorkerJob(Recording r, BaseWorker bw) {

        setRecording(r);
        setBaseWorker(bw);

        if (Util.isLinux()) {

            setNice("ionice -c3");
        }
    }

    /**
     * A job acts upon a Recording.
     *
     * @return A Recording instance.
     */
    public Recording getRecording() {
        return (recording);
    }

    /**
     * A job acts upon a Recording.
     *
     * @param r A Recording instance.
     */
    public void setRecording(Recording r) {
        recording = r;
    }

    /**
     * Most workers will need to do some system job so we have one as
     * a property as a convenience to extensions.
     *
     * @return A SystemJob instance.
     */
    public SystemJob getSystemJob() {
        return (systemJob);
    }

    /**
     * Most workers will need to do some system job so we have one as
     * a property as a convenience to extensions.
     *
     * @param j A SystemJob instance.
     */
    public void setSystemJob(SystemJob j) {
        systemJob = j;
    }

    /**
     * Most workers will need to do some job running so we have a
     * JobContainer as a property as a convenience to extensions.
     *
     * @return A JobContainer instance.
     */
    public JobContainer getJobContainer() {
        return (jobContainer);
    }

    /**
     * Most workers will need to do some job running so we have a
     * JobContainer as a property as a convenience to extensions.
     *
     * @param j A JobContainer instance.
     */
    public void setJobContainer(JobContainer j) {
        jobContainer = j;
    }

    /**
     * It's handy for the Job to be able to access the Worker that it is
     * associated.
     *
     * @return A BaseWorker instance.
     */
    public BaseWorker getBaseWorker() {
        return (baseWorker);
    }

    /**
     * It's handy for the Job to be able to access the Worker that it is
     * associated.
     *
     * @param bw A BaseWorker instance.
     */
    public void setBaseWorker(BaseWorker bw) {
        baseWorker = bw;
    }

    public String getExtension() {
        return (extension);
    }

    public void setExtension(String s) {
        extension = s;
    }

    /**
     * A worker can use a "nice" feature so as to not hog up too
     * many resources.  We include this because we have nice and
     * ionice on Linux but nothing like it on Windows.  So at
     * runtime this can be set to the right thing.
     *
     * @return A nice String.
     */
    public String getNice() {
        return (nice);
    }

    /**
     * A worker can use a "nice" feature so as to not hog up too
     * many resources.  We include this because we have nice and
     * ionice on Linux but nothing like it on Windows.  So at
     * runtime this can be set to the right thing.
     *
     * @param s A nice String.
     */
    public void setNice(String s) {
        nice = s;
    }

    public File computeFile(Recording r, boolean hidden) {

        File result = null;

        if (r != null) {

            result = new File(r.getPath());
            if (result.exists()) {

                String tname = null;
                if (hidden) {
                    tname = "." + result.getName() + "." + getExtension();
                } else {
                    tname = result.getName() + "." + getExtension();
                }

                result = new File(result.getParentFile(), tname);
            }
        }

        return (result);
    }

    public void move() {

        Recording r = getRecording();
        if (r != null) {

            File hidden = computeFile(r, true);
            if ((hidden != null) && (hidden.exists())) {

                LogUtil.log(LogUtil.INFO, "moving " + hidden.getPath() + " to "
                    + computeFile(r, false));
                hidden.renameTo(computeFile(r, false));
                r.setIndexedExtension(getExtension());
            }
        }
    }

    public void remove() {

        Recording r = getRecording();
        if (r != null) {

            File hidden = computeFile(r, true);
            if ((hidden != null) && (hidden.exists())) {

                if (!hidden.delete()) {

                    LogUtil.log(LogUtil.INFO, "Failed to delete hidden file.");
                }
            }
        }
    }

    /**
     * This method will return a String pointing to the Recording file
     * but take into account if this Recording is being done using HLS.
     * If it is HLS then the Recording.getPath() may not exist at this
     * point in time.  So we want it to return a path String to a segment.
     *
     * @param index The segment index to check for in case the Recording
     * path doesn't exist.
     * @return A String path to the Recording file to work on.
     */
    public String getRecordingPath(int index) {

        String result = null;

        if (index < 0) {
            index = 0;
        }

        Recording r = getRecording();
        if (r != null) {

            String path = r.getPath();
            File f = new File(path);
            if (f.exists()) {

                result = path;

            } else {

                path = path.substring(0, path.lastIndexOf("."));
                String indexstr = String.format("%06d", index);
                result = path + "." + indexstr + ".ts";
            }
        }

        return (result);
    }

    /**
     * We want to be able to determine if a Recording is being done
     * via HLS.  If it is, then in this moment of time it will not
     * have it's raw path to a .ts file.
     *
     * @return True when in HLS recording mode.
     */
    public boolean isHlsRecording() {

        boolean result = false;

        Recording r = getRecording();
        if (r != null) {

            String path = r.getPath();
            File f = new File(path);
            if (!f.exists()) {

                result = true;
            }
        }

        return (result);
    }

}

