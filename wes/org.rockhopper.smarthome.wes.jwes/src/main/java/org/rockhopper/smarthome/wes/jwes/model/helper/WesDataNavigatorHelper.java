package org.rockhopper.smarthome.wes.jwes.model.helper;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.rockhopper.smarthome.wes.jwes.model.data.TeleInfo;
import org.rockhopper.smarthome.wes.jwes.model.data.WesData;
import org.rockhopper.smarthome.wes.jwes.model.data.WesInfo;
import org.rockhopper.smarthome.wes.jwes.model.data.WesPulseCounter;
import org.rockhopper.smarthome.wes.jwes.model.data.WesRelay;
import org.rockhopper.smarthome.wes.jwes.model.data.WesRelaysCard;
import org.rockhopper.smarthome.wes.jwes.model.data.WesRelaysCards;
import org.rockhopper.smarthome.wes.jwes.model.data.WesSensor;
import org.rockhopper.smarthome.wes.jwes.model.data.type.Field;
import org.rockhopper.smarthome.wes.jwes.model.data.type.VirtualField;

public abstract class WesDataNavigatorHelper<T> {

    public static final String LABEL_DATA_PREFIX = "data";
    public static final String LABEL_RELAYSCARD_STATES_PATTERN_SUFFIX = ".relaysCards.card%s.states";
    public static final String LABEL_RELAYSCARDS_STATES_SUFFIX = ".relaysCards.states";

    public T browse(String context, T input, WesData wesData) {
        return _browse(context, input, wesData);
    }

    protected T browse(String context, T input, TeleInfo info) {
        return _browse(context, input, info);
    }

    public T browse(String context, T input, WesSensor info) {
        return _browse(context, input, info);
    }

    public T browse(String context, T input, WesRelaysCards info) {
        return _browse(context, input, info);
    }

    protected T _browse(String context, T input, Object object) {
        if (object == null) {
            return input;
        }
        if (Field.class == object.getClass()) {
            return input;
        }
        try {
            BeanInfo beanInfo = null;
            try {
                beanInfo = Introspector.getBeanInfo(object.getClass());
            } catch (IntrospectionException e) {
                e.printStackTrace();
            }
            if (beanInfo != null) {
                for (PropertyDescriptor propertyDesc : beanInfo.getPropertyDescriptors()) {
                    String propertyName = propertyDesc.getName();
                    Class<?> propertyClass = propertyDesc.getPropertyType();
                    if (propertyClass.isAssignableFrom(Field.class)) {
                        Field<?, ?> field = null;
                        if ((object instanceof VirtualField)
                                && (propertyName.equals(((VirtualField) object).getMemberName()))) {
                            // We have a VirtualField
                            field = ((Field<?, ?>) object);
                        } else {
                            field = (Field<?, ?>) propertyDesc.getReadMethod().invoke(object);
                        }
                        processField(context, input, propertyName, field);
                    }
                    if (propertyClass.isAssignableFrom(List.class)) {
                        List<?> objects = (List<?>) propertyDesc.getReadMethod().invoke(object);
                        processList(context, input, propertyName, objects);
                    } else if (propertyClass.isAssignableFrom(WesInfo.class)) {
                        _browse(context + "." + propertyName, input, propertyDesc.getReadMethod().invoke(object));
                    } else if (propertyClass.isAssignableFrom(WesRelay.class)) {
                        _browse(context + "." + propertyName, input, propertyDesc.getReadMethod().invoke(object));                      
                    } else if (propertyClass.isAssignableFrom(WesPulseCounter.class)) {
                    	_browse(context + "." + propertyName, input, propertyDesc.getReadMethod().invoke(object)); 
                    }else if (propertyClass.isAssignableFrom(WesRelaysCard.class)) {
                        _browse(context + "." + propertyName, input, propertyDesc.getReadMethod().invoke(object));
                    } else if (propertyClass.isAssignableFrom(WesRelaysCards.class)) {
                        _browse(context + "." + propertyName, input, propertyDesc.getReadMethod().invoke(object));
                    } else if (propertyClass.isAssignableFrom(TeleInfo.class)) {
                        browse(context + "." + propertyName, input,
                                (TeleInfo) propertyDesc.getReadMethod().invoke(object));
                    }
                }
            }
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return input;
    }

    abstract protected void processField(String context, T input, String propertyName, Field<?, ?> field);

    abstract protected void processList(String context, T input, String propertyName, List<?> list);
}
