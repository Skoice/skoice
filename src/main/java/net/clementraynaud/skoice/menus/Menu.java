package net.clementraynaud.skoice.menus;

import net.clementraynaud.skoice.bot.Bot;
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.config.ConfigField;
import net.clementraynaud.skoice.lang.LangFile;
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

public class Menu {

    public static final String CLOSE_BUTTON_ID = "close";

    public static boolean customizeRadius;

    private final String name;
    private final MenuEmoji emoji;
    private final MenuType type;
    private final MenuStyle style;
    private final String parent;
    private final List<String> fields;
    private SelectMenu selectMenu;

    public Menu(ConfigurationSection menu) {
        this.name = menu.getName();
        this.emoji = MenuEmoji.valueOf(menu.getString("emoji").toUpperCase());
        this.type = MenuType.valueOf(menu.getString("type").toUpperCase());
        this.style = menu.contains("style") ? MenuStyle.valueOf(menu.getString("style").toUpperCase()) : null;
        this.parent = menu.contains("parent") ? menu.getString("parent") : null;
        this.fields = menu.getStringList("fields");
    }

    public Message toMessage(Config config, LangFile lang, Bot bot) {
        return new MessageBuilder().setEmbeds(this.getEmbed(lang, bot)).setActionRows(this.getActionRows(config, lang, bot)).build();
    }

    private String getTitle(LangFile lang, boolean withEmoji) {
        return withEmoji ? this.emoji + lang.getMessage("discord.menu." + this.name + ".title") :
                lang.getMessage("discord.menu." + this.name + ".title");
    }

    private String getDescription(LangFile lang, Bot bot, boolean shortened) {
        if (!bot.isReady() && lang.contains("discord.menu." + this.name + ".alternative-description")) {
            return lang.getMessage("discord.menu." + this.name + ".alternative-description");
        } else if (shortened && lang.contains("discord.menu." + this.name + ".shortened-description")) {
            return lang.getMessage("discord.menu." + this.name + ".shortened-description");
        } else if (lang.contains("discord.menu." + this.name + ".description")) {
            return lang.getMessage("discord.menu." + this.name + ".description");
        }
        return null;
    }

    private MessageEmbed getEmbed(LangFile lang, Bot bot) {
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
        if (this.fields != null) {
            for (String field : this.fields) {
                embed.addField(bot.getFields().get(field).toField(lang));
            }
        }
        return embed.build();
    }

    private List<ActionRow> getActionRows(Config config, LangFile lang, Bot bot) {
        switch (this.name) {
            case "server":
                this.selectMenu = new ServerSelectMenu(lang, bot);
                break;
            case "lobby":
                this.selectMenu = new LobbySelectMenu(config, lang, bot);
                break;
            case "mode":
                this.selectMenu = new ModeSelectMenu(config, lang, bot);
                break;
            case "language":
                this.selectMenu = new LanguageSelectMenu(config, lang, bot);
                break;
            case "action-bar-alert":
                this.selectMenu = new ToggleSelectMenu(lang, this.name, config.getFile().getBoolean(ConfigField.ACTION_BAR_ALERT.get()), true);
                break;
            case "channel-visibility":
                this.selectMenu = new ToggleSelectMenu(lang, this.name, config.getFile().getBoolean(ConfigField.CHANNEL_VISIBILITY.get()), false);
                break;
            default:
                return Collections.singletonList(ActionRow.of(this.getButtons(config, lang, bot)));
        }
        return Arrays.asList(ActionRow.of(this.selectMenu.get()), ActionRow.of(this.getButtons(config, lang, bot)));
    }

    private List<Button> getButtons(Config config, LangFile lang, Bot bot) {
        List<Button> buttons = new ArrayList<>();
        if (this.parent != null) {
            buttons.add(Button.secondary(this.parent, "← " + lang.getMessage("discord.button-label.back")));
        }
        if (this.selectMenu != null && this.selectMenu.isRefreshable()) {
            buttons.add(Button.primary(this.name, "⟳ " + lang.getMessage("discord.button-label.refresh")));
        }
        if ("mode".equals(this.name)) {
            buttons.addAll(this.getModeAdditionalButtons(config, lang, bot));
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
        this.customizeRadius = false;
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
        return buttons;
    }

    private List<Button> getModeAdditionalButtons(Config config, LangFile lang, Bot bot) {
        if (this.isModeCustomizable(config, bot)) {
            Menu horizontalRadiusMenu = bot.getMenus().get("horizontal-radius");
            Menu verticalRadiusMenu = bot.getMenus().get("vertical-radius");
            return Arrays.asList(Button.primary(horizontalRadiusMenu.name, horizontalRadiusMenu.getTitle(lang, false))
                            .withEmoji(horizontalRadiusMenu.emoji.getEmojiFromUnicode()),
                    Button.primary(verticalRadiusMenu.name, verticalRadiusMenu.getTitle(lang, false))
                            .withEmoji(verticalRadiusMenu.emoji.getEmojiFromUnicode()));
        }
        return Collections.emptyList();
    }

    private boolean isModeCustomizable(Config config, Bot bot) {
        return bot.isReady() &&
                (this.customizeRadius
                        || (config.getFile().getInt(ConfigField.HORIZONTAL_RADIUS.get()) != 80
                        && config.getFile().getInt(ConfigField.HORIZONTAL_RADIUS.get()) != 40)
                        || (config.getFile().getInt(ConfigField.VERTICAL_RADIUS.get()) != 40
                        && config.getFile().getInt(ConfigField.VERTICAL_RADIUS.get()) != 20));
    }
}
