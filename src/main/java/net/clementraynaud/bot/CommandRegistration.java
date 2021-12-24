package net.clementraynaud.bot;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandRegistration extends ListenerAdapter {

    public static void registerCommands(Guild guild) {
        if (guild.retrieveCommands().complete().size() < 4) {
            guild.upsertCommand("configure", "Configure Skoice.").queue();
            guild.upsertCommand("link", "Link your Discord account to Minecraft.").queue();
            guild.upsertCommand("unlink", "Unlink your Discord account from Minecraft.").queue();
            guild.upsertCommand("invite", "Get the proximity voice chat on your server.").queue();
        }
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        registerCommands(event.getGuild());
    }
}
