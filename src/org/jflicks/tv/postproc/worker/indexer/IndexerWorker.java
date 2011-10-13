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
package org.jflicks.tv.postproc.worker.indexer;

import org.jflicks.job.JobContainer;
import org.jflicks.job.JobEvent;
import org.jflicks.job.JobListener;
import org.jflicks.job.JobManager;
import org.jflicks.tv.Recording;
import org.jflicks.tv.postproc.worker.BaseWorker;
import org.jflicks.tv.postproc.worker.WorkerEvent;

/**
 * A generic indexer that just runs a command line program.
 *
 * @author Doug Barnum
 * @version 1.0
 */
public class IndexerWorker extends BaseWorker implements JobListener {

    private String commandLine;
    private String extension;

    /**
     * Simple default constructor.
     */
    public IndexerWorker() {

        setTitle("IndexerWorker");
        setDescription("IndexerWorker");
        setHeavy(true);
        setDefaultRun(false);
        setUserSelectable(false);
        setIndexer(true);
    }

    public String getCommandLine() {
        return (commandLine);
    }

    public void setCommandLine(String s) {
        commandLine = s;
    }

    public String getExtension() {
        return (extension);
    }

    public void setExtension(String s) {
        extension = s;
    }

    /**
     * {@inheritDoc}
     */
    public void work(Recording r) {

        if (r != null) {

            IndexerJob job =
                new IndexerJob(r, this, getCommandLine(), getExtension());
            job.addJobListener(this);
            JobContainer jc = JobManager.getJobContainer(job);
            addJobContainer(jc);
            jc.start();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void jobUpdate(JobEvent event) {

        if (event.getType() == JobEvent.COMPLETE) {

            log(INFO, "IndexerWorker: completed");
            IndexerJob job = (IndexerJob) event.getSource();
            removeJobContainer(job);
            fireWorkerEvent(WorkerEvent.COMPLETE, job.getRecording(), true);

        } else {

            //log(DEBUG, "IndexerWorker: " + event.getMessage());
        }
    }

}
