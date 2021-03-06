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
package org.jflicks.tv.live;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.jflicks.configure.BaseConfig;
import org.jflicks.configure.Configuration;
import org.jflicks.configure.NameValue;
import org.jflicks.job.JobManager;
import org.jflicks.nms.NMS;
import org.jflicks.nms.NMSConstants;
import org.jflicks.tv.Channel;
import org.jflicks.tv.LiveTV;
import org.jflicks.tv.recorder.Recorder;
import org.jflicks.tv.scheduler.RecorderInformation;
import org.jflicks.tv.scheduler.Scheduler;
import org.jflicks.util.StartsWithFilter;
import org.jflicks.util.LogUtil;
import org.jflicks.util.Util;

/**
 * This class is a base implementation of the Live interface.
 *
 * @author Doug Barnum
 * @version 1.0
 */
public abstract class BaseLive extends BaseConfig implements Live {

    private String title;
    private NMS nms;
    private ArrayList<Session> sessionList;

    /**
     * Simple empty constructor.
     */
    public BaseLive() {

        setSessionList(new ArrayList<Session>());
    }

    /**
     * {@inheritDoc}
     */
    public String getTitle() {
        return (title);
    }

    /**
     * Convenience method to set this property.
     *
     * @param s The given title value.
     */
    public void setTitle(String s) {
        title = s;
    }

    /**
     * {@inheritDoc}
     */
    public NMS getNMS() {
        return (nms);
    }

    /**
     * {@inheritDoc}
     */
    public void setNMS(NMS n) {
        nms = n;
    }

    private ArrayList<Session> getSessionList() {
        return (sessionList);
    }

    private void setSessionList(ArrayList<Session> l) {
        sessionList = l;
    }

    private void addSession(Session s) {

        ArrayList<Session> l = getSessionList();
        if ((s != null) && (l != null)) {
            l.add(s);
        }
    }

    private void removeSession(Session s) {

        ArrayList<Session> l = getSessionList();
        if ((s != null) && (l != null)) {
            l.remove(s);
        }
    }

    private Session findSession(LiveTV liveTV) {

        Session result = null;

        ArrayList<Session> l = getSessionList();
        if ((liveTV != null) && (l != null)) {

            String id = liveTV.getId();
            if (id != null) {

                for (int i = 0; i < l.size(); i++) {

                    Session tmp = l.get(i);
                    if (tmp != null) {

                        LiveTV ltmp = tmp.getLiveTV();
                        if (id.equals(ltmp.getId())) {

                            result = tmp;
                            break;
                        }
                    }
                }
            }
        }

        return (result);
    }

    /**
     * {@inheritDoc}
     */
    public LiveTV openSession() {
        return (openSession(null));
    }

    /**
     * {@inheritDoc}
     */
    public LiveTV openSession(String channelNumber) {

        LiveTV result = new LiveTV();
        result.setMessageType(LiveTV.MESSAGE_TYPE_NONE);

        NMS n = getNMS();
        Recorder[] recs = getRecorders();
        if ((n != null) && (recs != null)) {

            String[] names = getListingNames();
            if (names != null) {

                ArrayList<RecorderInformation> ris =
                    new ArrayList<RecorderInformation>();
                for (int i = 0; i < names.length; i++) {

                    Recorder tmp = findRecorder(recs, names[i]);
                    if (tmp != null) {

                        RecorderInformation ri = new RecorderInformation();
                        ri.setRecorder(tmp);
                        ri.setChannels(n.getChannelsByListingName(names[i]));
                        ris.add(ri);
                    }
                }

                if (ris.size() > 0) {

                    RecorderInformation[] riArray =
                        ris.toArray(new RecorderInformation[ris.size()]);
                    Session session = new Session(result, riArray);
                    addSession(session);

                    result.setChannels(computeAllChannels(session));
                    Channel c = computeStartChannel(session, channelNumber);
                    changeChannel(result, c);

                } else {

                    result.setMessageType(LiveTV.MESSAGE_TYPE_ERROR);
                    result.setMessage("No Available Recorders!");
                }

            } else {

                result.setMessageType(LiveTV.MESSAGE_TYPE_ERROR);
                result.setMessage("No Guide data!");
            }

        } else {

            result.setMessageType(LiveTV.MESSAGE_TYPE_ERROR);
            result.setMessage("No Recorders!");
        }

        return (result);
    }

