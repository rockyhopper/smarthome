package org.rockhopper.smarthome.wes.jwes.communicator.xml;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.rockhopper.smarthome.wes.jwes.model.data.TeleInfo;
import org.rockhopper.smarthome.wes.jwes.model.data.WesData;
import org.rockhopper.smarthome.wes.jwes.model.data.WesOneWireRelay;
import org.rockhopper.smarthome.wes.jwes.model.data.WesPulseCounter;
import org.rockhopper.smarthome.wes.jwes.model.data.WesRelay;
import org.rockhopper.smarthome.wes.jwes.model.data.WesRelaysCard;
import org.rockhopper.smarthome.wes.jwes.model.data.WesRelaysCards;
import org.rockhopper.smarthome.wes.jwes.model.data.WesSensor;
import org.rockhopper.smarthome.wes.jwes.model.data.type.Field;
import org.rockhopper.smarthome.wes.jwes.model.helper.WesLabelsHelper;

public class XmlProtocol {

    private static Map<String, String> commands;
    private static Map<String, String> formats;

    protected String WES_DATA_INFO_DATE_CMD = "g d";
    protected String WES_DATA_INFO_DATE_FMT = "%02d/%02d/%02d";

    protected String WES_DATA_INFO_TIME_CMD = "h h";
    protected String WES_DATA_INFO_TIME_FMT = "%02d:%02d";

    protected String WES_DATA_INFO_FIRMWARE_CMD = "v v";
    protected String WES_DATA_INFO_FIRMWARE_FMT = "%s";

    protected String WES_DATA_INFO_NBTEMPSENSORS_CMD = "W Q";
    protected String WES_DATA_INFO_NBTEMPSENSORS_FMT = "%d";

    protected String WES_DATA_INFO_NBHUMIDSENSORS_CMD = "W H";
    protected String WES_DATA_INFO_NBHUMIDSENSORS_FMT = "%d";

    protected String WES_DATA_INFO_NBRELAYSCARDS_CMD = "WRn";
    protected String WES_DATA_INFO_NBRELAYSCARDS_FMT = "%d";

    protected String WES_DATA_RELAYSCARDS_STATES_CMD = "o W";
    protected String WES_RELAYSCARDS_STATES_FMT = "%xml";

    public XmlProtocol() {
        this(null);
    }

    public XmlProtocol(WesData wesData) {
        if (wesData == null) {
            init(new WesData());
        } else {
            init(wesData);
        }
    }

