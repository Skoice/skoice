package net.clementraynaud.bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class InviteCommand extends ListenerAdapter {

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if (event.getName().equals("invite")) {
            EmbedBuilder embed = new EmbedBuilder().setTitle(":envelope: Get the proximity voice chat")
                    .addField(":inbox_tray: Download Skoice", "Get the plugin [here](https://www.spigotmc.org/resources/skoice-proximity-voice-chat.82861/) and put it on your Minecraft server.", false)
                    .addField(":screwdriver: Troubleshooting", "Having issues? [Join our Discord server!](https://discord.gg/h3Tgccc)", false)
                    .setColor(Color.ORANGE);
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        }
    }
}
