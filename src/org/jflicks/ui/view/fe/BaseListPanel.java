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
package org.jflicks.ui.view.fe;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.util.concurrent.TimeUnit;
import javax.swing.BorderFactory;
import javax.swing.JLayeredPane;
import javax.swing.SwingConstants;

import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.KeyFrames;
import org.jdesktop.core.animation.timing.PropertySetter;
import org.jdesktop.core.animation.timing.TimingTarget;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.painter.MattePainter;

/**
 * This is a base list panel class that handles all the drawing and
 * updating of the list.  Extensions concentrate on laying themselves
 * out and handling their own objects.
 *
 * @author Doug Barnum
 * @version 1.0
 */
public abstract class BaseListPanel extends BaseCustomizePanel {

    protected static final double HGAP = 0.02;
    protected static final double VGAP = 0.02;

    private JXLabel[] labels;
    private int visibleCount;
    private int selectedIndex;
    private int oldSelectedIndex;
    private int startIndex;
    private Animator[] animators;
    private Object selectedObject;
    private Object pageModeSelectedObject;
    private String propertyName;
    private boolean pageMode;

    /**
     * Extensions need to make their objects displayed in the list available
     * to this base class so it can draw and maintain the list properly.
     *
     * @return An array of Objects.
     */
    public abstract Object[] getObjects();

    /**
     * Simple empty constructor.
     */
    public BaseListPanel() {

        setStartIndex(0);
        setOldSelectedIndex(-1);
        setSelectedIndex(0);
    }

    protected JXLabel[] getLabels() {
        return (labels);
    }

    protected void setLabels(JXLabel[] array) {
        labels = array;
    }

    protected Animator[] getAnimators() {
        return (animators);
    }

    protected void setAnimators(Animator[] array) {
        animators = array;
    }

    protected Object getSelectedObject() {
        return (selectedObject);
    }

    protected void setSelectedObject(Object o) {

        Object old = selectedObject;
        selectedObject = o;
        if (!isPageMode()) {
            firePropertyChange(getPropertyName(), old, selectedObject);
        } else {
            pageModeSelectedObject = old;
        }
    }

    protected String getPropertyName() {
        return (propertyName);
    }

    protected void setPropertyName(String s) {
        propertyName = s;
    }

    private boolean isPageMode() {
        return (pageMode);
    }

    private void setPageMode(boolean b) {

        pageMode = b;
        if (!pageMode) {
            firePropertyChange(getPropertyName(), pageModeSelectedObject,
                selectedObject);
        }
    }

    private int getCount() {

        int result = 0;

        Object[] array = getObjects();
        if (array != null) {

            result = array.length;
        }

        return (result);
    }

    /**
     * Convenience method to find the general height any particular
     * JXLabel would need to be using our large Font property.
     *
     * @return A double value.
     */
    public double getMaxHeight() {

        double result = 0.0;

        JXLabel tst = new JXLabel("TEST IT DUDE");
        tst.setFont(getLargeFont());
        tst.setHorizontalTextPosition(SwingConstants.CENTER);
        tst.setHorizontalAlignment(SwingConstants.LEFT);
        Dimension fd = tst.getPreferredSize();

        if (fd != null) {
            result = fd.getHeight();
        }

        return (result);
    }

    /**
     * Compute the max width that is needed so all the given String
     * instances will display correctly given our current large Font
     * property.
     *
     * @param array Need some String instances to check.
     * @return A double value.
     */
    public double getMaxWidth(String[] array) {

        double result = 0.0;

        double fudge = 12;
        if ((array != null) && (array.length > 0)) {

            JXLabel tst = new JXLabel();
            tst.setFont(getLargeFont());
            tst.setHorizontalTextPosition(SwingConstants.CENTER);
            tst.setHorizontalAlignment(SwingConstants.LEFT);

            Insets in = tst.getInsets();
            if (in != null) {

                fudge += (double) (in.left + in.right);
            }

            for (int i = 0; i < array.length; i++) {

                tst.setText(array[i]);
                Dimension fd = tst.getPreferredSize();
                if (fd != null) {

                    double tmp = fd.getWidth();
                    if (tmp > result) {

                        result = tmp;
                    }
                }
            }
        }

        return (result + fudge);
    }

    /**
     * {@inheritDoc}
     */
    public void performControl() {
        applyColor();
    }