    private void init(WesData wesData) {
        if (wesData == null) {
            throw new UnsupportedOperationException("You must provide an instance of WesData");
        }

        Map<String, String> _commands = new HashMap<String, String>();
        Map<String, String> _formats = new HashMap<String, String>();

        if (wesData.getInfo() != null) {
            if (wesData.getInfo().getDate() != null) {
                _commands.put(wesData.getInfo().getDate().getLabel(), WES_DATA_INFO_DATE_CMD);
                _formats.put(wesData.getInfo().getDate().getLabel(), WES_DATA_INFO_DATE_FMT);
            }
            if (wesData.getInfo().getTime() != null) {
                _commands.put(wesData.getInfo().getTime().getLabel(), WES_DATA_INFO_TIME_CMD);
                _formats.put(wesData.getInfo().getTime().getLabel(), WES_DATA_INFO_TIME_FMT);
            }
            if (wesData.getInfo().getFirmware() != null) {
                _commands.put(wesData.getInfo().getFirmware().getLabel(), WES_DATA_INFO_FIRMWARE_CMD);
                _formats.put(wesData.getInfo().getFirmware().getLabel(), WES_DATA_INFO_FIRMWARE_FMT);
            }
            if (wesData.getInfo().getNbTempSensors() != null) {
                _commands.put(wesData.getInfo().getNbTempSensors().getLabel(), WES_DATA_INFO_NBTEMPSENSORS_CMD);
                _formats.put(wesData.getInfo().getNbTempSensors().getLabel(), WES_DATA_INFO_NBTEMPSENSORS_FMT);
            }
            if (wesData.getInfo().getNbHumidSensors() != null) {
                _commands.put(wesData.getInfo().getNbHumidSensors().getLabel(), WES_DATA_INFO_NBHUMIDSENSORS_CMD);
                _formats.put(wesData.getInfo().getNbHumidSensors().getLabel(), WES_DATA_INFO_NBHUMIDSENSORS_FMT);
            }
            if (wesData.getInfo().getNbRelaysCards() != null) {
                _commands.put(wesData.getInfo().getNbRelaysCards().getLabel(), WES_DATA_INFO_NBRELAYSCARDS_CMD);
                _formats.put(wesData.getInfo().getNbRelaysCards().getLabel(), WES_DATA_INFO_NBRELAYSCARDS_FMT);
            }
        }

        TeleInfoXmlProtocol teleInfoXmlProtocol = new TeleInfoXmlProtocol(_commands, _formats);
        teleInfoXmlProtocol.initTic(wesData.getTic1());
        teleInfoXmlProtocol.initTic(wesData.getTic2());
        teleInfoXmlProtocol.initTic(wesData.getTic3());

        WesRelayXmlProtocol wesRelayXmlProtocol = new WesRelayXmlProtocol(_commands, _formats);
        wesRelayXmlProtocol.initWesRelay(wesData.getRelay1());
        wesRelayXmlProtocol.initWesRelay(wesData.getRelay2());
        
        /* TO-REENABLE
        if (wesData.getPulseCounters()!=null) {
        	WesPulseCounterXmlProtocol wesPulseCounterXmlProtocol = new WesPulseCounterXmlProtocol(_commands, _formats);
            for (byte i=0; i<=3; i++) {            	
            	wesPulseCounterXmlProtocol.initPulseCounter(wesData.getPulseCounters().get(i));
            }
        }
        */
        
        if (wesData.getSensors() != null) {
            WesSensorXmlProtocol wesSensorXmlProtocol = new WesSensorXmlProtocol(_commands, _formats);
            Iterator<WesSensor> sensorsIt = wesData.getSensors().iterator();
            while (sensorsIt.hasNext()) {
                WesSensor sensor = sensorsIt.next();
                wesSensorXmlProtocol.initSensor(sensor);
            }
        }

        WesRelaysCards relaysCards = wesData.getRelaysCards();
        if (relaysCards != null) {
            WesRelaysCardXmlProtocol wesRelaysCardXmlProtocol = new WesRelaysCardXmlProtocol(_commands, _formats);
            if (relaysCards.getCards() != null) {
                Iterator<WesRelaysCard> cardsIt = relaysCards.getCards().iterator();
                while (cardsIt.hasNext()) {
                    WesRelaysCard card = cardsIt.next();
                    wesRelaysCardXmlProtocol.initWesRelaysCard(card);
                }
            }
        }

        _commands.put(WesLabelsHelper.LABEL_DATA_PREFIX + WesLabelsHelper.LABEL_RELAYSCARDS_STATES_SUFFIX,
                WES_DATA_RELAYSCARDS_STATES_CMD);
        _formats.put(WesLabelsHelper.LABEL_DATA_PREFIX + WesLabelsHelper.LABEL_RELAYSCARDS_STATES_SUFFIX,
                WES_RELAYSCARDS_STATES_FMT);

        commands = Collections.unmodifiableMap(_commands);
        formats = Collections.unmodifiableMap(_formats);
    }

    public String getWesCommand(Field<?, ?> field) {
        if ((field == null) || (field.getLabel() == null)) {
            return null;
        }
        return commands.get(field.getLabel());
    }

    public String getWesFormat(Field<?, ?> field) {
        if ((field == null) || (field.getLabel() == null)) {
            return null;
        }
        return formats.get(field.getLabel());
    }

    private static class WesRelayXmlProtocol {
        private Map<String, String> _commands = new HashMap<String, String>();
        private Map<String, String> _formats = new HashMap<String, String>();

        protected String WES_DATA_RELAY_NAME_CMD = "o n%d"; // o n0 / o n1
        protected String WES_DATA_RELAY_NAME_FMT = "%s";

        protected String WES_DATA_RELAY_VALUE_CMD = "o E%d"; // o E1 / o E2
        protected String WES_DATA_RELAY_VALUE_FMT = "%d";

