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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import org.jflicks.job.AbstractJob;
import org.jflicks.job.JobContainer;
import org.jflicks.job.JobEvent;
import org.jflicks.job.JobListener;
import org.jflicks.job.JobManager;
import org.jflicks.nms.Video;
import org.jflicks.util.Util;

/**
 * A job that creates images from a video file.
 *
 * @author Doug Barnum
 * @version 1.0
 */
public class GenerateImageJob extends AbstractJob implements JobListener {

    private Video video;
    private File imageFile;

    /**
     * Constructor with 1 required argument.
     *
     * @param v A Video instance.
     */
    public GenerateImageJob(Video v) {

        setVideo(v);
    }

    private Video getVideo() {
        return (video);
    }

    private void setVideo(Video v) {
        video = v;
    }

    private File getImageFile() {
        return (imageFile);
    }

    private void setImageFile(File f) {
        imageFile = f;
    }

    /**
     * {@inheritDoc}
     */
    public void start() {

        Video v = getVideo();
        if (v != null) {

            try {

                File tmp = File.createTempFile("generate", ".png");
                setImageFile(tmp);
                ThumbnailerJob job =
                    new ThumbnailerJob(v.getPath(), tmp.getPath(), 10);
                job.addJobListener(this);
                JobContainer jc = JobManager.getJobContainer(job);
                jc.start();
                setTerminate(false);

            } catch (IOException ex) {
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void run() {

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
    }

    /**
     * {@inheritDoc}
     */
    public void jobUpdate(JobEvent event) {

        if (event.getType() == JobEvent.COMPLETE) {

            try {

                // Should have fanart now.  Let's read it and examine.
                BufferedImage bi = ImageIO.read(getImageFile());
                Video v = getVideo();
                if ((bi != null) && (v != null)) {

                    if (bi.getWidth() < 1280) {

                        // We have a source video that is 4x3.  We need to
                        // cut off the bottom.
                        //bi = bi.getSubimage(0, 0, 1280, height);
                        bi = Util.scale(bi, 1280);

                    } else if ((bi.getWidth() == 1440)
                        && (bi.getHeight() == 1080)) {

                        // We have a 1440x1080 image that needs to be
                        // stretched to 1920x1080.
                        bi = Util.resize(bi, 1280, 720);
                    }

                    int height = bi.getHeight();

                    // At this point our image should be 1280x720 which will
                    // be our fanart.  Next we make a poster by doing a
                    // center cut.
                    BufferedImage pbi = bi.getSubimage(360, 0, 495, height);

                    File fanart = File.createTempFile("fanart", ".png");
                    ImageIO.write(bi, "PNG", fanart);

                    File poster = File.createTempFile("poster", ".png");
                    ImageIO.write(pbi, "PNG", poster);

                    v.setFanartURL(fanart.toURI().toString());
                    v.setPosterURL(poster.toURI().toString());
                }

            } catch (IOException ex) {
            }

            stop();
        }
    }

}
