// Copyright 2020, 2021 Clément "carlodrift" Raynaud, rowisabeast
// Copyright 2016, 2017, 2018, 2019, 2020, 2021 Austin "Scarsz" Shapiro

// This file is part of Skoice.

// Skoice is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// Skoice is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Skoice.  If not, see <https://www.gnu.org/licenses/>.


package net.clementraynaud.api;

import net.clementraynaud.VoiceModule;
import net.clementraynaud.Skoice;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PlaceHolderAPI extends PlaceholderExpansion {
    private final VoiceModule plugin;
    public PlaceHolderAPI(VoiceModule plugin){
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

//        if(identifier.equalsIgnoreCase("isPlayerInVC")){
//            return plugin.isPlayerInVC(P1.getUniqueId()).toString();
//        }

        if(identifier.equalsIgnoreCase("getMutedUsers")){
            return plugin.getMutedUsers().toString();
        }

        return null;
    }
}