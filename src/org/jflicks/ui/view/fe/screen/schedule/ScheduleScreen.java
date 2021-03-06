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
package org.jflicks.ui.view.fe.screen.schedule;

import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.jflicks.job.JobContainer;
import org.jflicks.job.JobEvent;
import org.jflicks.job.JobListener;
import org.jflicks.job.JobManager;
import org.jflicks.nms.NMS;
import org.jflicks.nms.NMSConstants;
import org.jflicks.nms.NMSUtil;
import org.jflicks.photomanager.Tag;
import org.jflicks.rc.RC;
import org.jflicks.tv.Airing;
import org.jflicks.tv.Channel;
import org.jflicks.tv.RecordingRule;
import org.jflicks.tv.Show;
import org.jflicks.tv.ShowAiring;
import org.jflicks.tv.Upcoming;
import org.jflicks.ui.view.fe.AddRuleJob;
import org.jflicks.ui.view.fe.AllGuideJob;
import org.jflicks.ui.view.fe.ButtonPanel;
import org.jflicks.ui.view.fe.Dialog;
import org.jflicks.ui.view.fe.GridGuidePanel;
import org.jflicks.ui.view.fe.NMSProperty;
import org.jflicks.ui.view.fe.ParameterProperty;
import org.jflicks.ui.view.fe.RecordingRuleListPanel;
import org.jflicks.ui.view.fe.RecordingRulePanel;
import org.jflicks.ui.view.fe.RecordingRuleProperty;
import org.jflicks.ui.view.fe.ShowDetailPanel;
import org.jflicks.ui.view.fe.TagListPanel;
import org.jflicks.ui.view.fe.UpcomingListPanel;
import org.jflicks.ui.view.fe.UpcomingDetailPanel;
import org.jflicks.ui.view.fe.UpcomingProperty;
import org.jflicks.ui.view.fe.screen.Screen;
import org.jflicks.ui.view.fe.screen.ScreenEvent;
import org.jflicks.util.Busy;
import org.jflicks.util.Util;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.MattePainter;

/**
 * A screen to schedule recordings by title, using a guide or just to edit
 * rules.
 *
 * @author Doug Barnum
 * @version 1.0
 */
