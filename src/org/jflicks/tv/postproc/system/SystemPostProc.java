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
package org.jflicks.tv.postproc.system;

import java.util.ArrayList;

import org.jflicks.tv.Recording;
import org.jflicks.tv.RecordingRule;
import org.jflicks.tv.Task;
import org.jflicks.tv.postproc.BasePostProc;
import org.jflicks.tv.postproc.worker.Worker;
import org.jflicks.tv.postproc.worker.WorkerEvent;
import org.jflicks.util.LogUtil;

/**
 * Class that implements the PostProc service.
 *
 * @author Doug Barnum
 * @version 1.0
 */
public class SystemPostProc extends BasePostProc {

    private ArrayList<WorkerRecording> heavyWorkerRecordingList;
    private ArrayList<WorkerRecording> lightWorkerRecordingList;

    /**
     * Simple default constructor.
     */
    public SystemPostProc() {

        setTitle("System Post Proc");
        setHeavyWorkerRecordingList(new ArrayList<WorkerRecording>());
        setLightWorkerRecordingList(new ArrayList<WorkerRecording>());
    }

    private ArrayList<WorkerRecording> getHeavyWorkerRecordingList() {
        return (heavyWorkerRecordingList);
    }

    private void setHeavyWorkerRecordingList(ArrayList<WorkerRecording> l) {
        heavyWorkerRecordingList = l;
    }

    private void addHeavyWorkerRecording(WorkerRecording wr) {

        ArrayList<WorkerRecording> l = getHeavyWorkerRecordingList();
        if ((wr != null) && (l != null)) {

            synchronized (l) {

                l.add(wr);
                LogUtil.log(LogUtil.INFO, "Heavy queue size now: " + l.size());
            }
        }
    }

    /**
     * Perhaps after popping the heavy WorkerRecording, it may be determined
     * that it's not "ready" for work just yet.  This method allows it to be
     * placed back at the front of the queue, to wait some time before
     * trying again.
     *
     * @param wr A given WorkerRecording to be at the front of the queue.
     */
    public void pushHeavyWorkerRecording(WorkerRecording wr) {

        ArrayList<WorkerRecording> l = getHeavyWorkerRecordingList();
        if ((wr != null) && (l != null)) {

            synchronized (l) {

                l.add(0, wr);
                String title = "unknown";
                Worker w = wr.getWorker();
                if (w != null) {
                    title = w.getTitle();
                }

                LogUtil.log(LogUtil.INFO, "(Push) Heavy " + title + " queue size now: "
                    + l.size());
            }
        }
    }

    /**
     * We have a queue of heavy WorkerRecording instances that need to be done.
     * This method will return the next one that should be done.
     *
     * @return A WorkerRecording instance.
     */
    public WorkerRecording popHeavyWorkerRecording() {

        WorkerRecording result = null;

        ArrayList<WorkerRecording> l = getHeavyWorkerRecordingList();
        if ((l != null) && (l.size() > 0)) {

            synchronized (l) {

                result = l.get(0);
                l.remove(0);
                LogUtil.log(LogUtil.INFO, "(Pop) Heavy queue size now: " + l.size());
            }
        }

        return (result);
    }

    private ArrayList<WorkerRecording> getLightWorkerRecordingList() {
        return (lightWorkerRecordingList);
    }

    private void setLightWorkerRecordingList(ArrayList<WorkerRecording> l) {
        lightWorkerRecordingList = l;
    }

    private void addLightWorkerRecording(WorkerRecording wr) {

        ArrayList<WorkerRecording> l = getLightWorkerRecordingList();
        if ((wr != null) && (l != null)) {

            synchronized (l) {

                l.add(wr);
                LogUtil.log(LogUtil.INFO, "Light queue size now: " + l.size());
            }
        }
    }

    /**
     * Perhaps after popping the light WorkerRecording, it may be determined
     * that it's not "ready" for work just yet.  This method allows it to be
     * placed back at the front of the queue, to wait some time before trying
     * again.
     *
     * @param wr A given WorkerRecording to be at the front of the queue.
     */
    public void pushLightWorkerRecording(WorkerRecording wr) {

        ArrayList<WorkerRecording> l = getLightWorkerRecordingList();
        if ((wr != null) && (l != null)) {

            synchronized (l) {

                l.add(0, wr);
                String title = "unknown";
                Worker w = wr.getWorker();
                if (w != null) {
                    title = w.getTitle();
                }
                LogUtil.log(LogUtil.INFO, "(Push) Light " + title + " queue size now: "
                    + l.size());
            }
        }
    }

