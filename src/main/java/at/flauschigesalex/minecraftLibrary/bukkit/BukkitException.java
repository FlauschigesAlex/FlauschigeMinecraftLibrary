package at.flauschigesalex.minecraftLibrary.bukkit;

import at.flauschigesalex.defaultLibrary.utils.LibraryException;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class BukkitException extends LibraryException {

    public static final BukkitException bukkitNotFoundException = new BukkitException("Could not get bukkit main thread. Maybe you are not playing minecraft?");

    public static LibraryException bukkitListenerNameNotUniqueException(final @NotNull PluginListener listener) {
        return new ListenerException("PluginListener '"+listener.getClass().getSimpleName()+"' has a none unique name: "+listener.getName());
    }

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

@SuppressWarnings("unused")
class ListenerException extends BukkitException {

    protected ListenerException() {
        super();
    }

    protected ListenerException(Throwable cause) {
        super(cause);
    }

    protected ListenerException(String message) {
        super(message);
    }

    protected ListenerException(String message, Throwable cause) {
        super(message, cause);
    }
}