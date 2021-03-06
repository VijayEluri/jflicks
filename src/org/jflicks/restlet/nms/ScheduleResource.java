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
package org.jflicks.restlet.nms;

import java.io.IOException;

import org.jflicks.restlet.BaseServerResource;
import org.jflicks.restlet.NMSSupport;
import org.jflicks.tv.RecordingRule;
import org.jflicks.tv.Task;
import org.jflicks.util.LogUtil;

import org.restlet.data.MediaType;
import org.restlet.resource.Post;
import org.restlet.representation.Representation;

import com.google.gson.Gson;

/**
 * This class will return the current recording rules as XML or JSON.
 *
 * @author Doug Barnum
 * @version 1.0
 */
public class ScheduleResource extends BaseServerResource {

    /**
     * Simple empty constructor.
     */
    public ScheduleResource() {
    }

    @Post("json")
    public void schedule(Representation r) {

        Representation result = null;

        NMSSupport nsup = NMSSupport.getInstance();

        Gson g = getGson();
        if ((g != null) && (r != null)) {

            try {

                String json = r.getText();
                LogUtil.log(LogUtil.DEBUG, json);
                RecordingRule rr = g.fromJson(json, RecordingRule.class);
                LogUtil.log(LogUtil.DEBUG, "after gson: " + rr);
                if (rr != null) {

                    RecordingRule myrr = null;
                    RecordingRule old = nsup.getRecordingRuleById(rr.getId());
                    LogUtil.log(LogUtil.DEBUG, "old rule: " + old);

                    if (old != null) {

                        // Copy old rule and then overwrite with the
                        // users settings.
                        myrr = new RecordingRule(old);
                        myrr.setShowAiring(rr.getShowAiring());
                        myrr.setType(rr.getType());
                        myrr.setName(rr.getName());
                        myrr.setShowId(rr.getShowId());
                        myrr.setSeriesId(rr.getSeriesId());
                        myrr.setChannelId(rr.getChannelId());
                        myrr.setListingId(rr.getListingId());
                        myrr.setDuration(rr.getDuration());
                        myrr.setPriority(rr.getPriority());
                        myrr.setBeginPadding(rr.getBeginPadding());
                        myrr.setEndPadding(rr.getEndPadding());
                        myrr.setTasks(rr.getTasks());

                        // Make sure they have a non-null task list.
                        Task[] tasks = myrr.getTasks();
                        if (tasks == null) {

                            myrr.setTasks(nsup.getTasks());
                        }

                    } else {

                        // Should be a new rule unless they have mucked
                        // things up.
                        myrr = new RecordingRule();
                        myrr.setHostPort(rr.getHostPort());
                        myrr.setShowAiring(rr.getShowAiring());
                        myrr.setType(rr.getType());
                        myrr.setName(rr.getName());
                        myrr.setShowId(rr.getShowId());
                        myrr.setSeriesId(rr.getSeriesId());
                        myrr.setChannelId(rr.getChannelId());
                        myrr.setListingId(rr.getListingId());
                        myrr.setDuration(rr.getDuration());
                        myrr.setPriority(rr.getPriority());
                        myrr.setBeginPadding(rr.getBeginPadding());
                        myrr.setEndPadding(rr.getEndPadding());
                        myrr.setTasks(rr.getTasks());

                        // Make sure they have a non-null task list.
                        Task[] tasks = myrr.getTasks();
                        if (tasks == null) {

                            myrr.setTasks(nsup.getTasks());
                        }
                    }

                    LogUtil.log(LogUtil.DEBUG, "new rule: " + myrr);
                    nsup.schedule(myrr);
                }

            } catch (IOException ex) {

                LogUtil.log(LogUtil.DEBUG, ex.getMessage());
            }
        }
    }

}