    /**
     * We have a queue of light WorkerRecording instances that need to be done.
     * This method will return the next one that should be done.
     *
     * @return A WorkerRecording instance.
     */
    public WorkerRecording popLightWorkerRecording() {

        WorkerRecording result = null;

        ArrayList<WorkerRecording> l = getLightWorkerRecordingList();
        if ((l != null) && (l.size() > 0)) {

            synchronized (l) {

                result = l.get(0);
                l.remove(0);
                LogUtil.log(LogUtil.INFO, "(Pop) Light queue size now: " + l.size());
            }
        }

        return (result);
    }

    /**
     * {@inheritDoc}
     */
    public void addProcessing(RecordingRule rr, Recording r, boolean commercialDetect) {

        if ((rr != null) && (r != null)) {

            Task[] array = rr.getTasks();
            if (array != null) {

                // First we go through and add all post processing EXCEPT
                // for commercial skip.  We want to do that LAST.
                if (!commercialDetect) {

                    for (int i = 0; i < array.length; i++) {

                        if (array[i].isRun()) {

                            Worker w = getWorkerByTitle(array[i].getTitle());
                            if (w != null) {

                                if (!w.isCommercialDetector()) {

                                    WorkerRecording wr = new WorkerRecording();
                                    wr.setWorker(w);
                                    wr.setRecording(r);
                                    if (w.isHeavy()) {

                                        LogUtil.log(LogUtil.INFO, "Time to queue a heavy worker..." + w.getTitle());
                                        addHeavyWorkerRecording(wr);

                                    } else {

                                        LogUtil.log(LogUtil.INFO, "Time to queue a light worker..." + w.getTitle());
                                        addLightWorkerRecording(wr);
                                    }
                                }
                            }
                        }
                    }
                }

                if (commercialDetect) {

                    // Now just do commercial skipping
                    for (int i = 0; i < array.length; i++) {

                        if (array[i].isRun()) {

                            Worker w = getWorkerByTitle(array[i].getTitle());
                            if (w != null) {

                                if (w.isCommercialDetector()) {

                                    WorkerRecording wr = new WorkerRecording();
                                    wr.setWorker(w);
                                    wr.setRecording(r);
                                    if (w.isHeavy()) {

                                        LogUtil.log(LogUtil.INFO, "Time to queue a heavy worker..." + w.getTitle());
                                        addHeavyWorkerRecording(wr);

                                    } else {

                                        LogUtil.log(LogUtil.INFO, "Time to queue a light worker..." + w.getTitle());
                                        addLightWorkerRecording(wr);
                                    }

                                    // We just have at most one commercial skip per recording.
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addProcessing(String s, Recording r) {

        if ((s != null) && (r != null)) {

            Worker w = getWorkerByTitle(s);
            if (w != null) {

                WorkerRecording wr = new WorkerRecording();
                wr.setWorker(w);
                wr.setRecording(r);
                if (w.isHeavy()) {
                    LogUtil.log(LogUtil.INFO, "Time to queue up a heavy worker..." + w.getTitle());
                    addHeavyWorkerRecording(wr);
                } else {
                    LogUtil.log(LogUtil.INFO, "Time to queue up a light worker..." + w.getTitle());
                    addLightWorkerRecording(wr);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void workerUpdate(WorkerEvent event) {

        if (event.isUpdateRecording()) {

            Recording r = event.getRecording();
            if (r != null) {

                LogUtil.log(LogUtil.INFO, "workerUpdate: updating Recording in db");
                updateRecording(r);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isWorkPending(Recording r) {

        boolean result = false;

        if (r != null) {

            result = isWorkPending(r, getHeavyWorkerRecordingList());
            if (!result) {

                result = isWorkPending(r, getLightWorkerRecordingList());
            }

        } else {

            LogUtil.log(LogUtil.INFO, "isWorkPending given a null recording");
        }

        return (result);
    }

    private boolean isWorkPending(Recording r, ArrayList<WorkerRecording> l) {

        boolean result = false;

        if ((r != null) && (l != null) && (l.size() > 0)) {

            WorkerRecording[] array = l.toArray(new WorkerRecording[l.size()]);
            for (WorkerRecording wr : array) {

                if (r.equals(wr.getRecording())) {

                    result = true;
                    break;
                }
            }
        }

        return (result);
    }

}

