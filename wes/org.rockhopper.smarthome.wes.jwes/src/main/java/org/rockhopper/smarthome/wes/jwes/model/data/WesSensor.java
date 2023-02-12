package org.rockhopper.smarthome.wes.jwes.model.data;

import java.math.BigDecimal;

import org.rockhopper.smarthome.wes.jwes.model.data.type.Field;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Up to 30 sensors
 */
@XStreamAlias("sensor")
public class WesSensor {
    public static final byte MAX_SENSORS = 30;

    public WesSensor(byte index) {
        if ((index < 0) || (index >= 30)) {
            throw new UnsupportedOperationException("WES Sensor index is 0 to 29");
        }
        this.index = index;

        id = new Field<String, Void>(Field.PRIORITY_DISCOVERY);
        name = new Field<String, Void>(Field.PRIORITY_DISCOVERY);
        type = new Field<String, Void>(Field.PRIORITY_DISCOVERY);
        value = new Field<BigDecimal, Void>(BigDecimal.class, Field.PRIORITY_LOW);
    }

    private byte index;
    private Field<String, Void> id;
    private Field<String, Void> name;
    private Field<String, Void> type;
    private Field<BigDecimal, Void> value;

    public Field<String, Void> getId() {
        return id;
    }

    public void setId(Field<String, Void> id) {
        this.id = id;
    }

    public Field<String, Void> getName() {
        return name;
    }

    public void setName(Field<String, Void> name) {
        this.name = name;
    }

    public Field<String, Void> getType() {
        return type;
    }

    public void setType(Field<String, Void> type) {
        this.type = type;
    }

    public Field<BigDecimal, Void> getValue() {
        return value;
    }

    public void setValue(Field<BigDecimal, Void> value) {
        this.value = value;
    }

    public byte getIndex() {
        return index;
    }
}