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
    along with JFLICKS.  If not, see <recorder://www.gnu.org/licenses/>.
*/
package org.jflicks.nms.system;

import java.io.File;
import java.util.Hashtable;

import org.jflicks.discovery.ServiceDescription;
import org.jflicks.discovery.ServiceResponderJob;
import org.jflicks.job.JobContainer;
import org.jflicks.job.JobManager;
import org.jflicks.nms.NMS;
import org.jflicks.util.BaseActivator;
import org.jflicks.util.EventSender;
import org.jflicks.util.Hostname;
import org.jflicks.util.LogUtil;
import org.jflicks.util.Util;

import ch.ethz.iks.r_osgi.RemoteOSGiService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Simple activater that starts our NMS service.
 *
 * @author Doug Barnum
 * @version 1.0
 */
public class Activator extends BaseActivator {

    private RecorderTracker recorderTracker;
    private SchedulerTracker schedulerTracker;
    private LiveTracker liveTracker;
    private CleanerTracker cleanerTracker;
    private PhotoManagerTracker photoManagerTracker;
    private VideoManagerTracker videoManagerTracker;
    private AutoArtTracker autoArtTracker;
    private PostProcTracker postProcTracker;
    private ProgramDataTracker programDataTracker;
    private TrailerTracker trailerTracker;
    private OnDemandTracker onDemandTracker;
    private RemoteTracker remoteTracker;
    private SystemNMS systemNMS;
    private JobContainer removalJobContainer;

    /**
     * {@inheritDoc}
     */
    public void start(BundleContext bc) {

        setupJflicksPath();

        setBundleContext(bc);
        SystemNMS s = new SystemNMS();
        setSystemNMS(s);
        s.setEventSender(EventSender.getInstance(bc));

        RemoveRecordingJob rrj = new RemoveRecordingJob(s);
        JobContainer rjc = JobManager.getJobContainer(rrj);
        setRemovalJobContainer(rjc);
        rjc.start();

        RecorderTracker tracker = new RecorderTracker(bc, s);
        setRecorderTracker(tracker);
        tracker.open();

        SchedulerTracker stracker = new SchedulerTracker(bc, s);
        setSchedulerTracker(stracker);
        stracker.open();

        LiveTracker livetracker = new LiveTracker(bc, s);
        setLiveTracker(livetracker);
        livetracker.open();

        CleanerTracker cleanertracker = new CleanerTracker(bc, s);
        setCleanerTracker(cleanertracker);
        cleanertracker.open();

        PhotoManagerTracker phototracker = new PhotoManagerTracker(bc, s);
        setPhotoManagerTracker(phototracker);
        phototracker.open();

        VideoManagerTracker videotracker = new VideoManagerTracker(bc, s);
        setVideoManagerTracker(videotracker);
        videotracker.open();

        AutoArtTracker autoarttracker = new AutoArtTracker(bc, s);
        setAutoArtTracker(autoarttracker);
        autoarttracker.open();

        PostProcTracker pptracker = new PostProcTracker(bc, s);
        setPostProcTracker(pptracker);
        pptracker.open();

        ProgramDataTracker ptracker = new ProgramDataTracker(bc, s);
        setProgramDataTracker(ptracker);
        ptracker.open();

        TrailerTracker trailtracker = new TrailerTracker(bc, s);
        setTrailerTracker(trailtracker);
        trailtracker.open();

        OnDemandTracker odtracker = new OnDemandTracker(bc, s);
        setOnDemandTracker(odtracker);
        odtracker.open();

        Hashtable<String, Boolean> props = new Hashtable<String, Boolean>();
        props.put(RemoteOSGiService.R_OSGi_REGISTRATION, Boolean.TRUE);
        bc.registerService(NMS.class.getName(), s, props);

        ServiceDescription sd = new ServiceDescription();
        sd.setAddress(Hostname.getLocalhostAddress());
        sd.setPort(9278);
        sd.setInstanceName("SystemNMS");
        LogUtil.log(LogUtil.INFO, "Service details: " + sd.toString());
        s.setTitle("NMS - " + sd.getAddressAsString() + ":" + sd.getPort());
        s.setHost(sd.getAddressAsString());
        s.setPort(9278);
        s.setHttpPort(Util.str2int(
            bc.getProperty("org.osgi.service.http.port"), 8080));

        ServiceResponderJob job = new ServiceResponderJob("nms");
        job.setServiceDescription(sd);
        JobContainer jc = JobManager.getJobContainer(job);
        setJobContainer(jc);
        jc.start();

        RemoteTracker rtracker = new RemoteTracker(bc, sd);
        setRemoteTracker(rtracker);
        rtracker.open();

        // Add our shutdown hook so we close up cleanly.
        Runtime runtime = Runtime.getRuntime();
        Thread thread = new Thread(new ShutDownListener());
        runtime.addShutdownHook(thread);

        // Finally start up the web server
        s.startWebServer();
    }

