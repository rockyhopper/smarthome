package org.rockhopper.smarthome.wes.jwes.communicator.tcp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;
import org.rockhopper.smarthome.wes.jwes.model.data.TeleInfo;
import org.rockhopper.smarthome.wes.jwes.model.data.WesData;
import org.rockhopper.smarthome.wes.jwes.model.data.WesOneWireRelay;
import org.rockhopper.smarthome.wes.jwes.model.data.WesPulseCounter;
import org.rockhopper.smarthome.wes.jwes.model.data.WesRelay;
import org.rockhopper.smarthome.wes.jwes.model.data.WesRelaysCard;
import org.rockhopper.smarthome.wes.jwes.model.data.WesRelaysCards;
import org.rockhopper.smarthome.wes.jwes.model.data.WesSensor;
import org.rockhopper.smarthome.wes.jwes.model.data.type.FieldCommand;

public class TcpProtocol {

    private static Map<String, String> commands;
    private static Map<String, Function<String, ?>> responses;

    public TcpProtocol() {
        this(null);
    }

    public TcpProtocol(WesData wesData) {
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
        Map<String, Function<String, Object>> _responses = new HashMap<String, Function<String, Object>>();

        TeleInfoTcpProtocol teleInfoTcpProtocol = new TeleInfoTcpProtocol(_commands, _responses);
        teleInfoTcpProtocol.initTic(wesData.getTic1());
        teleInfoTcpProtocol.initTic(wesData.getTic2());
        teleInfoTcpProtocol.initTic(wesData.getTic3());

        WesRelayTcpProtocol wesRelayTcpProtocol = new WesRelayTcpProtocol(_commands, _responses);
        wesRelayTcpProtocol.initWesRelay(wesData.getRelay1());
        wesRelayTcpProtocol.initWesRelay(wesData.getRelay2());

        if (wesData.getPulseCounters()!=null) {
        	WesPulseCounterTcpProtocol wesPulseCounterTcpProtocol = new WesPulseCounterTcpProtocol(_commands, _responses);
            for (byte i=0; i<=3; i++) {            	
            	wesPulseCounterTcpProtocol.initPulseCounter(wesData.getPulseCounters().get(i));
            }
        }
        
        if (wesData.getSensors() != null) {
            WesSensorTcpProtocol wesSensorTcpProtocol = new WesSensorTcpProtocol(_commands, _responses);
            Iterator<WesSensor> sensorsIt = wesData.getSensors().iterator();
            while (sensorsIt.hasNext()) {
                WesSensor sensor = sensorsIt.next();
                wesSensorTcpProtocol.initSensor(sensor);
            }
        }

        WesRelaysCards relaysCards = wesData.getRelaysCards();
        if (relaysCards != null) {
            WesRelaysCardTcpProtocol wesRelaysCardTcpProtocol = new WesRelaysCardTcpProtocol(_commands, _responses);
            if (relaysCards.getCards() != null) {
                Iterator<WesRelaysCard> cardsIt = relaysCards.getCards().iterator();
                while (cardsIt.hasNext()) {
                    WesRelaysCard card = cardsIt.next();
                    wesRelaysCardTcpProtocol.initWesRelaysCard(card);
                }
            }
        }

        commands = Collections.unmodifiableMap(_commands);
        responses = Collections.unmodifiableMap(_responses);
    }

