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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.clementraynaud.util.DataGetters.getActionBarAlert;

public class ActionBarAlertConfiguration {

    public static Message getActionBarAlertConfigurationMessage() {
        EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: Configuration")
                .setColor(Color.ORANGE)
                .addField(":exclamation: Action Bar Alert", "If this option is enabled, players who are moving away and are soon to be disconnected from their current voice channel will receive an alert in their action bar.", false);
        List<ActionRow> actionRows = new ArrayList<>();
        actionRows.add(ActionRow.of(SelectionMenu.create("action-bar-alert")
                .addOptions(SelectOption.of("Enabled", "true")
                        .withDescription("This option is selected by default.").withEmoji(Emoji.fromUnicode("U+2714")),
                        SelectOption.of("Disabled", "false").withEmoji(Emoji.fromUnicode("U+2716")))
                .setDefaultValues(Collections.singleton(String.valueOf(getActionBarAlert()))).build()));
        actionRows.add(ActionRow.of(Button.secondary("advanced-settings", "← Back"), Button.danger("close", "Close").withEmoji(Emoji.fromUnicode("U+2716"))));
        return new MessageBuilder().setEmbeds(embed.build())
                .setActionRows(actionRows).build();
    }
}