        public WesRelayXmlProtocol(Map<String, String> _commands, Map<String, String> _formats) {
            this._commands = _commands;
            this._formats = _formats;
        }

        public void initWesRelay(WesRelay relay) {
            if (relay == null) {
                return;
            }

            if (relay.getName() != null) {
                _commands.put(relay.getName().getLabel(),
                        getCommandByIndex(WES_DATA_RELAY_NAME_CMD, (byte) (relay.getIndex() - 1)));
                _formats.put(relay.getName().getLabel(), WES_DATA_RELAY_NAME_FMT);
            }

            if (relay.getValue() != null) {
                _commands.put(relay.getValue().getLabel(),
                        getCommandByIndex(WES_DATA_RELAY_VALUE_CMD, relay.getIndex()));
                _formats.put(relay.getValue().getLabel(), WES_DATA_RELAY_VALUE_FMT);
            }
        }

        private String getCommandByIndex(String parametrizedCommand, byte index) {
            return String.format(parametrizedCommand, index);
        }
    }
    
    
    private static class WesPulseCounterXmlProtocol {
        private Map<String, String> _commands = new HashMap<String, String>();
        private Map<String, String> _formats = new HashMap<String, String>();

        protected String WES_DATA_PULSECOUNTER_NAME_CMD = "pn%s"; //  pn1, pn2, pn3, pn4
        protected String WES_DATA_PULSECOUNTER_NAME_FMT = "%s";
        
        protected String WES_DATA_PULSECOUNTER_DEBIT_CMD = "pd%s"; //  pd1, pd2, pd3, pd4
        protected String WES_DATA_PULSECOUNTER_DEBIT_FMT = "%.02f";
        
        protected String WES_DATA_PULSECOUNTER_CURRENTINDEX_CMD = "pIU%s"; // pIU1, pIU2, pIU3, pIU4 
        protected String WES_DATA_PULSECOUNTER_CURRENTINDEX_FMT = "%.0f";

        protected String WES_DATA_PULSECOUNTER_YESTERDAY_CMD = "pCh%s"; // pCh1, pCh2, pCh3, pCh4
        protected String WES_DATA_PULSECOUNTER_YESTERDAY_FMT = "%.0f";

        protected String WES_DATA_PULSECOUNTER_TODAY_CMD = "pCj%s"; 	// pCj1, pCj2, pCj3, pCj4
        protected String WES_DATA_PULSECOUNTER_TODAY_FMT = "%.0f";

        protected String WES_DATA_PULSECOUNTER_MONTH_CMD = "pCm%s"; // pCm1, pCm2, pCm3, pCm4
        protected String WES_DATA_PULSECOUNTER_MONTH_FMT = "%.3f";

        protected String WES_DATA_PULSECOUNTER_YEAR_CMD = "pCa%s"; 	// pCa1, pCa2, pCa3, pCa4
        protected String WES_DATA_PULSECOUNTER_YEAR_FMT = "%.3f";
                
        public WesPulseCounterXmlProtocol(Map<String, String> _commands, Map<String, String> _formats) {
            this._commands = _commands;
            this._formats = _formats;
        }

