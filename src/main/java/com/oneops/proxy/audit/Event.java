/*******************************************************************************
 *
 *   Copyright 2017 Walmart, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *******************************************************************************/
package com.oneops.proxy.audit;

import java.util.Map;

/**
 * An audit event for keywhiz-proxy.
 * <p>
 * Eg: <b>SECRET_CREATE | User: suresh | Group: oneops_keywhiz-proxy_prod | Object: prod_db_password.txt | ExtraInfo:</b>
 *
 * @author Suresh
 */
public class Event {

    /**
     * Event type
     */
    private final EventTag type;
    /**
     * User generated the event.
     */
    private final String user;
    /**
     * Application group of event.
     */
    private final String appGroup;
    /**
     * The name of the affected object
     */
    private final String objectName;

    /**
     * Any extra information
     */
    private final Map<String, String> extraInfo;

    public Event(EventTag type, String user, String appGroup, String objectName, Map<String, String> extraInfo) {
        this.type = type;
        this.user = user;
        this.appGroup = appGroup;
        this.objectName = objectName;
        this.extraInfo = extraInfo;
    }

    public Event(EventTag type, String user, String appGroup) {
        this(type, user, appGroup, "", null);
    }

    public Event(EventTag type, String user, String appGroup, String objectName) {
        this(type, user, appGroup, objectName, null);
    }

    public EventTag getType() {
        return type;
    }

    public String getUser() {
        return user;
    }

    public String getAppGroup() {
        return appGroup;
    }

    public String getObjectName() {
        return objectName;
    }

    public Map<String, String> getExtraInfo() {
        return extraInfo;
    }

    @Override
    public String toString() {
        String extInfo = extraInfo == null ? "" : extraInfo.toString();
        return String.format("%-25s | User: %-15s | Group: %-45s | Object: %-30s | ExtraInfo: %s ", type, user, appGroup, objectName, extInfo);
    }
}
