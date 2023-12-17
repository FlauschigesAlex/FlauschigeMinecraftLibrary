package at.flauschigesalex.minecraftLibrary.minecraft;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class ComponentBuilder {

    public static ComponentBuilder builder() {
        return new ComponentBuilder();
    }
    public static ComponentBuilder builder(@NotNull Component component, final @NotNull Component... moreComponents) {
        for (final ComponentLike moreComponent : moreComponents) {
            component = component.append(moreComponent);
        }
        return new ComponentBuilder(component);
    }

    Component component = Component.empty().decoration(TextDecoration.ITALIC, false);

    private ComponentBuilder() {
    }
    private ComponentBuilder(Component component) {
        this.component = component;
    }

    public final TextComponent asTextComponent() {
        return (TextComponent) component;
    }

    public final Component asComponent() {
        return component;
    }

    public final ArrayList<Component> asComponentList(final boolean useItalic) {
        ArrayList<Component> components = new ArrayList<>(decompile(component));

        ArrayList<Component> finalComponents = new ArrayList<>();
        int finalComponentInt = 0;
        for (Component finalComponent : components) {
            if (finalComponent.toString().contains("content=\"\\n\"")) {
                finalComponents.add(Component.empty());
                finalComponentInt++;
                continue;
            }

            if (finalComponents.size() == finalComponentInt) {
                finalComponents.add(finalComponent);
                continue;
            }

            if (!useItalic)
                finalComponent = finalComponent.decoration(TextDecoration.ITALIC, false);
            finalComponents.set(finalComponentInt, finalComponents.get(finalComponentInt).append(finalComponent));
        }

        return finalComponents;
    }

    /**
     * Clears previous component
     */
    public ComponentBuilder clear() {
        component = Component.empty().decoration(TextDecoration.ITALIC, false);
        return this;
    }

    public ComponentBuilder appendComponents(final @NotNull List<Component> components) {
        components.forEach(this::appendComponents);
        return this;
    }

    public ComponentBuilder appendComponents(final @NotNull ComponentLike... component) {
        for (ComponentLike componentLike : component) {
            this.component = this.component.append(componentLike);
        }
        return this;
    }

    public ComponentBuilder appendText(final @NotNull String... text) {
        for (String textLike : text) {
            this.component = this.component.append(Component.text(textLike));
        }
        return this;
    }

    final ArrayList<Component> decompile(final @NotNull Component component) {
        ArrayList<Component> list = new ArrayList<>();
        if (!component.children().isEmpty()) {
            Component withOutChildren = component.children(new ArrayList<>());
            list.add(withOutChildren);
            for (Component child : component.children()) {
                list.addAll(decompile(child));
            }
        } else {
            list.add(component);
        }

        return list;
    }

    /**
     * Overrides previous component
     */
    public ComponentBuilder setComponent(final @NotNull Component component) {
        this.component = component;
        return this;
    }

    /**
     * Overrides previous component
     */
    public ComponentBuilder setText(String text) {
        this.component = Component.text(text);
        return this;
    }

    /**
     * Overrides previous component
     */
    public ComponentBuilder setText(final @NotNull List<Component> components) {
        this.clear();
        this.appendComponents(components);
        return this;
    }
}