        public void initPulseCounter(WesPulseCounter pulseCounter) {
            if (pulseCounter == null) {
                return;
            }

            if (pulseCounter.getName() != null) {
                _commands.put(pulseCounter.getName().getLabel(),
                        getCommandByIndexAsChars(WES_DATA_PULSECOUNTER_NAME_CMD, pulseCounter.getIndex()));
                _formats.put(pulseCounter.getName().getLabel(), WES_DATA_PULSECOUNTER_NAME_FMT);
            }

            if (pulseCounter.getDebit() != null) {
                _commands.put(pulseCounter.getDebit().getLabel(),
                        getCommandByIndexAsChars(WES_DATA_PULSECOUNTER_DEBIT_CMD, pulseCounter.getIndex()));
                _formats.put(pulseCounter.getDebit().getLabel(), WES_DATA_PULSECOUNTER_DEBIT_FMT);
            }

            if (pulseCounter.getCurrentIndex() != null) {
                _commands.put(pulseCounter.getCurrentIndex().getLabel(),
                        getCommandByIndexAsChars(WES_DATA_PULSECOUNTER_CURRENTINDEX_CMD, pulseCounter.getIndex()));
                _formats.put(pulseCounter.getCurrentIndex().getLabel(), WES_DATA_PULSECOUNTER_CURRENTINDEX_FMT);
            }
            
            if (pulseCounter.getYesterdayConsumption() != null) {
                _commands.put(pulseCounter.getYesterdayConsumption().getLabel(),
                        getCommandByIndexAsChars(WES_DATA_PULSECOUNTER_YESTERDAY_CMD, pulseCounter.getIndex()));
                _formats.put(pulseCounter.getYesterdayConsumption().getLabel(), WES_DATA_PULSECOUNTER_YESTERDAY_FMT);
            }
            
            if (pulseCounter.getTodayConsumption() != null) {
                _commands.put(pulseCounter.getTodayConsumption().getLabel(),
                        getCommandByIndexAsChars(WES_DATA_PULSECOUNTER_TODAY_CMD, pulseCounter.getIndex()));
                _formats.put(pulseCounter.getTodayConsumption().getLabel(), WES_DATA_PULSECOUNTER_TODAY_FMT);
            }
            
            if (pulseCounter.getMonthConsumption() != null) {
                _commands.put(pulseCounter.getMonthConsumption().getLabel(),
                        getCommandByIndexAsChars(WES_DATA_PULSECOUNTER_MONTH_CMD, pulseCounter.getIndex()));
                _formats.put(pulseCounter.getMonthConsumption().getLabel(), WES_DATA_PULSECOUNTER_MONTH_FMT);
            }
            
            if (pulseCounter.getYearConsumption() != null) {
                _commands.put(pulseCounter.getYearConsumption().getLabel(),
                        getCommandByIndexAsChars(WES_DATA_PULSECOUNTER_YEAR_CMD, pulseCounter.getIndex()));
                _formats.put(pulseCounter.getYearConsumption().getLabel(), WES_DATA_PULSECOUNTER_YEAR_FMT);
            }
        }

        private String getCommandByIndexAsChars(String parametrizedCommand, byte index) {
        	byte indexForCmd= (byte)(index + 1);
            return String.format(parametrizedCommand, String.valueOf(indexForCmd));
        }
    }

    private static class WesSensorXmlProtocol {
        private Map<String, String> _commands = new HashMap<String, String>();
        private Map<String, String> _formats = new HashMap<String, String>();

        protected String WES_DATA_SENSOR_ID_CMD = "W%sS%s"; // W0S0
        protected String WES_DATA_SENSOR_ID_FMT = "%02X %02X %02X %02X %02X %02X %02X %02X";

        protected String WES_DATA_SENSOR_NAME_CMD = "W%sN%s"; // W0N0
        protected String WES_DATA_SENSOR_NAME_FMT = "%s";

        protected String WES_DATA_SENSOR_TYPE_CMD = "W%st%s"; // W0t0
        protected String WES_DATA_SENSOR_TYPE_FMT = "%s";

        protected String WES_DATA_SENSOR_VALUE_CMD = "W%sT%s"; // W0T0
        protected String WES_DATA_SENSOR_VALUE_FMT = "%.01f";

        public WesSensorXmlProtocol(Map<String, String> _commands, Map<String, String> _formats) {
            this._commands = _commands;
            this._formats = _formats;
        }

