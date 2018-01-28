package com.inceptionnotes.sync.objects;

import com.arangodb.entity.DocumentField;

/**
 * Created by jacob on 1/28/18.
 */

public class Relationship {
    public static final String PERSON = "person";
    public static final String REFERENCE = "reference";
    public static final String ITEM = "item";

    @DocumentField(DocumentField.Type.KEY)
    private String id;
    @DocumentField(DocumentField.Type.FROM)
    private String from;
    @DocumentField(DocumentField.Type.TO)
    private String to;
    private String type;

    public String getId() {
        return id;
    }

    public Relationship setId(String id) {
        this.id = id;
        return this;
    }

    public String getFrom() {
        return from;
    }

    public Relationship setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getTo() {
        return to;
    }

    public Relationship setTo(String to) {
        this.to = to;
        return this;
    }

    public String getType() {
        return type;
    }

    public Relationship setType(String type) {
        this.type = type;
        return this;
    }
}
