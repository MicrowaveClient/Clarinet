package me.nuf.subjectapi;

import me.nuf.subjectapi.filter.Filter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by nuf on 2/27/2016.
 */
public abstract class Listener<S extends Subject> {

    private final String identifier;
    private Class<S> subject;

    private final List<Filter> filters = new CopyOnWriteArrayList<>();

    public Listener(String identifier) {
        this.identifier = identifier;
        Type generic = getClass().getGenericSuperclass();
        if (generic instanceof ParameterizedType)
            for (Type type : ((ParameterizedType) generic).getActualTypeArguments())
                if (type instanceof Class && Subject.class.isAssignableFrom((Class<?>) type)) {
                    subject = (Class<S>) type;
                    break;
                }
    }

    public Class<S> getSubject() {
        return subject;
    }

    public final String getIdentifier() {
        return identifier;
    }

    public final List<Filter> getFilters() {
        return filters;
    }

    public void addFilters(Filter... filters) {
        for (Filter filter : filters)
            this.filters.add(filter);
    }

    public abstract void call(S subject);

}
