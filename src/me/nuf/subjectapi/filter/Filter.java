package me.nuf.subjectapi.filter;

import me.nuf.subjectapi.Listener;
import me.nuf.subjectapi.Subject;

/**
 * Created by nuf on 2/27/2016.
 */
public interface Filter {

    boolean filter(Listener listener, Subject subject);

}
