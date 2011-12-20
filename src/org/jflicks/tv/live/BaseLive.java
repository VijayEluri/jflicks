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
import org.jflicks.nms.NMS;
import org.jflicks.nms.NMSConstants;
import org.jflicks.tv.Channel;
import org.jflicks.tv.LiveTV;
import org.jflicks.tv.recorder.Recorder;
import org.jflicks.tv.scheduler.RecorderInformation;
import org.jflicks.tv.scheduler.Scheduler;

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
                    Channel c = computeStartChannel(session);
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
                    Channel c = computeStartChannel(session);
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

            if (l.isStreaming()) {
                result = changeChannelStreaming(l, c);
            } else {
                result = changeChannelRecording(l, c);
            }
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

                    log(DEBUG, "stop recording");
                    old.stopRecording();
                    File output = old.getDestination();
                    if (output != null) {

                        if (!output.delete()) {

                            log(WARNING, "Failed to delete live file.");
                        }
                    }
                }

                Recorder r = computeRecorder(s, c);
                log(DEBUG, "channel: " + c);
                log(DEBUG, "recorder: " + r);
                if ((r != null) && (!r.isRecording())) {

                    File output = computeFile(s, c, r.getExtension());
                    if (output != null) {

                        log(DEBUG, "recording to file <" + output + ">");
                        s.setCurrentRecorder(r);
                        r.startRecording(c, 60 * 60 * 4, output, true);
                        l.setPath(output.getPath());
                        l.setCurrentChannel(c);

                    } else {

                        l.setMessageType(LiveTV.MESSAGE_TYPE_ERROR);
                        l.setMessage("No File!");
                        log(DEBUG, "No File!");
                    }
                }

            } else {

                l.setMessageType(LiveTV.MESSAGE_TYPE_ERROR);
                l.setMessage("No Available Recorders!");
                log(DEBUG, "No Available Recorders!");
            }
        }

        return (l);
    }

    private LiveTV changeChannelStreaming(LiveTV l, Channel c) {

        if ((l != null) && (c != null)) {

            Session s = findSession(l);
            if (s != null) {

                // Assume quickTune is out of the mix...
                boolean doQuickTune = false;

                Recorder old = s.getCurrentRecorder();
                Recorder r = computeRecorder(s, c);
                if (old != null) {

                    // If we are switching recorders, then we have to stop
                    // the current stream - no question.
                    if (!old.equals(r)) {

                        log(DEBUG, "stop streaming - changing recorders");
                        old.stopStreaming();

                    } else {

                        // We have the same recorder but have to check if
                        // quick tuning is possible...
                        if (r.isQuickTunable()) {

                            doQuickTune = true;

                        } else {

                            log(DEBUG, "stop streaming because no quick tune");
                            old.stopStreaming();
                        }
                    }
                }

                log(DEBUG, "channel: " + c);
                log(DEBUG, "recorder: " + r);
                if (r != null) {

                    s.setCurrentRecorder(r);

                    if (doQuickTune) {

                        log(DEBUG, "already streaming - do quicktune....");
                        r.quickTune(c);

                    } else {

                        if (!r.isRecording()) {

                            log(DEBUG, "starting to stream...");
                            r.startStreaming(c, l.getDestinationHost(),
                                l.getDestinationPort());
                        }
                    }

                    l.setCurrentChannel(c);

                } else {

                    l.setMessageType(LiveTV.MESSAGE_TYPE_ERROR);
                    l.setMessage("No Available Recorders!");
                    log(DEBUG, "No Available Recorders!");
                }

            } else {

                l.setMessageType(LiveTV.MESSAGE_TYPE_ERROR);
                l.setMessage("Invalid session!");
                log(DEBUG, "Invalid session!");
            }
        }

        return (l);
    }

    /**
     * {@inheritDoc}
     */
    public void closeSession(LiveTV l) {

        if (l != null) {

            boolean streaming = l.isStreaming();
            Session s = findSession(l);
            if (s != null) {

                Recorder r = s.getCurrentRecorder();
                if (r != null) {

                    if (streaming) {

                        r.stopStreaming();
                        log(DEBUG, "closeSession: stopStreaming");

                    } else {

                        r.stopRecording();
                        File output = r.getDestination();
                        log(DEBUG, "closeSession: " + output);
                        if (output != null) {

                            if (!output.delete()) {

                                log(WARNING, "Failed to delete live file.");
                            }
                        }
                    }
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

    private Recorder[] getRecorders() {

        Recorder[] result = null;

        NMS n = getNMS();
        if (n != null) {

            Scheduler s = n.getScheduler();
            if (s != null) {

                result = s.getConfiguredRecorders();
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
                            log(DEBUG, "findRecorder: found <" + tmp + ">");
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

    private Channel computeStartChannel(Session s) {

        Channel result = null;

        if (s != null) {

            RecorderInformation[] array = s.getRecorderInformations();
            if (array != null) {

                String startc = getConfiguredStartChannel();
                log(DEBUG, "start channel: " + startc);
                if (startc != null) {

                    for (int i = 0; i < array.length; i++) {

                        result = findChannel(array[i].getChannels(), startc);
                        if (result != null) {
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

            log(DEBUG, "channel count: " + array.length);
            ArrayList<Channel> clist = new ArrayList<Channel>();
            for (int i = 0; i < array.length; i++) {

                Channel[] tmp = array[i].getChannels();
                if (tmp != null) {

                    log(DEBUG, "computeAllChannels: adding "
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

