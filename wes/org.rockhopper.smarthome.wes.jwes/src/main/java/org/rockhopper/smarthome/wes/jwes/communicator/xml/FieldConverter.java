package org.rockhopper.smarthome.wes.jwes.communicator.xml;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.rockhopper.smarthome.wes.jwes.model.data.type.BSSW;
import org.rockhopper.smarthome.wes.jwes.model.data.type.Field;

import java.util.Set;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class FieldConverter implements Converter {

    protected XmlProtocol xmlProtocol;
    protected boolean withFormat;

    public FieldConverter(XmlProtocol xmlProtocol, boolean withFormat) {
        this.xmlProtocol = xmlProtocol;
        this.withFormat = withFormat;
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class clazz) {
        return clazz.equals(Field.class);
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Field<?, ?> field = (Field<?, ?>) source;
        if (field != null) {
            if (xmlProtocol != null) {
                String command = xmlProtocol.getWesCommand(field);
                if (command != null) {
                    writer.addAttribute("command", command);
                }
            }

            if (field.getType() != null) {
                if (!(field.getType().equals(String.class))) {
                    writer.addAttribute("type", field.getType().getSimpleName());
                }
            }

            if (field.getSubType() != null) {
                writer.addAttribute("subType", field.getSubType().getSimpleName());
            }

            if (field.getPriority() != null) {
                writer.addAttribute("priority", field.getPriority().toString());
            }

            if (withFormat) {
                if (xmlProtocol != null) {
                    String format = xmlProtocol.getWesFormat(field);
                    if (format != null) {
                        writer.setValue(format);
                    }
                }
            } else if (field.getValue() != null) {
                if ((field.getType() == null) || (field.getType().equals(String.class))) {
                    writer.setValue(field.getValue().toString());
                } else if (field.getType().equals(Boolean.class)) {
                    writer.setValue(((Boolean) field.getValue()).toString());
                } else if (field.getType().equals(Byte.class)) {
                    writer.setValue(((Byte) field.getValue()).toString());
                } else if (field.getType().equals(BigDecimal.class)) {
                    writer.setValue(new DecimalFormat("#,###.##", new DecimalFormatSymbols(Locale.ENGLISH))
                            .format((field.getValue())));
                } else if (field.getType().equals(BigInteger.class)) {
                    writer.setValue(field.getValue().toString());
                }                  
                else if (field.getType().equals(BSSW.class)) {
                    BSSW bsswValue = (BSSW) field.getValue();
                    if (bsswValue.size() > 0) {
                        Set<Entry<String, Object>> bsswValueEntries = bsswValue.entrySet();
                        for (Entry<String, Object> entry : bsswValueEntries) {
                            String id = entry.getKey();
                            Object value = entry.getValue();
                            if ((id != null) && (value != null)) {
                                writer.startNode("bssw");
                                writer.startNode("id");
                                writer.setValue(id);
                                writer.endNode();
                                writer.startNode("value");
                                if ((field.getSubType() == null) || (field.getSubType().equals(String.class))
                                        || field.getSubType().equals(Byte.class)) {
                                    writer.setValue(value.toString());
                                } else if (field.getSubType().equals(Boolean.class)) {
                                    writer.setValue(((Boolean) value).toString());
                                }
                                writer.endNode();
                                writer.endNode();
                            }
                        }
                    }

                }
            }
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        // String command = reader.getAttribute("command");
        String type = reader.getAttribute("type");
        String subType = reader.getAttribute("subType");
        String priority = reader.getAttribute("priority");
        String valueOrFormat = reader.getValue();
        Field<?, ?> toReturn = null;

        Byte bytePriority = null;
        if (priority != null) {
            bytePriority = Byte.valueOf(priority);
        }

        if ((type == null) || (type.equalsIgnoreCase(String.class.getSimpleName()))) {
            Field<String, ?> field = new Field<String, Void>(String.class, bytePriority);
            if (!withFormat) {
                field.setValue(valueOrFormat);
            }
            toReturn = field;
        } else if (type.equalsIgnoreCase(Byte.class.getSimpleName())) {
            Field<Byte, Void> field = new Field<Byte, Void>(Byte.class, bytePriority);
            if (!withFormat) {
                if (valueOrFormat.length() > 0) {
                    field.setValue(Byte.valueOf(valueOrFormat));
                }
            }
            toReturn = field;
        } else if (type.equalsIgnoreCase(Boolean.class.getSimpleName())) {
            Field<Boolean, Void> field = new Field<Boolean, Void>(Boolean.class, bytePriority);
            if (!withFormat) {
                if (valueOrFormat.length() > 0) {
                    field.setValue(Boolean.valueOf(valueOrFormat));
                }
            }
            toReturn = field;
        } else if (type.equalsIgnoreCase(BigDecimal.class.getSimpleName())) {
            Field<BigDecimal, Void> field = new Field<BigDecimal, Void>(BigDecimal.class, bytePriority);
            if (!withFormat) {
                if (valueOrFormat.length() > 0) {
                    try {
                        DecimalFormat df = new DecimalFormat("#,###.##", new DecimalFormatSymbols(Locale.ENGLISH));
                        df.setParseBigDecimal(true);
                        df.parse(valueOrFormat);
                        field.setValue((BigDecimal) df.parse(valueOrFormat));
                    } catch (ParseException e) {
                        throw new RuntimeException(
                                "Fail parsing BigDecimal value '" + valueOrFormat + "' with US Locale(\"#,###.##\")");
                    }
                }
            }
            toReturn = field;
        } else if (type.equalsIgnoreCase(BigInteger.class.getSimpleName())) {
            Field<BigInteger, Void> field = new Field<BigInteger, Void>(BigInteger.class, bytePriority);
            if (!withFormat) {
                if ((valueOrFormat.length() > 0) && (StringUtils.isNumeric(valueOrFormat))){
                    try {
                        field.setValue(new BigInteger(valueOrFormat));
                    } catch (NumberFormatException e) {
                        throw new RuntimeException(
                                "Fail parsing BigInteger value '" + valueOrFormat + "'");
                    }
                }
            }
            toReturn = field;
        } else if (type.equalsIgnoreCase(BSSW.class.getSimpleName())) {
            Field<BSSW, ?> field = null;

            if ((subType == null) || (subType.equals(String.class.getSimpleName()))) {
                field = new Field<BSSW, String>(BSSW.class, String.class, bytePriority);
            } else if (subType.equals(Boolean.class.getSimpleName())) {
                field = new Field<BSSW, Boolean>(BSSW.class, Boolean.class, bytePriority);
            } else if (subType.equals(Byte.class.getSimpleName())) {
                field = new Field<BSSW, Byte>(BSSW.class, Byte.class, bytePriority);
            }
            if (field != null) {
                field.setValue(new BSSW());
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    if ("bssw".equals(reader.getNodeName())) {
                        String id = null;
                        Object value = null;
                        while (reader.hasMoreChildren()) {
                            reader.moveDown();
                            if ("id".equals(reader.getNodeName())) {
                                id = reader.getValue();
                            }
                            if ("value".equals(reader.getNodeName())) {
                                String sValue = reader.getValue();
                                if ((subType == null) || (subType.equals(String.class.getSimpleName()))) {
                                    value = sValue;
                                } else if (subType.equals(Boolean.class.getSimpleName())) {
                                    value = Boolean.valueOf(sValue);
                                } else if (subType.equals(Byte.class.getSimpleName())) {
                                    value = Byte.valueOf(sValue);
                                }
                            }
                            reader.moveUp();
                        }
                        if ((id != null) && (value != null)) {
                            field.getValue().put(id, value);
                        }
                    }
                    reader.moveUp();
                }
            }
            toReturn = field;
        }
        return toReturn;
    }

}
