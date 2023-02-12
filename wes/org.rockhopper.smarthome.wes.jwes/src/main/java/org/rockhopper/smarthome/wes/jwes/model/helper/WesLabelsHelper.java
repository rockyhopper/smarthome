package org.rockhopper.smarthome.wes.jwes.model.helper;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.rockhopper.smarthome.wes.jwes.model.data.WesData;
import org.rockhopper.smarthome.wes.jwes.model.data.type.Field;

public class WesLabelsHelper extends WesDataNavigatorHelper<Set<Field<?, ?>>> {

    public void label(String labelDataPrefix, WesData wesData) {
        super.browse(labelDataPrefix, null, wesData);
    }

    @Override
    protected void processField(String context, Set<Field<?, ?>> input, String propertyName, Field<?, ?> field) {
        if (field != null) {
            field.setLabel(context + "." + propertyName);
            // System.out.println("OK>" + context + "." + propertyName);
            if (input != null) {
                if ((field.getPriority() != null) && (field.getPriority().byteValue() >= Field.PRIORITY_DISCOVERY)) {
                    input.add(field);
                    // System.out.println("WesLabelsHelper!" + Field.PRIORITY_DISCOVERY + "!OK>" + field.getLabel() +
                    // "->" + field.getValue());
                } else {
                    // System.out.println("WesLabelsHelper!" + field.getPriority() + "!OK>" + field.getLabel());
                }
            }
        } else {
            // System.out.println("NULL>" + context + "." + propertyName);
        }
    }

    @Override
    protected void processList(String context, Set<Field<?, ?>> input, String propertyName, List<?> list) {
        if (list == null) {
            return;
        }
        Iterator<?> objectsIterator = list.iterator();
        int c = 0;
        while (objectsIterator.hasNext()) {
            _browse(context + "." + StringUtils.removeEnd(propertyName, "s") + c, input, objectsIterator.next());
            c++;
        }
    }
    
    public static final Set<Field<?, ?>> label(WesData wesData) {
        return new WesLabelsHelper().browse(WesDataNavigatorHelper.LABEL_DATA_PREFIX, new HashSet<Field<?, ?>>(), wesData);
    }

}
