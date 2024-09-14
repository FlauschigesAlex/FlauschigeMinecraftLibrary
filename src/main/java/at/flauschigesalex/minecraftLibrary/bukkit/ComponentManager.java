package at.flauschigesalex.minecraftLibrary.bukkit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public final class ComponentManager {

    public static ArrayList<Component> spliterator(@NotNull Component component) {
        return spliterator(component, new ArrayList<>());
    }

    private static ArrayList<Component> spliterator(@NotNull Component component, @NotNull ArrayList<Component> list) {
        Component base = list.isEmpty() ? Component.empty() : list.getLast();
        component = component.replaceText(builder -> builder.match("<br>").replacement("<newLine>"));

        final Component withoutChild = component.children(List.of());
        if (withoutChild instanceof TextComponent textComponent && textComponent.content().contains("\n")) {
            for (final String string : textComponent.content().split("\n")) {
                if (!string.isBlank() && !string.isEmpty())
                    base = base.append(Component.text(string).style(textComponent.style()));

                list.add(base);
                base = Component.empty();
            }
        } else if (withoutChild.equals(Component.newline())) {
            list.add(base);
            base = Component.empty();
        } else base = base.append(withoutChild);

        for (final Component child : component.children())
            spliterator(child, list);

        if (!base.equals(Component.empty()))
            list.add(base);

        return list;
    }

    public static Component compromiser(final List<Component> list) {
        if (list == null || list.isEmpty())
            return Component.empty();

        Component base = list.getFirst();
        for (int i = 1; i < list.size(); i++) {
            base = base.append(list.get(i));
        }

        return base;
    }
}
