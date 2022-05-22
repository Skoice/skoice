package net.clementraynaud.skoice.menus;

import net.clementraynaud.skoice.bot.Bot;
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.config.ConfigField;
import net.clementraynaud.skoice.lang.Lang;
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

    private final String name;
    private final MenuEmoji emoji;
    private final MenuType type;
    private final MenuStyle style;
    private final String parent;
    private final Set<MessageEmbed.Field> fields;
    private SelectMenu selectMenu;

    public Menu(ConfigurationSection menu, Set<MessageEmbed.Field> fields) {
        this.name = menu.getName();
        this.emoji = MenuEmoji.valueOf(menu.getString("emoji").toUpperCase());
        this.type = menu.contains("type") ? MenuType.valueOf(menu.getString("type").toUpperCase()) : null;
        this.style = menu.contains("style") ? MenuStyle.valueOf(menu.getString("style").toUpperCase()) : null;
        this.parent = menu.contains("parent") ? menu.getString("parent") : null;
        this.fields = fields;
    }

    public Menu(ConfigurationSection menu, Set<MessageEmbed.Field> fields, MenuType type) {
        this.name = menu.getName();
        this.emoji = MenuEmoji.valueOf(menu.getString("emoji").toUpperCase());
        this.type = type;
        this.style = menu.contains("style") ? MenuStyle.valueOf(menu.getString("style").toUpperCase()) : null;
        this.parent = menu.contains("parent") ? menu.getString("parent") : null;
        this.fields = fields;
    }

    public Message toMessage(Config config, Lang lang, Bot bot, boolean customizeRadius) {
        return new MessageBuilder().setEmbeds(this.getEmbed(lang, bot))
                .setActionRows(this.getActionRows(config, lang, bot, customizeRadius)).build();
    }

    public Message toMessage(Config config, Lang lang, Bot bot) {
        return this.toMessage(config, lang, bot, false);
    }

    private String getTitle(Lang lang, boolean withEmoji) {
        return withEmoji ? this.emoji + lang.getMessage("discord.menu." + this.name + ".title") :
                lang.getMessage("discord.menu." + this.name + ".title");
    }

    private String getDescription(Lang lang, Bot bot, boolean shortened) {
        if (!bot.isReady() && lang.contains("discord.menu." + this.name + ".alternative-description")) {
            return lang.getMessage("discord.menu." + this.name + ".alternative-description");
        } else if (shortened && lang.contains("discord.menu." + this.name + ".shortened-description")) {
            return lang.getMessage("discord.menu." + this.name + ".shortened-description");
        } else if (lang.contains("discord.menu." + this.name + ".description")) {
            return lang.getMessage("discord.menu." + this.name + ".description");
        }
        return null;
    }

    private MessageEmbed getEmbed(Lang lang, Bot bot) {
        EmbedBuilder embed = new EmbedBuilder().setTitle(this.getTitle(lang, true))
                .setColor(this.type.getColor())
                .setFooter(lang.getMessage("discord.menu.footer"), "https://www.spigotmc.org/data/resource_icons/82/82861.jpg?1597701409");
        if (this.getDescription(lang, bot, false) != null) {
            embed.setDescription(this.getDescription(lang, bot, false));
        }
        if (bot.isReady()) {
            StringBuilder author = new StringBuilder();
            String parentMenu = this.parent;
            while (parentMenu != null) {
                Menu menuParent = bot.getMenus().get(parentMenu);
                author.insert(0, menuParent.getTitle(lang, false) + " › ");
                parentMenu = menuParent.parent;
            }
            embed.setAuthor(author.toString());
        }
        if (!"mode".equals(this.name)) {
            for (Menu menu : bot.getMenus().values()) {
                if (menu.parent != null && menu.parent.equals(this.name)) {
                    embed.addField(menu.getTitle(lang, true), menu.getDescription(lang, bot, true), true);
                }
            }
        }
        for (MessageEmbed.Field field : this.fields) {
            embed.addField(field);
        }
        return embed.build();
    }

    private List<ActionRow> getActionRows(Config config, Lang lang, Bot bot, boolean customizeRadius) {
        switch (this.name) {
            case "server":
                this.selectMenu = new ServerSelectMenu(lang, bot);
                break;
            case "lobby":
                this.selectMenu = new LobbySelectMenu(lang, config, bot);
                break;
            case "mode":
                this.selectMenu = new ModeSelectMenu(lang, config, bot, customizeRadius);
                break;
            case "language":
                this.selectMenu = new LanguageSelectMenu(lang, config, bot);
                break;
            case "action-bar-alert":
                this.selectMenu = new ToggleSelectMenu(lang, this.name, config.getFile().getBoolean(ConfigField.ACTION_BAR_ALERT.get()), true);
                break;
            case "channel-visibility":
                this.selectMenu = new ToggleSelectMenu(lang, this.name, config.getFile().getBoolean(ConfigField.CHANNEL_VISIBILITY.get()), false);
                break;
            default:
                List<Button> buttons = this.getButtons(config, lang, bot, customizeRadius);
                if (!buttons.isEmpty()) {
                    return Collections.singletonList(ActionRow.of(buttons));
                }
                return Collections.emptyList();
        }
        return Arrays.asList(ActionRow.of(this.selectMenu.get()), ActionRow.of(this.getButtons(config, lang, bot, customizeRadius)));
    }

    private List<Button> getButtons(Config config, Lang lang, Bot bot, boolean customizeRadius) {
        List<Button> buttons = new ArrayList<>();
        if (this.parent != null) {
            buttons.add(Button.secondary(this.parent, "← " + lang.getMessage("discord.button-label.back")));
        }
        if (this.selectMenu != null && this.selectMenu.isRefreshable()) {
            buttons.add(Button.primary(this.name, "⟳ " + lang.getMessage("discord.button-label.refresh")));
        }
        if ("mode".equals(this.name)) {
            buttons.addAll(this.getModeAdditionalButtons(config, lang, bot, customizeRadius));
        } else {
            for (Menu menu : bot.getMenus().values()) {
                if (menu.parent != null && menu.parent.equals(this.name)) {
                    buttons.add(menu.style == MenuStyle.PRIMARY
                            ? Button.primary(menu.name, menu.getTitle(lang, false))
                            .withEmoji(menu.emoji.getEmojiFromUnicode())
                            : Button.secondary(menu.name, menu.getTitle(lang, false))
                            .withEmoji(menu.emoji.getEmojiFromUnicode()));
                }
            }
        }
        if (this.type == MenuType.DEFAULT) {
            if (bot.isReady()) {
                buttons.add(Button.danger(Menu.CLOSE_BUTTON_ID, lang.getMessage("discord.button-label.close"))
                        .withEmoji(MenuEmoji.HEAVY_MULTIPLICATION_X.getEmojiFromUnicode()));
            } else {
                Menu languageMenu = bot.getMenus().get("language");
                buttons.addAll(Arrays.asList(Button.secondary(languageMenu.name, languageMenu.getTitle(lang, false))
                                .withEmoji(MenuEmoji.GLOBE_WITH_MERIDIANS.getEmojiFromUnicode()),
                        Button.secondary(Menu.CLOSE_BUTTON_ID, lang.getMessage("discord.button-label.configure-later"))
                                .withEmoji(MenuEmoji.CLOCK3.getEmojiFromUnicode())));
            }
        }
        return buttons;
    }

    private List<Button> getModeAdditionalButtons(Config config, Lang lang, Bot bot, boolean customizeRadius) {
        if (this.isModeCustomizable(config, bot, customizeRadius)) {
            Menu horizontalRadiusMenu = bot.getMenus().get("horizontal-radius");
            Menu verticalRadiusMenu = bot.getMenus().get("vertical-radius");
            return Arrays.asList(Button.primary(horizontalRadiusMenu.name, horizontalRadiusMenu.getTitle(lang, false))
                            .withEmoji(horizontalRadiusMenu.emoji.getEmojiFromUnicode()),
                    Button.primary(verticalRadiusMenu.name, verticalRadiusMenu.getTitle(lang, false))
                            .withEmoji(verticalRadiusMenu.emoji.getEmojiFromUnicode()));
        }
        return Collections.emptyList();
    }

    private boolean isModeCustomizable(Config config, Bot bot, boolean customizeRadius) {
        return bot.isReady() &&
                (customizeRadius
                        || (config.getFile().getInt(ConfigField.HORIZONTAL_RADIUS.get()) != 80
                        && config.getFile().getInt(ConfigField.HORIZONTAL_RADIUS.get()) != 40)
                        || (config.getFile().getInt(ConfigField.VERTICAL_RADIUS.get()) != 40
                        && config.getFile().getInt(ConfigField.VERTICAL_RADIUS.get()) != 20));
    }
}
