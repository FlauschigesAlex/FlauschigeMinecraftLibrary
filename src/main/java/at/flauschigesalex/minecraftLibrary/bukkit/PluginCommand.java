package at.flauschigesalex.minecraftLibrary.bukkit;

import at.flauschigesalex.minecraftLibrary.FlauschigeMinecraftLibrary;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static at.flauschigesalex.minecraftLibrary.FlauschigeMinecraftLibrary.getLibrary;

@SuppressWarnings({"unused", "deprecation"})
@Getter
public abstract class PluginCommand extends Command {

    private String pluginPrefix;
    private final String command;

    protected PluginCommand(@NotNull String command) {
        this(command, "");
    }

    protected PluginCommand(@NotNull String command, @NotNull String description) {
        this(command, "", "/" + command);
    }

    protected PluginCommand(@NotNull String command, @NotNull String description, @NotNull String usage) {
        this(command, "", "/" + command, new ArrayList<>());
    }

    protected PluginCommand(@NotNull String command, @NotNull String description, @NotNull String usage, @NotNull ArrayList<String> aliases) {
        super(command, description, usage, aliases);
        final String pluginName = FlauschigeMinecraftLibrary.getPluginName();
        this.pluginPrefix = pluginName == null ? "flauschigesalex" : pluginName.toLowerCase();

        this.command = command;
    }

    public final boolean register(final @NotNull CommandMap commandMap) {
        return super.register(commandMap);
    }

    public final boolean unregister(final @NotNull CommandMap commandMap) {
        return super.unregister(commandMap);
    }

    public final boolean isRegistered() {
        return super.isRegistered();
    }

    public final @NotNull String getName() {
        return super.getName();
    }

    public final @Deprecated boolean setName(final @NotNull String name) {
        return super.setName(name);
    }

    public final @Nullable String getPermission() {
        return super.getPermission();
    }

    public final void setPermission(final @Nullable String permission) {
        super.setPermission(permission);
    }

    /**
     * @see #permissible(CommandSender)
     * @deprecated
     */
    public final boolean testPermission(final @NotNull CommandSender permissible) {
        return this.permissible(permissible);
    }

    /**
     * @deprecated
     */
    public final boolean testPermissionSilent(final @NotNull CommandSender target) {
        return super.testPermissionSilent(target);
    }

    public final @NotNull String getLabel() {
        getLibrary();
        if (FlauschigeMinecraftLibrary.getPluginName() != null)
            return FlauschigeMinecraftLibrary.getPluginName();
        
        return super.getLabel();
    }

    public final boolean setLabel(final @NotNull String name) {
        return super.setLabel(name);
    }

    /**
     * @see #permissionMessage()
     * @deprecated
     */
    @SuppressWarnings("deprecation")
    public final @Nullable String getPermissionMessage() {
        return super.getPermissionMessage();
    }

    /**
     * @see #permissionMessage(Component)
     * @deprecated
     */
    @SuppressWarnings("deprecation")
    public final @NotNull Command setPermissionMessage(final @Nullable String permissionMessage) {
        return super.setPermissionMessage(permissionMessage);
    }

    public final @NotNull String getDescription() {
        return super.getDescription();
    }

    public final @NotNull Command setDescription(final @NotNull String description) {
        return super.setDescription(description);
    }

    public final @NotNull String getUsage() {
        return super.getUsage();
    }

    /**
     * @see #PluginCommand(String, String, String)
     */
    public final @NotNull Command setUsage(final @NotNull String usage) {
        return super.setUsage(usage);
    }

    public final @NotNull List<String> getAliases() {
        return super.getAliases();
    }

    /**
     * @see #PluginCommand(String, String, String, ArrayList)
     */
    public final @NotNull Command setAliases(final @NotNull List<String> aliases) {
        return super.setAliases(aliases);
    }

    public final @Nullable Component permissionMessage() {
        return super.permissionMessage();
    }

    public final void permissionMessagefinal(final @Nullable Component permissionMessage) {
        super.permissionMessage(permissionMessage);
    }

    public final @Deprecated boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {
        this.executeCommand(commandSender, s, strings);
        return true;
    }
    protected abstract void executeCommand(final @NotNull CommandSender sender, final @NotNull String fullCommand, final @NotNull String[] args);

    protected Set<TabComplete> tabCompletes(final CommandSender sender) {
        return TabComplete.onlinePlayers(sender instanceof Player player ? player : null);
    }

    public final @Deprecated @NotNull List<String> tabComplete(final @NotNull CommandSender sender, final @NotNull String alias, final @NotNull String[] args) throws IllegalArgumentException {
        return List.of();
    }

    public final @Deprecated @NotNull List<String> tabComplete(final @NotNull CommandSender sender, final @NotNull String alias, final @NotNull String[] args, final @Nullable Location location) throws IllegalArgumentException {
        return tabCompletes(sender).stream()
                .filter(complete -> {
                    if (complete.arg == null)
                        return true;

                    return complete.arg == args.length - 1;
                })
                .filter(complete -> {
                    final int arg = args.length-1;
                    if (arg < 0)
                        return true;

                    if (args[arg].isBlank())
                        return true;

                    return complete.completable.toLowerCase().startsWith(args[arg].toLowerCase());
                })
                .filter(complete -> {
                    if (complete.location == null || complete.location.getWorld() == null)
                        return true;

                    if (location == null || location.getWorld() == null)
                        return false;

                    if (complete.maxDistance < 0)
                        return complete.location.getWorld().equals(location.getWorld());

                    return complete.location.distance(location) <= complete.maxDistance;
                })
                .map(complete -> complete.completable).toList();
    }

    public boolean permissible(final @NotNull CommandSender permissible) {
        return super.testPermission(permissible);
    }

    public PluginCommand setPluginPrefix(final @NotNull String pluginPrefix) {
        this.pluginPrefix = pluginPrefix;
        return this;
    }

    public static final class TabComplete {
        public static Set<TabComplete> onlinePlayers() {
            return onlinePlayers(null);
        }

        public static Set<TabComplete> onlinePlayers(final @Nullable Player player) {
            return new HashSet<>(Bukkit.getOnlinePlayers().stream()
                    .filter(onlinePlayer -> {
                        if (player == null)
                            return true;

                        return player.canSee(onlinePlayer);
                    })
                    .map(onlinePlayer -> new TabComplete(onlinePlayer.getName())).toList());
        }

        private final Integer arg;
        private final String completable;

        private final Location location;
        private final double maxDistance;

        public TabComplete(final @NotNull String completable) {
            this(null, completable, null);
        }
        public TabComplete(final @Nullable Integer arg, final @NotNull String completable) {
            this(arg, completable, null);
        }
        public TabComplete(final @Nullable Integer arg, final @NotNull String completable, final @Nullable World requiredWorld) {
            this(arg, completable, new Location(requiredWorld, 0, 0, 0), -1);
        }
        public TabComplete(final @Nullable Integer arg, final @NotNull String completable, final @Nullable Location location, final @Range(from = 0, to = Long.MAX_VALUE) double maxDistance) {
            this.arg = arg;
            this.completable = completable;

            this.location = location;
            this.maxDistance = maxDistance;
        }

        static @Unmodifiable List<TabComplete> players(final int arg) {
            return Bukkit.getOnlinePlayers().stream().map(player -> new TabComplete(arg, player.getName())).toList();
        }

        public boolean equals(Object obj) {
            if (obj instanceof TabComplete complete)
                return arg == complete.arg && completable.equalsIgnoreCase(complete.completable);

            return false;
        }
    }
}
