package dev.oribuin.whiteboy.util;

public final class BotUtils {

    /**
     * Convert a name into an enum value from a specific enum class
     *
     * @param enumClass The enum class
     * @param name      The name of the enum
     * @param <T>       The enum name
     *
     * @return The enum
     */
    public static <T extends Enum<T>> T getEnum(Class<T> enumClass, String name, T def) {
        if (name == null)
            return def;

        try {
            return Enum.valueOf(enumClass, name.toUpperCase());
        } catch (IllegalArgumentException ignored) {
        }

        return def;
    }
    
}