        public void initSensor(WesSensor sensor) {
            if (sensor == null) {
                return;
            }

            if (sensor.getId() != null) {
                _commands.put(sensor.getId().getLabel(),
                        getCommandByIndexAsChars(WES_DATA_SENSOR_ID_CMD, sensor.getIndex()));
                _formats.put(sensor.getId().getLabel(), WES_DATA_SENSOR_ID_FMT);
            }

            if (sensor.getName() != null) {
                _commands.put(sensor.getName().getLabel(),
                        getCommandByIndexAsChars(WES_DATA_SENSOR_NAME_CMD, sensor.getIndex()));
                _formats.put(sensor.getName().getLabel(), WES_DATA_SENSOR_NAME_FMT);
            }

            if (sensor.getType() != null) {
                _commands.put(sensor.getType().getLabel(),
                        getCommandByIndexAsChars(WES_DATA_SENSOR_TYPE_CMD, sensor.getIndex()));
                _formats.put(sensor.getType().getLabel(), WES_DATA_SENSOR_TYPE_FMT);
            }

            if (sensor.getValue() != null) {
                _commands.put(sensor.getValue().getLabel(),
                        getCommandByIndexAsChars(WES_DATA_SENSOR_VALUE_CMD, sensor.getIndex()));
                _formats.put(sensor.getValue().getLabel(), WES_DATA_SENSOR_VALUE_FMT);
            }
        }

        private char[] getIndexAsChars(byte index) {
            String formattedId = StringUtils.leftPad(String.valueOf(index), 2, '0');
            return formattedId.toCharArray();
        }

        private String getCommandByIndexAsChars(String parametrizedCommand, byte index) {
            char[] indexAsChars = getIndexAsChars(index);
            return String.format(parametrizedCommand, indexAsChars[0], indexAsChars[1]);
        }
    }

    private static class TeleInfoXmlProtocol {
        private Map<String, String> _commands = new HashMap<String, String>();
        private Map<String, String> _formats = new HashMap<String, String>();

        protected String WES_DATA_TELEINFO_NAME_CMD = "en%d"; // en1 / en2 / en3
        protected String WES_DATA_TELEINFO_NBRELAYSCARDS_FMT = "%s";

        protected String WES_DATA_TELEINFO_ADCO_CMD = "ea%d"; // ea1 / ea2 / ea3
        protected String WES_DATA_TELEINFO_ADCO_FMT = "%s";

        protected String WES_DATA_TELEINFO_ABONAME_CMD = "eN%d"; // eN1 / eN2 / eN3 is '%s' ( ed1 / ed2 / ed3 is '%d')
        protected String WES_DATA_TELEINFO_ABONAME_FMT = "%s";

        protected String WES_DATA_TELEINFO_INDEXN_CMD = "Ti%d%d"; // Ti11 / Ti21 / Ti31
        protected String WES_DATA_TELEINFO_INDEXN_FMT = "%s";

        protected String WES_DATA_TELEINFO_IABO_CMD = "es%d"; // eS1 eS2 eS3
        protected String WES_DATA_TELEINFO_IABO_FMT = "%d";

        protected String WES_DATA_TELEINFO_IINSTN_CMD = "ii%d%d"; // ii10 / ii20 / ii30 (0= Mono)
        protected String WES_DATA_TELEINFO_IINSTN_FMT = "%d";

        protected String WES_DATA_TELEINFO_PAPPS_CMD = "iP"; // TODO: Fix this! // iP ? ? (%lu,%lu,%lu => String)
        protected String WES_DATA_TELEINFO_PAPPS_FMT = "%lu,%lu,%lu";

        protected String WES_DATA_TELEINFO_VOLTAGES_CMD = "iU%d"; // iU1 iU2 iU3 (%lu,%lu,%lu => String)
        protected String WES_DATA_TELEINFO_VOLTAGES_FMT = "%lu,%lu,%lu";

        public TeleInfoXmlProtocol(Map<String, String> _commands, Map<String, String> _formats) {
            this._commands = _commands;
            this._formats = _formats;
        }

