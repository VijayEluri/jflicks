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
package org.jflicks.ui.view.metadata;

import java.awt.BorderLayout;

import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import org.jflicks.job.JobEvent;
import org.jflicks.job.JobListener;
import org.jflicks.metadata.Metadata;
import org.jflicks.metadata.SearchPanel;
import org.jflicks.nms.NMS;
import org.jflicks.ui.view.JFlicksView;
import org.jflicks.util.LogUtil;
import org.jflicks.util.ProgressBar;
import org.jflicks.util.Util;

import org.jdesktop.swingx.JXFrame;

/**
 * A implements a View so a user can control the scheduling of
 * recordings.
 *
 * @author Doug Barnum
 * @version 1.0
 */
public class MetadataView extends JFlicksView implements ActionListener {

    private static final String HOWTO =
        "http://www.jflicks.org/wiki/index.php?title=Video_Manager";

    private NMS[] nms;
    private JXFrame frame;
    private JMenu searchMenu;
    private AboutPanel aboutPanel;
    private JComboBox nmsComboBox;
    private JTabbedPane tabbedPane;
    private VideoPanel videoPanel;
    private RecordingPanel recordingPanel;
    private ArrayList<Metadata> metadataList;

    /**
     * Default constructor.
     */
    public MetadataView() {

        setMetadataList(new ArrayList<Metadata>());
    }

    private NMS[] getNMS() {
        return (nms);
    }

    private void setNMS(NMS[] array) {
        nms = array;
    }

    private ArrayList<Metadata> getMetadataList() {
        return (metadataList);
    }

    private void setMetadataList(ArrayList<Metadata> l) {
        metadataList = l;
    }

    /**
     * We keep track of Metadata that are available to us.
     *
     * @param m A given Metadata.
     */
    public void addMetadata(Metadata m) {

        ArrayList<Metadata> l = getMetadataList();
        if ((l != null) && (m != null)) {

            l.add(m);

            updateMetadata();
        }
    }

    /**
     * We keep track of Metadata that are available to us.
     *
     * @param m A given Metadata.
     */
    public void removeMetadata(Metadata m) {

        ArrayList<Metadata> l = getMetadataList();
        if ((l != null) && (m != null)) {

            l.remove(m);
            updateMetadata();
        }
    }

