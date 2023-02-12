package org.rockhopper.smarthome.wes.jwes.model.data;

import java.util.ArrayList;
import java.util.List;

import org.rockhopper.smarthome.wes.jwes.model.data.type.Field;
import org.rockhopper.smarthome.wes.jwes.model.data.type.VirtualField;
import org.rockhopper.smarthome.wes.jwes.model.helper.WesDataNavigatorHelper;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Up to 10 relaysCards
 */
@XStreamAlias("relayCard")
public class WesRelaysCard extends Field<String, Void> implements VirtualField {

    public WesRelaysCard(byte index) {
        this(index, Field.PRIORITY_REALTIME);
        for (int j = 1; j <= 8; j++) {
            addRelay(new WesOneWireRelay(index, (byte) j));
        }
    }

    public WesRelaysCard(byte index, Byte priority) {
        super(priority);
        if ((index < 0) || (index >= 10)) {
            throw new UnsupportedOperationException("WES RelaysCard index is 0 to 9");
        }
        this.index = index;

        id = new Field<String, Void>(Field.PRIORITY_DISCOVERY);
        name = new Field<String, Void>(Field.PRIORITY_DISCOVERY);
        cardType = new Field<String, Void>(Field.PRIORITY_DISCOVERY);
    }

    private byte index;
    private Field<String, Void> id;
    private Field<String, Void> name;
    private Field<String, Void> cardType;

    @XStreamImplicit(itemFieldName = "relay")
    List<WesOneWireRelay> relays;

    public void addRelay(WesOneWireRelay relay) {
        if (relays == null) {
            relays = new ArrayList<WesOneWireRelay>(8);
        }
        relays.add(relay);
    }

    public byte getIndex() {
        return index;
    }

    public Field<String, Void> getName() {
        return name;
    }

    public void setName(Field<String, Void> name) {
        this.name = name;
    }

    public Field<String, Void> getCardType() {
        return cardType;
    }

    public void setCardType(Field<String, Void> cardType) {
        this.cardType = cardType;
    }

    public List<WesOneWireRelay> getRelays() {
        return relays;
    }

    public void setRelays(List<WesOneWireRelay> relays) {
        this.relays = relays;
    }

    public Field<String, Void> getId() {
        return id;
    }

    public Field<String, Void> getStates() {
        Field<String, Void> states = null;
        if (relays != null) {
            states = new Field<String, Void>(Field.PRIORITY_REALTIME);

            String cardLabel = WesDataNavigatorHelper.LABEL_DATA_PREFIX
                    + String.format(WesDataNavigatorHelper.LABEL_RELAYSCARD_STATES_PATTERN_SUFFIX, index);

            states.setLabel(cardLabel);
            states.setValue(getValue());
        }
        return states;
    }

    public void setStates(Field<String, Void> states) {
        if ((states != null) && (states.getValue() != null)) {
            String sStates = states.getValue();
            setValue(sStates);
        }
    }

    public Boolean[] getStatesAsBooleanArray() {
        String states = getStatesAsString();
        return fromString(states);
    }

    public String getStatesAsString() {
        if (relays == null) {
            return null;
        }
        String states = "";
        for (WesOneWireRelay wesOneWireRelay : relays) {
            char state = ' ';
            if ((wesOneWireRelay.getState() != null) && (wesOneWireRelay.getState().getValue() != null)) {
                if (wesOneWireRelay.getState().getValue().booleanValue()) {
                    state = '1';
                } else {
                    state = '0';
                }
            }
            states += state;
        }
        return states;
    }

    private static Boolean[] fromString(String binary) {
        Boolean[] bitset = new Boolean[binary.length()];
        for (int i = 0; i < binary.length(); i++) {
            if (binary.charAt(i) == '1') {
                bitset[i] = true;
            } else if (binary.charAt(i) == '0') {
                bitset[i] = false;
            } else {
                bitset[i] = null;
            }
        }
        return bitset;
    }

    public Boolean getState(byte relayIndex) {
        if (getRelays() == null) {
            return null;
        }
        WesOneWireRelay relay = getRelays().get(relayIndex);
        Field<Boolean, Void> state = null;
        if (relay != null) {
            state = relay.getState();
        }
        return (state != null) ? state.getValue() : null;
    }

    public void setState(byte relayIndex, Boolean state) {
        if (getRelays() == null) {
            return;
        }
        WesOneWireRelay relay = getRelays().get(relayIndex);
        if (relay != null) {
            relay.setState(state);
        }
    }

    @Override
    public String getValue() {
        return getStatesAsString();
    }

    @Override
    public void setValue(String value) {
        Boolean[] valueAsBooleanArray = toBooleanArray(value);
        byte i = 0;
        while (i < valueAsBooleanArray.length) {
            setState(i, valueAsBooleanArray[i]);
            i++;
        }
    }

    private Boolean[] toBooleanArray(String value) {
        if (value == null) {
            return null;
        }
        Boolean[] booleanArray = new Boolean[Math.max(value.length(), 8)];
        for (int i = 0; i < Math.min(value.length(), 8); i++) {
            if (value.charAt(i) == '1') {
                booleanArray[i] = Boolean.TRUE;
            } else if (value.charAt(i) == '0') {
                booleanArray[i] = Boolean.FALSE;
            } else if (value.charAt(i) == ' ') {
                booleanArray[i] = null;
            }
        }
        return booleanArray;
    }

    @Override
    public String getMemberName() {
        return "states";
    }
}