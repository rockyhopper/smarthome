package org.rockhopper.smarthome.wes.jwes.model.data;

import org.rockhopper.smarthome.wes.jwes.model.data.type.Field;

public class WesOneWireRelay {
    public WesOneWireRelay(byte cardIndex, byte index) {
        if ((index <= 0) || (index > 8)) {
            throw new UnsupportedOperationException("WES OneWire Relays index is 1 to 8");
        }
        if ((cardIndex < 0) || (cardIndex >= 10)) {
            throw new UnsupportedOperationException("WES RelaysCard index is 0 to 9");
        }
        this.index = index;
        this.cardIndex = cardIndex;
        name = new Field<String, Void>(Field.PRIORITY_DISCOVERY);
        state = new Field<Boolean, Void>(Boolean.class, Field.PRIORITY_SKIPPED);
    }

    private byte index;
    private byte cardIndex;
    private Field<String, Void> name;
    private Field<Boolean, Void> state;

    public Field<String, Void> getName() {
        return name;
    }

    public void setName(Field<String, Void> name) {
        this.name = name;
    }

    public Field<Boolean, Void> getState() {
        return state;
    }

    public void setState(Field<Boolean, Void> state) {
        this.state = state;
    }

    public void setState(Boolean state) {
        if (state != null) {
            if (this.state == null) {
                this.state = new Field<Boolean, Void>(Boolean.class, Field.PRIORITY_SKIPPED);
            }
            this.state.setValue(state);
        }
    }

    public byte getIndex() {
        return index;
    }

    public byte getCardIndex() {
        return cardIndex;
    }
}