    private void updateMetadata() {

        ArrayList<Metadata> l = getMetadataList();
        VideoPanel vp = getVideoPanel();
        if ((vp != null) && (l != null)) {

            Metadata[] array = null;
            if (l.size() > 0) {

                array = l.toArray(new Metadata[l.size()]);

                JMenu menu = getSearchMenu();
                if (menu != null) {

                    menu.removeAll();
                    for (int i = 0; i < array.length; i++) {

                        menu.add(new SearchAction(array[i]));
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void modelPropertyChange(PropertyChangeEvent event) {

        String name = event.getPropertyName();
        if (name != null) {

            if (name.equals("NMS")) {

                JComboBox cb = getNMSComboBox();
                if (cb != null) {

                    cb.removeAllItems();
                    NMS[] array = (NMS[]) event.getNewValue();
                    setNMS(array);
                    if (array != null) {

                        for (int i = 0; i < array.length; i++) {

                            cb.addItem(array[i].getTitle());
                        }
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public JFrame getFrame() {

        if (frame == null) {

            frame = new JXFrame("jflicks media system - Video Manager");
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent evt) {
                    exitAction(true);
                }
            });

            frame.setLayout(new GridBagLayout());

            JComboBox cb = new JComboBox();
            cb.addActionListener(this);
            setNMSComboBox(cb);

            JTabbedPane pane = new JTabbedPane();
            setTabbedPane(pane);

            VideoPanel vp = new VideoPanel();
            setVideoPanel(vp);

            RecordingPanel rp = new RecordingPanel();
            setRecordingPanel(rp);

            pane.add("Videos", vp);
            pane.add("Recordings", rp);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.5;
            gbc.weighty = 0.0;
            gbc.insets = new Insets(4, 4, 4, 4);

            frame.add(cb, gbc);

            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.insets = new Insets(4, 4, 4, 4);

            frame.add(pane, gbc);

            // Build our menubar.
            JMenuBar mb = new JMenuBar();
            JMenu fileMenu = new JMenu("File");
            fileMenu.setMnemonic(Integer.valueOf(KeyEvent.VK_F));
            JMenu sMenu = new JMenu("Search");
            sMenu.setMnemonic(Integer.valueOf(KeyEvent.VK_S));
            setSearchMenu(sMenu);
            JMenu helpMenu = new JMenu("Help");
            helpMenu.setMnemonic(Integer.valueOf(KeyEvent.VK_H));

            VideoScanAction vsa = new VideoScanAction();
            fileMenu.add(vsa);

            PhotoScanAction psa = new PhotoScanAction();
            fileMenu.add(psa);

            ExitAction exitAction = new ExitAction();
            fileMenu.addSeparator();
            fileMenu.add(exitAction);

            HelpAction helpAction = new HelpAction();
            helpMenu.add(helpAction);
            AboutAction aboutAction = new AboutAction();
            helpMenu.add(aboutAction);

            mb.add(fileMenu);
            mb.add(sMenu);
            mb.add(helpMenu);
            frame.setJMenuBar(mb);

            try {

                BufferedImage image =
                    ImageIO.read(getClass().getResource("icon.png"));
                frame.setIconImage(image);

            } catch (IOException ex) {

                LogUtil.log(LogUtil.WARNING, "Did not find icon for aplication.");
            }

            frame.pack();

            updateMetadata();
        }

        return (frame);
    }

    private JMenu getSearchMenu() {
        return (searchMenu);
    }

    private void setSearchMenu(JMenu m) {
        searchMenu = m;
    }

    private JComboBox getNMSComboBox() {
        return (nmsComboBox);
    }

    private void setNMSComboBox(JComboBox cb) {
        nmsComboBox = cb;
    }

    private JTabbedPane getTabbedPane() {
        return (tabbedPane);
    }

    private void setTabbedPane(JTabbedPane tp) {
        tabbedPane = tp;
    }

    private VideoPanel getVideoPanel() {
        return (videoPanel);
    }

    private void setVideoPanel(VideoPanel p) {
        videoPanel = p;
    }

    private RecordingPanel getRecordingPanel() {
        return (recordingPanel);
    }

    private void setRecordingPanel(RecordingPanel p) {
        recordingPanel = p;
    }

    private NMS getSelectedNMS() {

        NMS result = null;

        NMS[] array = getNMS();
        JComboBox cb = getNMSComboBox();
        if ((array != null) && (cb != null)) {

            int index = cb.getSelectedIndex();
            if ((index >= 0) && (index < array.length)) {
                result = array[index];
            }
        }

        return (result);
    }

    /**
     * {@inheritDoc}
     */
    public void messageReceived(String s) {

        LogUtil.log(LogUtil.INFO, "messageReceived: " + s);
    }

    /**
     * We listen for action events to respond to user action.
     *
     * @param event A given action event.
     */
    public void actionPerformed(ActionEvent event) {

        if (event.getSource() == getNMSComboBox()) {

            NMS n = getSelectedNMS();
            if (n != null) {

                VideoPanel vp = getVideoPanel();
                if (vp != null) {
                    vp.setNMS(n);
                }

                RecordingPanel rp = getRecordingPanel();
                if (rp != null) {
                    rp.setNMS(n);
                }
            }
        }
    }

    class VideoScanAction extends AbstractAction implements JobListener {

        public VideoScanAction() {

            ImageIcon sm =
                new ImageIcon(getClass().getResource("videoScan16.png"));
            ImageIcon lge =
                new ImageIcon(getClass().getResource("videoScan32.png"));
            putValue(NAME, "Video Scan");
            putValue(SHORT_DESCRIPTION, "Video Scan");
            putValue(SMALL_ICON, sm);
            putValue(LARGE_ICON_KEY, lge);
            putValue(MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_V));
        }

        public void jobUpdate(JobEvent event) {

            if (event.getType() == JobEvent.COMPLETE) {

                VideoPanel vp = getVideoPanel();
                if (vp != null) {
                    vp.setNMS(getSelectedNMS());
                }
            }
        }

        public void actionPerformed(ActionEvent e) {

            NMS n = getSelectedNMS();
            if (n != null) {

                VideoScanJob vsj = new VideoScanJob(n);
                ProgressBar pbar =
                    new ProgressBar(getTabbedPane(), "Scanning...", vsj);
                pbar.addJobListener(this);
                pbar.execute();
            }
        }
    }

    class PhotoScanAction extends AbstractAction implements JobListener {

        public PhotoScanAction() {

            ImageIcon sm =
                new ImageIcon(getClass().getResource("photoScan16.png"));
            ImageIcon lge =
                new ImageIcon(getClass().getResource("photoScan32.png"));
            putValue(NAME, "Photo Scan");
            putValue(SHORT_DESCRIPTION, "Photo Scan");
            putValue(SMALL_ICON, sm);
            putValue(LARGE_ICON_KEY, lge);
            putValue(MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_P));
        }

        public void jobUpdate(JobEvent event) {
        }

        public void actionPerformed(ActionEvent e) {

            NMS n = getSelectedNMS();
            if (n != null) {

                PhotoScanJob psj = new PhotoScanJob(n);
                ProgressBar pbar =
                    new ProgressBar(getTabbedPane(), "Scanning...", psj);
                pbar.addJobListener(this);
                pbar.execute();
            }
        }
    }

    class ExitAction extends AbstractAction {

        public ExitAction() {

            ImageIcon sm = new ImageIcon(getClass().getResource("exit16.png"));
            ImageIcon lge = new ImageIcon(getClass().getResource("exit32.png"));
            putValue(NAME, "Exit");
            putValue(SHORT_DESCRIPTION, "Exit");
            putValue(SMALL_ICON, sm);
            putValue(LARGE_ICON_KEY, lge);
            putValue(MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_X));
        }

        public void actionPerformed(ActionEvent e) {
            exitAction(true);
        }
    }

    class AboutAction extends AbstractAction {

        public AboutAction() {

            ImageIcon sm = new ImageIcon(getClass().getResource("about16.png"));
            ImageIcon lge =
                new ImageIcon(getClass().getResource("about32.png"));
            putValue(NAME, "About");
            putValue(SHORT_DESCRIPTION, "About jflicks Video Manager");
            putValue(SMALL_ICON, sm);
            putValue(LARGE_ICON_KEY, lge);
            putValue(MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_A));
        }

        public void actionPerformed(ActionEvent e) {

            if (aboutPanel == null) {

                aboutPanel = new AboutPanel();
            }

            if (aboutPanel != null) {

                Util.showDialog(getFrame(), "About...", aboutPanel, false);
            }
        }
    }

    class HelpAction extends AbstractAction {

        public HelpAction() {

            ImageIcon sm = new ImageIcon(getClass().getResource("help16.png"));
            ImageIcon lge = new ImageIcon(getClass().getResource("help32.png"));
            putValue(NAME, "Online Help");
            putValue(SHORT_DESCRIPTION, "Online Documentaion");
            putValue(SMALL_ICON, sm);
            putValue(LARGE_ICON_KEY, lge);
            putValue(MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_O));
        }

        public void actionPerformed(ActionEvent e) {

            Desktop desktop = Desktop.getDesktop();
            if (desktop != null) {

                if (desktop.isDesktopSupported()) {

                    try {

                        desktop.browse(new URI(HOWTO));

                    } catch (URISyntaxException ex) {

                        JOptionPane.showMessageDialog(getFrame(),
                            "Could not load browser", "alert",
                            JOptionPane.ERROR_MESSAGE);

                    } catch (IOException ex) {

                        JOptionPane.showMessageDialog(getFrame(),
                            "Could not load browser", "alert",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    class SearchAction extends AbstractAction {

        private Metadata metadata;
        private JFrame metadataFrame;

        public SearchAction(Metadata m) {

            metadata = m;
            if (m != null) {

                putValue(NAME, m.getTitle());

                JFrame f = new JFrame("Search " + m.getTitle());
                f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                f.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent evt) {

                        JFrame f = getMetadataFrame();
                        if (f != null) {
                            f.setVisible(false);
                        }
                    }
                });

                f.setLayout(new BorderLayout());
                SearchPanel sp = m.getSearchPanel();
                if (sp != null) {

                    f.add(sp, BorderLayout.CENTER);
                    f.pack();
                    setMetadataFrame(f);
                    sp.addSearchListener(getVideoPanel());
                    getVideoPanel().addSearchListener(sp);
                    sp.addSearchListener(getRecordingPanel());
                    getRecordingPanel().addSearchListener(sp);
                }
            }
        }

        private JFrame getMetadataFrame() {
            return (metadataFrame);
        }

        private void setMetadataFrame(JFrame f) {
            metadataFrame = f;
        }

        public void actionPerformed(ActionEvent e) {

            if (metadataFrame != null) {
                metadataFrame.setVisible(true);
            }
        }

    }

}
