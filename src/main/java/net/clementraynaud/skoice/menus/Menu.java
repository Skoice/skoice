package net.clementraynaud.skoice.menus;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.config.ConfigurationField;
import net.clementraynaud.skoice.menus.selectmenus.LanguageSelectMenu;
import net.clementraynaud.skoice.menus.selectmenus.LobbySelectMenu;
import net.clementraynaud.skoice.menus.selectmenus.ModeSelectMenu;
import net.clementraynaud.skoice.menus.selectmenus.SelectMenu;
import net.clementraynaud.skoice.menus.selectmenus.ServerSelectMenu;
import net.clementraynaud.skoice.menus.selectmenus.ToggleSelectMenu;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Menu {

    public static final String CLOSE_BUTTON_ID = "close";

    private final Skoice plugin;
    private final String name;
    private final MenuEmoji emoji;
    private final MenuType type;
    private final MenuStyle style;
    private final String parent;
    private final Set<MenuField> fields;
    private SelectMenu selectMenu;

    public Menu(Skoice plugin, String path, Set<MenuField> fields) {
        this.plugin = plugin;
        ConfigurationSection menu = this.plugin.getBot().getMenusYaml().getConfigurationSection(path);
        this.name = menu.getName();
        this.emoji = MenuEmoji.valueOf(menu.getString("emoji").toUpperCase());
        this.type = menu.contains("type") ? MenuType.valueOf(menu.getString("type").toUpperCase()) : null;
        this.style = menu.contains("style") ? MenuStyle.valueOf(menu.getString("style").toUpperCase()) : null;
        this.parent = menu.contains("parent") ? menu.getString("parent") : null;
        this.fields = fields;
    }

    public Menu(Skoice plugin, String path, Set<MenuField> fields, MenuType type) {
        this.plugin = plugin;
        ConfigurationSection menu = this.plugin.getBot().getMenusYaml().getConfigurationSection(path);
        this.name = menu.getName();
        this.emoji = MenuEmoji.valueOf(menu.getString("emoji").toUpperCase());
        this.type = type;
        this.style = menu.contains("style") ? MenuStyle.valueOf(menu.getString("style").toUpperCase()) : null;
        this.parent = menu.contains("parent") ? menu.getString("parent") : null;
        this.fields = fields;
    }

    public Message toMessage(boolean customizeRadius) {
        return new MessageBuilder().setEmbeds(this.getEmbed())
                .setActionRows(this.getActionRows(customizeRadius)).build();
    }

    public Message toMessage() {
        return this.toMessage(false);
    }

    private String getTitle(boolean withEmoji) {
        return withEmoji ? this.emoji + this.plugin.getLang().getMessage("discord.menu." + this.name + ".title") :
                this.plugin.getLang().getMessage("discord.menu." + this.name + ".title");
    }

    private String getDescription(boolean shortened) {
        if (!this.plugin.getBot().isReady() && this.plugin.getLang().contains("discord.menu." + this.name + ".alternative-description")) {
            return this.plugin.getLang().getMessage("discord.menu." + this.name + ".alternative-description");
        } else if (shortened && this.plugin.getLang().contains("discord.menu." + this.name + ".shortened-description")) {
            return this.plugin.getLang().getMessage("discord.menu." + this.name + ".shortened-description");
        } else if (this.plugin.getLang().contains("discord.menu." + this.name + ".description")) {
            return this.plugin.getLang().getMessage("discord.menu." + this.name + ".description");
        }
        return null;
    }

    private MessageEmbed getEmbed() {
        EmbedBuilder embed = new EmbedBuilder().setTitle(this.getTitle(true))
                .setColor(this.type.getColor())
                .setFooter(this.plugin.getLang().getMessage("discord.menu.footer"), "https://www.spigotmc.org/data/resource_icons/82/82861.jpg?1597701409");
        if (this.getDescription(false) != null) {
            embed.setDescription(this.getDescription(false));
        }
        if (this.plugin.getBot().isReady()) {
            StringBuilder author = new StringBuilder();
            String parentMenu = this.parent;
            while (parentMenu != null) {
                Menu menuParent = this.plugin.getBot().getMenus().get(parentMenu);
                author.insert(0, menuParent.getTitle(false) + " › ");
                parentMenu = menuParent.parent;
            }
            embed.setAuthor(author.toString());
        }
        if (!"mode".equals(this.name)) {
            for (Menu menu : this.plugin.getBot().getMenus().values()) {
                if (menu.parent != null && menu.parent.equals(this.name)) {
                    embed.addField(menu.getTitle(true), menu.getDescription(true), true);
                }
            }
        }
        for (MenuField field : this.fields) {
            embed.addField(field.toField());
        }
        return embed.build();
    }

    private List<ActionRow> getActionRows(boolean customizeRadius) {
        switch (this.name) {
            case "server":
                this.selectMenu = new ServerSelectMenu(this.plugin);
                break;
            case "lobby":
                this.selectMenu = new LobbySelectMenu(this.plugin);
                break;
            case "mode":
                this.selectMenu = new ModeSelectMenu(this.plugin, customizeRadius);
                break;
            case "language":
                this.selectMenu = new LanguageSelectMenu(this.plugin);
                break;
            case "action-bar-alert":
                this.selectMenu = new ToggleSelectMenu(this.plugin.getLang(), this.name, this.plugin.getConfiguration().getFile().getBoolean(ConfigurationField.ACTION_BAR_ALERT.toString()), true);
                break;
            case "channel-visibility":
                this.selectMenu = new ToggleSelectMenu(this.plugin.getLang(), this.name, this.plugin.getConfiguration().getFile().getBoolean(ConfigurationField.CHANNEL_VISIBILITY.toString()), false);
                break;
            default:
                List<Button> buttons = this.getButtons(customizeRadius);
                if (!buttons.isEmpty()) {
                    return Collections.singletonList(ActionRow.of(buttons));
                }
                return Collections.emptyList();
        }
        return Arrays.asList(ActionRow.of(this.selectMenu.get()), ActionRow.of(this.getButtons(customizeRadius)));
    }

    private List<Button> getButtons(boolean customizeRadius) {
        List<Button> buttons = new ArrayList<>();
        if (this.parent != null) {
            buttons.add(Button.secondary(this.parent, "← " + this.plugin.getLang().getMessage("discord.button-label.back")));
        }
        if (this.selectMenu != null && this.selectMenu.isRefreshable()) {
            buttons.add(Button.primary(this.name, "⟳ " + this.plugin.getLang().getMessage("discord.button-label.refresh")));
        }
        buttons.addAll(this.getAdditionalButtons(customizeRadius));
        if (!"mode".equals(this.name)) {
            for (Menu menu : this.plugin.getBot().getMenus().values()) {
                if (menu.parent != null && menu.parent.equals(this.name)) {
                    buttons.add(menu.style == MenuStyle.PRIMARY
                            ? Button.primary(menu.name, menu.getTitle(false))
                            .withEmoji(menu.emoji.get())
                            : Button.secondary(menu.name, menu.getTitle(false))
                            .withEmoji(menu.emoji.get()));
                }
            }
        }
        if (this.type == MenuType.DEFAULT) {
            if (this.plugin.getBot().isReady()) {
                buttons.add(Button.danger(Menu.CLOSE_BUTTON_ID, this.plugin.getLang().getMessage("discord.button-label.close"))
                        .withEmoji(MenuEmoji.HEAVY_MULTIPLICATION_X.get()));
            } else {
                if (!"language".equals(this.name)) {
                    Menu languageMenu = this.plugin.getBot().getMenus().get("language");
                    buttons.add(Button.secondary(languageMenu.name, languageMenu.getTitle(false))
                            .withEmoji(MenuEmoji.GLOBE_WITH_MERIDIANS.get()));
                }
                buttons.add(Button.secondary(Menu.CLOSE_BUTTON_ID, this.plugin.getLang().getMessage("discord.button-label.configure-later"))
                        .withEmoji(MenuEmoji.CLOCK3.get()));
            }
        }
        return buttons;
    }

    private List<Button> getAdditionalButtons(boolean customizeRadius) {
        if ("empty-configuration".equals(this.name) && this.type == MenuType.ERROR) {
            return Collections.singletonList(Button.primary("resume-configuration", "Resume Configuration")
                    .withEmoji(MenuEmoji.ARROW_FORWARD.get()));
        } else if ("permissions".equals(this.name)) {
            return Collections.singletonList(Button.link("https://discord.com/api/oauth2/authorize?client_id="
                    + this.plugin.getBot().getJda().getSelfUser().getApplicationId()
                    + "&permissions=8&scope=bot%20applications.commands", "Update Permissions")
                    .withEmoji(this.emoji.get()));
        } else if ("mode".equals(this.name) && this.isModeCustomizable(customizeRadius)) {
            Menu horizontalRadiusMenu = this.plugin.getBot().getMenus().get("horizontal-radius");
            Menu verticalRadiusMenu = this.plugin.getBot().getMenus().get("vertical-radius");
            return Arrays.asList(Button.primary(horizontalRadiusMenu.name, horizontalRadiusMenu.getTitle(false))
                            .withEmoji(horizontalRadiusMenu.emoji.get()),
                    Button.primary(verticalRadiusMenu.name, verticalRadiusMenu.getTitle(false))
                            .withEmoji(verticalRadiusMenu.emoji.get()));
        }
        return Collections.emptyList();
    }

    private boolean isModeCustomizable(boolean customizeRadius) {
        return this.plugin.getBot().isReady() &&
                (customizeRadius
                        || (this.plugin.getConfiguration().getFile().getInt(ConfigurationField.HORIZONTAL_RADIUS.toString()) != 80
                        && this.plugin.getConfiguration().getFile().getInt(ConfigurationField.HORIZONTAL_RADIUS.toString()) != 40)
                        || (this.plugin.getConfiguration().getFile().getInt(ConfigurationField.VERTICAL_RADIUS.toString()) != 40
                        && this.plugin.getConfiguration().getFile().getInt(ConfigurationField.VERTICAL_RADIUS.toString()) != 20));
    }
}
