package org.rockhopper.smarthome.wes.jwes.model;

public interface WesEventListener {
    public default void onEvent(WesEvent event) {
        System.out.println("(WES) Event -" + event.getFieldLabel() + "[" + event.getEventCode() + "]:"
                + event.getOldValue() + "->" + event.getNewValue());
    }
}
