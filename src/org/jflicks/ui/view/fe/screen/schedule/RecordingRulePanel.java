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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jflicks.nms.NMS;
import org.jflicks.tv.Channel;
import org.jflicks.tv.RecordingRule;
import org.jflicks.tv.Task;
import org.jflicks.util.ColumnPanel;
import org.jflicks.ui.view.fe.Dialog;
import org.jflicks.ui.view.fe.BaseCustomizePanel;

import org.jdesktop.swingx.JXLabel;

/**
 * Panel that deals with adding a RecordingRule.
 *
 * @author Doug Barnum
 * @version 1.0
 */
public class RecordingRulePanel extends BaseCustomizePanel
    implements ActionListener, ChangeListener {

    private static final String ADVANCED_TEXT = "Advanced Settings";

    private RecordingRule recordingRule;
    private NMS nms;
    private JXLabel nameLabel;
    private JXLabel channelLabel;
    private JXLabel durationLabel;
    private JComboBox typeComboBox;
    private JComboBox priorityComboBox;
    private JSpinner beginSpinner;
    private JSpinner endSpinner;
    private JButton advancedButton;
    private JButton okButton;
    private JButton cancelButton;
    private JButton advancedOkButton;
    private JButton advancedCancelButton;
    private boolean accept;
    private boolean advancedAccept;

    /**
     * Simple constructor.
     */
    public RecordingRulePanel() {

        JXLabel nameprompt = new JXLabel("Name");
        nameprompt.setHorizontalTextPosition(SwingConstants.RIGHT);
        nameprompt.setHorizontalAlignment(SwingConstants.RIGHT);
        nameprompt.setFont(getSmallFont());

        JXLabel namelab = new JXLabel();
        namelab.setFont(getSmallFont());
        setNameLabel(namelab);

        JXLabel channelprompt = new JXLabel("Channel");
        channelprompt.setHorizontalTextPosition(SwingConstants.RIGHT);
        channelprompt.setHorizontalAlignment(SwingConstants.RIGHT);
        channelprompt.setFont(getSmallFont());

        JXLabel channellab = new JXLabel();
        channellab.setFont(getSmallFont());
        setChannelLabel(channellab);

        JXLabel durationprompt = new JXLabel("Duration");
        durationprompt.setHorizontalTextPosition(SwingConstants.RIGHT);
        durationprompt.setHorizontalAlignment(SwingConstants.RIGHT);
        durationprompt.setFont(getSmallFont());

        JXLabel durationlab = new JXLabel();
        durationlab.setFont(getSmallFont());
        setDurationLabel(durationlab);

        JXLabel typeprompt = new JXLabel("Type");
        typeprompt.setHorizontalTextPosition(SwingConstants.RIGHT);
        typeprompt.setHorizontalAlignment(SwingConstants.RIGHT);
        typeprompt.setFont(getSmallFont());

        JComboBox tcb = new JComboBox(RecordingRule.getTypeNames());
        tcb.setSelectedIndex(RecordingRule.SERIES_TYPE);
        tcb.addActionListener(this);
        tcb.setFont(getSmallFont());
        setTypeComboBox(tcb);

        JXLabel priorityprompt = new JXLabel("Priority");
        priorityprompt.setHorizontalTextPosition(SwingConstants.RIGHT);
        priorityprompt.setHorizontalAlignment(SwingConstants.RIGHT);
        priorityprompt.setFont(getSmallFont());

        JComboBox pcb = new JComboBox(RecordingRule.getPriorityNames());
        pcb.setSelectedIndex(RecordingRule.NORMAL_PRIORITY);
        pcb.addActionListener(this);
        pcb.setFont(getSmallFont());
        setPriorityComboBox(pcb);

        JXLabel beginprompt = new JXLabel("Begin Padding (min)");
        beginprompt.setHorizontalTextPosition(SwingConstants.RIGHT);
        beginprompt.setHorizontalAlignment(SwingConstants.RIGHT);
        beginprompt.setFont(getSmallFont());

        SpinnerNumberModel bmodel = new SpinnerNumberModel(0, -120, 120, 1);
        JSpinner bspinner = new JSpinner(bmodel);
        bspinner.addChangeListener(this);
        bspinner.setFont(getSmallFont());
        setBeginSpinner(bspinner);

        JXLabel endprompt = new JXLabel("End Padding (min)");
        endprompt.setHorizontalTextPosition(SwingConstants.RIGHT);
        endprompt.setHorizontalAlignment(SwingConstants.RIGHT);
        endprompt.setFont(getSmallFont());

        SpinnerNumberModel emodel = new SpinnerNumberModel(0, -120, 120, 1);
        JSpinner espinner = new JSpinner(emodel);
        espinner.addChangeListener(this);
        espinner.setFont(getSmallFont());
        setEndSpinner(espinner);

        JButton advanced = new JButton(ADVANCED_TEXT);
        advanced.addActionListener(this);
        advanced.setEnabled(false);
        advanced.setFont(getSmallFont());
        setAdvancedButton(advanced);

        JButton ok = new JButton("Ok");
        ok.addActionListener(this);
        ok.setFont(getSmallFont());
        setOkButton(ok);

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(this);
        cancel.setFont(getSmallFont());
        setCancelButton(cancel);

        JButton aok = new JButton("Ok");
        aok.addActionListener(this);
        aok.setFont(getSmallFont());
        setAdvancedOkButton(aok);

        JButton acancel = new JButton("Cancel");
        acancel.addActionListener(this);
        acancel.setFont(getSmallFont());
        setAdvancedCancelButton(acancel);

        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        add(nameprompt, gbc);

        gbc = new GridBagConstraints();
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        add(namelab, gbc);

        gbc = new GridBagConstraints();
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        add(channelprompt, gbc);

        gbc = new GridBagConstraints();
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        add(channellab, gbc);

        gbc = new GridBagConstraints();
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        add(durationprompt, gbc);

        gbc = new GridBagConstraints();
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        add(durationlab, gbc);

        gbc = new GridBagConstraints();
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        add(typeprompt, gbc);

        gbc = new GridBagConstraints();
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        add(tcb, gbc);

        gbc = new GridBagConstraints();
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        add(priorityprompt, gbc);

        gbc = new GridBagConstraints();
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        add(pcb, gbc);

        gbc = new GridBagConstraints();
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        add(beginprompt, gbc);

        gbc = new GridBagConstraints();
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        add(bspinner, gbc);

        gbc = new GridBagConstraints();
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        add(endprompt, gbc);

        gbc = new GridBagConstraints();
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        add(espinner, gbc);

        gbc = new GridBagConstraints();
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(4, 4, 4, 4);

        add(advanced, gbc);

        gbc = new GridBagConstraints();
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        add(ok, gbc);

        gbc = new GridBagConstraints();
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        add(cancel, gbc);
        setBorder(BorderFactory.createLineBorder(getHighlightColor()));

        setFocusable(true);
        requestFocus();

        InputMap map = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        CancelAction ca = new CancelAction(getCancelButton());
        map.put(KeyStroke.getKeyStroke("ESC"), "cancel");
        getActionMap().put("cancel", ca);
    }

    /**
     * All UI components show data from a RecordingRule instance.
     *
     * @return A RecordingRule object.
     */
    public RecordingRule getRecordingRule() {
        return (recordingRule);
    }

    /**
     * All UI components show data from a RecordingRule instance.
     *
     * @param rr A RecordingRule object.
     */
    public void setRecordingRule(RecordingRule rr) {
        recordingRule = rr;

        if (rr != null) {

            apply(getNameLabel(), rr.getName());
            NMS n = getNMS();
            if (n != null) {

                Channel c = n.getChannelById(rr.getChannelId());
                if (c != null) {
                    apply(getChannelLabel(), c.getNumber());
                } else {
                    apply(getChannelLabel(), null);
                }

            } else {
                apply(getChannelLabel(), null);
            }

            apply(getDurationLabel(), durationToString(rr.getDuration()));
            apply(getTypeComboBox(), rr.getType());
            apply(getPriorityComboBox(), rr.getPriority());
            apply(getBeginSpinner(), rr.getBeginPadding() / 60);
            apply(getEndSpinner(), rr.getEndPadding() / 60);
            getAdvancedButton().setEnabled(rr.getTasks() != null);

        } else {

            apply(getNameLabel(), null);
            apply(getChannelLabel(), null);
            apply(getDurationLabel(), null);
            apply(getTypeComboBox(), RecordingRule.SERIES_TYPE);
            apply(getPriorityComboBox(), RecordingRule.NORMAL_PRIORITY);
            apply(getBeginSpinner(), 0);
            apply(getEndSpinner(), 0);
            getAdvancedButton().setEnabled(false);
        }
    }

    /**
     * A refernce to NMS is needed to do the work of this UI component.
     *
     * @return An NMS instance.
     */
    public NMS getNMS() {
        return (nms);
    }

    /**
     * A refernce to NMS is needed to do the work of this UI component.
     *
     * @param n An NMS instance.
     */
    public void setNMS(NMS n) {
        nms = n;
    }

    /**
     * {@inheritDoc}
     */
    public void performControl() {
    }

    /**
     * {@inheritDoc}
     */
    public void performLayout(Dimension d) {
    }

    /**
     * True if  the user decided to accept what they have changed/edited
     * in the UI panel.
     *
     * @return True when the user clicks the OK button.
     */
    public boolean isAccept() {
        return (accept);
    }

    private void setAccept(boolean b) {
        accept = b;
    }

    private boolean isAdvancedAccept() {
        return (advancedAccept);
    }

    private void setAdvancedAccept(boolean b) {
        advancedAccept = b;
    }

    private JXLabel getNameLabel() {
        return (nameLabel);
    }

    private void setNameLabel(JXLabel l) {
        nameLabel = l;
    }

    private JXLabel getChannelLabel() {
        return (channelLabel);
    }

    private void setChannelLabel(JXLabel l) {
        channelLabel = l;
    }

    private JXLabel getDurationLabel() {
        return (durationLabel);
    }

    private void setDurationLabel(JXLabel l) {
        durationLabel = l;
    }

    private JComboBox getTypeComboBox() {
        return (typeComboBox);
    }

    private void setTypeComboBox(JComboBox cb) {
        typeComboBox = cb;
    }

    private JComboBox getPriorityComboBox() {
        return (priorityComboBox);
    }

    private void setPriorityComboBox(JComboBox cb) {
        priorityComboBox = cb;
    }

    private JSpinner getBeginSpinner() {
        return (beginSpinner);
    }

    private void setBeginSpinner(JSpinner s) {
        beginSpinner = s;
    }

    private JSpinner getEndSpinner() {
        return (endSpinner);
    }

    private void setEndSpinner(JSpinner s) {
        endSpinner = s;
    }

    private JButton getAdvancedButton() {
        return (advancedButton);
    }

    private void setAdvancedButton(JButton b) {
        advancedButton = b;
    }

    /**
     * This panel has an ok button so the user can choose to accept their
     * action.
     *
     * @return A JButton instance.
     */
    public JButton getOkButton() {
        return (okButton);
    }

    private void setOkButton(JButton b) {
        okButton = b;
    }

    /**
     * This panel has a cancel button so the user can choose to do no
     * action.
     *
     * @return A JButton instance.
     */
    public JButton getCancelButton() {
        return (cancelButton);
    }

    private void setCancelButton(JButton b) {
        cancelButton = b;
    }

    private JButton getAdvancedOkButton() {
        return (advancedOkButton);
    }

    private void setAdvancedOkButton(JButton b) {
        advancedOkButton = b;
    }

    private JButton getAdvancedCancelButton() {
        return (advancedCancelButton);
    }

    private void setAdvancedCancelButton(JButton b) {
        advancedCancelButton = b;
    }

    private String durationToString(long l) {

        return ((l / 60) + " minutes");
    }

    private void apply(JXLabel l, String s) {

        if ((l != null) && (s != null)) {

            l.setText(s);
        }
    }

    private void apply(JComboBox cb, int i) {

        if (cb != null) {

            cb.setSelectedIndex(i);
        }
    }

    private void apply(JSpinner s, int value) {

        if (s != null) {

            s.setValue(value);
        }
    }

    private int spinnerToInt(JSpinner s) {

        int result = 0;

        if (s != null) {

            Integer iobj = (Integer) s.getValue();
            if (iobj != null) {

                result = iobj.intValue() * 60;
            }
        }

        return (result);
    }

    private void advancedAction() {

        RecordingRule rr = getRecordingRule();
        if (rr != null) {

            Task[] tasks = rr.getTasks();
            if (tasks != null) {

                System.out.println("tasks.length: " + tasks.length);
                JComponent[] cbuts = new JComponent[tasks.length + 2];
                for (int i = 0; i < tasks.length; i++) {

                    JCheckBox cb = new JCheckBox(tasks[i].getDescription());
                    cb.setFont(getSmallFont());

                    cb.getInputMap().put(KeyStroke.getKeyStroke("ENTER"),
                        "toggle");
                    cb.getActionMap().put("toggle", new CheckAction());

                    cb.setSelected(tasks[i].isRun());
                    cbuts[i] = cb;
                }

                cbuts[tasks.length] = getAdvancedOkButton();
                cbuts[tasks.length + 1] = getAdvancedCancelButton();

                setAdvancedAccept(false);
                ColumnPanel cp = new ColumnPanel(cbuts);
                cp.setBorder(BorderFactory.createLineBorder(
                    getHighlightColor()));
                HashSet<AWTKeyStroke> set =
                    new HashSet<AWTKeyStroke>(cp.getFocusTraversalKeys(
                        KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
                set.clear();
                set.add(KeyStroke.getKeyStroke("RIGHT"));
                cp.setFocusTraversalKeys(
                    KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, set);

                set = new HashSet<AWTKeyStroke>(cp.getFocusTraversalKeys(
                        KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
                set.clear();
                set.add(KeyStroke.getKeyStroke("LEFT"));
                cp.setFocusTraversalKeys(
                    KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, set);

                InputMap map =
                    cp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
                map.put(KeyStroke.getKeyStroke("ESC"), "cancel");
                cp.getActionMap().put("cancel",
                    new CancelAction(getAdvancedCancelButton()));

                Dialog.showPanel(null, cp, getAdvancedOkButton(),
                    getAdvancedCancelButton());

                requestFocus();
                if (isAdvancedAccept()) {

                    for (int i = 0; i < tasks.length; i++) {

                        JCheckBox cb = (JCheckBox) cbuts[i];
                        tasks[i].setRun(cb.isSelected());
                    }
                }
            }
        }
    }

    /**
     * We need to listen to action events to update from user actions
     * with the UI.
     *
     * @param event A given action event.
     */
    public void actionPerformed(ActionEvent event) {

        RecordingRule rr = getRecordingRule();
        if (rr != null) {

            if (event.getSource() == getTypeComboBox()) {
                rr.setType(getTypeComboBox().getSelectedIndex());
            } else if (event.getSource() == getPriorityComboBox()) {
                rr.setPriority(getPriorityComboBox().getSelectedIndex());
            } else if (event.getSource() == getAdvancedButton()) {
                advancedAction();
            } else if (event.getSource() == getOkButton()) {
                setAccept(true);
            } else if (event.getSource() == getCancelButton()) {
                setAccept(false);
            } else if (event.getSource() == getAdvancedOkButton()) {
                setAdvancedAccept(true);
            } else if (event.getSource() == getAdvancedCancelButton()) {
                setAdvancedAccept(false);
            }
        }
    }

    /**
     * We listen for changes in our Spinner UI elements.
     *
     * @param event A ChangeEvent instance.
     */
    public void stateChanged(ChangeEvent event) {

        RecordingRule rr = getRecordingRule();
        if (rr != null) {

            if (event.getSource() == getBeginSpinner()) {

                rr.setBeginPadding(spinnerToInt(getBeginSpinner()));

            } else if (event.getSource() == getEndSpinner()) {

                rr.setEndPadding(spinnerToInt(getEndSpinner()));
            }
        }
    }

    static class CheckAction extends AbstractAction {

        public CheckAction() {
        }

        public void actionPerformed(ActionEvent e) {

            JCheckBox cb = (JCheckBox) e.getSource();
            cb.setSelected(!cb.isSelected());
        }
    }

    static class CancelAction extends AbstractAction {

        private JButton button;

        public CancelAction(JButton b) {
            button = b;
        }

        public void actionPerformed(ActionEvent e) {

            System.out.println("got to");
            if (button != null) {

                button.doClick();
            }
        }
    }

}
