package com.inceptionnotes.sync.store;

import java.util.List;

/**
 * Created by jacob on 2/10/18.
 */

public class PropSet {
    private final String noteId;
    private final List<NoteProp> props;

    public PropSet(String noteId, List<NoteProp> props) {
        this.noteId = noteId;
        this.props = props;
    }

    public String getNoteId() {
        return noteId;
    }

    public List<NoteProp> getProps() {
        return props;
    }
}
