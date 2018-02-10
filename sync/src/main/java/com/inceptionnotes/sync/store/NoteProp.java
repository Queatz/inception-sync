package com.inceptionnotes.sync.store;

import com.google.gson.JsonElement;

/**
 * Created by jacob on 2/10/18.
 */

public class NoteProp {
    private final String id;
    private final String type;
    private final JsonElement value;

    public NoteProp(String id, String type, JsonElement value) {
        this.id = id;
        this.type = type;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public JsonElement getValue() {
        return value;
    }
}