        public void initTic(TeleInfo teleinfo) {
            if (teleinfo == null) {
                return;
            }
            if (teleinfo.getName() != null) {
                _commands.put(teleinfo.getName().getLabel(),
                        getCommandByIndex(WES_DATA_TELEINFO_NAME_CMD, teleinfo.getIndex()));
                _formats.put(teleinfo.getName().getLabel(), WES_DATA_TELEINFO_NBRELAYSCARDS_FMT);
            }

            if (teleinfo.getAdco() != null) {
                _commands.put(teleinfo.getAdco().getLabel(),
                        getCommandByIndex(WES_DATA_TELEINFO_ADCO_CMD, teleinfo.getIndex()));
                _formats.put(teleinfo.getAdco().getLabel(), WES_DATA_TELEINFO_ADCO_FMT);
            }

            if (teleinfo.getAboName() != null) {
                _commands.put(teleinfo.getAboName().getLabel(),
                        getCommandByIndex(WES_DATA_TELEINFO_ABONAME_CMD, teleinfo.getIndex()));
                _formats.put(teleinfo.getAboName().getLabel(), WES_DATA_TELEINFO_ABONAME_FMT);
            }

            if (teleinfo.getIndex1() != null) {
                _commands.put(teleinfo.getIndex1().getLabel(),
                        getCommandByIndexAndPhaseNb(WES_DATA_TELEINFO_INDEXN_CMD, teleinfo.getIndex(), (byte) 1));
                _formats.put(teleinfo.getIndex1().getLabel(), WES_DATA_TELEINFO_INDEXN_FMT);
            }

            if (teleinfo.getiAbo() != null) {
                _commands.put(teleinfo.getiAbo().getLabel(),
                        getCommandByIndex(WES_DATA_TELEINFO_IABO_CMD, teleinfo.getIndex()));
                _formats.put(teleinfo.getiAbo().getLabel(), WES_DATA_TELEINFO_IABO_FMT);
            }

            if (teleinfo.getIinstMono() != null) {
                _commands.put(teleinfo.getIinstMono().getLabel(),
                        getCommandByIndexAndPhaseNb(WES_DATA_TELEINFO_IINSTN_CMD, teleinfo.getIndex(), (byte) 0));
                _formats.put(teleinfo.getIinstMono().getLabel(), WES_DATA_TELEINFO_IINSTN_FMT);
            }

            if (teleinfo.getpApps() != null) {
                _commands.put(teleinfo.getpApps().getLabel(),
                        getCommandByIndex(WES_DATA_TELEINFO_PAPPS_CMD, teleinfo.getIndex()));
                _formats.put(teleinfo.getpApps().getLabel(), WES_DATA_TELEINFO_PAPPS_FMT);
            }

            if (teleinfo.getVoltages() != null) {
                _commands.put(teleinfo.getVoltages().getLabel(),
                        getCommandByIndex(WES_DATA_TELEINFO_VOLTAGES_CMD, teleinfo.getIndex()));
                _formats.put(teleinfo.getVoltages().getLabel(), WES_DATA_TELEINFO_VOLTAGES_FMT);
            }
        }

        private String getCommandByIndex(String parametrizedCommand, byte index) {
            return String.format(parametrizedCommand, index);
        }

        private String getCommandByIndexAndPhaseNb(String parametrizedCommand, byte index, byte n) {
            return String.format(parametrizedCommand, index, n);
        }
    }

    private static class WesOneWireRelayXmlProtocol {
        private Map<String, String> _commands = new HashMap<String, String>();
        private Map<String, String> _formats = new HashMap<String, String>();

        protected String WES_DATA_RELAY_NAME_CMD = "WR%d%d"; // WR01
        protected String WES_DATA_RELAY_NAME_FMT = "%s";

        public WesOneWireRelayXmlProtocol(Map<String, String> _commands, Map<String, String> _formats) {
            this._commands = _commands;
            this._formats = _formats;
        }

        public void initWesOneWireRelay(WesOneWireRelay relay) {
            if (relay == null) {
                return;
            }

            if (relay.getName() != null) {
                _commands.put(relay.getName().getLabel(),
                        getCommandByCardIndexAndIndex(WES_DATA_RELAY_NAME_CMD, relay.getCardIndex(), relay.getIndex()));
                _formats.put(relay.getName().getLabel(), WES_DATA_RELAY_NAME_FMT);
            }
        }

        static private String getCommandByCardIndexAndIndex(String parametrizedCommand, byte cardIndex, byte index) {
            return String.format(parametrizedCommand, cardIndex, index);
        }
    }

    private static class WesRelaysCardXmlProtocol {
        private Map<String, String> _commands = new HashMap<String, String>();
        private Map<String, String> _formats = new HashMap<String, String>();

        protected String WES_DATA_RELAYSCARD_ID_CMD = "WRS%d"; // WRS0
        protected String WES_DATA_RELAYSCARD_ID_FMT = "%02X %02X %02X %02X %02X %02X %02X %02X";

