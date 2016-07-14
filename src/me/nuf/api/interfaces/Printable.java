package me.nuf.api.interfaces;

import java.util.logging.Level;

/**
 * Created by nuf on 3/19/2016.
 */
public interface Printable {

    void print(Level level, String message);

    void printToChat(String message);

}