    /**
     * {@inheritDoc}
     */
    public void performLayout(Dimension d) {

        JLayeredPane pane = getLayeredPane();
        if ((d != null) && (pane != null)) {

            // This most likely our screen size...
            double width = d.getWidth();
            double height = d.getHeight();

            // Compute our gaps...
            double hgap = width * HGAP;
            double vgap = height * VGAP;

            double realHeight = height - (vgap * 2.0);
            double labelMaxHeight = getMaxHeight();

            int vcount = (int) (realHeight / labelMaxHeight);
            JXLabel[] array = new JXLabel[vcount];
            for (int i = 0; i < vcount; i++) {

                array[i] = new JXLabel();
                if (i == 0) {
                    array[i].setFont(getLargeFont());
                } else {
                    array[i].setFont(getSmallFont());
                }
                array[i].setHorizontalTextPosition(SwingConstants.CENTER);
                array[i].setHorizontalAlignment(SwingConstants.LEFT);
            }

            setVisibleCount(vcount);
            setLabels(array);

            // Do the background.
            Color color = getPanelColor();
            color = new Color(color.getRed(), color.getGreen(),
                color.getBlue(), (int) (getPanelAlpha() * 255));
            MattePainter mpainter = new MattePainter(color);
            setBackgroundPainter(mpainter);
            setAlpha((float) getPanelAlpha());

            FontEvaluator feval = new FontEvaluator(getSmallFont(),
                getLargeFont(), getSmallFontSize(), getLargeFontSize());
            KeyFrames.Builder<Font> kfb = new KeyFrames.Builder<Font>();
            kfb.addFrames(feval.getFonts());
            kfb.setEvaluator(feval);
            KeyFrames<Font> fontFrames = kfb.build();
            Animator[] anis = new Animator[array.length];
            double center = (realHeight - (vcount * labelMaxHeight)) / 2.0;
            double top = vgap + center;
            for (int i = 0; i < array.length; i++) {

                array[i].setBounds((int) hgap, (int) top, (int) width,
                    (int) labelMaxHeight);
                pane.add(array[i], Integer.valueOf(110));
                top += labelMaxHeight;
                TimingTarget tt =
                    PropertySetter.getTarget(array[i], "font", fontFrames);
                anis[i] = new Animator.Builder().setDuration(250,
                    TimeUnit.MILLISECONDS).addTarget(tt).build();
            }

            setAnimators(anis);
        }
    }

    /**
     * Move the group of labels down a page.
     */
    public void movePageUp() {

        int count = getVisibleCount() - 1;
        if (count > 0) {

            setPageMode(true);
            for (int i = 0; i < count; i++) {

                moveUp();
            }

            setPageMode(false);
            update();
        }
    }

    /**
     * Move the group of labels down a page.
     */
    public void movePageDown() {

        int count = getVisibleCount() - 1;
        if (count > 0) {

            setPageMode(true);
            for (int i = 0; i < count; i++) {

                moveDown();
            }

            setPageMode(false);
            update();
        }
    }

    /**
     * Move the group of labels down one.
     */
    public void moveDown() {

        Object[] array = getObjects();
        if (array != null) {

            if (isWindowGreaterOrEqual()) {

                int selected = getSelectedIndex();
                if ((selected + 1) < array.length) {

                    setSelectedIndex(selected + 1);

                } else {

                    setSelectedIndex(0);
                }

            } else {

                // We have more channels that can fit in the window.
                // First see if we can just move the selection.
                if (!isSelectedAtTheBottomWindow()) {

                    // Then we can just update the index and be done.
                    int selected = getSelectedIndex();
                    setSelectedIndex(selected + 1);

                } else {

                    // Ok we have to increment our start and leave the
                    // selected at the bottom as long as we haven't
                    // reached the end of the list.
                    if (!isSelectedAtTheBottomList()) {

                        int start = getStartIndex();
                        setStartIndex(start + 1);

                    } else {

                        // No where to go down.  We want to reset to the
                        // top of the list.
                        setStartIndex(0);
                        setSelectedIndex(0);
                    }
                }
            }
        }
    }

    /**
     * Move the group of labels up one.
     */
    public void moveUp() {

        Object[] array = getObjects();
        if (array != null) {

            if (isWindowGreaterOrEqual()) {

                int selected = getSelectedIndex();
                if ((selected - 1) >= 0) {

                    setSelectedIndex(selected - 1);

                } else {

                    setSelectedIndex(array.length - 1);
                }

            } else {

                // We have more channels that can fit in the window.
                // First see if we can just move the selection.
                if (!isSelectedAtTheTopWindow()) {

                    // Then we can just update the index and be done.
                    int selected = getSelectedIndex();
                    setSelectedIndex(selected - 1);

                } else {

                    // Ok we have to decrement our start and leave the
                    // selected at the top as long as we haven't
                    // reached the top of the list.
                    if (!isSelectedAtTheTopList()) {

                        int start = getStartIndex();
                        setStartIndex(start - 1);

                    } else {

                        // No where to go down.  We want to reset to the
                        // top of the list.
                        setStartIndex(array.length - getVisibleCount());
                        setSelectedIndex(getVisibleCount() - 1);
                    }
                }
            }
        }
    }

