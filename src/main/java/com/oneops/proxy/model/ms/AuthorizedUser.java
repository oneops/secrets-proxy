package com.oneops.proxy.model.ms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/** Response object for Auth Api in Managed Services. Return if true/false if user is authorized with given namespace.
 *
 * @author Varsha
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorizedUser {

    @JsonProperty
    private String user;
    @JsonProperty
    private boolean authorized;
    @JsonProperty
    private String error;
    @JsonProperty
    private Map<String, String> metadata;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "AuthorizedUser{"
            + " user='"
            + user
            + '\''
            + ", authorized='"
            + authorized
            + '\''
            + ", error='"
            + error
            + '\''
            + ", metadata="
            + metadata
            + '\''
            + '}';
    }
}
