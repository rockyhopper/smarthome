package org.rockhopper.smarthome.wes.jwes.communicator.tcp;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.rockhopper.smarthome.wes.jwes.communicator.TcpClient;
import org.rockhopper.smarthome.wes.jwes.model.WesEvent;
import org.rockhopper.smarthome.wes.jwes.model.WesEventListener;
import org.rockhopper.smarthome.wes.jwes.model.data.WesData;
import org.rockhopper.smarthome.wes.jwes.model.data.WesRelaysCard;
import org.rockhopper.smarthome.wes.jwes.model.data.type.Field;
import org.rockhopper.smarthome.wes.jwes.model.data.type.FieldCommand;
import org.rockhopper.smarthome.wes.jwes.model.helper.WesDataNavigatorHelper;
import org.rockhopper.smarthome.wes.jwes.model.helper.WesPrioritiesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpPolling {
	protected Logger log= LoggerFactory.getLogger(getClass());
	
    private ScheduledExecutorService executor;
    private PriorityBlockingQueue<FIFOEntry<?,?>> fieldsToUpdate;

    public static final int DELAY = 500;

    private WesEventListener eventListener;
    
    private TcpClient tcpClient = null;
    private TcpProtocol tcpProtocol = null;

    private WesData wesData; 
    
    private static Pattern RELAYSCARD_STATES_PATTERN_EXPR;
    private static Pattern RELAYSCARD_RELAY_STATE_PATTERN_EXPR;
    static {
    	String pattern= StringUtils.replace(WesDataNavigatorHelper.LABEL_RELAYSCARD_STATES_PATTERN_SUFFIX, "%s", "([^\\\\d]+)");
    	System.out.println("RELAYSCARD_STATES_PATTERN_EXPR: " + pattern);
    	RELAYSCARD_STATES_PATTERN_EXPR= Pattern.compile(pattern);
    	
    	pattern= ".relaysCards.card([^\\\\d]+).relay([^\\\\d]+).state";
        System.out.println("RELAYSCARD_STATES_PATTERN_EXPR: " + pattern);
    	RELAYSCARD_RELAY_STATE_PATTERN_EXPR= Pattern.compile(pattern);
    }
    
    public TcpPolling(String wesDeviceIp, int wesTcpPort, WesData wesData) {
		this.wesData= wesData;
		this.tcpClient= new TcpClient(wesDeviceIp, wesTcpPort);
	}
    
    public void run() {
        if (tcpClient == null) {
            throw new IllegalArgumentException("TcpPolling requires a TcpClient!");
        }
    	if (eventListener==null) {
    		eventListener = new WesEventListener() {
    		};
    	}

        executor = Executors.newScheduledThreadPool(10);
        fieldsToUpdate = new PriorityBlockingQueue<FIFOEntry<?,?>>(50);

        tcpProtocol = new TcpProtocol(wesData);

        /*
        WesPrioritiesHelper wesPrioritiesHelper1 = new WesPrioritiesHelper(Field.PRIORITY_DISCOVERY);
        wesPrioritiesHelper1.browse(wesData);
        */
        
        WesPrioritiesHelper wesPrioritiesHelper = new WesPrioritiesHelper(Field.PRIORITY_REALTIME);
        Map<Field<?, ?>, Byte> fieldsPrioritiesMap = wesPrioritiesHelper.browse(wesData);
        populateFieldsToUpdate(fieldsPrioritiesMap);

        
        /*
        FieldCommand<Boolean, Void> oneWireRelay6ON = new FieldCommand<Boolean, Void>(
                wesData.getRelaysCards().getCards().get(0).getRelays().get(5).getState(), Boolean.TRUE);
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                fieldsToUpdate.add(new FIFOEntry<FieldCommand<?, ?>>(oneWireRelay6ON));
            }
        }, 10, TimeUnit.SECONDS);
        FieldCommand<Boolean, Void> oneWireRelay6OFF = new FieldCommand<Boolean, Void>(
                wesData.getRelaysCards().getCards().get(0).getRelays().get(5).getState(), Boolean.FALSE);
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                fieldsToUpdate.add(new FIFOEntry<FieldCommand<?, ?>>(oneWireRelay6OFF));
            }
        }, 20, TimeUnit.SECONDS);
        
        */
        
        while (!executor.isShutdown()) {
            FIFOEntry<?,?> fieldToUpdate = null;
            try {
                fieldToUpdate = fieldsToUpdate.take();
            } 
            catch (InterruptedException e) {
            }
            if (fieldToUpdate != null) {
                update(fieldToUpdate.getEntry());
            }
        }
    }

    public void halt() {
    	if (executor!=null) {
    		executor.shutdown();
            try {
    			executor.awaitTermination(2, TimeUnit.SECONDS);
    		} 
            catch (InterruptedException ie) {
    		}
    	}
    }
    
    private void populateFieldsToUpdate(Map<Field<?, ?>, Byte> fieldsPrioritiesMap) {
        if (fieldsPrioritiesMap != null) {
            Iterator<Entry<Field<?, ?>, Byte>> entriesIt = fieldsPrioritiesMap.entrySet().iterator();
            while (entriesIt.hasNext()) {
                Entry<Field<?, ?>, Byte> entry = entriesIt.next();
                if ((entry.getKey() != null) && (entry.getValue() != null)) {
                    if (entry.getKey().getPriority() >= 0) {
                        addFieldToUpdate(entry.getKey());
                    }
                }
            }
        }
    }

    private <V, W> void addFieldToUpdate(Field<V, W> field) {
        addFieldToUpdate(new FieldCommand<V, W>(field, null));
    }

    private <V,W> void addFieldToUpdate(FieldCommand<V,W> field) {
        if (field != null) {
            fieldsToUpdate.add(new FIFOEntry<V,W>(field));
        }
    }

    private <V,W> void update(FieldCommand<V,W> fieldToUpdate) {
    	if (fieldToUpdate.getPriority() != null) {
            String response = null;
            if (tcpProtocol != null) {
                String command = tcpProtocol.getWesCommand(fieldToUpdate);
                if (command != null) {
                    response = tcpClient.call(command);
                }
            }
            if (response != null) {
                Object oldValue = fieldToUpdate.getValue();
                tcpProtocol.updateField(fieldToUpdate, response);
                if (fieldToUpdate.getValue() != oldValue) {
                    if ((oldValue == null) || (fieldToUpdate.getValue() == null)
                            || (!oldValue.equals(fieldToUpdate.getValue()))) {
                    	
                        eventListener.onEvent(new WesEvent(fieldToUpdate.getField().getLabel(),
                                WesEvent.WesEventCode.UPDATE, oldValue, fieldToUpdate.getValue()));
                        
                    	Matcher matcherStates= RELAYSCARD_STATES_PATTERN_EXPR.matcher(fieldToUpdate.getField().getLabel());
                    	Matcher matcherState= RELAYSCARD_RELAY_STATE_PATTERN_EXPR.matcher(fieldToUpdate.getField().getLabel());
                        if (matcherStates.find()) {
                            System.out.println("------------ Y" + fieldToUpdate.getField().getLabel() + "->" + matcherStates.group(1));
                            // data.relaysCards.card0.states > data.relaysCards.card0.relayN.state
                            String relayStatePrefix= StringUtils.removeEnd(fieldToUpdate.getField().getLabel(), "states") + "relay";
                            String states= (String)fieldToUpdate.getValue();
                            byte i=0;
                            String relayCardNb= matcherStates.group(1);       
                            byte iRelayCardNb= Byte.valueOf(relayCardNb);                            
                            WesRelaysCard relaysCard= wesData.getRelaysCards().getCards().get(iRelayCardNb);
                            
                            while (i<8) {
                            	String currentRelayStateLabel= relayStatePrefix + i + ".state";
                            	Boolean currentState= relaysCard.getState(i);
                            	if (states.charAt(i)=='0') {
                            		eventListener.onEvent(new WesEvent(currentRelayStateLabel,WesEvent.WesEventCode.UPDATE, currentState, Boolean.FALSE));
                            	}
                            	else if (states.charAt(i)=='1') { 
                            		eventListener.onEvent(new WesEvent(currentRelayStateLabel,WesEvent.WesEventCode.UPDATE, currentState, Boolean.TRUE));
                            	}
                            	i++;
                            }                            
                        }
                        else if (matcherState.find()) {
                        	log.info("Card#{} Relay{} update is triggering Card#{}.states refresh! ({}->{})", matcherState.group(1), matcherState.group(2), matcherState.group(1), oldValue, fieldToUpdate.getValue());
                            String relayCardNb= matcherState.group(1);       
                            byte iRelayCardNb= Byte.valueOf(relayCardNb);
                            String currentStates= wesData.getRelaysCards().getCards().get(iRelayCardNb).getStates().getValue();
                            eventListener.onEvent(new WesEvent(wesData.getRelaysCards().getCards().get(iRelayCardNb).getStates().getLabel(),WesEvent.WesEventCode.SYNC, currentStates, currentStates));
                        }
                        else {
                        	System.out.println("------------ N" + fieldToUpdate.getField().getLabel());
                            eventListener.onEvent(new WesEvent(fieldToUpdate.getField().getLabel(),
                                    WesEvent.WesEventCode.UPDATE, oldValue, fieldToUpdate.getValue()));
                        }
                    }
                }
            }
            
            if (fieldToUpdate.getNewValue() != null) {
                // WRITE command, nothing more to do
            } else {
                // READ command, implicitly plan next READ (with same priority)
            	if (!executor.isShutdown()) {
	                executor.schedule(new Runnable() {
	                    @Override
	                    public void run() {
	                        fieldsToUpdate.add(new FIFOEntry<V,W>(fieldToUpdate));
	                    }
	                }, fieldToUpdate.getPriority() * DELAY, TimeUnit.MILLISECONDS);
            	}
            }
        }
    }

    public <V, W> void forceUpdate(Field<V, W> field, V newValue) {
        addFieldToUpdate(new FieldCommand<V, W>(field, newValue));
    }
    
    public void setEventListener(WesEventListener eventListener) {
		this.eventListener = eventListener;
	}   
}
