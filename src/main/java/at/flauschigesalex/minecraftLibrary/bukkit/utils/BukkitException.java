package at.flauschigesalex.minecraftLibrary.bukkit.utils;

import at.flauschigesalex.defaultLibrary.utils.LibraryException;

@SuppressWarnings("unused")
public class BukkitException extends LibraryException {

    public static final BukkitException bukkitNotFoundException = new BukkitException("Could not get bukkit main thread. Maybe you are not playing minecraft?");

    public BukkitException() {
        super();
    }

    public BukkitException(String message) {
        super(message);
    }

    public BukkitException(String message, Throwable cause) {
        super(message, cause);
    }

    public BukkitException(Throwable cause) {
        super(cause);
    }
}