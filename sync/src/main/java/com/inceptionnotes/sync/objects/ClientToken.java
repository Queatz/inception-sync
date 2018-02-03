package com.inceptionnotes.sync.objects;

import java.util.Date;

/**
 * Created by jacob on 1/30/18.
 */

public class ClientToken {
    private String clientToken;
    private String personId;
    private Date lastSyncTime;

    public String getClientToken() {
        return clientToken;
    }

    public ClientToken setClientToken(String clientToken) {
        this.clientToken = clientToken;
        return this;
    }

    public Date getLastSyncTime() {
        return lastSyncTime;
    }

    public ClientToken setLastSyncTime(Date lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
        return this;
    }

    public String getPersonId() {
        return personId;
    }

    public ClientToken setPersonId(String personId) {
        this.personId = personId;
        return this;
    }
}
