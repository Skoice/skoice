package net.clementraynaud.skoice.menus.selectmenus;

import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

public abstract class SelectMenu {

    private final boolean isRefreshable;

    protected SelectMenu(boolean isRefreshable) {
        this.isRefreshable = isRefreshable;
    }

    public boolean isRefreshable() {
        return this.isRefreshable;
    }

    public abstract SelectionMenu get();
}
