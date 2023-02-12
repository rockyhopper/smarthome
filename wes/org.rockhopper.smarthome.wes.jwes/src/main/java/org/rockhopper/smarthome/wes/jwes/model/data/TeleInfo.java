package org.rockhopper.smarthome.wes.jwes.model.data;

import org.rockhopper.smarthome.wes.jwes.model.data.type.Field;

public class TeleInfo {

    public static final String DISABLED_ADCO_VALUE = "Pas Dispo";

    public TeleInfo(byte index) {
        if ((index <= 0) || (index > 3)) {
            throw new UnsupportedOperationException("TIC index is 1 to 3");
        }
        this.index = index;

        name = new Field<String, Void>(Field.PRIORITY_DISCOVERY);
        adco = new Field<String, Void>(Field.PRIORITY_DISCOVERY);
        aboName = new Field<String, Void>(Field.PRIORITY_DISCOVERY);
        index1 = new Field<String, Void>(Field.PRIORITY_MEDIUM);
        iAbo = new Field<Byte, Void>(Byte.class, Field.PRIORITY_DISCOVERY);
        iinstMono = new Field<Byte, Void>(Byte.class, Field.PRIORITY_MEDIUM);
        pApps = new Field<String, Void>(Field.PRIORITY_HIGH);
        voltages = new Field<String, Void>(Field.PRIORITY_DISCOVERY); // Should be Field.PRIORITY_HIGH but TCP code is
                                                                      // not documented!
    }

    private byte index;
    private Field<String, Void> name;
    private Field<String, Void> adco;
    private Field<String, Void> aboName;
    private Field<String, Void> index1;
    private Field<Byte, Void> iAbo;
    private Field<Byte, Void> iinstMono;
    private Field<String, Void> pApps;
    private Field<String, Void> voltages;

    public byte getIndex() {
        return index;
    }

    public void setIndex(byte index) {
        this.index = index;
    }

    public Field<String, Void> getName() {
        return name;
    }

    public void setName(Field<String, Void> name) {
        this.name = name;
    }

    public Field<String, Void> getAdco() {
        return adco;
    }

    public void setAdco(Field<String, Void> adco) {
        this.adco = adco;
    }

    public Field<String, Void> getAboName() {
        return aboName;
    }

    public void setAboName(Field<String, Void> aboName) {
        this.aboName = aboName;
    }

    public Field<String, Void> getIndex1() {
        return index1;
    }

    public void setIndex1(Field<String, Void> index1) {
        this.index1 = index1;
    }

    public Field<Byte, Void> getiAbo() {
        return iAbo;
    }

    public void setiAbo(Field<Byte, Void> iAbo) {
        this.iAbo = iAbo;
    }

    public Field<Byte, Void> getIinstMono() {
        return iinstMono;
    }

    public void setIinstMono(Field<Byte, Void> iinstMono) {
        this.iinstMono = iinstMono;
    }

    public Field<String, Void> getpApps() {
        return pApps;
    }

    public void setpApps(Field<String, Void> pApps) {
        this.pApps = pApps;
    }

    public Field<String, Void> getVoltages() {
        return voltages;
    }

    public void setVoltages(Field<String, Void> voltages) {
        this.voltages = voltages;
    }

    public Boolean isEnabled() {
        Boolean enabled = null;
        if ((adco != null) && (adco.getValue() != null)) {
            enabled = (DISABLED_ADCO_VALUE.equals(adco.getValue()) ? Boolean.FALSE : Boolean.TRUE);
        }
        return enabled;

    }
}