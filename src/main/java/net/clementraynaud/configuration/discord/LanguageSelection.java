package net.clementraynaud.configuration.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.awt.*;
import java.util.*;
import java.util.List;

import static net.clementraynaud.Skoice.getPlugin;
import static net.clementraynaud.util.DataGetters.getLanguage;

public class LanguageSelection {

    public static Message getLanguageSelectionMessage() {
        EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: Configuration")
                .setColor(Color.ORANGE)
                .addField(":globe_with_meridians: Language", "This is the language that will be used to display messages.", false);
        List<SelectOption> options = new ArrayList<>();
        options.add(SelectOption.of("English", "EN")
                .withDescription("This option is selected by default.").withEmoji(Emoji.fromUnicode("U+1F1ECU+1F1E7")));
        options.add(SelectOption.of("Français", "FR")
                .withEmoji(Emoji.fromUnicode("U+1F1EBU+1F1F7")));
        List<ActionRow> actionRows = new ArrayList<>();
        if (getPlugin().isBotConfigured()) {
            actionRows.add(ActionRow.of(SelectionMenu.create("languages")
                    .addOptions(options)
                    .setDefaultValues(Collections.singleton(getLanguage())).build()));
            actionRows.add(ActionRow.of(Button.secondary("settings", "← Back"),
                    Button.danger("close", "Close").withEmoji(Emoji.fromUnicode("U+2716"))));
        } else {
            actionRows.add(ActionRow.of(SelectionMenu.create("languages")
                    .setPlaceholder("Select a Language")
                    .addOptions(options).build()));
            actionRows.add(ActionRow.of(Button.secondary("close", "Configure Later").withEmoji(Emoji.fromUnicode("U+1F552"))));
        }
        return new MessageBuilder().setEmbeds(embed.build()).setActionRows(actionRows).build();
    }
}
