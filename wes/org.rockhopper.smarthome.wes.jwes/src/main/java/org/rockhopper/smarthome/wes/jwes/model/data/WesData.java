package org.rockhopper.smarthome.wes.jwes.model.data;

import java.util.ArrayList;
import java.util.List;

import org.rockhopper.smarthome.wes.jwes.model.helper.WesLabelsHelper;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias(WesLabelsHelper.LABEL_DATA_PREFIX)
public class WesData {
	
	private transient boolean truncated;

    private WesInfo info;

    private TeleInfo tic1;

    private TeleInfo tic2;

    private TeleInfo tic3;

    private WesRelay relay1;

    private WesRelay relay2;
    
    @XStreamImplicit(itemFieldName = "pulseCounter")
    private List<WesPulseCounter> pulseCounters;

    @XStreamImplicit(itemFieldName = "sensor")
    private List<WesSensor> sensors;

    private WesRelaysCards relaysCards;

    public WesData() {
    	truncated= false;
    	
        this.info = new WesInfo();
        this.tic1 = new TeleInfo((byte) 1);
        this.tic2 = new TeleInfo((byte) 2);
        this.tic3 = new TeleInfo((byte) 3);
        this.relay1 = new WesRelay((byte) 1);
        this.relay2 = new WesRelay((byte) 2);
        
        /*
        this.pulseCounters= new ArrayList<WesPulseCounter>(4);        
        for (byte i=0; i<=3; i++) {
        	this.pulseCounters.add(i, new WesPulseCounter(i));
        }
        */
    }

    public WesInfo getInfo() {
        return info;
    }

    public void setInfo(WesInfo info) {
        this.info = info;
    }

    public TeleInfo getTic1() {
        return tic1;
    }

    public void setTic1(TeleInfo tic1) {
        this.tic1 = tic1;
    }

    public TeleInfo getTic2() {
        return tic2;
    }

    public void setTic2(TeleInfo tic2) {
        this.tic2 = tic2;
    }

    public TeleInfo getTic3() {
        return tic3;
    }

    public void setTic3(TeleInfo tic3) {
        this.tic3 = tic3;
    }

    public WesRelay getRelay1() {
        return relay1;
    }

    public void setRelay1(WesRelay relay1) {
        this.relay1 = relay1;
    }

    public WesRelay getRelay2() {
        return relay2;
    }

    public void setRelay2(WesRelay relay2) {
        this.relay2 = relay2;
    }
    
    public List<WesPulseCounter> getPulseCounters() {
		return pulseCounters;
	}

    public WesRelaysCards getRelaysCards() {
        return relaysCards;
    }

    public void setRelaysCards(WesRelaysCards relaysCards) {
        this.relaysCards = relaysCards;
    }

    public List<WesSensor> getSensors() {
        return sensors;
    }

    public void addSensor(WesSensor sensor) {
        if (sensors == null) {
            sensors = new ArrayList<WesSensor>(30);
        }
        sensors.add(sensor);
    }

    public void truncate() {
        if (info != null) {
            if ((info.getNbHumidSensors() != null) && (info.getNbHumidSensors().getValue() != null)
                    && (info.getNbTempSensors() != null) && (info.getNbTempSensors().getValue() != null)) {
                int nbSensors = info.getNbHumidSensors().getValue().byteValue()
                        + info.getNbTempSensors().getValue().byteValue();
                if ((sensors != null) && (sensors.size() > nbSensors)) {
                    sensors.subList(nbSensors, sensors.size()).clear();
                }
            }
            if ((info.getNbRelaysCards() != null) && (info.getNbRelaysCards().getValue() != null)) {
                relaysCards.truncateTo(info.getNbRelaysCards().getValue().byteValue());
            }
        }
        if (tic1 != null) {
            Boolean ticEnabled = tic1.isEnabled();
            if ((ticEnabled != null) && (!ticEnabled.booleanValue())) {
                tic1 = null;
            }
        }
        if (tic2 != null) {
            Boolean ticEnabled = tic2.isEnabled();
            if ((ticEnabled != null) && (!ticEnabled.booleanValue())) {
                tic2 = null;
            }
        }
        if (tic3 != null) {
            Boolean ticEnabled = tic3.isEnabled();
            if ((ticEnabled != null) && (!ticEnabled.booleanValue())) {
                tic3 = null;
            }
        }
        truncated= true;
    }
    
    public boolean isTruncated() {
		return truncated;
	}
    
    public static final WesData getInstance(int nbSensors, int nbRelaysCard) {
    	WesData wesData = new WesData();
    	if (nbSensors>0) {
	        for (int i = 0; i < nbSensors; i++) {
	            wesData.addSensor(new WesSensor((byte) i));
	        }
    	}
    	if (nbRelaysCard>0) {
    		wesData.setRelaysCards(new WesRelaysCards((byte) nbRelaysCard));
    	}
    	return wesData;
    }
}