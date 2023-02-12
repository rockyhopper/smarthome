package org.rockhopper.smarthome.wes.jwes.model.data;

import java.io.Serializable;

import org.rockhopper.smarthome.wes.jwes.model.data.type.Field;

public class WesInfo implements Serializable {

    private static final long serialVersionUID = -1173298814583885217L;

    public WesInfo() {
        date = new Field<String, Void>(Field.PRIORITY_DISCOVERY);
        time = new Field<String, Void>(Field.PRIORITY_DISCOVERY);
        firmware = new Field<String, Void>(Field.PRIORITY_DISCOVERY);

        nbTempSensors = new Field<Byte, Void>(Byte.class, Field.PRIORITY_DISCOVERY);
        nbHumidSensors = new Field<Byte, Void>(Byte.class, Field.PRIORITY_DISCOVERY);
        nbRelaysCards = new Field<Byte, Void>(Byte.class, Field.PRIORITY_DISCOVERY);
    }

    private Field<String, Void> date;
    private Field<String, Void> time;
    private Field<String, Void> firmware;

    private Field<Byte, Void> nbTempSensors;
    private Field<Byte, Void> nbHumidSensors;
    private Field<Byte, Void> nbRelaysCards;

    public Field<String, Void> getDate() {
        return date;
    }

    public void setDate(Field<String, Void> date) {
        this.date = date;
    }

    public Field<String, Void> getTime() {
        return time;
    }

    public void setTime(Field<String, Void> time) {
        this.time = time;
    }

    public Field<String, Void> getFirmware() {
        return firmware;
    }

    public void setFirmware(Field<String, Void> firmware) {
        this.firmware = firmware;
    }

    public Field<Byte, Void> getNbTempSensors() {
        return nbTempSensors;
    }

    public void setNbTempSensors(Field<Byte, Void> nbTempSensors) {
        this.nbTempSensors = nbTempSensors;
    }

    public Field<Byte, Void> getNbHumidSensors() {
        return nbHumidSensors;
    }

    public void setNbHumidSensors(Field<Byte, Void> nbHumidSensors) {
        this.nbHumidSensors = nbHumidSensors;
    }

    public Field<Byte, Void> getNbRelaysCards() {
        return nbRelaysCards;
    }

    public void setNbRelaysCards(Field<Byte, Void> nbRelaysCards) {
        this.nbRelaysCards = nbRelaysCards;
    }
}