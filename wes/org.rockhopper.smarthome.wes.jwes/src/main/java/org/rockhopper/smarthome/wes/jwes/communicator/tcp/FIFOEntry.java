package org.rockhopper.smarthome.wes.jwes.communicator.tcp;

import java.util.concurrent.atomic.AtomicLong;

import org.rockhopper.smarthome.wes.jwes.model.data.type.FieldCommand;

public class FIFOEntry<V,W> implements Comparable<FIFOEntry<V,W>> {
    static final AtomicLong seq = new AtomicLong(0);
    final long seqNum;
    final FieldCommand<V,W> entry;

    public FIFOEntry(FieldCommand<V,W> entry) {
        seqNum = seq.getAndIncrement();
        this.entry = entry;
    }

    public FieldCommand<?, ?> getEntry() {
        return entry;
    }

    @Override
    public int compareTo(FIFOEntry<V,W> other) {
        int res = entry.compareTo(other.entry);
        if (res == 0 && other.entry != this.entry) {
            res = (seqNum < other.seqNum ? -1 : 1);
        }
        return res;
    }
}