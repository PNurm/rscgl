package com.rscgl.ui.container;

import com.badlogic.gdx.utils.reflect.ClassReflection;

import java.util.ArrayList;
import java.util.Iterator;

public class ItemContainer <T extends Item>  {

    private ArrayList<T> itemSlots;
    private ArrayList<ItemChangeListener> listeners;

    /**
     * Constructs an ItemContainer with fixed size, used in cases where scroll bar is not required.
     *
     * @param size
     */
    public ItemContainer(int size, Class<T> type) {
        try {
            itemSlots = new ArrayList<T>(size);
            for (int i = 0; i < size; i++) {
                T it = ClassReflection.newInstance(type);
                it.setSlotIndex(i);
                it.set(-1, -1);
                itemSlots.add(it);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        listeners = new ArrayList<ItemChangeListener>();
    }

    public ItemContainer(int size) {
        this(size, (Class<T>) Item.class);
    }

    public void addListener(ItemChangeListener l) {
        listeners.add(l);
    }

    public void add(T itemSlot) {
        itemSlot.setSlotIndex(itemSlots.size());
        itemSlots.add(itemSlot);
        for (ItemChangeListener l : listeners)
            l.itemAdded(itemSlot);
    }

    public void remove(int slot) {
        itemSlots.remove(slot);
        for (ItemChangeListener l : listeners)
            l.itemRemoved(slot);
    }

    public T get(int id) {
        if (id > itemSlots.size()) {
            return null;
        }
        return itemSlots.get(id);
    }
    public T getFirstItem(int id) {
        for (T i : itemSlots) {
            if (id == i.getId())
                return i;
        }
        return null;
    }

    public int countItem(int id) {
        int a = 0;
        for (T i : itemSlots) {
            if (id == i.getId())
                a += i.getAmount();
        }
        return a;
    }

    public void set(int slot, int id, int amount) {
        Item item = this.itemSlots.get(slot);
        item.set(id, amount);
        for (ItemChangeListener l : listeners)
            l.itemChanged(slot, item);
    }

    public void update(int slot) {
        Item item = this.itemSlots.get(slot);
        for (ItemChangeListener l : listeners)
            l.itemChanged(slot, item);
    }

    public ArrayList<T> getItems() {
        return itemSlots;
    }

    public int size() {
        return itemSlots.size();
    }

    public void clear() {
        Iterator<T> itemIterator = itemSlots.iterator();
        while (itemIterator.hasNext()) {
            T item = itemIterator.next();
            for (ItemChangeListener l : listeners)
                l.itemRemoved(item.getSlotIndex());
        }
        itemSlots.clear();
    }

    public void reset() {
        for (T item : itemSlots) {
            item.set(-1, -1);
            for (ItemChangeListener l : listeners)
                l.itemChanged(item.getSlotIndex(), item);
        }
    }

    public int count() {
        int count = 0;
        for (Item item : itemSlots) {
            if(item.getId() != -1)
                count++;
        }
        return count;
    }

    public boolean containsItem(T item) {
        return containsItem(item.getId());
    }

    public boolean containsItem(int item) {
        for(T i : itemSlots)
            if(i.getId() == item)
                return true;
        return false;
    }
}
