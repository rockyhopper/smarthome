package org.rockhopper.smarthome.wes.jwes.model.data;

import java.math.BigDecimal;

import org.rockhopper.smarthome.wes.jwes.model.data.type.Field;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("pulseCounter")
public class WesPulseCounter {

    private byte index;
	
    private Field<String, Void> name;
    private Field<BigDecimal, Void> debit;
    private Field<BigDecimal, Void> currentIndex;

    private Field<BigDecimal, Void> todayConsumption;
    private Field<BigDecimal, Void> yesterdayConsumption;
    private Field<BigDecimal, Void> monthConsumption;
    private Field<BigDecimal, Void> YearConsumption;
    
 
    public WesPulseCounter(byte index) {
        if ((index < 0) || (index > 3)) {
            throw new UnsupportedOperationException("WES PulseCounter index is 0 to 3");
        }
        this.index = index;

        name = new Field<String, Void>(Field.PRIORITY_DISCOVERY);
        debit = new Field<BigDecimal, Void>(BigDecimal.class, Field.PRIORITY_REALTIME);
        
        currentIndex = new Field<BigDecimal, Void>(BigDecimal.class, Field.PRIORITY_HIGH);
        
        todayConsumption = new Field<BigDecimal, Void>(BigDecimal.class, Field.PRIORITY_HIGH);
        yesterdayConsumption = new Field<BigDecimal, Void>(BigDecimal.class, Field.PRIORITY_LOW);
        monthConsumption = new Field<BigDecimal, Void>(BigDecimal.class, Field.PRIORITY_LOW);
        YearConsumption = new Field<BigDecimal, Void>(BigDecimal.class, Field.PRIORITY_LOW);
        
    }

	public Field<String, Void> getName() {
		return name;
	}


	public void setName(Field<String, Void> name) {
		this.name = name;
	}


	public Field<BigDecimal, Void> getDebit() {
		return debit;
	}


	public void setDebit(Field<BigDecimal, Void> debit) {
		this.debit = debit;
	}


	public Field<BigDecimal, Void> getCurrentIndex() {
		return currentIndex;
	}


	public void setCurrentIndex(Field<BigDecimal, Void> currentIndex) {
		this.currentIndex = currentIndex;
	}


	public Field<BigDecimal, Void> getTodayConsumption() {
		return todayConsumption;
	}


	public void setTodayConsumption(Field<BigDecimal, Void> todayConsumption) {
		this.todayConsumption = todayConsumption;
	}


	public Field<BigDecimal, Void> getYesterdayConsumption() {
		return yesterdayConsumption;
	}


	public void setYesterdayConsumption(Field<BigDecimal, Void> yesterdayConsumption) {
		this.yesterdayConsumption = yesterdayConsumption;
	}


	public Field<BigDecimal, Void> getMonthConsumption() {
		return monthConsumption;
	}


	public void setMonthConsumption(Field<BigDecimal, Void> monthConsumption) {
		this.monthConsumption = monthConsumption;
	}


	public Field<BigDecimal, Void> getYearConsumption() {
		return YearConsumption;
	}


	public void setYearConsumption(Field<BigDecimal, Void> yearConsumption) {
		YearConsumption = yearConsumption;
	}
	
	public byte getIndex() {
		return index;
	}
}
