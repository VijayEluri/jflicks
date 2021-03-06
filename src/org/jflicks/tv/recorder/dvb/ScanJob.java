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
package org.jflicks.tv.recorder.dvb;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.jflicks.job.JobContainer;
import org.jflicks.job.JobEvent;
import org.jflicks.job.JobManager;
import org.jflicks.job.SystemJob;
import org.jflicks.tv.recorder.BaseDeviceJob;
import org.jflicks.util.Util;

/**
 *
 * @author Doug Barnum
 * @version 1.0
 */
public class ScanJob extends BaseDeviceJob {

    private static final String SERVICE_LINE = "service is running";
    private static final String CHANNEL_TEXT = "Channel number:";
    private static final String NAME_TEXT = "Name: '";
    private static final String VSB_BAD_TEXT = "VSB_8";
    private static final String VSB_GOOD_TEXT = "8VSB";

    private File file;
    private String[] args;
    private String fileText;
    private String dumpText;

    /**
     * Simple no argument constructor.
     */
    public ScanJob() {
    }

    /**
     * Simple no argument constructor.
     *
     * @param args The arguments to use.
     */
    public ScanJob(String[] args) {

        try {

            File f = File.createTempFile("wscan", ".dump");
            fileText = null;
            dumpText = null;
            setFile(f);
            if (args != null) {

                String[] plus = new String[args.length + 2];
                for (int i = 0; i < args.length; i++) {

                    plus[i] = args[i];
                }

                plus[args.length] = ">";
                plus[args.length + 1] = f.getPath();
                args = plus;
            }

        } catch (IOException ex) {
        }

        setArgs(args);
    }

    private File getFile() {
        return (file);
    }

    private void setFile(File f) {
        file = f;
    }

    private String getDumpText() {
        return (dumpText);
    }

    private void setDumpText(String s) {
        dumpText = s;
    }

    /**
     * Can't do much work with out arguments to run as a system job.
     *
     * @return An array of String instances.
     */
    public String[] getArgs() {
        return (args);
    }

    private void setArgs(String[] array) {
        args = array;
    }

    private String ensurePrintable(String s) {

        String result = s;

        if (result != null) {

            result = result.trim();
            char[] chars = result.toCharArray();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < chars.length; i++) {

                if ((Character.isLetterOrDigit(chars[i]))
                    || (Character.isSpaceChar(chars[i]))
                    || (chars[i] == '.')
                    || (chars[i] == ':')
                    || (chars[i] == '-')
                    || (chars[i] == '_')) {

                    if (chars[i] == ':') {
                        sb.append(".");
                    } else {
                        sb.append(chars[i]);
                    }
                }
            }

            result = sb.toString();
        }

        return (result);
    }

    private String parseChannel(String s) {

        String result = null;

        if (s != null) {

            int index = s.indexOf(CHANNEL_TEXT);
            if (index != -1) {

                index += CHANNEL_TEXT.length();
                int lastIndex = s.indexOf(".", index);
                if (lastIndex != -1) {

                    result = s.substring(index, lastIndex);
                    if (result != null) {

                        result = ensurePrintable(result);
                    }
                }
            }
        }

        return (result);
    }

    private String parseName(String s) {

        String result = null;

        if (s != null) {

            int index = s.indexOf(NAME_TEXT);
            if (index != -1) {

                index += NAME_TEXT.length();
                int lastIndex = s.lastIndexOf("'");
                if ((lastIndex != -1) && (lastIndex > index)) {

                    result = s.substring(index, lastIndex);
                    if (result != null) {

                        result = ensurePrintable(result);
                    }

                } else {

                    // This is a hack as a name sometimes doesn't
                    // have an ending tick.  So just take the rest...
                    result = ensurePrintable(s.substring(index + 1));
                }
            }
        }

        return (result);
    }

    /**
     * Convenience method to parse out the output from the scan program
     * and format the text so it is proper for our use.
     *
     * @return Text usable to write channel config to a conf file.
     */
    public String getFileText() {

        if (fileText == null) {

            String output = getDumpText();
            if (output != null) {

                String[] array = output.split("\n");
                if ((array != null) && (array.length > 0)) {

                    HashMap<String, String> hm = new HashMap<String, String>();
                    for (int i = 0; i < array.length; i++) {

                        // We are at the beginning and we look for
                        // lines that "define" a channel number by
                        // it's name - we need to substitute it later...
                        if (array[i].indexOf(CHANNEL_TEXT) != -1) {

                            String channel = parseChannel(array[i]);
                            String name = parseName(array[i]);
                            if ((channel != null) && (name != null)) {

                                hm.put(name, channel);
                            }
                        }
                    }

                    // OK we have built a hash of channel names and
                    // numbers.  The next thing to do is load the
                    // file output and replace the name with the proper
                    // number.
                    StringBuilder sb = new StringBuilder();
                    array = Util.readTextFile(getFile());
                    for (int i = 0; i < array.length; i++) {

                        // First a hack to fix ATSC bad text.
                        if (array[i].indexOf(VSB_BAD_TEXT) != -1) {

                            array[i] = array[i].replaceAll(VSB_BAD_TEXT,
                                VSB_GOOD_TEXT);
                        }

                        int index = array[i].indexOf(":");
                        if (index != -1) {

                            String name = array[i].substring(0, index);
                            name = ensurePrintable(name);
                            String rest = array[i].substring(index);

                            String channel = hm.get(name);
                            if (channel != null) {

                                sb.append(channel);
                                sb.append(rest);
                                sb.append("\n");
                            }
                        }
                    }

                    // Now at this point we should have lines with
                    // the channel numbers instead of the channel
                    // names.
                    if (sb.length() > 0) {

                        fileText = sb.toString();
                    }
                }
            }
        }

        return (fileText);
    }

    /**
     * {@inheritDoc}
     */
    public void start() {

        setTerminate(false);
    }

    /**
     * {@inheritDoc}
     */
    public void run() {

        SystemJob job = null;
        String[] myargs = getArgs();
        if ((myargs != null) && (args.length > 0)) {

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < myargs.length; i++) {

                if (i > 0) {

                    sb.append(" ");
                }

                sb.append(args[i]);
            }
            job = SystemJob.getInstance(sb.toString());
            fireJobEvent(JobEvent.UPDATE, "command:<" + job.getCommand() + ">");
            setSystemJob(job);
            job.addJobListener(this);
            JobContainer jc = JobManager.getJobContainer(job);
            setJobContainer(jc);
            jc.start();

        } else {

            setTerminate(true);
        }

        while (!isTerminate()) {

            JobManager.sleep(getSleepTime());
        }

        fireJobEvent(JobEvent.COMPLETE);
    }

    /**
     * {@inheritDoc}
     */
    public void stop() {

        setTerminate(true);
        JobContainer jc = getJobContainer();
        if (jc != null) {

            jc.stop();
            setJobContainer(null);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void jobUpdate(JobEvent event) {

        if (event.getType() == JobEvent.COMPLETE) {

            SystemJob job = getSystemJob();
            if (job != null) {

                fireJobEvent(JobEvent.UPDATE, "ProgramJob: exit: "
                    + job.getExitValue());
                setDumpText(job.getOutputText());
                getFileText();
                stop();
            }

        } else {

            fireJobEvent(JobEvent.UPDATE, event.getMessage());
        }
    }

}
