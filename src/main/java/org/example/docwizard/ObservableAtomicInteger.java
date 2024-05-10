package org.example.docwizard;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ObservableAtomicInteger {

    private final AtomicInteger atomicInteger;
    private final List<ChangeListener> listeners;

    public ObservableAtomicInteger(int initialValue) {
        atomicInteger = new AtomicInteger(initialValue);
        listeners = new ArrayList<>();
    }

    public int get() {
        return atomicInteger.get();
    }

    public void set(int newValue) {
        int oldValue = atomicInteger.getAndSet(newValue);
        if (oldValue != newValue) {
            notifyListeners(oldValue, newValue);
        }
    }

    public void incrementAndGet() {
        int oldValue = atomicInteger.getAndIncrement();
        int newValue = oldValue + 1;
        notifyListeners(oldValue, newValue);
    }

    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(int oldValue, int newValue) {
        for (ChangeListener listener : listeners) {
            listener.noise(oldValue, newValue);
        }
    }

    public interface ChangeListener {
        void noise(int oldValue, int newValue);
    }
}