    public String getWesCommand(FieldCommand<?, ?> fieldCommand) {
        if ((fieldCommand != null) && (fieldCommand.getField().getLabel() != null)) {
            if (fieldCommand.getNewValue() != null) {
                String cmd = commands.get(fieldCommand.getField().getLabel() + "_W");

                Object param = fieldCommand.getNewValue();
                if (param instanceof Boolean) {
                    param = ((Boolean) param).booleanValue() ? (byte) 1 : (byte) 0;
                }

                return (cmd != null) ? String.format(cmd, param) : null;
            } else {
                return commands.get(fieldCommand.getField().getLabel());
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T processResponse(FieldCommand<?, ?> fieldCommand, String response) {
        Function<String, ?> responseHandler = null;
        if ((fieldCommand != null) && (fieldCommand.getField().getLabel() != null)) {
            if (fieldCommand.getNewValue() != null) {
                responseHandler = responses.get(fieldCommand.getField().getLabel() + "_W");
            } else {
                responseHandler = responses.get(fieldCommand.getField().getLabel());
            }
            if (responseHandler == null) {
                System.out.println(
                        "Don't know how to handle response for label '" + fieldCommand.getField().getLabel() + "'");
            } else {
                return (T) responseHandler.apply(response);
            }
        }
        return null;
    }

    public <T> void updateField(FieldCommand<T, ?> fieldCommand, String response) {
        if (fieldCommand != null) {
            fieldCommand.getField().setValue(processResponse(fieldCommand, response));
        }
    }

    static class DummyResponseHandler implements Function<String, Object> {

        @Override
        public Object apply(String response) {
            // System.out.println("Response is '" + response + "'");
            return response;
        }

    }

    static class StringFormatResponseHandler extends DummyResponseHandler {
        private String pattern;

        public StringFormatResponseHandler(String pattern) {
            this.pattern = pattern;
        }

        @Override
        public Object apply(String response) {
            if (response == null) {
                return null;
            }
            if (pattern == null) {
                return null;
            }
            return super.apply(String.format(pattern, response));
        }
    }

    static class RemoveUnitResponseHandler extends DummyResponseHandler {
        @Override
        public Object apply(String response) {
            if (response == null) {
                return null;
            }
            int firstSpaceIndex= response.indexOf(' ');
            if (firstSpaceIndex>-1) {
            	response= response.substring(0, firstSpaceIndex);
            }
            return super.apply(response);
        }
    }
    
    static class BigIntegerRoundResponseHandler extends DummyResponseHandler {
        @Override
        public Object apply(String response) {
            if (response == null) {
                return null;
            }
            return BigDecimal.valueOf(Double.parseDouble(response));
        }
    }
    
    static class CombinedResponseHandler extends DummyResponseHandler {    	
    	private DummyResponseHandler handler1;
    	private DummyResponseHandler handler2;
    	
    	public CombinedResponseHandler(DummyResponseHandler handler1, DummyResponseHandler handler2) {
    		this.handler1= handler1;
    		this.handler2= handler2;
		}
    	
        @Override
        public Object apply(String response) {            
            return super.apply(handler2.apply(handler1.apply(response).toString()).toString());
        }
    }
    
    static class BigDecimalFormatResponseHandler extends DummyResponseHandler {
        private BigDecimal divideBy;
        private Integer scale;
        private RoundingMode roundingMode;

        public BigDecimalFormatResponseHandler() {
            this(null, null);
        }

        public BigDecimalFormatResponseHandler(BigDecimal divideBy, Integer scale) {
            this(divideBy, scale, RoundingMode.UNNECESSARY);
        }

        public BigDecimalFormatResponseHandler(BigDecimal divideBy, Integer scale, RoundingMode roundingMode) {
            this.divideBy = divideBy;
            this.scale = scale;
            this.roundingMode = roundingMode;
        }

        @Override
        public Object apply(String response) {
            if (response == null) {
                return null;
            }
            BigDecimal value = new BigDecimal(response);
            if ((divideBy != null) && (scale != null)) {
                value = value.divide(divideBy, scale.intValue(), roundingMode);
            }
            return super.apply(value.toString());
        }
    }

    private static class WesRelayTcpProtocol {
        private Map<String, String> _commands;
        private Map<String, Function<String, Object>> _responses;

        protected String WES_DATA_RELAY_VALUE_CMD = "gr00%1d"; // gr001 or gr002
        protected String WES_DATA_RELAY_VALUE_CMD_W = "srl%1d=%%d"; // srl1=1 -> RLW1=10000000

        public WesRelayTcpProtocol(Map<String, String> _commands, Map<String, Function<String, Object>> _responses) {
            this._commands = _commands;
            this._responses = _responses;
        }

        public void initWesRelay(WesRelay relay) {
            if (relay == null) {
                return;
            }

            if (relay.getValue() != null) {
                _commands.put(relay.getValue().getLabel(),
                        getCommandByIndex(WES_DATA_RELAY_VALUE_CMD, relay.getIndex()));
                _responses.put(relay.getValue().getLabel(), new DummyResponseHandler());
                
                _commands.put(relay.getValue().getLabel() + "_W", getCommandByIndex(
                		WES_DATA_RELAY_VALUE_CMD_W, relay.getIndex()));
                _responses.put(relay.getValue().getLabel() + "_W",
                        new SetRelayResponseHandler(relay.getIndex()));
            }
        }

        private String getCommandByIndex(String parametrizedCommand, byte index) {
            return String.format(parametrizedCommand, index);
        }
        
        public class SetRelayResponseHandler implements Function<String, Object> {
            private byte index;

            public SetRelayResponseHandler( byte index) {
                this.index = index;
            }

            @Override
            public Object apply(String response) throws UnsupportedOperationException {
                String responsePrefix = "RLMB=";
                int prefixLength = responsePrefix.length();
                char c = '?';
                if ((response != null) && (response.length() == prefixLength + 2)
                        && response.startsWith(responsePrefix)) {
                    c = response.charAt(prefixLength + index - 1);
                }

                System.out.println(response + " for " + index + ";c=" + c);

                if (c == '1') {
                    return Boolean.TRUE;
                } else if (c == '0') {
                    return Boolean.FALSE;
                } else {
                    return null;
                }
            }
        }
    }

    private static class WesPulseCounterTcpProtocol {
        private Map<String, String> _commands;
        private Map<String, Function<String, Object>> _responses;

        protected String WES_DATA_PULSECOUNTER_GENERIC_CMD 					= "gP%s0%s"; 	// gPx0y  (x= index, y= value)
        protected char WES_DATA_PULSECOUNTER_DEBIT_SUFFIX 					= '0'; 			// gPx00  (Debit)
        protected char WES_DATA_PULSECOUNTER_INDEX_SUFFIX 					= '1'; 			// gPx01  (Current Index)
        protected char WES_DATA_PULSECOUNTER_TODAYCONSUMPTION_SUFFIX 		= '2'; 			// gPx02  (Current Day Consumption)
        protected char WES_DATA_PULSECOUNTER_MONTHCONSUMPTION_SUFFIX 		= '3'; 			// gPx03  (Current Month Consumption)
        protected char WES_DATA_PULSECOUNTER_YEARCONSUMPTION_SUFFIX 		= '4'; 			// gPx04  (Current Year Consumption)
        protected char WES_DATA_PULSECOUNTER_YESTERDAYCONSUMPTION_SUFFIX 	= '2'; 			// gPx0?  (Command is unknown!!!)  TODO: FIX THAT!

        public WesPulseCounterTcpProtocol(Map<String, String> _commands, Map<String, Function<String, Object>> _responses) {
            this._commands = _commands;
            this._responses = _responses;
        }

        public void initPulseCounter(WesPulseCounter pulseCounter) {
            if (pulseCounter == null) {
                return;
            }

            if (pulseCounter.getDebit() != null) {
                _commands.put(pulseCounter.getDebit().getLabel(),
                        getCommandByIndexAsChars(WES_DATA_PULSECOUNTER_GENERIC_CMD, (byte) (pulseCounter.getIndex() + 1), WES_DATA_PULSECOUNTER_DEBIT_SUFFIX));
                _responses.put(pulseCounter.getDebit().getLabel(), new DummyResponseHandler());
            }
            
            if (pulseCounter.getCurrentIndex() != null) {
                _commands.put(pulseCounter.getCurrentIndex().getLabel(),
                        getCommandByIndexAsChars(WES_DATA_PULSECOUNTER_GENERIC_CMD, (byte) (pulseCounter.getIndex() + 1), WES_DATA_PULSECOUNTER_INDEX_SUFFIX));
                _responses.put(pulseCounter.getCurrentIndex().getLabel(), new DummyResponseHandler());
            }
            
            Function<String, Object> removeUnitAndRoundResponseHandler= new CombinedResponseHandler(new RemoveUnitResponseHandler(), new BigIntegerRoundResponseHandler());
            
            if (pulseCounter.getTodayConsumption() != null) {
                _commands.put(pulseCounter.getTodayConsumption().getLabel(),
                        getCommandByIndexAsChars(WES_DATA_PULSECOUNTER_GENERIC_CMD, (byte) (pulseCounter.getIndex() + 1), WES_DATA_PULSECOUNTER_TODAYCONSUMPTION_SUFFIX));
                _responses.put(pulseCounter.getTodayConsumption().getLabel(), removeUnitAndRoundResponseHandler);  // Response Handler should round the value.
            }
            
            
            if (pulseCounter.getMonthConsumption() != null) {
                _commands.put(pulseCounter.getMonthConsumption().getLabel(),
                        getCommandByIndexAsChars(WES_DATA_PULSECOUNTER_GENERIC_CMD, (byte) (pulseCounter.getIndex() + 1), WES_DATA_PULSECOUNTER_MONTHCONSUMPTION_SUFFIX));
                _responses.put(pulseCounter.getMonthConsumption().getLabel(), removeUnitAndRoundResponseHandler);  // Response Handler should round the value.
            }
            
            if (pulseCounter.getYearConsumption() != null) {
                _commands.put(pulseCounter.getYearConsumption().getLabel(),
                        getCommandByIndexAsChars(WES_DATA_PULSECOUNTER_GENERIC_CMD, (byte) (pulseCounter.getIndex() + 1), WES_DATA_PULSECOUNTER_YEARCONSUMPTION_SUFFIX));
                _responses.put(pulseCounter.getYearConsumption().getLabel(), removeUnitAndRoundResponseHandler);  // Response Handler should round the value.
            }
            
            if (pulseCounter.getYesterdayConsumption() != null) {
                _commands.put(pulseCounter.getYesterdayConsumption().getLabel(),
                        getCommandByIndexAsChars(WES_DATA_PULSECOUNTER_GENERIC_CMD, (byte) (pulseCounter.getIndex() + 1), WES_DATA_PULSECOUNTER_YESTERDAYCONSUMPTION_SUFFIX));
                _responses.put(pulseCounter.getYesterdayConsumption().getLabel(), removeUnitAndRoundResponseHandler);  // Response Handler should round the value.
            }
        }

        private char[] getIndexAsChars(byte index) {
            String formattedIdx = StringUtils.leftPad(String.valueOf(index), 1, '0');
            return formattedIdx.toCharArray();
        }

        private String getCommandByIndexAsChars(String parametrizedCommand, byte index, char commandSuffix) {
            char[] indexAsChars = getIndexAsChars(index);
            return String.format(parametrizedCommand, indexAsChars[0], commandSuffix);
        }
    }
    
    private static class WesSensorTcpProtocol {
        private Map<String, String> _commands;
        private Map<String, Function<String, Object>> _responses;

        protected String WES_DATA_SENSOR_VALUE_CMD = "gW0%s%s"; // gW001

        public WesSensorTcpProtocol(Map<String, String> _commands, Map<String, Function<String, Object>> _responses) {
            this._commands = _commands;
            this._responses = _responses;
        }

        public void initSensor(WesSensor sensor) {
            if (sensor == null) {
                return;
            }

            if (sensor.getValue() != null) {
                _commands.put(sensor.getValue().getLabel(),
                        getCommandByIndexAsChars(WES_DATA_SENSOR_VALUE_CMD, (byte) (sensor.getIndex() + 1)));
                _responses.put(sensor.getValue().getLabel(), new DummyResponseHandler());
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

    private static class TeleInfoTcpProtocol {
        private Map<String, String> _commands;
        private Map<String, Function<String, Object>> _responses;

        protected String WES_DATA_TELEINFO_INDEXN_CMD = "gT%1d%2d"; // gT110

        protected String WES_DATA_TELEINFO_IINSTN_CMD = "gT%1d0%1d"; // gT101 , gT102, gT103

        protected String WES_DATA_TELEINFO_PAPPS_CMD = "gT%1d00"; // gT100

        protected byte WES_DATA_TELEINFO_INDEX0 = 10; // Index Base / H.Creuse HP / EJP HN /TEMPO Bleu HC
        @SuppressWarnings("unused")
        protected byte WES_DATA_TELEINFO_INDEX1 = 11; // Index H. Creuse HC / EJP PM /TEMPO Bleu HP
        @SuppressWarnings("unused")
        protected byte WES_DATA_TELEINFO_INDEX2 = 12; // Index TEMPO Blanc HC
        @SuppressWarnings("unused")
        protected byte WES_DATA_TELEINFO_INDEX3 = 13; // Index TEMPO Blanc HP
        @SuppressWarnings("unused")
        protected byte WES_DATA_TELEINFO_INDEX4 = 14; // Index TEMPO Rouge HC
        @SuppressWarnings("unused")
        protected byte WES_DATA_TELEINFO_INDEX5 = 15; // Index TEMPO Rouge HP

        public TeleInfoTcpProtocol(Map<String, String> _commands, Map<String, Function<String, Object>> _responses) {
            this._commands = _commands;
            this._responses = _responses;
        }

        public void initTic(TeleInfo teleinfo) {
            if (teleinfo == null) {
                return;
            }
            if (teleinfo.getIndex1() != null) {
                _commands.put(teleinfo.getIndex1().getLabel(), getCommandByIndexAndIndexType(
                        WES_DATA_TELEINFO_INDEXN_CMD, teleinfo.getIndex(), WES_DATA_TELEINFO_INDEX0));
                // We divide by 1000, expect no leading zeroes and 3 digits after '.'
                _responses.put(teleinfo.getIndex1().getLabel(),
                        new BigDecimalFormatResponseHandler(new BigDecimal(1000), 3));
            }

            if (teleinfo.getIinstMono() != null) {
                _commands.put(teleinfo.getIinstMono().getLabel(),
                        getCommandByIndexAndPhaseNb(WES_DATA_TELEINFO_IINSTN_CMD, teleinfo.getIndex(), (byte) 1));
                _responses.put(teleinfo.getIinstMono().getLabel(), new DummyResponseHandler());

            }

            if (teleinfo.getpApps() != null) {
                _commands.put(teleinfo.getpApps().getLabel(),
                        getCommandByIndex(WES_DATA_TELEINFO_PAPPS_CMD, teleinfo.getIndex()));
                _responses.put(teleinfo.getpApps().getLabel(), new StringFormatResponseHandler("(%s,0,0)"));
            }

        }

        private String getCommandByIndex(String parametrizedCommand, byte index) {
            return String.format(parametrizedCommand, index);
        }

        private String getCommandByIndexAndPhaseNb(String parametrizedCommand, byte index, byte n) {
            return String.format(parametrizedCommand, index, n);
        }

        private String getCommandByIndexAndIndexType(String parametrizedCommand, byte index, byte indexType) {
            return String.format(parametrizedCommand, index, indexType);
        }
    }

    private class WesOneWireRelayTcpProtocol {
        private Map<String, String> _commands;
        private Map<String, Function<String, Object>> _responses;

        protected String WES_DATA_RELAY_STATE_CMD_W = "srl%d%02d=%%d"; // srl101=1 -> RLW1=10000000
        // (state of the eight relays of the card '1'in RLW
        // means Card#1)

        public WesOneWireRelayTcpProtocol(Map<String, String> _commands,
                Map<String, Function<String, Object>> _responses) {
            this._commands = _commands;
            this._responses = _responses;
        }

        public void initWesOneWireRelay(WesOneWireRelay relay) {
            if (relay == null) {
                return;
            }

            if (relay.getState() != null) {
                _commands.put(relay.getState().getLabel() + "_W", getCommandByCardIndexAndIndex(
                        WES_DATA_RELAY_STATE_CMD_W, (byte) (relay.getCardIndex() + 1), relay.getIndex()));
                _responses.put(relay.getState().getLabel() + "_W",
                        new SetOneWireRelayResponseHandler(relay.getCardIndex(), relay.getIndex()));
            }
        }

        private String getCommandByCardIndexAndIndex(String parametrizedCommand, byte cardIndex, byte index) {
            return String.format(parametrizedCommand, cardIndex, index);
        }

        public class SetOneWireRelayResponseHandler implements Function<String, Object> {
            private byte cardIndex;
            private byte index;

            public SetOneWireRelayResponseHandler(byte cardIndex, byte index) {
                this.cardIndex = cardIndex;
                this.index = index;
            }

            @Override
            public Object apply(String response) throws UnsupportedOperationException {
                String responsePrefix = "RLW%01d=";
                int prefixLength = "RLWx=".length();
                char c = '?';
                if ((response != null) && (response.length() == (prefixLength + 8))
                        && response.startsWith(String.format(responsePrefix, cardIndex))) {
                    c = response.charAt(prefixLength + index - 1);
                }

                System.out.println(response + " for " + cardIndex + "/" + index + ";c=" + c);

                if (c == '1') {
                    return Boolean.TRUE;
                } else if (c == '0') {
                    return Boolean.FALSE;
                } else {
                    return null;
                }
            }
        }
    }

    private class WesRelaysCardTcpProtocol {
        private Map<String, String> _commands;
        private Map<String, Function<String, Object>> _responses;

        protected String WES_DATA_RELAYSCARD_STATES_CMD = "gr%1d09"; // $r109

        public WesRelaysCardTcpProtocol(Map<String, String> _commands,
                Map<String, Function<String, Object>> _responses) {
            this._commands = _commands;
            this._responses = _responses;
        }

        public void initWesRelaysCard(WesRelaysCard wesRelaysCard) {
            if (wesRelaysCard == null) {
                return;
            }

            List<WesOneWireRelay> wesOneWireRelays = wesRelaysCard.getRelays();
            if (wesOneWireRelays != null) {
                WesOneWireRelayTcpProtocol oneWireRelayXmlProtocol = new WesOneWireRelayTcpProtocol(_commands,
                        _responses);
                Iterator<WesOneWireRelay> wesOneWireRelaysIt = wesOneWireRelays.iterator();
                while (wesOneWireRelaysIt.hasNext()) {
                    WesOneWireRelay oneWirerelay = wesOneWireRelaysIt.next();
                    oneWireRelayXmlProtocol.initWesOneWireRelay(oneWirerelay);
                }
            }

            _commands.put(wesRelaysCard.getLabel(),
                    getCommandByIndex(WES_DATA_RELAYSCARD_STATES_CMD, (byte) (wesRelaysCard.getIndex() + 1)));
            _responses.put(wesRelaysCard.getLabel(), new DummyResponseHandler());
        }

        private String getCommandByIndex(String parametrizedCommand, byte index) {
            return String.format(parametrizedCommand, index);
        }
    }
}

/*
 * srl1=1 (Enable relay1 server) -> RLMB=10 (state of the two relays)
 * srl1=0 (Disable relay1 server) -> RLMB=00 (state of the two relays)
 *
 *
 * --- Virtual Switchs
 * svs1=1 (Enable Virtual Switch 1) -> SW=10000000 (state of the eight virtual switches)
 * svs1=0 (Disable Virtual Switch 1) -> SW=00000000 (state of the eight virtual switches)
 */