public class ScheduleScreen extends Screen implements ParameterProperty,
    NMSProperty, UpcomingProperty, PropertyChangeListener, JobListener,
    ActionListener, RecordingRuleProperty {

    private static final String CANCEL = "Cancel";

    private static final String BY_TITLE = "By Title";
    private static final String BY_GUIDE = "Using Guide";
    private static final String UPCOMING_RECORDINGS = "Upcoming Recordings";
    private static final int ALL_UPCOMING = 1;
    private static final int RECORDING_UPCOMING = 2;
    private static final int NOT_RECORDING_UPCOMING = 3;
    private static final String ALL_UPCOMING_TEXT = "Showing All";
    private static final String RECORDING_UPCOMING_TEXT = "Showing Recording";
    private static final String NOT_RECORDING_UPCOMING_TEXT =
        "Showing Not Recording";
    private static final int ALL_CHANNELS = 1;
    private static final int FAVORITE_CHANNELS = 2;
    private static final String ALL_CHANNELS_TEXT = "All Channels";
    private static final String FAVORITE_CHANNELS_TEXT = "Favorite Channels";
    private static final String ADD_FAVORITE = "Add to Favorites";
    private static final String SWITCH_TO_FAVORITE = "Switch to Favorites";
    private static final String REMOVE_FAVORITE = "Remove from Favorites";
    private static final String SWITCH_FROM_FAVORITE = "Switch from Favorites";

    private NMS[] nms;
    private String[] parameters;
    private String selectedParameter;
    private boolean updatedParameter;
    private Upcoming[] upcomings;
    private RecordingRule[] recordingRules;
    private HashMap<Channel, ShowAiring[]> guideMap;
    private JobContainer allGuideJobContainer;
    private long lastGuide;
    private HashMap<Tag, TagValue> tagMap;
    private Channel[] allChannels;
    private ArrayList<String> favoriteChannelList;

    private JXPanel waitPanel;
    private TagListPanel titleTagListPanel;
    private ShowDetailPanel showDetailPanel;
    private RecordingRulePanel recordingRulePanel;
    private GridGuidePanel gridGuidePanel;
    private RecordingRuleListPanel recordingRuleListPanel;
    private ShowAiring selectedShowAiring;
    private RecordingRule selectedRecordingRule;
    private UpcomingListPanel upcomingListPanel;
    private UpcomingDetailPanel upcomingDetailPanel;
    private JXLabel upcomingLabel;
    private int upcomingState;
    private JXLabel channelLabel;
    private int channelState;
    private ButtonPanel userButtonPanel;
    private boolean popupEnabled;

    /**
     * Simple empty constructor.
     */
    public ScheduleScreen() {

        setTitle("Schedule");
        BufferedImage bi = getImageByName("Schedule");
        setDefaultBackgroundImage(bi);
        setFavoriteChannelList(new ArrayList<String>());

        setFocusable(true);
        requestFocus();
        setUpcomingState(ALL_UPCOMING);
        setUpcomingLabel(new JXLabel(ALL_UPCOMING_TEXT));
        setChannelState(ALL_CHANNELS);
        setChannelLabel(new JXLabel(ALL_CHANNELS_TEXT));

        InputMap map = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

        LeftAction leftAction = new LeftAction();
        map.put(KeyStroke.getKeyStroke("LEFT"), "left");
        getActionMap().put("left", leftAction);

        RightAction rightAction = new RightAction();
        map.put(KeyStroke.getKeyStroke("RIGHT"), "right");
        getActionMap().put("right", rightAction);

        UpAction upAction = new UpAction();
        map.put(KeyStroke.getKeyStroke("UP"), "up");
        getActionMap().put("up", upAction);

        DownAction downAction = new DownAction();
        map.put(KeyStroke.getKeyStroke("DOWN"), "down");
        getActionMap().put("down", downAction);

        PageUpAction pageUpAction = new PageUpAction();
        map.put(KeyStroke.getKeyStroke("PAGE_UP"), "pageup");
        getActionMap().put("pageup", pageUpAction);

        PageDownAction pageDownAction = new PageDownAction();
        map.put(KeyStroke.getKeyStroke("PAGE_DOWN"), "pagedown");
        getActionMap().put("pagedown", pageDownAction);

        EnterAction enterAction = new EnterAction();
        map.put(KeyStroke.getKeyStroke("ENTER"), "enter");
        getActionMap().put("enter", enterAction);

        InfoAction infoAction = new InfoAction();
        map.put(KeyStroke.getKeyStroke("I"), "info");
        getActionMap().put("info", infoAction);

        String[] array = {

            BY_TITLE,
            BY_GUIDE,
            UPCOMING_RECORDINGS
        };

        setParameters(array);

        setTagMap(new HashMap<Tag, TagValue>());

        RecordingRulePanel rrp = new RecordingRulePanel();
        HashSet<AWTKeyStroke> set =
            new HashSet<AWTKeyStroke>(rrp.getFocusTraversalKeys(
                KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        set.clear();
        set.add(KeyStroke.getKeyStroke("DOWN"));
        rrp.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
            set);

        set = new HashSet<AWTKeyStroke>(rrp.getFocusTraversalKeys(
                KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        set.clear();
        set.add(KeyStroke.getKeyStroke("UP"));
        rrp.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
            set);
        setRecordingRulePanel(rrp);
    }

    /**
     * {@inheritDoc}
     */
    public NMS[] getNMS() {

        NMS[] result = null;

        if (nms != null) {

            result = Arrays.copyOf(nms, nms.length);
        }

        return (result);
    }

    /**
     * {@inheritDoc}
     */
    public void setNMS(NMS[] array) {

        if (array != null) {
            nms = Arrays.copyOf(array, array.length);
        } else {
            nms = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public String[] getParameters() {

        String[] result = null;

        if (parameters != null) {

            result = Arrays.copyOf(parameters, parameters.length);
        }

        return (result);
    }

   private void setParameters(String[] array) {

        if (array != null) {
            parameters = Arrays.copyOf(array, array.length);
        } else {
            parameters = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getSelectedParameter() {
        return (selectedParameter);
    }

    /**
     * {@inheritDoc}
     */
    public void setSelectedParameter(String s) {

        if ((s != null) && (selectedParameter != null)) {
            setUpdatedParameter(!s.equals(selectedParameter));
        } else {
            setUpdatedParameter(true);
        }

        selectedParameter = s;
    }

    private boolean isUpdatedParameter() {
        return (updatedParameter);
    }

    private void setUpdatedParameter(boolean b) {
        updatedParameter = b;
    }

    /**
     * {@inheritDoc}
     */
    public Upcoming[] getUpcomings() {

        Upcoming[] result = null;

        if (upcomings != null) {

            result = Arrays.copyOf(upcomings, upcomings.length);
        }

        return (result);
    }

    /**
     * {@inheritDoc}
     */
    public void setUpcomings(Upcoming[] array) {

        if (array != null) {
            upcomings = Arrays.copyOf(array, array.length);
        } else {
            upcomings = null;
        }

        applyUpcoming();
    }

    private ArrayList<String> getFavoriteChannelList() {
        return (favoriteChannelList);
    }

    private void setFavoriteChannelList(ArrayList<String> l) {
        favoriteChannelList = l;
    }

    private Channel[] getAllChannels() {
        return (allChannels);
    }

    private void setAllChannels(Channel[] array) {
        allChannels = array;
    }

    private int getUpcomingState() {
        return (upcomingState);
    }

    private void setUpcomingState(int i) {
        upcomingState = i;
    }

    private int getChannelState() {
        return (channelState);
    }

    private void setChannelState(int i) {
        channelState = i;
    }

    private Upcoming[] filterByRule(Upcoming[] array) {

        Upcoming[] result = null;

        RecordingRule rr = getSelectedRecordingRule();
        if ((array != null) && (rr != null)) {

            String name = rr.getName();
            if (name.equals("All")) {

                result = array;

            } else {

                String seriesId = rr.getSeriesId();
                if (seriesId != null) {

                    // The user has selected a particular rule.
                    ArrayList<Upcoming> l = new ArrayList<Upcoming>();
                    for (int i = 0; i < array.length; i++) {

                        if (seriesId.equals(array[i].getSeriesId())) {

                            l.add(array[i]);
                        }
                    }

                    if (l.size() > 0) {

                        result = l.toArray(new Upcoming[l.size()]);
                    }
                }
            }
        }

        return (result);
    }

    private void applyUpcoming() {

        Upcoming[] array = getUpcomings();
        UpcomingListPanel ulp = getUpcomingListPanel();
        if ((array != null) && (ulp != null)) {

            array = filterByRule(array);
            ArrayList<Upcoming> list = new ArrayList<Upcoming>();
            int state = getUpcomingState();
            switch (state) {

            case ALL_UPCOMING:
            default:

                ulp.setUpcomings(array);

                if (array == null) {

                    UpcomingDetailPanel dp = getUpcomingDetailPanel();
                    if (dp != null) {

                        dp.setUpcoming(null);
                    }
                }
                break;

            case RECORDING_UPCOMING:

                if (array != null) {

                    for (int i = 0; i < array.length; i++) {

                        if (NMSConstants.READY.equals(array[i].getStatus())) {

                            list.add(array[i]);
                        }
                    }
                }
                if (list.size() > 0) {

                    array = list.toArray(new Upcoming[list.size()]);
                    ulp.setUpcomings(array);

                } else {

                    ulp.setUpcomings(null);

                    UpcomingDetailPanel dp = getUpcomingDetailPanel();
                    if (dp != null) {

                        dp.setUpcoming(null);
                    }
                }
                break;

            case NOT_RECORDING_UPCOMING:

                if (array != null) {

                    for (int i = 0; i < array.length; i++) {

                        if (!NMSConstants.READY.equals(array[i].getStatus())) {

                            list.add(array[i]);
                        }
                    }
                }
                if (list.size() > 0) {

                    array = list.toArray(new Upcoming[list.size()]);
                    ulp.setUpcomings(array);

                } else {

                    ulp.setUpcomings(null);

                    UpcomingDetailPanel dp = getUpcomingDetailPanel();
                    if (dp != null) {

                        dp.setUpcoming(null);
                    }
                }
                break;
            }

            ulp.setSelectedIndex(0);
        }
    }

    private Channel[] filterByFavorite(Channel[] array) {

        Channel[] result = null;

        ArrayList<String> favlist = getFavoriteChannelList();
        if ((array != null) && (favlist != null)) {

            ArrayList<Channel> filter = new ArrayList<Channel>();
            for (int i = 0; i < favlist.size(); i++) {

                String tmp = favlist.get(i);
                if (tmp != null) {

                    for (int j = 0; j < array.length; j++) {

                        if (tmp.equals(array[j].toString())) {

                            filter.add(array[j]);
                            break;
                        }
                    }
                }
            }

            if (filter.size() > 0) {

                Collections.sort(filter);
                result = filter.toArray(new Channel[filter.size()]);
            }
        }

        return (result);
    }

    private void applyChannels() {

        GridGuidePanel ggp = getGridGuidePanel();
        Channel[] carray = getAllChannels();
        JXLabel l = getChannelLabel();
        if ((ggp != null) && (carray != null) && (l != null)) {

            if (getChannelState() == ALL_CHANNELS) {

                ggp.setChannels(carray);
                l.setText(ALL_CHANNELS_TEXT);

            } else if (getChannelState() == FAVORITE_CHANNELS) {

                Channel[] only = filterByFavorite(carray);
                if (only != null) {

                    ggp.setChannels(only);
                    l.setText(FAVORITE_CHANNELS_TEXT);

                } else {

                    setChannelState(ALL_CHANNELS);
                }
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    public RecordingRule[] getRecordingRules() {

        RecordingRule[] result = null;

        if (recordingRules != null) {

            result = Arrays.copyOf(recordingRules, recordingRules.length);
        }

        return (result);
    }

    /**
     * {@inheritDoc}
     */
    public void setRecordingRules(RecordingRule[] array) {

        if (array != null) {
            recordingRules = Arrays.copyOf(array, array.length);
        } else {
            recordingRules = null;
        }
    }

    private boolean isParameterByTitle() {
        return (BY_TITLE.equals(getSelectedParameter()));
    }

    private boolean isParameterByGuide() {
        return (BY_GUIDE.equals(getSelectedParameter()));
    }

    private boolean isParameterUpcomingRecordings() {
        return (UPCOMING_RECORDINGS.equals(getSelectedParameter()));
    }

    private TagListPanel getTitleTagListPanel() {
        return (titleTagListPanel);
    }

    private void setTitleTagListPanel(TagListPanel p) {
        titleTagListPanel = p;
    }

    private GridGuidePanel getGridGuidePanel() {
        return (gridGuidePanel);
    }

    private void setGridGuidePanel(GridGuidePanel p) {
        gridGuidePanel = p;
    }

    private RecordingRuleListPanel getRecordingRuleListPanel() {
        return (recordingRuleListPanel);
    }

    private void setRecordingRuleListPanel(RecordingRuleListPanel p) {
        recordingRuleListPanel = p;
    }

    private ShowDetailPanel getShowDetailPanel() {
        return (showDetailPanel);
    }

    private void setShowDetailPanel(ShowDetailPanel p) {
        showDetailPanel = p;
    }

    private JXPanel getWaitPanel() {
        return (waitPanel);
    }

    private void setWaitPanel(JXPanel p) {
        waitPanel = p;
    }

    private RecordingRulePanel getRecordingRulePanel() {
        return (recordingRulePanel);
    }

    private void setRecordingRulePanel(RecordingRulePanel p) {
        recordingRulePanel = p;
    }

    private UpcomingListPanel getUpcomingListPanel() {
        return (upcomingListPanel);
    }

    private void setUpcomingListPanel(UpcomingListPanel p) {
        upcomingListPanel = p;
    }

    private JXLabel getUpcomingLabel() {
        return (upcomingLabel);
    }

    private void setUpcomingLabel(JXLabel l) {
        upcomingLabel = l;
    }

    private JXLabel getChannelLabel() {
        return (channelLabel);
    }

    private void setChannelLabel(JXLabel l) {
        channelLabel = l;
    }

    private UpcomingDetailPanel getUpcomingDetailPanel() {
        return (upcomingDetailPanel);
    }

    private void setUpcomingDetailPanel(UpcomingDetailPanel p) {
        upcomingDetailPanel = p;
    }

    private ButtonPanel getUserButtonPanel() {
        return (userButtonPanel);
    }

    private void setUserButtonPanel(ButtonPanel p) {
        userButtonPanel = p;
    }

    private JobContainer getAllGuideJobContainer() {
        return (allGuideJobContainer);
    }

    private void setAllGuideJobContainer(JobContainer c) {
        allGuideJobContainer = c;
    }

    private long getLastGuide() {
        return (lastGuide);
    }

    private void setLastGuide(long l) {
        lastGuide = l;
    }

    private ShowAiring getSelectedShowAiring() {
        return (selectedShowAiring);
    }

    private void setSelectedShowAiring(ShowAiring sa) {
        selectedShowAiring = sa;
    }

    private RecordingRule getSelectedRecordingRule() {
        return (selectedRecordingRule);
    }

    private void setSelectedRecordingRule(RecordingRule rr) {
        selectedRecordingRule = rr;
    }

    private boolean isWithinAnHalfHour() {

        boolean result = false;

        long l = getLastGuide() + 30 * 60 * 1000;
        if (System.currentTimeMillis() < l) {

            result = true;
        }

        return (result);
    }

    private HashMap<Channel, ShowAiring[]> getGuideMap() {
        return (guideMap);
    }

    private void setGuideMap(HashMap<Channel, ShowAiring[]> m) {
        guideMap = m;

        setLastGuide(System.currentTimeMillis());

        GridGuidePanel ggp = getGridGuidePanel();
        if (ggp != null) {

            ggp.setGuideMap(guideMap);
        }
    }

    private HashMap<Tag, TagValue> getTagMap() {
        return (tagMap);
    }

    private void setTagMap(HashMap<Tag, TagValue> m) {
        tagMap = m;
    }

    private void clearTagMap() {

        HashMap<Tag, TagValue> m = getTagMap();
        if (m != null) {
            m.clear();
        }
    }

    private void add(Tag t, Channel c, ShowAiring sa) {

        add(t, new TagValue(c, sa));
    }

    private void add(Tag t, TagValue tv) {

        HashMap<Tag, TagValue> m = getTagMap();
        if ((m != null) && (t != null) && (tv != null)) {

            m.put(t, tv);
        }
    }

    private TagValue getTagValueByTag(Tag t) {

        TagValue result = null;

        HashMap<Tag, TagValue> m = getTagMap();
        if ((m != null) && (t != null)) {

            result = m.get(t);
        }

        return (result);
    }

    private boolean isPopupEnabled() {
        return (popupEnabled);
    }

    protected void setPopupEnabled(boolean b) {
        popupEnabled = b;
    }

    protected void popup(String[] choices) {

        JLayeredPane pane = getLayeredPane();
        if ((pane != null) && (choices != null)) {

            Dimension d = pane.getSize();
            int width = (int) d.getWidth();
            int height = (int) d.getHeight();

            ButtonPanel bp = new ButtonPanel();
            bp.setMediumFont(bp.getLargeFont());
            bp.setSmallFont(bp.getLargeFont());
            bp.addActionListener(this);
            bp.setButtons(choices);
            setUserButtonPanel(bp);

            d = bp.getPreferredSize();
            int bpwidth = (int) d.getWidth();
            int bpheight = (int) d.getHeight();
            int bpx = (int) ((width - bpwidth) / 2);
            int bpy = (int) ((height - bpheight) / 2);
            bp.setBounds(bpx, bpy, bpwidth, bpheight);

            setPopupEnabled(true);
            pane.add(bp, Integer.valueOf(300));
            bp.requestFocus();
            bp.setControl(true);
            bp.setButtons(choices);
        }
    }

    protected void unpopup() {

        setPopupEnabled(false);
        JLayeredPane pane = getLayeredPane();
        ButtonPanel bp = getUserButtonPanel();
        if ((pane != null) && (bp != null)) {

            bp.removeActionListener(this);
            setUserButtonPanel(null);
            pane.remove(bp);
            pane.repaint();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void save() {

        ArrayList<String> favlist = getFavoriteChannelList();
        if (favlist != null) {

            StringBuilder sb = new StringBuilder("");
            for (int i = 0; i < favlist.size(); i++) {

                sb.append(favlist.get(i));
                sb.append("\n");
            }

            try {

                File here = new File (".");
                Util.writeTextFile(new File(here, "fav-chan.txt"),
                    sb.toString());

            } catch (IOException ex) {
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void commandReceived(String command) {
    }

    /**
     * Override so we can start up the player.
     *
     * @param b When true we are ready to start the player.
     */
    public void setVisible(boolean b) {

        super.setVisible(b);
        if (b) {

            // We are being shown so let's read the local channel favorite
            // text file in case it's been changed since last time we were
            // here.  Each entry is a line that came from a Channel.toString()
            // call.  Since we can dynamically change them here we will read
            // them into an ArrayList<String>.  When this screen ends we
            // will need to write out the current state in case other screens
            // need the same info.
            ArrayList<String> favlist = getFavoriteChannelList();
            if (favlist != null) {

                favlist.clear();
                File here = new File (".");
                String[] cnames =
                    Util.readTextFile(new File(here, "fav-chan.txt"));
                if ((cnames != null) && (cnames.length > 0)) {

                    for (int i = 0; i < cnames.length; i++) {

                        favlist.add(cnames[i]);
                    }

                    Collections.sort(favlist);
                }
            }

            if ((isParameterByTitle()) || (isParameterByGuide())) {

                if (!isWithinAnHalfHour()) {

                    AllGuideJob gjob = new AllGuideJob(getNMS());
                    gjob.addJobListener(this);
                    JobContainer jc = JobManager.getJobContainer(gjob);
                    setAllGuideJobContainer(jc);
                    jc.start();
                    updateLayout(true);

                } else {

                    applyChannels();
                    updateLayout(false);
                }

            } else if (isParameterUpcomingRecordings()) {

                NMS[] array = getNMS();
                RecordingRuleListPanel rrlp = getRecordingRuleListPanel();
                if ((array != null) && (array.length > 0) && (rrlp != null)) {

                    RecordingRule[] rules = null;
                    ArrayList<RecordingRule> rlist =
                        new ArrayList<RecordingRule>();
                    for (int i = 0; i < array.length; i++) {

                        rules = array[i].getRecordingRules();
                        if (rules != null) {

                            for (int j = 0; j < rules.length; j++) {

                                rlist.add(rules[j]);
                            }
                        }
                    }

                    if (rlist.size() > 0) {

                        Collections.sort(rlist, new RecordingRuleSortByName());
                        RecordingRule all = new RecordingRule();
                        all.setName("All");
                        rlist.add(0, all);
                        rules = rlist.toArray(new RecordingRule[rlist.size()]);
                        int sindex = rrlp.getStartIndex();
                        int selindex = rrlp.getSelectedIndex();
                        rrlp.setRecordingRules(rules);
                        rrlp.setStartIndex(sindex);
                        rrlp.setSelectedIndex(selindex);
                    }

                    updateLayout(false);
                }

            } else if (isParameterUpcomingRecordings()) {

                updateLayout(false);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void performLayout(Dimension d) {

        JLayeredPane pane = getLayeredPane();
        if ((d != null) && (pane != null)) {

            float alpha = (float) getPanelAlpha();

            int width = (int) d.getWidth();
            int height = (int) d.getHeight();

            int wspan = (int) (width * 0.03);
            int listwidth = (width - (2 * wspan));
            int halflistwidth = (width - (3 * wspan)) / 2;
            int onethirdlistwidth = (width - (3 * wspan)) / 3;
            int twothirdlistwidth = onethirdlistwidth * 2;

            int hspan = (int) (height * 0.03);
            int listheight = (int) ((height - (2 * hspan)) / 1.5);

            int detailwidth = listwidth;
            int detailheight = (height - (3 * hspan)) - listheight;

            JXPanel panel = new JXPanel(new BorderLayout());
            JXLabel l = new JXLabel("Getting TV data, please wait...");
            l.setHorizontalTextPosition(SwingConstants.CENTER);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setFont(getLargeFont());
            panel.add(l, BorderLayout.CENTER);
            MattePainter p = new MattePainter(Color.BLACK);
            panel.setBackgroundPainter(p);
            panel.setBounds(0, 0, (int) d.getWidth(), (int) d.getHeight());
            setWaitPanel(panel);

            TagListPanel tlp = new TagListPanel();
            tlp.setControl(true);
            tlp.addPropertyChangeListener("SelectedTag", this);
            tlp.setBounds(wspan, hspan, listwidth, listheight);
            setTitleTagListPanel(tlp);

            RecordingRuleListPanel rrlp = new RecordingRuleListPanel();
            rrlp.setControl(true);
            rrlp.addPropertyChangeListener("SelectedRecordingRule", this);
            setRecordingRuleListPanel(rrlp);

            ShowDetailPanel sdp = new ShowDetailPanel();
            sdp.setBounds(wspan, hspan + listheight + hspan, detailwidth,
                detailheight);
            setShowDetailPanel(sdp);

            UpcomingListPanel ulp = new UpcomingListPanel();
            ulp.setControl(false);
            ulp.setAlpha(alpha);
            ulp.addPropertyChangeListener("SelectedUpcoming", this);

            setUpcomingListPanel(ulp);

            UpcomingDetailPanel dp = new UpcomingDetailPanel();
            dp.setAlpha(alpha);
            setUpcomingDetailPanel(dp);

            GridGuidePanel ggp = new GridGuidePanel();
            ggp.setAlpha(alpha);
            ggp.addPropertyChangeListener("SelectedShowAiring", this);
            setGridGuidePanel(ggp);

            JXLabel label = getChannelLabel();
            label.setFont(ggp.getLargeFont());
            label.setForeground(ggp.getInfoColor());
            label.setHorizontalTextPosition(SwingConstants.CENTER);
            label.setHorizontalAlignment(SwingConstants.RIGHT);
            Dimension ldim = label.getPreferredSize();
            int labelHeight = (int) ldim.getHeight();
            label.setBounds(wspan, hspan, listwidth, labelHeight);

            ggp.setBounds(wspan, hspan + labelHeight, listwidth,
                listheight - labelHeight);

            label = getUpcomingLabel();
            label.setFont(ulp.getLargeFont());
            label.setForeground(ulp.getInfoColor());
            label.setHorizontalTextPosition(SwingConstants.CENTER);
            label.setHorizontalAlignment(SwingConstants.RIGHT);
            ldim = label.getPreferredSize();
            labelHeight = (int) ldim.getHeight();
            label.setBounds(wspan, hspan, listwidth, labelHeight);

            rrlp.setBounds(wspan, hspan + labelHeight, halflistwidth,
                listheight - labelHeight);
            ulp.setBounds(wspan + wspan + halflistwidth, hspan + labelHeight,
                halflistwidth, listheight - labelHeight);
            dp.setBounds(wspan, hspan + hspan + listheight, detailwidth,
                detailheight);

            setDefaultBackgroundImage(
                Util.resize(getDefaultBackgroundImage(), width, height));
        }

    }

    private void updateLayout(boolean wait) {

        JLayeredPane pane = getLayeredPane();
        if (pane != null) {

            pane.removeAll();
            if (wait) {

                pane.add(getWaitPanel(), Integer.valueOf(100));

            } else {

                if (isParameterByTitle()) {

                    pane.add(getTitleTagListPanel(), Integer.valueOf(100));
                    pane.add(getShowDetailPanel(), Integer.valueOf(100));

                } else if (isParameterByGuide()) {

                    pane.add(getChannelLabel(), Integer.valueOf(110));
                    pane.add(getGridGuidePanel(), Integer.valueOf(100));
                    pane.add(getShowDetailPanel(), Integer.valueOf(100));

                } else if (isParameterUpcomingRecordings()) {

                    pane.add(getRecordingRuleListPanel(), Integer.valueOf(100));
                    pane.add(getUpcomingListPanel(), Integer.valueOf(100));
                    pane.add(getUpcomingLabel(), Integer.valueOf(110));
                    pane.add(getUpcomingDetailPanel(), Integer.valueOf(100));
                }
            }

            repaint();
        }
    }

    private boolean isPaidProgramming(Show s) {

        boolean result = false;

        if (s != null) {

            String type = s.getType();
            if ((type != null) && (type.equalsIgnoreCase("Paid Programming"))) {

                result = true;
            }
        }

        return (result);
    }

    private void add(Tag root, Channel c, ShowAiring[] array) {

        if ((root != null) && (c != null) && (array != null)) {

            String chantext = c.toString();

            for (int i = 0; i < array.length; i++) {

                Show show = array[i].getShow();
                if ((show != null) && (!isPaidProgramming(show))) {

                    //String title = show.getTitle();
                    String title = Util.toSortableTitle(show.getTitle(), true);
                    if (title != null) {

                        String letter = title.substring(0, 1);
                        letter = letter.toUpperCase();
                        Tag letterTag = null;
                        if (root.hasChildByName(letter)) {

                            letterTag = root.getChildByName(letter);

                        } else {

                            letterTag = new Tag();
                            letterTag.setName(letter);
                            root.addChild(letterTag);
                        }

                        if (letterTag != null) {

                            Tag titleTag = null;
                            if (letterTag.hasChildByName(title)) {

                                titleTag = letterTag.getChildByName(title);

                            } else {

                                titleTag = new Tag();
                                titleTag.setName(title);
                                letterTag.addChild(titleTag);
                            }

                            if (titleTag != null) {

                                Tag channelTag = null;
                                if (titleTag.hasChildByName(chantext)) {

                                    channelTag =
                                        titleTag.getChildByName(chantext);

                                } else {

                                    channelTag = new Tag();
                                    channelTag.setName(chantext);
                                    titleTag.addChild(channelTag);
                                }

                                if (channelTag != null) {

                                    Tag when = new Tag();
                                    when.setName(array[i].toString());
                                    channelTag.addChild(when);
                                    add(when, c, array[i]);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private Upcoming[] getUpcomings(RecordingRule rr) {

        Upcoming[] result = null;

        if (rr != null) {

            String seriesId = rr.getSeriesId();
            int channelId = rr.getChannelId();
            String lid = rr.getListingId();
            NMS n = NMSUtil.select(getNMS(), rr.getHostPort());
            if ((seriesId != null) && (n != null) && (lid != null)) {

                Channel c = n.getChannelById(channelId, lid);
                Upcoming[] all = n.getUpcomings();
                if ((all != null) && (c != null)) {

                    String cn = c.getName();
                    if (cn != null) {

                        ArrayList<Upcoming> ul = new ArrayList<Upcoming>();
                        for (int i = 0; i < all.length; i++) {

                            if ((seriesId.equals(all[i].getSeriesId()))
                                && (cn.equals(all[i].getChannelName()))) {

                                ul.add(all[i]);
                            }
                        }

                        if (ul.size() > 0) {

                            result = ul.toArray(new Upcoming[ul.size()]);
                        }
                    }
                }
            }
        }

        return (result);
    }

    private RecordingRule getRecordingRule(ShowAiring sa) {

        RecordingRule result = null;

        if (sa != null) {

            NMS n = NMSUtil.select(getNMS(), sa.getHostPort());
            Show show = sa.getShow();
            Airing airing = sa.getAiring();

            if ((n != null) && (show != null) && (airing != null)) {

                int cid = airing.getChannelId();
                String seriesId = show.getSeriesId();
                RecordingRule[] rules = n.getRecordingRules();
                if ((seriesId != null) && (rules != null)) {

                    for (int i = 0; i < rules.length; i++) {

                        ShowAiring rrsa = rules[i].getShowAiring();
                        if (rrsa != null) {

                            // We have a rule that is a ONCE recording.  It
                            // is only our rule to edit if the two ShowAiring
                            // instances are the same.
                            if (rrsa.equals(sa)) {

                                result = rules[i];
                                break;
                            }

                        } else {

                            if ((cid == rules[i].getChannelId())
                                && (seriesId.equals(rules[i].getSeriesId()))) {

                                result = rules[i];
                                break;
                            }
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
    @SuppressWarnings("unchecked")
    public void jobUpdate(JobEvent event) {

        if (event.getType() == JobEvent.COMPLETE) {

            Serializable s = event.getState();
            if (s instanceof HashMap<?, ?>) {

                HashMap<Channel, ShowAiring[]> map =
                    (HashMap<Channel, ShowAiring[]>) s;
                setGuideMap(map);
                setAllGuideJobContainer(null);
                TagListPanel tlp = getTitleTagListPanel();
                if (tlp != null) {

                    clearTagMap();
                    Tag root = new Tag();
                    root.setName("Root");

                    Set<Map.Entry<Channel, ShowAiring[]>> set = map.entrySet();
                    Iterator<Map.Entry<Channel, ShowAiring[]>> iter =
                        set.iterator();

                    while (iter.hasNext()) {

                        Map.Entry<Channel, ShowAiring[]> entry = iter.next();
                        Channel key = entry.getKey();
                        ShowAiring[] array = entry.getValue();
                        add(root, key, array);
                    }

                    tlp.setRootTag(root);
                }

                Set<Channel> set = map.keySet();
                if (set.size() > 0) {

                    Channel[] chans = set.toArray(new Channel[set.size()]);
                    Arrays.sort(chans);
                    setAllChannels(chans);
                    applyChannels();
                }

                updateLayout(false);
                requestFocus();
            }
        }
    }

    private void updateRecordingStatus() {

        ShowDetailPanel sdp = getShowDetailPanel();
        if (sdp != null) {

            ShowAiring sa = getSelectedShowAiring();
            if (sa != null) {

                String sadatestr = "";
                Airing airing = sa.getAiring();
                if (airing != null) {

                    Date d = airing.getAirDate();
                    if (d != null) {

                        sadatestr = d.toString();
                    }
                }
                RecordingRule rr = getRecordingRule(sa);
                if (rr != null) {

                    Upcoming[] ups = getUpcomings(rr);
                    if (ups != null) {

                        boolean found = false;
                        int doing = 0;
                        for (int i = 0; i < ups.length; i++) {

                            if (NMSConstants.READY.equals(ups[i].getStatus())) {

                                doing++;

                                if (!found) {

                                    found = sadatestr.equals(ups[i].getStart());
                                }
                            }
                        }

                        String extra = "";
                        if (doing > 0) {

                            if (found) {
                                extra = " including this one";
                            } else {
                                extra = " but not this one";
                            }
                        }

                        sdp.setRecordingStatus("Recording " + doing + " of "
                            + ups.length + extra);

                    } else {

                        sdp.setRecordingStatus("Recording 0 of 0");
                    }

                } else {

                    sdp.setRecordingStatus("Not Recording");
                }

            } else {

                sdp.setRecordingStatus(null);
            }
        }
    }

    private Frame getFrame() {
        return (Util.findFrame(this));
    }

    /**
     * We need to listen for events from the "upcoming override dialog".
     *
     * @param event A given event.
     */
    public void actionPerformed(ActionEvent event) {

        if (event.getSource() == getUserButtonPanel()) {

            ButtonPanel bp = getUserButtonPanel();
            if (isParameterByGuide()) {

                if ((ADD_FAVORITE.equals(bp.getSelectedButton()))
                    || (REMOVE_FAVORITE.equals(bp.getSelectedButton()))) {

                    GridGuidePanel ggp = getGridGuidePanel();
                    ArrayList<String> favlist = getFavoriteChannelList();
                    if ((ggp != null) && (favlist != null)) {

                        Channel chan = ggp.getSelectedChannel();
                        if (chan != null) {

                            String text = chan.toString();
                            if (getChannelState() == ALL_CHANNELS) {

                                if (!favlist.contains(text)) {

                                    favlist.add(text);
                                    Collections.sort(favlist);
                                }

                            } else if (getChannelState() == FAVORITE_CHANNELS) {

                                if (favlist.contains(text)) {

                                    favlist.remove(text);
                                    Collections.sort(favlist);
                                    applyChannels();
                                }
                            }
                        }
                    }

                } else if (SWITCH_TO_FAVORITE.equals(bp.getSelectedButton())) {

                    setChannelState(FAVORITE_CHANNELS);
                    applyChannels();

                } else if (SWITCH_FROM_FAVORITE.equals(
                    bp.getSelectedButton())) {

                    setChannelState(ALL_CHANNELS);
                    applyChannels();
                }

            } else {

                UpcomingListPanel ulp = getUpcomingListPanel();
                if (ulp != null) {

                    if (!CANCEL.equals(bp.getSelectedButton())) {

                        Upcoming up = ulp.getSelectedUpcoming();
                        if (up != null) {

                            NMS n = NMSUtil.select(getNMS(), up.getHostPort());
                            if (n != null) {

                                OverrideUpcomingJob ouj =
                                    new OverrideUpcomingJob(n, up);
                                Busy busy = new Busy(getLayeredPane(), ouj);
                                busy.addJobListener(this);
                                busy.execute();
                            }
                        }
                    }
                }
            }

            unpopup();
        }
    }

    private void handleFavorite() {

        GridGuidePanel ggp = getGridGuidePanel();
        if ((ggp != null) && (!isPopupEnabled())) {

            Channel chan = ggp.getSelectedChannel();
            if (chan != null) {

                ArrayList<String> blist = new ArrayList<String>();
                if (getChannelState() == ALL_CHANNELS) {

                    blist.add(SWITCH_TO_FAVORITE);
                    blist.add(ADD_FAVORITE);

                } else {

                    blist.add(SWITCH_FROM_FAVORITE);
                    blist.add(REMOVE_FAVORITE);
                }

                blist.add(CANCEL);
                popup(blist.toArray(new String[blist.size()]));
            }
        }
    }

    /**
     * We listen for property change events from the panels that deal
     * with selecting a show to schedule.
     *
     * @param event A given PropertyChangeEvent instance.
     */
    public void propertyChange(PropertyChangeEvent event) {

        if (event.getPropertyName().equals("SelectedTag")) {

            ShowDetailPanel sdp = getShowDetailPanel();
            if (sdp != null) {

                TagValue tv = getTagValueByTag((Tag) event.getNewValue());
                if (tv != null) {

                    Channel c = tv.getChannel();
                    ShowAiring sa = tv.getShowAiring();
                    sdp.setChannel(c);
                    sdp.setShowAiring(sa);
                    setSelectedShowAiring(sa);

                } else {

                    sdp.setChannel(null);
                    sdp.setShowAiring(null);
                    setSelectedShowAiring(null);
                }
            }

            updateRecordingStatus();

        } else if (event.getPropertyName().equals("SelectedShowAiring")) {

            ShowDetailPanel sdp = getShowDetailPanel();
            if (sdp != null) {

                ShowAiring sa = (ShowAiring) event.getNewValue();
                sdp.setShowAiring(sa);
                setSelectedShowAiring(sa);
            }

            updateRecordingStatus();

        } else if (event.getPropertyName().equals("SelectedRecordingRule")) {

            RecordingRule rr = (RecordingRule) event.getNewValue();
            setSelectedRecordingRule(rr);
            applyUpcoming();

        } else if (event.getPropertyName().equals("SelectedUpcoming")) {

            UpcomingDetailPanel dp = getUpcomingDetailPanel();
            if (dp != null) {

                Upcoming u = (Upcoming) event.getNewValue();
                dp.setUpcoming(u);
            }
        }
    }

    class LeftAction extends AbstractAction {

        public LeftAction() {
        }

        public void actionPerformed(ActionEvent e) {

            if (!isPopupEnabled()) {

                if (isParameterByTitle()) {

                    TagListPanel tlp = getTitleTagListPanel();
                    if (tlp != null) {

                        if (tlp.isExpanded()) {

                            tlp.toggle();

                        } else {

                            Tag selected = tlp.getSelectedTag();
                            if (selected != null) {

                                Tag parent = selected.getParent();
                                if ((parent != null) && (!parent.isRoot())) {

                                    if (tlp.isExpanded(parent)) {

                                        tlp.setSelectedTag(parent);
                                        tlp.toggle();
                                    }
                                }
                            }
                        }
                    }

                } else if (isParameterByGuide()) {

                    GridGuidePanel ggp = getGridGuidePanel();
                    if (ggp != null) {

                        ggp.left();
                    }

                } else if (isParameterUpcomingRecordings()) {

                    RecordingRuleListPanel rrlp = getRecordingRuleListPanel();
                    if (rrlp != null) {

                        rrlp.setControl(true);
                    }

                    UpcomingListPanel ulp = getUpcomingListPanel();
                    if (ulp != null) {

                        ulp.setControl(false);
                    }
                }
            }

            fireScreenEvent(ScreenEvent.USER_INPUT);
        }
    }

    class RightAction extends AbstractAction {

        public RightAction() {
        }

        public void actionPerformed(ActionEvent e) {

            if (!isPopupEnabled()) {

                if (isParameterByTitle()) {

                    TagListPanel tlp = getTitleTagListPanel();
                    if (tlp != null) {

                        tlp.toggle();
                    }

                } else if (isParameterByGuide()) {

                    GridGuidePanel ggp = getGridGuidePanel();
                    if (ggp != null) {

                        ggp.right();
                    }

                } else if (isParameterUpcomingRecordings()) {

                    RecordingRuleListPanel rrlp = getRecordingRuleListPanel();
                    if (rrlp != null) {
                        rrlp.setControl(false);
                    }

                    UpcomingListPanel ulp = getUpcomingListPanel();
                    if (ulp != null) {

                        if (!ulp.isControl()) {

                            ulp.setControl(true);

                        } else {

                            String text = null;
                            int state = getUpcomingState();
                            switch (state) {

                            default:
                            case ALL_UPCOMING:
                                state = RECORDING_UPCOMING;
                                text = RECORDING_UPCOMING_TEXT;
                                break;

                            case RECORDING_UPCOMING:
                                state = NOT_RECORDING_UPCOMING;
                                text = NOT_RECORDING_UPCOMING_TEXT;
                                break;

                            case NOT_RECORDING_UPCOMING:
                                state = ALL_UPCOMING;
                                text = ALL_UPCOMING_TEXT;
                                break;
                            }

                            JXLabel l = getUpcomingLabel();
                            if (l != null) {

                                l.setText(text);
                            }
                            setUpcomingState(state);
                            applyUpcoming();
                        }
                    }
                }
            }

            fireScreenEvent(ScreenEvent.USER_INPUT);
        }
    }

    class UpAction extends AbstractAction {

        public UpAction() {
        }

        public void actionPerformed(ActionEvent e) {

            if (isPopupEnabled()) {

                ButtonPanel bp = getUserButtonPanel();
                if (bp != null) {

                    bp.moveUp();
                }

            } else {

                if (isParameterByTitle()) {

                    TagListPanel tlp = getTitleTagListPanel();
                    if (tlp != null) {

                        tlp.moveUp();
                    }

                } else if (isParameterByGuide()) {

                    GridGuidePanel ggp = getGridGuidePanel();
                    if (ggp != null) {

                        ggp.up();
                    }

                } else if (isParameterUpcomingRecordings()) {

                    RecordingRuleListPanel rrlp = getRecordingRuleListPanel();
                    if ((rrlp != null) && (rrlp.isControl())) {

                        rrlp.moveUp();
                    }

                    UpcomingListPanel ulp = getUpcomingListPanel();
                    if ((ulp != null) && (ulp.isControl())) {

                        ulp.moveUp();
                    }
                }
            }

            fireScreenEvent(ScreenEvent.USER_INPUT);
        }
    }

    class DownAction extends AbstractAction {

        public DownAction() {
        }

        public void actionPerformed(ActionEvent e) {

            if (isPopupEnabled()) {

                ButtonPanel bp = getUserButtonPanel();
                if (bp != null) {

                    bp.moveDown();
                }

            } else {

                if (isParameterByTitle()) {

                    TagListPanel tlp = getTitleTagListPanel();
                    if (tlp != null) {

                        tlp.moveDown();
                    }

                } else if (isParameterByGuide()) {

                    GridGuidePanel ggp = getGridGuidePanel();
                    if (ggp != null) {

                        ggp.down();
                    }

                } else if (isParameterUpcomingRecordings()) {

                    RecordingRuleListPanel rrlp = getRecordingRuleListPanel();
                    if ((rrlp != null) && (rrlp.isControl())) {

                        rrlp.moveDown();
                    }

                    UpcomingListPanel ulp = getUpcomingListPanel();
                    if ((ulp != null) && (ulp.isControl())) {

                        ulp.moveDown();
                    }
                }
            }

            fireScreenEvent(ScreenEvent.USER_INPUT);
        }
    }

    class PageUpAction extends AbstractAction {

        public PageUpAction() {
        }

        public void actionPerformed(ActionEvent e) {

            if (!isPopupEnabled()) {

                if (isParameterByTitle()) {

                    TagListPanel tlp = getTitleTagListPanel();
                    if (tlp != null) {

                        tlp.movePageUp();
                    }

                } else if (isParameterByGuide()) {

                    GridGuidePanel ggp = getGridGuidePanel();
                    if (ggp != null) {

                        ggp.pageUp();
                    }

                } else if (isParameterUpcomingRecordings()) {

                    RecordingRuleListPanel rrlp = getRecordingRuleListPanel();
                    if ((rrlp != null) && (rrlp.isControl())) {

                        rrlp.movePageUp();
                    }

                    UpcomingListPanel ulp = getUpcomingListPanel();
                    if ((ulp != null) && (ulp.isControl())) {

                        ulp.movePageUp();
                    }
                }
            }

            fireScreenEvent(ScreenEvent.USER_INPUT);
        }
    }

    class PageDownAction extends AbstractAction {

        public PageDownAction() {
        }

        public void actionPerformed(ActionEvent e) {

            if (!isPopupEnabled()) {

                if (isParameterByTitle()) {

                    TagListPanel tlp = getTitleTagListPanel();
                    if (tlp != null) {

                        tlp.movePageDown();
                    }

                } else if (isParameterByGuide()) {

                    GridGuidePanel ggp = getGridGuidePanel();
                    if (ggp != null) {

                        ggp.pageDown();
                    }

                } else if (isParameterUpcomingRecordings()) {

                    RecordingRuleListPanel rrlp = getRecordingRuleListPanel();
                    if ((rrlp != null) && (rrlp.isControl())) {

                        rrlp.movePageDown();
                    }

                    UpcomingListPanel ulp = getUpcomingListPanel();
                    if ((ulp != null) && (ulp.isControl())) {

                        ulp.movePageDown();
                    }
                }
            }

            fireScreenEvent(ScreenEvent.USER_INPUT);
        }
    }

    class EnterAction extends AbstractAction implements JobListener {

        public EnterAction() {
        }

        public void jobUpdate(JobEvent event) {

            if (event.getType() == JobEvent.COMPLETE) {

                if (isParameterUpcomingRecordings()) {

                    setVisible(true);
                }
            }
        }

        public void editRule(NMS n, RecordingRulePanel p, RecordingRule rr) {

            if ((n != null) && (p != null) && (rr != null)) {

                p.setNMS(n);
                p.setRecordingRule(rr);
                p.setFrame(getFrame());

                Dialog.showPanel(getFrame(), p, p.getOkButton(),
                    p.getCancelButton());
                if (p.isAccept()) {

                    rr = p.getRecordingRule();
                    AddRuleJob arj = new AddRuleJob(n, rr);
                    Busy busy = new Busy(getLayeredPane(), arj);
                    busy.addJobListener(this);
                    busy.execute();

                } else {

                    requestFocus();
                }
            }
        }

        public void handleUpcoming() {

            UpcomingListPanel ulp = getUpcomingListPanel();
            if (ulp != null) {

                Upcoming up = ulp.getSelectedUpcoming();
                if (up != null) {

                    ArrayList<String> blist = new ArrayList<String>();
                    if (NMSConstants.PREVIOUSLY_RECORDED.equals(
                        up.getStatus())) {

                        blist.add("Forget Old Recording");

                    } else {

                        blist.add("Don't Record");
                    }

                    blist.add(CANCEL);
                    popup(blist.toArray(new String[blist.size()]));
                }
            }
        }

        public void actionPerformed(ActionEvent e) {

            RecordingRulePanel p = getRecordingRulePanel();
            if (p != null) {

                if (isParameterUpcomingRecordings()) {

                    UpcomingListPanel ulp = getUpcomingListPanel();
                    if ((ulp != null) && (ulp.isControl())) {

                        handleUpcoming();

                    } else {

                        RecordingRule rr = getSelectedRecordingRule();
                        if (rr != null) {

                            NMS n = NMSUtil.select(getNMS(), rr.getHostPort());
                            if (n != null) {

                                editRule(n, p, rr);
                            }
                        }
                    }

                } else if (isParameterByTitle()) {

                    ShowAiring sa = getSelectedShowAiring();
                    if (sa != null) {

                        NMS n = NMSUtil.select(getNMS(), sa.getHostPort());
                        if (n != null) {

                            Show show = sa.getShow();
                            Airing airing = sa.getAiring();
                            if ((show != null) && (airing != null)) {

                                RecordingRule rr = getRecordingRule(sa);

                                if (rr == null) {

                                    rr = new RecordingRule();
                                    rr.setShowAiring(sa);
                                    rr.setType(RecordingRule.SERIES_TYPE);
                                    rr.setName(show.getTitle());
                                    rr.setShowId(show.getId());
                                    rr.setSeriesId(show.getSeriesId());
                                    rr.setChannelId(airing.getChannelId());
                                    rr.setListingId(airing.getListingId());
                                    rr.setDuration(airing.getDuration());
                                    rr.setPriority(
                                        RecordingRule.NORMAL_PRIORITY);
                                    rr.setTasks(n.getTasks());
                                }

                                editRule(n, p, rr);
                            }
                        }
                    }

                } else if (isParameterByGuide()) {

                    GridGuidePanel ggp = getGridGuidePanel();
                    if (ggp != null) {

                        ShowAiring sa = getSelectedShowAiring();
                        if (sa != null) {

                            NMS n = NMSUtil.select(getNMS(), sa.getHostPort());
                            if (n != null) {

                                Show show = sa.getShow();
                                Airing airing = sa.getAiring();
                                if ((show != null) && (airing != null)) {

                                    RecordingRule rr = getRecordingRule(sa);

                                    if (rr == null) {

                                        rr = new RecordingRule();
                                        rr.setShowAiring(sa);
                                        rr.setType(RecordingRule.SERIES_TYPE);
                                        rr.setName(show.getTitle());
                                        rr.setShowId(show.getId());
                                        rr.setSeriesId(show.getSeriesId());
                                        rr.setChannelId(airing.getChannelId());
                                        rr.setListingId(airing.getListingId());
                                        rr.setDuration(airing.getDuration());
                                        rr.setPriority(
                                            RecordingRule.NORMAL_PRIORITY);
                                        rr.setTasks(n.getTasks());
                                    }

                                    editRule(n, p, rr);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    class InfoAction extends AbstractAction {

        public InfoAction() {
        }

        public void actionPerformed(ActionEvent e) {

            if (isParameterByGuide()) {

                handleFavorite();
            }

            fireScreenEvent(ScreenEvent.USER_INPUT);
        }

    }

    static class TagValue {

        private Channel channel;
        private ShowAiring showAiring;

        public TagValue(Channel c, ShowAiring sa) {

            setChannel(c);
            setShowAiring(sa);
        }

        public Channel getChannel() {
            return (channel);
        }

        private void setChannel(Channel c) {
            channel = c;
        }

        public ShowAiring getShowAiring() {
            return (showAiring);
        }

        private void setShowAiring(ShowAiring c) {
            showAiring = c;
        }

    }

    static class RecordingRuleSortByName implements Comparator<RecordingRule>,
        Serializable {

        public RecordingRuleSortByName() {
        }

        public int compare(RecordingRule rr0, RecordingRule rr1) {

            String s0 = Util.toSortableTitle(rr0.getName());
            String s1 = Util.toSortableTitle(rr1.getName());

            return (s0.compareTo(s1));
        }
    }

}

