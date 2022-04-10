package net.clementraynaud.skoice.lang;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStreamReader;
import java.util.Arrays;

public class LangFile {

    private static final String CHAT_PREFIX = ChatColor.LIGHT_PURPLE + "Skoice " + ChatColor.DARK_GRAY + "• " + ChatColor.GRAY;

    private YamlConfiguration englishMessages;
    private YamlConfiguration messages = new YamlConfiguration();

    public void load(Lang lang) {
        InputStreamReader englishLangFile = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("lang/" + Lang.EN + ".yml"));
        this.englishMessages = YamlConfiguration.loadConfiguration(englishLangFile);
        if (lang != Lang.EN) {
            InputStreamReader langFile = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("lang/" + lang + ".yml"));
            this.messages = YamlConfiguration.loadConfiguration(langFile);
        }
    }

    public String getMessage(String path) {
        String message = this.messages.contains(path) ? this.messages.getString(path) : this.englishMessages.getString(path);
        if (path.startsWith("minecraft.chat.") && message != null) {
            return String.format(message, LangFile.CHAT_PREFIX);
        }
        return message;
    }

    public String getMessage(String path, Object... args) {
        String message = this.messages.contains(path) ? this.messages.getString(path) : this.englishMessages.getString(path);
        if (message == null) {
            return null;
        }
        if (path.startsWith("minecraft.chat.")) {
            return String.format(message, LangFile.CHAT_PREFIX, Arrays.toString(args));
        }
        return String.format(message, args);
    }

    public boolean contains(String path) {
        return this.englishMessages.contains(path);
    }
}
