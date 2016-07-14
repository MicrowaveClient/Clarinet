package me.nuf.subjectapi.basic;

import me.nuf.subjectapi.Listener;
import me.nuf.subjectapi.Subject;
import me.nuf.subjectapi.SubjectManager;
import me.nuf.subjectapi.events.system.ShutdownSubject;
import me.nuf.subjectapi.events.system.StartupSubject;
import me.nuf.subjectapi.filter.Filter;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by nuf on 2/27/2016.
 */
public class BasicSubjectManager implements SubjectManager {

    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void register(Listener listener) {
        if (!has(listener) && listener != null)
            listeners.add(listener);
    }

    @Override
    public void unregister(Listener listener) {
        if (has(listener) && listener != null)
            listeners.remove(listener);
    }

    @Override
    public void clear() {
        if (!listeners.isEmpty())
            listeners.clear();
    }

    @Override
    public void dispatch(Subject subject) {
        listeners.forEach(listener -> {
            if (subject != null && filter(listener, subject) && listener.getSubject() == subject.getClass())
                if ((subject instanceof StartupSubject || subject instanceof ShutdownSubject) || Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().theWorld != null)
                    listener.call(subject);
        });
    }

    @Override
    public List<Listener> getListeners() {
        return listeners;
    }

    public Listener getListener(String identifier) {
        if (!listeners.isEmpty())
            for (Listener listener : listeners)
                if (listener.getIdentifier().equalsIgnoreCase(identifier))
                    return listener;
        return null;
    }

    private boolean filter(Listener listener, Subject subject) {
        List<Filter> filters = listener.getFilters();
        if (!filters.isEmpty())
            for (Filter filter : filters)
                if (!filter.filter(listener, subject))
                    return false;
        return true;
    }

    private boolean has(Listener listener) {
        return listeners.contains(listener);
    }
}
