package me.nuf.glade.properties;

public class EnumProperty<T extends Enum> extends Property<T> {

    public EnumProperty(T value, String... aliases) {
        super(value, aliases);
    }

    public void setViaString(String strValue) {
        Enum[] array;
        for (int length = (array = ((Enum) getValue()).getClass().getEnumConstants()).length, i = 0; i < length; i++) {
            if (array[i].name().equalsIgnoreCase(strValue))
                value = (T) array[i];
        }
    }

}