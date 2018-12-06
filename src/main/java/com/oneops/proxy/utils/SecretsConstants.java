package com.oneops.proxy.utils;

/** Constants used for invoking authorization API
 * @author Varsha
 */
public class SecretsConstants {

    /* Secrets-Proxy support for new applications like ms -> Managed services, tekton -> Tekton*/
    public static final String MS_APP = "ms";
    public static final String TEKTON_APP = "tekton";

    /* Auth Api URL prefix for MS and Tekton applications */
    public static final String MS_AUTH_PREFIX = "api/ms/auth";
    public static final String TEKTON_AUTH_PREFIX = "v1/access/";

    /* Auth Api header info for MS and Tekton applications */
    public static final String MS_AUTH_HEADER = "X-Auth-Token";
    public static final String TEKTON_AUTH_HEADER = "Authorization";

    /** Application group name separator. */
    public static final String GROUP_SEP = "_";

    /** Application domain name separator. */
    public static final String DOMAIN_SEP = "-";
}
