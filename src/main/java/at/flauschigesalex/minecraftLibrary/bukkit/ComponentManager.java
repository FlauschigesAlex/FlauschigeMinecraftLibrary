package at.flauschigesalex.minecraftLibrary.bukkit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public final class ComponentManager {
    public static ArrayList<Component> spliterator(@NotNull Component component) {
        final ArrayList<Component> list = new ArrayList<>();
        if (!component.children().isEmpty()) {
            final Component withOutChildren = component.children(new ArrayList<>());
            list.add(withOutChildren);

            for (final Component child : component.children())
                list.addAll(spliterator(child));

            return list;
        }

        component = component.replaceText(TextReplacementConfig.builder().match("\\n").replacement("").build());
        list.add(component);
        return list;
    }
}
