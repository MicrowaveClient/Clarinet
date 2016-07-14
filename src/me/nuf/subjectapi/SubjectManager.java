package me.nuf.subjectapi;

import java.util.List;

/**
 * Created by nuf on 2/27/2016.
 */
public interface SubjectManager {

    void register(Listener listener);

    void unregister(Listener listener);

    void clear();

    void dispatch(Subject subject);

    List<Listener> getListeners();

}
