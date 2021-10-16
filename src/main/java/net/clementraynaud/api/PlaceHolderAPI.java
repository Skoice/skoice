package net.clementraynaud.api;

import net.clementraynaud.Bot;
import net.clementraynaud.Skoice;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PlaceHolderAPI extends PlaceholderExpansion {
    private final Bot plugin;
    public PlaceHolderAPI(Bot plugin){
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "Skoice";
    }
    @Override
    public String getAuthor() {
        return "SkoiceAuthors";
    }
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    @Override
    public String onPlaceholderRequest(Player P1, String identifier){
        if(P1 == null) return null;

        if(identifier.equalsIgnoreCase("isPlayerInVC")){
            return plugin.isPlayerInVC(P1.getUniqueId()).toString();
        }

        return null;
    }
}