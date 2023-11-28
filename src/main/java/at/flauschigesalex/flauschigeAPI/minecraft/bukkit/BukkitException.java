package at.flauschigesalex.flauschigeAPI.minecraft.bukkit;

import at.flauschigesalex.flauschigeAPI.exception.APIException;

@SuppressWarnings("unused")
public class BukkitException extends APIException {

    public static final BukkitException bukkitNotFoundException = new BukkitException("Could not get bukkit main thread. Maybe you are not playing minecraft?");

    protected BukkitException() {
        super();
    }

    protected BukkitException(String message) {
        super(message);
    }

    protected BukkitException(String message, Throwable cause) {
        super(message, cause);
    }

    protected BukkitException(Throwable cause) {
        super(cause);
    }
}
