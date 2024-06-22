package at.flauschigesalex.minecraftLibrary.bukkit;

import at.flauschigesalex.minecraftLibrary.FlauschigeMinecraftLibrary;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

import static at.flauschigesalex.minecraftLibrary.FlauschigeMinecraftLibrary.getLibrary;

@SuppressWarnings("unused")
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
        this.pluginPrefix = pluginName == null ? "flauschigesalex" : pluginName;

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

    @Deprecated
    public final @NotNull String getTimingName() {
        return super.getTimingName();
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
        if (FlauschigeMinecraftLibrary.getPluginName() != null) {
            getLibrary();
            return FlauschigeMinecraftLibrary.getPluginName();
        } else {
            return super.getLabel();
        }
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

    public @NotNull List<String> tabComplete(final @NotNull CommandSender sender, final @NotNull String alias, final @NotNull String[] args) throws IllegalArgumentException {
        return new ArrayList<>();
    }

    public @NotNull List<String> tabComplete(final @NotNull CommandSender sender, final @NotNull String alias, final @NotNull String[] args, final @Nullable Location location) throws IllegalArgumentException {
        return tabComplete(sender, alias, args);
    }

    public boolean permissible(final @NotNull CommandSender permissible) {
        return super.testPermission(permissible);
    }

    public PluginCommand setPluginPrefix(final @NotNull String pluginPrefix) {
        this.pluginPrefix = pluginPrefix;
        return this;
    }
}
