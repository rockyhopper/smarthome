package org.rockhopper.smarthome.wes.jwes.model;

import java.io.Serializable;

public class WesEvent implements Serializable {

    private static final long serialVersionUID = -5249669276345154490L;

    private String fieldLabel;
    private WesEventCode eventCode;
    private Object oldValue;
    private Object newValue;

    public WesEvent(String fieldLabel, WesEventCode eventCode, Object oldValue, Object newValue) {
        this.fieldLabel = fieldLabel;
        this.eventCode = eventCode;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getFieldLabel() {
        return fieldLabel;
    }

    public WesEventCode getEventCode() {
        return eventCode;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    public enum WesEventCode {
        UPDATE,
        COMMAND,
        SYNC
    }
}
