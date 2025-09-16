package org.elmorshedy.meeting.model;

public enum Location {
    ONLINE,
    OFFLINE;

    public boolean requiresOfflineLocation() {
        return this == OFFLINE;
    }
}
