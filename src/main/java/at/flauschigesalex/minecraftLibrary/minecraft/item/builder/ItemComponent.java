package at.flauschigesalex.minecraftLibrary.minecraft.item.builder;

import at.flauschigesalex.minecraftLibrary.minecraft.ComponentBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import java.util.List;

@SuppressWarnings("unused")
public class ItemComponent extends ComponentBuilder {

    public static ItemComponent fromText(String... text) {
        return new ItemComponent().appendText(text);
    }
    public static ItemComponent fromComponent(Component... component) {
        return new ItemComponent().appendComponents(component);
    }

    protected ItemComponent() {}

    /**
     * Clears previous component
     */
    @Override
    public ItemComponent clear() {
        super.clear();
        return this;
    }
    /**
     * Overrides previous component
     */
    @Override
    public ItemComponent setComponent(Component component) {
        super.setComponent(component);
        return this;
    }
    /**
     * Overrides previous component
     */
    @Override
    public ItemComponent setText(String text) {
        super.setText(text);
        return this;
    }
    /**
     * Overrides previous component
     */
    @Override
    public ItemComponent setText(List<Component> components) {
        super.setText(components);
        return this;
    }

    @Override
    public ItemComponent appendComponents(List<Component> components) {
        super.appendComponents(components);
        return this;
    }

    @Override
    public ItemComponent appendComponents(ComponentLike... component) {
        super.appendComponents(component);
        return this;
    }

    @Override
    public ItemComponent appendText(String... text) {
        super.appendText(text);
        return this;
    }
}