        protected String WES_DATA_RELAYSCARD_NAME_CMD = "WRN%d"; // WRN0
        protected String WES_DATA_RELAYSCARD_NAME_FMT = "%s";

        protected String WES_DATA_RELAYSCARD_TYPE_CMD = "?"; // WRT0?
        protected String WES_DATA_RELAYSCARD_TYPE_FMT = "?";

        @SuppressWarnings("unused")
        protected String WES_DATA_RELAYSCARD_STATES_CMD = "?"; // None?
        @SuppressWarnings("unused")
        protected String WES_DATA_RELAYSCARD_STATES_FMT = "?";

        public WesRelaysCardXmlProtocol(Map<String, String> _commands, Map<String, String> _formats) {
            this._commands = _commands;
            this._formats = _formats;
        }

        public void initWesRelaysCard(WesRelaysCard wesRelaysCard) {
            if (wesRelaysCard == null) {
                return;
            }

            if (wesRelaysCard.getId() != null) {
                _commands.put(wesRelaysCard.getId().getLabel(),
                        getCommandByIndex(WES_DATA_RELAYSCARD_ID_CMD, wesRelaysCard.getIndex()));
                _formats.put(wesRelaysCard.getId().getLabel(), WES_DATA_RELAYSCARD_ID_FMT);
            }

            if (wesRelaysCard.getName() != null) {
                _commands.put(wesRelaysCard.getName().getLabel(),
                        getCommandByIndex(WES_DATA_RELAYSCARD_NAME_CMD, wesRelaysCard.getIndex()));
                _formats.put(wesRelaysCard.getName().getLabel(), WES_DATA_RELAYSCARD_NAME_FMT);
            }

            if (wesRelaysCard.getCardType() != null) {
                _commands.put(wesRelaysCard.getCardType().getLabel(),
                        getCommandByIndex(WES_DATA_RELAYSCARD_TYPE_CMD, wesRelaysCard.getIndex()));
                _formats.put(wesRelaysCard.getCardType().getLabel(), WES_DATA_RELAYSCARD_TYPE_FMT);
            }

            List<WesOneWireRelay> wesOneWireRelays = wesRelaysCard.getRelays();
            if (wesOneWireRelays != null) {
                WesOneWireRelayXmlProtocol oneWireRelayXmlProtocol = new WesOneWireRelayXmlProtocol(_commands,
                        _formats);
                Iterator<WesOneWireRelay> wesOneWireRelaysIt = wesOneWireRelays.iterator();
                while (wesOneWireRelaysIt.hasNext()) {
                    WesOneWireRelay oneWirerelay = wesOneWireRelaysIt.next();
                    oneWireRelayXmlProtocol.initWesOneWireRelay(oneWirerelay);
                }
            }

            /*
             * String cardLabel = WesLabelsHelper.LABEL_DATA_PREFIX
             * + String.format(WesLabelsHelper.LABEL_RELAYSCARD_STATES_PATTERN_SUFFIX, wesRelaysCard.getIndex());
             * _commands.put(cardLabel, getCommandByIndex(WES_DATA_RELAYSCARD_STATES_CMD, wesRelaysCard.getIndex()));
             * _formats.put(cardLabel, WES_DATA_RELAYSCARD_STATES_FMT);
             */

            /*
             * if (relay.getName() != null) {
             * _commands.put(relay.getName().getLabel(),
             * getCommandByIndex(WES_DATA_RELAY_NAME_CMD, (byte) (relay.getIndex() - 1)));
             * _formats.put(relay.getName().getLabel(), WES_DATA_RELAY_NAME_FMT);
             * }
             *
             * if (relay.getValue() != null) {
             * _commands.put(relay.getValue().getLabel(),
             * getCommandByIndex(WES_DATA_RELAY_VALUE_CMD, relay.getIndex()));
             * _formats.put(relay.getValue().getLabel(), WES_DATA_RELAY_VALUE_FMT);
             * }
             */
        }

        private String getCommandByIndex(String parametrizedCommand, byte index) {
            return String.format(parametrizedCommand, index);
        }
    }

}