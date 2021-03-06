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

import org.jflicks.restlet.BaseApplication;

import org.restlet.Restlet;
import org.restlet.resource.ClientResource;
import org.restlet.routing.Router;

/**
 * This class is an implementation of a restlet application.
 *
 * @author Doug Barnum
 * @version 1.0
 */
public class NMSApplication extends BaseApplication {

    /**
     * Simple empty constructor.
     */
    public NMSApplication() {

        setName("RESTful jflicks media system");
        setDescription("Access and control local jflicks server component.");
        setOwner("jflicks.org");
        setAuthor("Doug Barnum, copyright 2014");
        setAlias("nms");
    }

    @Override
    public Restlet createInboundRoot() {

        Router router = new Router(getContext());

        router.attach("/", RootResource.class);
        router.attach("/{version}/recordings.{format}", RecordingResource.class);
        router.attach("/{version}/recordings/{title}.{format}", RecordingByTitleResource.class);
        router.attach("/{version}/recording/titles.{format}", RecordingTitleResource.class);
        router.attach("/{version}/recording/{recordingId}/"
            + "{allowRerecord}", DeleteRecordingResource.class);
        router.attach("/{version}/recordingstop/{recordingId}", StopRecordingResource.class);
        router.attach("/{version}/recordingrefresh/{recordingId}.{format}", RecordingRefreshResource.class);
        router.attach("/{version}/recordingrules.{format}", RecordingRuleResource.class);
        router.attach("/{version}/recordingrule/{ruleId}", RecordingRuleResource.class);
        router.attach("/{version}/state.{format}", StateResource.class);
        router.attach("/{version}/search/{term}.{format}", SearchResource.class);
        router.attach("/{version}/searchtitlestartswith/{unique}/{term}.{format}",
            SearchTitleStartsWithResource.class);
        router.attach("/{version}/channels.{format}", ChannelResource.class);
        router.attach("/{version}/schedule", ScheduleResource.class);
        router.attach("/{version}/tasks.{format}", TaskResource.class);
        router.attach("/{version}/upcomings.{format}", UpcomingResource.class);
        router.attach("/{version}/upcoming", UpcomingResource.class);
        router.attach("/{version}/videos.{format}", VideoResource.class);
        router.attach("/{version}/guide/{channelId}.{format}", GuideChannelResource.class);
        router.attach("/{version}/guide/{term}/{channelId}.{format}", GuideTitleChannelResource.class);
        router.attach("/{version}/livetv/items.{format}", LiveTVItemResource.class);
        router.attach("/{version}/livetv/itemrefresh/{channelId}.{format}", LiveTVItemRefreshResource.class);
        router.attach("/{version}/livetv/opendirect/{recorderId}.{format}", LiveTVOpenDirectResource.class);
        router.attach("/{version}/livetv/closedirect/{recorderId}.{format}", LiveTVCloseDirectResource.class);
        router.attach("/{version}/livetv/open/{channelId}.{format}", LiveTVOpenCloseResource.class);
        router.attach("/{version}/livetv/close/{liveTVId}.{format}", LiveTVOpenCloseResource.class);
        router.attach("/{version}/j4cc/{host}/configuration.{format}", J4ccConfigurationResource.class);
        router.attach("/{version}/inuse/{recordingId}/{hostPort}/yes", InUseYesResource.class);
        router.attach("/{version}/inuse/{recordingId}/{hostPort}/no", InUseNoResource.class);
        router.attach("/{version}/inuse/free", InUseFreeResource.class);

        return (router);
    }

}