    /**
     * {@inheritDoc}
     */
    public void stop(BundleContext context) {

        JobContainer rjc = getRemovalJobContainer();
        if (rjc != null) {

            rjc.stop();
        }

        SystemNMS s = getSystemNMS();
        if (s != null) {

            s.stopWebServer();
            s.close();
        }

        RecorderTracker tracker = getRecorderTracker();
        if (tracker != null) {
            tracker.close();
        }

        SchedulerTracker stracker = getSchedulerTracker();
        if (stracker != null) {
            stracker.close();
        }

        LiveTracker livetracker = getLiveTracker();
        if (livetracker != null) {
            livetracker.close();
        }

        CleanerTracker cleanertracker = getCleanerTracker();
        if (cleanertracker != null) {
            cleanertracker.close();
        }

        PhotoManagerTracker phototracker = getPhotoManagerTracker();
        if (phototracker != null) {
            phototracker.close();
        }

        VideoManagerTracker videotracker = getVideoManagerTracker();
        if (videotracker != null) {
            videotracker.close();
        }

        AutoArtTracker autoarttracker = getAutoArtTracker();
        if (autoarttracker != null) {
            autoarttracker.close();
        }

        PostProcTracker pptracker = getPostProcTracker();
        if (pptracker != null) {
            pptracker.close();
        }

        ProgramDataTracker ptracker = getProgramDataTracker();
        if (ptracker != null) {
            ptracker.close();
        }

        TrailerTracker trailtracker = getTrailerTracker();
        if (trailtracker != null) {
            trailtracker.close();
        }

        OnDemandTracker odtracker = getOnDemandTracker();
        if (odtracker != null) {
            odtracker.close();
        }

        RemoteTracker rtracker = getRemoteTracker();
        if (rtracker != null) {
            rtracker.close();
        }

        JobContainer jc = getJobContainer();
        if (jc != null) {
            jc.stop();
        }
    }

    private JobContainer getRemovalJobContainer() {
        return (removalJobContainer);
    }

    private void setRemovalJobContainer(JobContainer jc) {
        removalJobContainer = jc;
    }

    private RecorderTracker getRecorderTracker() {
        return (recorderTracker);
    }

    private void setRecorderTracker(RecorderTracker t) {
        recorderTracker = t;
    }

    private SchedulerTracker getSchedulerTracker() {
        return (schedulerTracker);
    }

    private void setSchedulerTracker(SchedulerTracker t) {
        schedulerTracker = t;
    }

    private LiveTracker getLiveTracker() {
        return (liveTracker);
    }

    private void setLiveTracker(LiveTracker t) {
        liveTracker = t;
    }

    private CleanerTracker getCleanerTracker() {
        return (cleanerTracker);
    }

    private void setCleanerTracker(CleanerTracker t) {
        cleanerTracker = t;
    }

    private PhotoManagerTracker getPhotoManagerTracker() {
        return (photoManagerTracker);
    }

    private void setPhotoManagerTracker(PhotoManagerTracker t) {
        photoManagerTracker = t;
    }

    private VideoManagerTracker getVideoManagerTracker() {
        return (videoManagerTracker);
    }

    private void setVideoManagerTracker(VideoManagerTracker t) {
        videoManagerTracker = t;
    }

    private AutoArtTracker getAutoArtTracker() {
        return (autoArtTracker);
    }

    private void setAutoArtTracker(AutoArtTracker t) {
        autoArtTracker = t;
    }

    private PostProcTracker getPostProcTracker() {
        return (postProcTracker);
    }

    private void setPostProcTracker(PostProcTracker t) {
        postProcTracker = t;
    }

    private ProgramDataTracker getProgramDataTracker() {
        return (programDataTracker);
    }

    private void setProgramDataTracker(ProgramDataTracker t) {
        programDataTracker = t;
    }

    private TrailerTracker getTrailerTracker() {
        return (trailerTracker);
    }

    private void setTrailerTracker(TrailerTracker t) {
        trailerTracker = t;
    }

    private OnDemandTracker getOnDemandTracker() {
        return (onDemandTracker);
    }

    private void setOnDemandTracker(OnDemandTracker t) {
        onDemandTracker = t;
    }

    private RemoteTracker getRemoteTracker() {
        return (remoteTracker);
    }

    private void setRemoteTracker(RemoteTracker t) {
        remoteTracker = t;
    }

    private SystemNMS getSystemNMS() {
        return (systemNMS);
    }

    private void setSystemNMS(SystemNMS s) {
        systemNMS = s;
    }

    private void setupJflicksPath() {

        File dot = new File(".");
        File parent = dot.getParentFile();
        File platform = new File(parent, "platform");
        if ((platform.exists()) && (platform.isDirectory())) {

            String pname = null;
            if (Util.isLinux()) {
                pname = "linux";
            } else if (Util.isMac()) {
                pname = "mac";
            } else if (Util.isWindows()) {
                pname = "win";
            }

            File os = new File(platform, pname);
            if ((os.exists()) && (os.isDirectory())) {

                File bin = new File(os, "bin");
                if ((bin.exists()) && (bin.isDirectory())) {

                    System.setProperty("jflicks.path", bin.getAbsolutePath());
                }
            }
        }
    }

    class ShutDownListener implements Runnable {

        public void run() {

            SystemNMS s = getSystemNMS();
            BundleContext bc = getBundleContext();
            if ((s != null) && (bc != null)) {

                LogUtil.log(LogUtil.INFO, "Shutting down via shutdown hook!");
                try {

                    Bundle b = bc.getBundle(0L);
                    if (b != null) {

                        LogUtil.log(LogUtil.INFO, "BundleContext: stop");
                        b.stop();
                    }

                } catch (BundleException ex) {
                }
            }
        }
    }

}