    protected void applyColor() {

        if (isControl()) {
            setBorder(BorderFactory.createLineBorder(getHighlightColor()));
        } else {
            setBorder(BorderFactory.createLineBorder(getUnselectedColor()));
        }

        JXLabel[] array = getLabels();
        if (array != null) {

            for (int i = 0; i < array.length; i++) {

                if (isControl()) {

                    if (i == getSelectedIndex()) {

                        array[i].setForeground(getHighlightColor());

                    } else {

                        array[i].setForeground(getUnselectedColor());
                    }

                } else {

                    if (i == getSelectedIndex()) {

                        array[i].setForeground(getSelectedColor());

                    } else {

                        array[i].setForeground(getUnselectedColor());
                    }
                }
            }
        }
    }

    protected void animate() {

        JXLabel[] array = getLabels();
        Animator[] anis = getAnimators();
        if ((array != null) && (anis != null)) {

            int old = getOldSelectedIndex();
            int current = getSelectedIndex();

            // Stop all previous running animations...
            for (int i = 0; i < anis.length; i++) {

                if (anis[i].isRunning()) {

                    anis[i].stop();
                    array[i].setFont(getSmallFont());
                }
            }

            // Animate the old selected so it goes small...
            if ((old >= 0) && (old < array.length) && (old != current)) {

                anis[old].startReverse();
            }

            // Animate the new selected so it goes large...
            if ((current >= 0) && (current < array.length)) {

                anis[current].start();
            }
        }
    }

    /**
     * Extensions and users of lists need to know the visible count of
     * items.
     *
     * @return The visible count of items in our list.
     */
    public int getVisibleCount() {
        return (visibleCount);
    }

    protected void setVisibleCount(int i) {
        visibleCount = i;
    }

    /**
     * Which item is selected by index.
     *
     * @return The selected index.
     */
    public int getSelectedIndex() {
        return (selectedIndex);
    }

    /**
     * Which item is selected by index.
     *
     * @param i The selected index.
     */
    public void setSelectedIndex(int i) {

        // In case we are being set to an index out of range, fix it to
        // the last item.
        int tmp = getCount();
        if ((i + 1) > tmp) {

            if (tmp > 0) {
                i = tmp - 1;
            } else {
                i = 0;
            }

        } else if (i < 0) {

            i = 0;
        }
        int old = selectedIndex;
        setOldSelectedIndex(old);
        selectedIndex = i;
        update();
    }

    protected int getOldSelectedIndex() {
        return (oldSelectedIndex);
    }

    protected void setOldSelectedIndex(int i) {
        oldSelectedIndex = i;
    }

    /**
     * We start at a particular index.
     *
     * @return The start index.
     */
    public int getStartIndex() {
        return (startIndex);
    }

    /**
     * We start at a particular index.
     *
     * @param i The start index.
     */
    public void setStartIndex(int i) {

        startIndex = i;
        update();
    }

    private boolean isWindowGreaterOrEqual() {

        boolean result = false;

        Object[] array = getObjects();
        if (array != null) {

            result = getVisibleCount() >= array.length;
        }

        return (result);
    }

    private boolean isSelectedAtTheBottomWindow() {
        return ((getVisibleCount() - 1) == getSelectedIndex());
    }

    private boolean isSelectedAtTheBottomList() {

        boolean result = false;

        Object[] array = getObjects();
        if (array != null) {

            Object selected = getSelectedObject();
            Object last = array[array.length - 1];

            if ((selected != null) && (last != null)) {

                result = selected.equals(last);
            }
        }

        return (result);
    }

    private boolean isSelectedAtTheTopWindow() {
        return (getSelectedIndex() == 0);
    }

    private boolean isSelectedAtTheTopList() {

        boolean result = false;

        Object[] array = getObjects();
        if (array != null) {

            Object selected = getSelectedObject();
            Object first = array[0];
            if ((selected != null) && (first != null)) {

                result = selected.equals(first);
            }
        }

        return (result);
    }

    /**
     * Update the UI.
     */
    protected void update() {

        Object[] array = getObjects();
        JXLabel[] labs = getLabels();
        if ((array != null) && (labs != null)) {

            int index = getStartIndex();
            for (int i = 0; i < labs.length; i++) {

                if (index < array.length) {

                    labs[i].setText(array[index].toString());

                } else {
                    labs[i].setText("");
                }

                index++;
            }

            applyColor();
            int sindex = getSelectedIndex() + getStartIndex();
            if (array.length > sindex) {
                setSelectedObject(array[sindex]);
            }

            animate();
        }
    }

}