    /**
     * {@inheritDoc}
     */
    public LiveTV openSession(String host, int port) {
        return (openSession(host, port, null));
    }

    /**
     * {@inheritDoc}
     */
    public LiveTV openSession(String host, int port, String channelNumber) {

        LiveTV result = new LiveTV(host, port);
        result.setMessageType(LiveTV.MESSAGE_TYPE_NONE);

        NMS n = getNMS();
        Recorder[] recs = getRecorders();
        if ((n != null) && (recs != null)) {

            String[] names = getListingNames();
            if (names != null) {

                ArrayList<RecorderInformation> ris =
                    new ArrayList<RecorderInformation>();
                for (int i = 0; i < names.length; i++) {

                    Recorder tmp = findRecorder(recs, names[i]);
                    if (tmp != null) {

                        RecorderInformation ri = new RecorderInformation();
                        ri.setRecorder(tmp);
                        ri.setChannels(n.getChannelsByListingName(names[i]));
                        ris.add(ri);
                    }
                }

                if (ris.size() > 0) {

                    RecorderInformation[] riArray =
                        ris.toArray(new RecorderInformation[ris.size()]);
                    Session session = new Session(result, riArray);
                    addSession(session);

                    result.setChannels(computeAllChannels(session));
                    Channel c = computeStartChannel(session, channelNumber);
                    changeChannel(result, c);

                } else {

                    result.setMessageType(LiveTV.MESSAGE_TYPE_ERROR);
                    result.setMessage("No Available Recorders!");
                }

            } else {

                result.setMessageType(LiveTV.MESSAGE_TYPE_ERROR);
                result.setMessage("No Guide data!");
            }

        } else {

            result.setMessageType(LiveTV.MESSAGE_TYPE_ERROR);
            result.setMessage("No Recorders!");
        }

        return (result);
    }

    /**
     * {@inheritDoc}
     */
    public LiveTV changeChannel(LiveTV l, Channel c) {

        LiveTV result = null;

        if ((l != null) && (c != null)) {

            result = changeChannelRecording(l, c);
        }

        return (result);
    }

    private LiveTV changeChannelRecording(LiveTV l, Channel c) {

        if ((l != null) && (c != null)) {

            Session s = findSession(l);
            if (s != null) {

                // We tear down the old to start a new channel.  We don't
                // care about quick tuning as we should never change
                // channels on a recording.
                Recorder old = s.getCurrentRecorder();
                if (old != null) {

                    LogUtil.log(LogUtil.DEBUG, "stop recording");
                    old.stopRecording();
                    cleanup(l);
                }

                Recorder r = computeRecorder(s, c);
                LogUtil.log(LogUtil.DEBUG, "channel: " + c);
                LogUtil.log(LogUtil.DEBUG, "recorder: " + r);
                if ((r != null) && (!r.isRecording())) {

                    File output = computeFile(s, c, r.getExtension());
                    if (output != null) {

                        // If we are using the same recorder, let's sleep
                        // a short time so things can settle down.
                        if (r.equals(old)) {

                            LogUtil.log(LogUtil.DEBUG, "waiting a bit...");
                            JobManager.sleep(500);
                        }

                        LogUtil.log(LogUtil.DEBUG, "recording to file <" + output + ">");
                        s.setCurrentRecorder(r);
                        r.startRecording(c, 60 * 60 * 4, output, true);

                        String hls = output.getPath();
                        hls = hls.substring(0, hls.lastIndexOf("."));
                        hls = hls + ".m3u8";
                        l.setPath(hls);

                        l.setCurrentChannel(c);

                        // We are going to block here until we
                        // have a valid files.  Things take time
                        // to spin up.
                        int minseg = getConfiguredMinimumSegmentCount();
                        int loopmax = minseg * 10;
                        File m3u8 = new File(hls);
                        int loops = 0;
                        boolean done = false;
                        while (!done) {

                            loops++;
                            done = count(l) >= minseg;
                            if (!done) {

                                done = loops > loopmax;
                            }

                            try {

                                Thread.sleep(1000);

                            } catch (Exception ex) {
                            }
                        }

                    } else {

                        l.setMessageType(LiveTV.MESSAGE_TYPE_ERROR);
                        l.setMessage("No File!");
                        LogUtil.log(LogUtil.DEBUG, "No File!");
                    }
                }

            } else {

                l.setMessageType(LiveTV.MESSAGE_TYPE_ERROR);
                l.setMessage("No Available Recorders!");
                LogUtil.log(LogUtil.DEBUG, "No Available Recorders!");
            }
        }

        return (l);
    }

