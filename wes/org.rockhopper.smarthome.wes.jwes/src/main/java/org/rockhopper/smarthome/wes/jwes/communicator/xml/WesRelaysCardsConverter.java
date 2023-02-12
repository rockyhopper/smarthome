package org.rockhopper.smarthome.wes.jwes.communicator.xml;

import java.util.ArrayList;

import org.rockhopper.smarthome.wes.jwes.model.data.WesRelaysCard;
import org.rockhopper.smarthome.wes.jwes.model.data.WesRelaysCards;
import org.rockhopper.smarthome.wes.jwes.model.data.type.BSSW;
import org.rockhopper.smarthome.wes.jwes.model.data.type.Field;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

public class WesRelaysCardsConverter implements Converter {

    protected XmlProtocol xmlProtocol;
    protected boolean withFormat;

    private FieldConverter fieldConverter;
    private CollectionConverter listConverter;

    public WesRelaysCardsConverter(Mapper mapper, XmlProtocol xmlProtocol, boolean withFormat) {
        this.xmlProtocol = xmlProtocol;
        this.withFormat = withFormat;
        this.fieldConverter = new FieldConverter(xmlProtocol, withFormat);
        this.listConverter = new CollectionConverter(mapper);
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class clazz) {
        return clazz.equals(WesRelaysCards.class);
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        WesRelaysCards wesRelaysCards = (WesRelaysCards) source;
        if (wesRelaysCards != null) {
            writer.startNode("cards");
            if (wesRelaysCards.getCards() != null) {
                listConverter.marshal(wesRelaysCards.getCards(), writer, context);
            }
            writer.endNode();
            writer.startNode("states");

            Field<BSSW, Byte> states = wesRelaysCards.getStates();
            if (states != null) {
                /*
                 * if (xmlProtocol != null) {
                 * String command = xmlProtocol.getWesCommand(states);
                 * if (command != null) {
                 * writer.addAttribute("command", command);
                 * }
                 * }
                 */

                fieldConverter.marshal(states, writer, context);

                /*
                 * if (withFormat) {
                 * if (xmlProtocol != null) {
                 * String format = xmlProtocol.getWesFormat(states);
                 * if (format != null) {
                 * writer.setValue(format);
                 * }
                 * }
                 * }
                 */
            }
            writer.endNode();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

        reader.moveDown();
        ArrayList<WesRelaysCard> wesRelaysCardList = new ArrayList<WesRelaysCard>();
        wesRelaysCardList = (ArrayList<WesRelaysCard>) context.convertAnother(wesRelaysCardList, ArrayList.class);
        reader.moveUp();

        reader.moveDown();
        Field<BSSW, Byte> states = new Field<BSSW, Byte>(BSSW.class, Byte.class, Field.PRIORITY_DISCOVERY);
        states = (Field<BSSW, Byte>) context.convertAnother(states, Field.class);
        reader.moveUp();

        WesRelaysCards toReturn = new WesRelaysCards((byte) (wesRelaysCardList.size() - 1));
        toReturn.setCards(wesRelaysCardList);
        toReturn.setStates(states);
        return toReturn;
    }

}
