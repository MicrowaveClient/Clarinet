package me.nuf.glade.config;

import me.nuf.api.interfaces.Labeled;
import me.nuf.api.interfaces.Loadable;
import me.nuf.api.interfaces.Savable;
import me.nuf.glade.core.Glade;

import java.io.File;

/**
 * Created by nuf on 2/26/2016.
 */
public abstract class Config implements Labeled, Loadable, Savable {

    private final String label;
    private final File directory, file;

    public Config(String label) {
        this.label = label;
        this.directory = Glade.getInstance().getDirectory();
        this.file = new File(directory, label);
        Glade.getInstance().getConfigManager().register(this);
    }

    public Config(String label, File directory) {
        this.label = label;
        this.directory = directory;
        this.file = new File(directory, label);
        Glade.getInstance().getConfigManager().register(this);
    }

    @Override
    public final String getLabel() {
        return label;
    }

    public final File getDirectory() {
        return directory;
    }

    public final File getFile() {
        return file;
    }

    @Override
    public abstract void load(Object... source);

    @Override
    public abstract void save(Object... destination);
}
