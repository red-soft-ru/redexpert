package org.underworldlabs.swing.listener;

import java.awt.*;
import java.util.List;

public class DefaultTraversalPolicy extends FocusTraversalPolicy {
    private final List<Component> componentOrder;

    public DefaultTraversalPolicy(List<Component> componentOrder) {
        this.componentOrder = componentOrder;
    }

    @Override
    public Component getComponentAfter(final Container focusCycleRoot, final Component aComponent) {
        int newIndex = (componentOrder.indexOf(aComponent) + 1) % componentOrder.size();

        Component component = componentOrder.get(newIndex);
        if (component.isEnabled() && component.isVisible())
            return component;

        return getComponentAfter(focusCycleRoot, component);
    }

    @Override
    public Component getComponentBefore(final Container focusCycleRoot, final Component aComponent) {
        int currentIndex = componentOrder.indexOf(aComponent);
        int newIndex = currentIndex > 0 ? currentIndex - 1 : componentOrder.size() - 1;

        Component component = componentOrder.get(newIndex);
        if (component.isEnabled() && component.isVisible())
            return component;

        return getComponentBefore(focusCycleRoot, component);
    }

    @Override
    public Component getFirstComponent(final Container focusCycleRoot) {
        return componentOrder.get(0);
    }

    @Override
    public Component getLastComponent(final Container focusCycleRoot) {
        return componentOrder.get(componentOrder.size() - 1);
    }

    @Override
    public Component getDefaultComponent(final Container focusCycleRoot) {
        return getFirstComponent(focusCycleRoot);
    }

}
