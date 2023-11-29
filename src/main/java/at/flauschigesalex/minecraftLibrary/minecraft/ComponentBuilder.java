package at.flauschigesalex.minecraftLibrary.minecraft;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class ComponentBuilder {

    Component component = Component.empty().decoration(TextDecoration.ITALIC, false);

    /**
     * Clears previous component
     */
    public ComponentBuilder clear() {
        component = Component.empty().decoration(TextDecoration.ITALIC, false);
        return this;
    }
    /**
     * Overrides previous component
     */
    public ComponentBuilder setComponent(Component component) {
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
    public ComponentBuilder setText(List<Component> components) {
        this.clear();
        this.appendComponents(components);
        return this;
    }

    public ComponentBuilder appendComponents(List<Component> components) {
        components.forEach(this::appendComponents);
        return this;
    }
    public ComponentBuilder appendComponents(ComponentLike... component) {
        for (ComponentLike componentLike : component) {
            this.component = this.component.append(componentLike);
        }
        return this;
    }
    public ComponentBuilder appendText(String... text) {
        for (String textLike : text) {
            this.component = this.component.append(Component.text(textLike));
        }
        return this;
    }

    public final TextComponent asTextComponent() {
        return (TextComponent) component;
    }
    public final Component asComponent() {
        return component;
    }
    public final ArrayList<Component> asComponentList() {
        ArrayList<Component> components = new ArrayList<>(deconstruct(component));

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

            finalComponents.set(finalComponentInt, finalComponents.get(finalComponentInt).append(finalComponent));
        }

        return finalComponents;
    }
    final ArrayList<Component> deconstruct(Component component) {
        ArrayList<Component> list = new ArrayList<>();
        if (!component.children().isEmpty()) {
            Component withOutChildren = component.children(new ArrayList<>());
            list.add(withOutChildren);
            for (Component child : component.children()) {
                list.addAll(deconstruct(child));
            }
        } else {
            list.add(component.decoration(TextDecoration.ITALIC, false));
        }

        return list;
    }
}