    private int count(LiveTV l) {

        int result = 0;

        File file = new File(l.getPath());
        File parent = file.getParentFile();
        String fname = file.getName();
        if ((parent != null) && (fname != null)) {

            // This really should get everything.
            fname = fname.substring(0, fname.lastIndexOf("."));
            File[] array =
                parent.listFiles(new StartsWithFilter(fname));
            if (array != null) {

                result = array.length;
            }
        }

        return (result);
    }

    private void cleanup(LiveTV l) {

        if (l != null) {

            File file = new File(l.getPath());
            File parent = file.getParentFile();
            String fname = file.getName();
            if ((parent != null) && (fname != null)) {

                // This really should get everything.
                fname = fname.substring(0, fname.lastIndexOf("."));
                File[] array =
                    parent.listFiles(new StartsWithFilter(fname));
                if (array != null) {

                    for (int i = 0; i < array.length; i++) {

                        if (!array[i].delete()) {
                            LogUtil.log(LogUtil.WARNING, array[i].getPath() + " del fail");
                        }
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void closeSession(LiveTV l) {

        if (l != null) {

            Session s = findSession(l);
            if (s != null) {

                Recorder r = s.getCurrentRecorder();
                if (r != null) {

                    r.stopRecording();
                    cleanup(l);
                    LogUtil.log(LogUtil.DEBUG, "closeSession: stopRecording");
                }
            }
        }
    }

    private String getConfiguredLiveDirectory() {

        String result = null;

        Configuration c = getConfiguration();
        if (c != null) {

            NameValue nv = c.findNameValueByName(NMSConstants.LIVE_DIRECTORY);
            if (nv != null) {

                result = nv.getValue();
            }
        }

        return (result);
    }

    private String getConfiguredStartChannel() {

        String result = null;

        Configuration c = getConfiguration();
        if (c != null) {

            NameValue nv =
                c.findNameValueByName(NMSConstants.LIVE_START_CHANNEL);
            if (nv != null) {

                result = nv.getValue();
            }
        }

        return (result);
    }

    private int getConfiguredMinimumSegmentCount() {

        int result = 6;

        Configuration c = getConfiguration();
        if (c != null) {

            NameValue nv =
                c.findNameValueByName(NMSConstants.MINIMUM_SEGMENT_COUNT);
            if (nv != null) {

                result = Util.str2int(nv.getValue(), result);
            }
        }

        return (result);
    }

    private Recorder[] getRecorders() {

        Recorder[] result = null;

        NMS n = getNMS();
        if (n != null) {

            Scheduler s = n.getScheduler();
            if (s != null) {

                Recorder[] array = s.getConfiguredRecorders();
                if ((array != null) && (array.length > 0)) {

                    ArrayList<Recorder> rlist = new ArrayList<Recorder>();
                    for (int i = 0; i < array.length; i++) {

                        if (array[i].isHlsMode()) {

                            rlist.add(array[i]);
                        }
                    }

                    if (rlist.size() > 0) {

                        result = rlist.toArray(new Recorder[rlist.size()]);
                    }
                }
            }
        }

        return (result);
    }

    private String[] getListingNames() {

        String[] result = null;

        NMS n = getNMS();
        if (n != null) {

            Scheduler s = n.getScheduler();
            if (s != null) {

                result = s.getConfiguredListingNames();
            }
        }

        return (result);
    }

    private Recorder findRecorder(Recorder[] array, String name) {

        Recorder result = null;

        if ((array != null) && (name != null)) {

            NMS n = getNMS();
            if (n != null) {

                Scheduler s = n.getScheduler();
                if (s != null) {

                    for (int i = 0; i < array.length; i++) {

                        if (!array[i].isRecording()) {

                            String tmp = s.getListingNameByRecorder(array[i]);
                            LogUtil.log(LogUtil.DEBUG, "findRecorder: found <" + tmp + ">");
                            if ((tmp != null) && (tmp.equals(name))) {

                                // Found one.  We won't break because we want
                                // to use the least likely tuner to be used
                                // for a recording which would be later in
                                // the list.
                                result = array[i];
                            }
                        }
                    }
                }
            }
        }

        return (result);
    }

    private Channel findChannel(Channel[] array, String cnumber) {

        Channel result = null;

        if ((array != null) && (cnumber != null)) {

            for (int i = 0; i < array.length; i++) {

                if (cnumber.equals(array[i].getNumber())) {

                    result = array[i];
                    break;
                }
            }
        }

        return (result);
    }

    private Channel computeStartChannel(Session s, String startc) {

        Channel result = null;

        if (s != null) {

            RecorderInformation[] array = s.getRecorderInformations();
            if ((array != null) && (array.length > 0)) {

                if (startc == null) {
                    startc = getConfiguredStartChannel();
                }
                LogUtil.log(LogUtil.DEBUG, "start channel: " + startc);
                if (startc != null) {

                    for (int i = 0; i < array.length; i++) {

                        result = findChannel(array[i].getChannels(), startc);
                        if (result != null) {
                            break;
                        }
                    }
                }

                if (result == null) {

                    // We didn't find the right Channel so let's just
                    // set it to something so we don't crap out.
                    for (int i = 0; i < array.length; i++) {

                        Channel[] chans = array[i].getChannels();
                        if ((chans != null) && (chans.length > 0)) {

                            result = chans[0];
                            break;
                        }
                    }
                }
            }
        }

        return (result);
    }

    private Channel[] computeAllChannels(Session s) {

        Channel[] result = null;

        RecorderInformation[] array = s.getRecorderInformations();
        if (array != null) {

            LogUtil.log(LogUtil.DEBUG, "channel count: " + array.length);
            ArrayList<Channel> clist = new ArrayList<Channel>();
            for (int i = 0; i < array.length; i++) {

                Channel[] tmp = array[i].getChannels();
                if (tmp != null) {

                    LogUtil.log(LogUtil.DEBUG, "computeAllChannels: adding "
                        + tmp.length + " channels");
                    Collections.addAll(clist, tmp);
                }
            }

            if (clist.size() > 0) {

                result = clist.toArray(new Channel[clist.size()]);
                Arrays.sort(result);
            }
        }

        return (result);
    }

    private Recorder computeRecorder(Session s, Channel c) {

        Recorder result = null;

        if ((s != null) && (c != null)) {

            RecorderInformation[] array = s.getRecorderInformations();
            if (array != null) {

                for (int i = 0; i < array.length; i++) {

                    if (array[i].supports(c)) {

                        result = array[i].getRecorder();
                        break;
                    }
                }
            }
        }

        return (result);
    }

    private File computeFile(Session s, Channel c, String ext) {

        File result = null;

        String sdir = getConfiguredLiveDirectory();
        if ((s != null) && (sdir != null) && (c != null)) {

            File dir = new File(sdir);
            if ((dir.exists()) && (dir.isDirectory())) {

                LiveTV l = s.getLiveTV();
                if (l != null) {

                    String id = l.getId() + "-" + System.currentTimeMillis();
                    result = new File(dir, "live-" + id + "-" + c.getNumber()
                        + "." + ext);
                }
            }
        }

        return (result);
    }

}

