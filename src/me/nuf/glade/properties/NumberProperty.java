package me.nuf.glade.properties;

/**
 * Created by nuf on 3/19/2016.
 */
public class NumberProperty<T extends Number> extends Property<T> {

    private final T minimum, maximum;

    private boolean clamp;

    public NumberProperty(T value, T minimum, T maximum, String... aliases) {
        super(value, aliases);
        clamp = true;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public NumberProperty(T value, String... aliases) {
        super(value, aliases);
        clamp = false;
        this.minimum = maximum = null;
    }

    public final T getMaximum() {
        return maximum;
    }

    public final T getMinimum() {
        return minimum;
    }

    @Override
    public void setValue(T value) {
        if (clamp) {
            if (value instanceof Integer) {
                if (value.intValue() > maximum.intValue()) {
                    value = maximum;
                } else if (value.intValue() < minimum.intValue()) {
                    value = minimum;
                }
            } else if (value instanceof Float) {
                if (value.floatValue() > maximum.floatValue()) {
                    value = maximum;
                } else if (value.floatValue() < minimum.floatValue()) {
                    value = minimum;
                }
            } else if (value instanceof Double) {
                if (value.doubleValue() > maximum.doubleValue()) {
                    value = maximum;
                } else if (value.doubleValue() < minimum.doubleValue()) {
                    value = minimum;
                }
            } else if (value instanceof Long) {
                if (value.longValue() > maximum.longValue()) {
                    value = maximum;
                } else if (value.longValue() < minimum.longValue()) {
                    value = minimum;
                }
            } else if (value instanceof Short) {
                if (value.shortValue() > maximum.shortValue()) {
                    value = maximum;
                } else if (value.shortValue() < minimum.shortValue()) {
                    value = minimum;
                }
            } else if (value instanceof Byte) {
                if (value.byteValue() > maximum.byteValue()) {
                    value = maximum;
                } else if (value.byteValue() < minimum.byteValue()) {
                    value = minimum;
                }
            }
        }
        this.value = value;
    }
}
