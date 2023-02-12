package org.rockhopper.smarthome.wes.jwes.model.data.type;

public class FieldCommand<V, W> implements Comparable<FieldCommand<V, W>> {
    private Field<V, W> field;
    private V newValue;
    private Byte priority;

    public FieldCommand(Field<V, W> field, V newValue) throws UnsupportedOperationException {
        if (field == null) {
            throw new UnsupportedOperationException("A command cannot refer to a NULL field!");
        }
        this.field = field;
        this.newValue = newValue;

        // Priority for writing commands in by default IMMEDIATE
        if (newValue != null) {
            priority = Field.PRIORITY_IMMEDIATE;
        }
    }

    public Field<V, W> getField() {
        return field;
    }

    public V getValue() {
        return field.getValue();
    }

    public Byte getPriority() {
        return (priority != null) ? priority : field.getPriority();
    }

    public V getNewValue() {
        return newValue;
    }

    @Override
    public int compareTo(FieldCommand<V, W> otherField) {
        if ((otherField.getPriority() == null) && (this.getPriority() == null)) {
            return 0;
        }
        if ((otherField.getPriority() != null) && (this.getPriority() == null)) {
            return 1;
        }
        if ((otherField.getPriority() == null) && (this.getPriority() != null)) {
            return -1;
        }
        return this.getPriority() - otherField.getPriority();
    }
}