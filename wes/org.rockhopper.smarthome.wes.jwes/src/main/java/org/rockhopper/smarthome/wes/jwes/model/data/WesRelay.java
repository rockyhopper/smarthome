package org.rockhopper.smarthome.wes.jwes.model.data;

import org.rockhopper.smarthome.wes.jwes.model.data.type.Field;

public class WesRelay {
    public WesRelay(byte index) {
        if ((index <= 0) || (index > 2)) {
            throw new UnsupportedOperationException("WES Relays index is 1 to 2");
        }
        this.index = index;

        name = new Field<String, Void>(Field.PRIORITY_DISCOVERY);
        value = new Field<Byte, Void>(Byte.class, Field.PRIORITY_REALTIME);
    }

    private byte index;
    private Field<String, Void> name;
    private Field<Byte, Void> value;

    public byte getIndex() {
        return index;
    }

    public Field<String, Void> getName() {
        return name;
    }

    public void setName(Field<String, Void> name) {
        this.name = name;
    }

    public Field<Byte, Void> getValue() {
        return value;
    }

    public void setValue(Field<Byte, Void> value) {
        this.value = value;
    }
}