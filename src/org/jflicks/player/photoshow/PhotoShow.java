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
package org.jflicks.player.photoshow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.Timer;

import org.jflicks.player.BasePlayer;
import org.jflicks.player.Bookmark;
import org.jflicks.player.PlayState;
import org.jflicks.util.Util;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.ImagePainter;
import org.jdesktop.swingx.painter.MattePainter;

/**
 * This Player will display photos in a JDialog.
 *
 * @author Doug Barnum
 * @version 1.0
 */
public class PhotoShow extends BasePlayer implements ActionListener {

    private JDialog dialog;
    private JXPanel panel;
    private Timer timer;
    private URL[] photoURLs;
    private int currentIndex;

    /**
     * Simple constructor.
     */
    public PhotoShow() {

        setType(PLAYER_SLIDESHOW);
        setTitle("PhotoShow");
    }

    private JDialog getDialog() {
        return (dialog);
    }

    private void setDialog(JDialog d) {
        dialog = d;
    }

    private JXPanel getPanel() {
        return (panel);
    }

    private void setPanel(JXPanel p) {
        panel = p;
    }

    private Timer getTimer() {
        return (timer);
    }

    private void setTimer(Timer t) {
        timer = t;
    }

    private URL[] getPhotoURLs() {
        return (photoURLs);
    }

    private void setPhotoURLs(URL[] array) {
        photoURLs = array;
    }

    private int getCurrentIndex() {
        return (currentIndex);
    }

    private void setCurrentIndex(int i) {
        currentIndex = i;
    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsPause() {
        return (true);
    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsAutoSkip() {
        return (false);
    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsMaximize() {
        return (false);
    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsSeek() {
        return (false);
    }

    /**
     * {@inheritDoc}
     */
    public void play(String ... urls) {

        if ((urls != null) && (urls.length > 0)) {

            play(urls[0], null);
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void play(String url, Bookmark b) {

        if (!isPlaying()) {

            setAudioOffset(0);
            setPaused(false);
            setPlaying(true);
            setCompleted(false);

            String[] lines = Util.readTextFile(new File(url));
            if (lines != null) {

                pathsToURLs(lines);
                setCurrentIndex(0);

                Rectangle r = null;
                if (isFullscreen()) {

                    r = getFullscreenRectangle();

                } else {

                    r = getRectangle();
                }

                int x = (int) r.getX();
                int y = (int) r.getY();
                int width = (int) r.getWidth();
                int height = (int) r.getHeight();

                JXPanel p = new JXPanel();
                p.setOpaque(false);
                p.setBounds(x, y, width, height);
                setPanel(p);

                JXPanel backp = new JXPanel(new BorderLayout());
                backp.setOpaque(false);
                backp.setBounds(x, y, width, height);
                backp.setBackgroundPainter(new MattePainter(Color.BLACK));
                backp.add(p, BorderLayout.CENTER);

                Cursor cursor = Util.getNoCursor();
                JDialog w = new JDialog(getFrame());
                w.setUndecorated(true);

                w.setBounds(x, y, width, height);
                w.add(backp);
                w.requestFocus();
                if (cursor != null) {
                    w.getContentPane().setCursor(cursor);
                }
                setDialog(w);

                p.setFocusable(true);
                InputMap map = p.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

                InfoAction infoAction = new InfoAction();
                map.put(KeyStroke.getKeyStroke("I"), "i");
                p.getActionMap().put("i", infoAction);

                QuitAction quitAction = new QuitAction();
                map.put(KeyStroke.getKeyStroke("Q"), "q");
                p.getActionMap().put("q", quitAction);

                PauseAction pauseAction = new PauseAction();
                map.put(KeyStroke.getKeyStroke("P"), "p");
                p.getActionMap().put("p", pauseAction);

                Timer t = new Timer(5000, this);
                t.setInitialDelay(500);
                setTimer(t);
                t.start();

                final JXPanel fpan = p;
                ActionListener focusPerformer = new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {

                        fpan.requestFocus();
                    }
                };
                Timer focusTimer = new Timer(2000, focusPerformer);
                focusTimer.setRepeats(false);
                focusTimer.start();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stop() {

        setPaused(false);
        setPlaying(false);
        setCompleted(true);

        JDialog w = getDialog();
        if (w != null) {

            w.setVisible(false);
            w.dispose();
            setDialog(null);
        }

        Timer t = getTimer();
        if (t != null) {

            t.stop();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void pause(boolean b) {

        setPaused(b);
    }

    /**
     * {@inheritDoc}
     */
    public void maximize(boolean b) {

        setMaximized(b);
    }

    /**
     * {@inheritDoc}
     */
    public void seek(int seconds) {
    }

    /**
     * {@inheritDoc}
     */
    public void seekPosition(int seconds) {
    }

    /**
     * {@inheritDoc}
     */
    public void seekPosition(double percentage) {
    }

    /**
     * {@inheritDoc}
     */
    public void next() {
    }

    /**
     * {@inheritDoc}
     */
    public void previous() {
    }

    /**
     * {@inheritDoc}
     */
    public void audiosync(double offset) {
    }

    /**
     * {@inheritDoc}
     */
    public PlayState getPlayState() {

        PlayState result = null;

        return (result);
    }

    /**
     * Time to switch to the next photo.
     *
     * @param event A given ActionEvent instance.
     */
    public void actionPerformed(ActionEvent event) {

        if ((isPlaying()) && (!isPaused())) {

            JDialog w = getDialog();
            JXPanel p = getPanel();
            URL url = getNextURL();
            if ((w != null) && (p != null) && (url != null)) {

                try {

                    BufferedImage bi = ImageIO.read(url);
                    ImagePainter painter =
                        (ImagePainter) p.getBackgroundPainter();
                    if (painter != null) {

                        painter.setImage(bi);

                    } else {

                        painter = new ImagePainter(bi);
                        painter.setScaleToFit(true);
                        p.setBackgroundPainter(painter);
                    }

                    p.repaint();

                    if (!w.isVisible()) {

                        w.setVisible(true);
                    }

                } catch (IOException ex) {
                }
            }
        }
    }

    private void pathsToURLs(String[] array) {

        if ((array != null) && (array.length > 0)) {

            URL[] uarray = new URL[array.length];
            for (int i = 0; i < array.length; i++) {

                try {

                    if (array[i].startsWith("http")) {
                        uarray[i] = new URL(array[i]);
                    } else {
                        uarray[i] = new URL("file://" + array[i]);
                    }

                } catch (MalformedURLException ex) {
                }
            }

            setPhotoURLs(uarray);
        }
    }

    private URL getNextURL() {

        URL result = null;

        URL[] urls = getPhotoURLs();
        if ((urls != null) && (urls.length > 0)) {

            int index = getCurrentIndex();
            if ((index >= 0) && (index < urls.length)) {

                result = urls[index];

            } else {

                setCurrentIndex(0);
                result = urls[0];
            }

            setCurrentIndex(getCurrentIndex() + 1);
        }

        return (result);
    }

}

