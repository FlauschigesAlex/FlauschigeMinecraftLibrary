package at.flauschigesalex.minecraftLibrary.minecraft.bukkit;

import at.flauschigesalex.defaultLibrary.exception.LibraryException;

@SuppressWarnings("unused")
public class BukkitException extends LibraryException {

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
