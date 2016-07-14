package me.nuf.api.management;

import java.util.List;

/**
 * Created by nuf on 3/19/2016.
 */
public class ListManager<T> {

    protected List<T> elements;

    public List<T> getElements() {
        return elements;
    }

    public void register(T element) {
        elements.add(element);
    }

    public void unregister(T element) {
        elements.remove(element);
    }

    public T get(T element) {
        if (elements.contains(element))
            return element;
        return null;
    }

}
