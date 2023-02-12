package org.rockhopper.smarthome.wes.jwes.model.data.type;

public class Field<V, W> {
    public static final Byte PRIORITY_IMMEDIATE = 0;
    public static final Byte PRIORITY_REALTIME = 1;
    public static final Byte PRIORITY_HIGH = 5;
    public static final Byte PRIORITY_MEDIUM = 10;
    public static final Byte PRIORITY_LOW = 30;
    public static final Byte PRIORITY_DISCOVERY = -1; // Only discovered (using XML protocol but not by TCP protocol)
    public static final Byte PRIORITY_SKIPPED = -2; // Field is skipped because refreshed elsewhere!

    /**
     * Implicit type is String
     *//*
        * @SuppressWarnings("unchecked")
        * public Field() {
        * this((Byte) null);
        * }
        *
        * public Field(Class<V> type) {
        * this(type, (Byte) null);
        * }
        *
        * public Field(Class<V> type, Class<W> subType) {
        * this(type, subType, null);
        * }
        */
    /**
     * Implicit type is String
     * @param priority Field's priority
     */
    @SuppressWarnings("unchecked")
    public Field(Byte priority) {
        this((Class<V>) String.class, null, priority);
    }

    /**
     * Constructor
     * @param type Type of Field
     * @param priority Priority given to that Field
     */
    public Field(Class<V> type, Byte priority) {
        this(type, null, priority);
    }
    
	/**
	 * 
	 * @param type Type of Field
	 * @param subType Sub-Type of the Field
	 * @param priority Priority given to that Field
	 */
    public Field(Class<V> type, Class<W> subType, Byte priority) {
        this.type = type;
        this.subType = subType;
        this.priority = priority;
    }

    private String label;
    private V value;
    private final Class<V> type;
    private final Class<W> subType;
    private Byte priority;

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public Class<V> getType() {
        return this.type;
    }

    public Class<W> getSubType() {
        return this.subType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Byte getPriority() {
        return priority;
    }

    public void setPriority(Byte priority) {
        this.priority = priority;
    }
}