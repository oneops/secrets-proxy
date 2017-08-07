package com.oneops.proxy.audit;

/**
 * Possible types of events in Keywhiz Proxy.
 *
 * @author Suresh G
 */
public enum EventTag {
    SECRET_CREATE,
    SECRET_CREATEORUPDATE,
    SECRET_UPDATE,
    SECRET_CHANGEVERSION,
    SECRET_DELETE,
    SECRET_BACKFILLEXPIRY,
    SECRET_READCONTENT,

    GROUP_CREATE,
    GROUP_DELETE,
    GROUP_BACKUP,

    CLIENT_CREATE,
    CLIENT_DELETE,

    CHANGEACL_GROUP_SECRET,
    CHANGEACL_GROUP_CLIENT,

    GENERATE_TOKEN
}