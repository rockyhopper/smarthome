package org.rockhopper.smarthome.wes.jwes.model.helper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.rockhopper.smarthome.wes.jwes.model.data.WesData;
import org.rockhopper.smarthome.wes.jwes.model.data.type.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WesPrioritiesHelper extends WesDataNavigatorHelper<Map<Field<?, ?>, Byte>> {
	protected Logger log= LoggerFactory.getLogger(getClass());
	
    private byte minPriority = Field.PRIORITY_REALTIME;

    public WesPrioritiesHelper() {
    }

    public WesPrioritiesHelper(byte minPriority) {
        this.minPriority = minPriority;
    }

    /**
     * This method assumes that wesData is already labeled!
     *
     * @param wesData WES Data to browse for priorities
     * @return Map with Field as Key and Priority as Value
     */
    public Map<Field<?, ?>, Byte> browse(WesData wesData) {
        return super.browse(null, new HashMap<Field<?, ?>, Byte>(), wesData);
    }

    @Override
    protected void processField(String context, Map<Field<?, ?>, Byte> input, String propertyName, Field<?, ?> field) {
        if ((field != null) && (field.getLabel() != null) && (field.getPriority() != null)
                && (field.getPriority().byteValue() >= minPriority)) {
            input.put(field, field.getPriority());
            // System.out.println("WesPrioritiesHelper!" + minPriority + "!OK>" + field.getLabel() + "->" +
            // field.getPriority());
        }
    }

    @Override
    protected void processList(String context, Map<Field<?, ?>, Byte> input, String propertyName, List<?> list) {
    	if (list==null) {
    	    log.warn("No list (null)");
    		return;
    	}
        Iterator<?> listIterator = list.iterator();
        while (listIterator.hasNext()) {
            _browse(context, input, listIterator.next());
        }
    }
}
