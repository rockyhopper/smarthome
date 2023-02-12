package org.rockhopper.smarthome.wes.jwes.model.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.rockhopper.smarthome.wes.jwes.model.data.type.BSSW;
import org.rockhopper.smarthome.wes.jwes.model.data.type.Field;
import org.rockhopper.smarthome.wes.jwes.model.data.type.VirtualField;
import org.rockhopper.smarthome.wes.jwes.model.helper.WesLabelsHelper;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class WesRelaysCards extends Field<BSSW, Byte> implements VirtualField {

    public static final byte MAX_RELAYSCARD = 10;

    /**
     * Up to 10 (relaysCards)
     */
    @XStreamImplicit(itemFieldName = "card")
    private List<WesRelaysCard> cards;

    public WesRelaysCards() {
        this(MAX_RELAYSCARD, Field.PRIORITY_DISCOVERY);
    }

    public WesRelaysCards(Byte priority) {
        this(MAX_RELAYSCARD, priority);
    }

    public WesRelaysCards(byte nbCards) {
        this(nbCards, Field.PRIORITY_DISCOVERY);
    }

    public WesRelaysCards(byte nbCards, Byte priority) {
        super(priority);
        for (int i = 0; i < nbCards; i++) {
            WesRelaysCard relaysCard = new WesRelaysCard((byte) i);
            addCard(relaysCard);
        }
    }

    public void addCard(WesRelaysCard relaysCard) {
        if (cards == null) {
            cards = new ArrayList<WesRelaysCard>(10);
        }
        cards.add(relaysCard);
    }

    public Boolean getState(byte cardIndex, byte relayIndex) {
        if (cards == null) {
            return null;
        }
        WesRelaysCard relaysCard = cards.get(cardIndex);
        return (relaysCard != null) ? relaysCard.getState(relayIndex) : null;
    }

    public void setState(byte cardIndex, byte relayIndex, Boolean state) {
        if (cards == null) {
            return;
        }
        WesRelaysCard relaysCard = cards.get(cardIndex);
        if (relaysCard != null) {
            relaysCard.setState(relayIndex, state);
        }
    }

    public Field<BSSW, Byte> getStates() {
        Field<BSSW, Byte> states = null;
        if (cards != null) {
            states = new Field<BSSW, Byte>(BSSW.class, Byte.class, Field.PRIORITY_DISCOVERY);
            states.setLabel(WesLabelsHelper.LABEL_DATA_PREFIX + WesLabelsHelper.LABEL_RELAYSCARDS_STATES_SUFFIX);
            states.setValue(new BSSW());
            Iterator<WesRelaysCard> cardsIt = cards.iterator();
            int i = 0;
            while (cardsIt.hasNext()) {
                WesRelaysCard relaysCard = cardsIt.next();
                Boolean[] relaysCardStates = relaysCard.getStatesAsBooleanArray();
                for (int j = 1; j <= relaysCardStates.length; j++) {
                    if (relaysCardStates[j - 1] != null) {
                        if (relaysCardStates[j - 1]) {
                            states.getValue().put("rl1" + i + j, (byte) 1);
                        } else {
                            states.getValue().put("rl1" + i + j, (byte) 0);
                        }
                    }
                }
                i++;
            }
        }
        return states;
    }

    public void setStates(Field<BSSW, Byte> states) {
        if ((states != null) && (states.getValue() != null)) {
            BSSW bssw = states.getValue();
            if (bssw.size() > 0) {
                Set<String> keys = bssw.keySet();
                for (String key : keys) {
                    if (key.startsWith("rl1") && (key.length() == "rl1ij".length())) {
                        char[] iCharArray = new char[] { key.charAt(3) };
                        char[] jCharArray = new char[] { key.charAt(4) };
                        byte i = Byte.valueOf(new String(iCharArray));
                        byte j = Byte.valueOf(new String(jCharArray));
                        setState(i, (byte) (j - 1),
                                ((((Byte) bssw.get(key)).byteValue() == 1) ? Boolean.TRUE : Boolean.FALSE));
                    }
                }
            }
        }
    }

    public List<WesRelaysCard> getCards() {
        return cards;
    }

    public void setCards(List<WesRelaysCard> cards) {
        this.cards = cards;
    }

    public void truncateTo(byte byteValue) {
        if ((cards != null) && (cards.size() > byteValue)) {
            cards.subList(byteValue, cards.size()).clear();
        }
    }

    @Override
    public BSSW getValue() {
        Field<BSSW, Byte> states = getStates();
        return (states != null) ? states.getValue() : null;
    }

    @Override
    public void setValue(BSSW value) {
        if (value != null) {
            Field<BSSW, Byte> states = new Field<BSSW, Byte>(BSSW.class, Byte.class, Field.PRIORITY_DISCOVERY);
            states.setValue(value);
        }
    }

    @Override
    public String getMemberName() {
        return "states";
    }
}