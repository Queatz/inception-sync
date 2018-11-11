package com.inceptionnotes.sync.objects;

import com.arangodb.entity.DocumentField;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jacob on 1/20/18.
 */

public class Note {

    @DocumentField(DocumentField.Type.KEY)
    private String id;
    private String version;
    private Date created;
    private Date updated;

    private String name;
    private String description;
    private String color;
    private List<String> items;
    private List<String> ref;
    private List<String> people;
    private String backgroundUrl;
    private Boolean collapsed;
    private Float estimate;

    private List<String> sync;

    public String getId() {
        return id;
    }

    public Note setId(String id) {
        this.id = id;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public Note setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getName() {
        return name;
    }

    public Note setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Note setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getColor() {
        return color;
    }

    public Note setColor(String color) {
        this.color = color;
        return this;
    }

    public List<String> getItems() {
        return items;
    }

    public Note setItems(List<String> items) {
        this.items = items;
        return this;
    }

    public List<String> getRef() {
        return ref;
    }

    public Note setRef(List<String> ref) {
        this.ref = ref;
        return this;
    }

    public List<String> getPeople() {
        return people;
    }

    public Note setPeople(List<String> people) {
        this.people = people;
        return this;
    }

    public String getBackgroundUrl() {
        return backgroundUrl;
    }

    public Note setBackgroundUrl(String backgroundUrl) {
        this.backgroundUrl = backgroundUrl;
        return this;
    }

    public Boolean getCollapsed() {
        return collapsed;
    }

    public Note setCollapsed(Boolean collapsed) {
        this.collapsed = collapsed;
        return this;
    }

    public Float getEstimate() {
        return estimate;
    }

    public Note setEstimate(Float estimate) {
        this.estimate = estimate;
        return this;
    }

    public Date getCreated() {
        return created;
    }

    public Note setCreated(Date created) {
        this.created = created;
        return this;
    }

    public Date getUpdated() {
        return updated;
    }

    public Note setUpdated(Date updated) {
        this.updated = updated;
        return this;
    }

    public List<String> getSync() {
        return sync;
    }

    public Note setSync(List<String> sync) {
        this.sync = sync;
        return this;
    }

    public Note toSyncNote() {
        Note result = new Note().setId(id);
        result.version = version;
        result.sync = new ArrayList<>();

        if (name != null) result.sync.add("name");
        if (description != null) result.sync.add("description");
        if (color != null) result.sync.add("color");
        if (items != null) result.sync.add("items");
        if (ref != null) result.sync.add("ref");
        if (people != null) result.sync.add("people");
        if (backgroundUrl != null) result.sync.add("backgroundUrl");
        if (collapsed != null) result.sync.add("collapsed");
        if (estimate != null) result.sync.add("estimate");

        return result;
    }

    public void setProp(String type, JsonElement value) {
        switch (type) {
            case "name": setName(value.getAsString()); break;
            case "description": setDescription(value.getAsString()); break;
            case "color": setColor(value.getAsString()); break;
            case "items": setItems(aToL(value.getAsJsonArray())); break;
            case "ref": setRef(aToL(value.getAsJsonArray())); break;
            case "people": setPeople(aToL(value.getAsJsonArray())); break;
            case "backgroundUrl": setBackgroundUrl(value.getAsString()); break;
            case "collapsed": setCollapsed(value.getAsBoolean()); break;
            case "estimate": setEstimate(value.getAsFloat()); break;
        }
    }

    private static List<String> aToL(JsonArray jsonArray) {
        List<String> result = new ArrayList<>();
        jsonArray.forEach(x -> result.add(x.getAsString()));
        return result;
    }
}
