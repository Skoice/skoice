<img align="right" src="https://clementraynaud.net/Skoice.jpeg" height="200" width="200" alt="Skoice logo">

# Skoice: Proximity Voice Chat<br>[![](https://img.shields.io/spiget/downloads/82861?style=flat&labelColor=697EC4&color=7289DA&label=Downloads)](https://www.spigotmc.org/resources/skoice-proximity-voice-chat.82861) [![](https://img.shields.io/spiget/rating/82861?style=flat&labelColor=697EC4&color=7289DA&label=Rating)](https://www.spigotmc.org/resources/skoice-proximity-voice-chat.82861) [![](https://img.shields.io/discord/741375523275407461.svg?style=flat&labelColor=697EC4&color=7289DA&label=Discord)](https://discord.gg/skoice-proximity-voice-chat-741375523275407461)

Skoice allows players on a Minecraft server to communicate with each other by voice depending on their in-game location. In other words, they are only able to talk to nearby players.

The defining feature of our plugin is that it works with Discord. Therefore, users will not have to download any additional software in order to use the proximity voice chat.

Skoice is currently available in the following languages: English, Danish, German, Spanish, French, Italian, Japanese, Norwegian, Polish, Portuguese, Russian and Turkish.
You can help us translate Skoice by [joining Crowdin](https://crowdin.com/project/skoice).

## Download

To download our plugin, please [head to our Spigot page](https://www.spigotmc.org/resources/skoice-proximity-voice-chat.82861). Consider leaving a review if you enjoy our work!

Skoice is supported on servers that run any Minecraft version from 1.8 to 1.21.

You can use Skoice on the following server software:
- **Spigot** (or derivative like [Paper](https://papermc.io/downloads/paper), [Purpur](https://purpurmc.org/downloads))
- **Fabric** (using [Cardboard](https://cardboardpowered.org/))
- **Forge** (using [Magma](https://magmafoundation.org/) or [Mohist](https://mohistmc.com/))

Skoice is also available directly from the panels of many free Minecraft hosts. Thus, you can [get it on Alternos](https://alternos.org/addons/a/spigot/82861).

If your server allows players from **Bedrock Edition** (with [Geyser](https://geysermc.org/) for example), they can use Skoice just like on Java Edition.

## Installation

Once you have downloaded our plugin, simply drop the file in the `plugins` folder of your Minecraft server and start it. Join the server with an account that has operator privileges (or `skoice.manage` permission) and follow the instructions that are sent to you.

## Features

Skoice can be heavily customized directly from Discord. Here are a few things you can do:
- Customize the range of the proximity voice chat.
- Include or exclude certain types of players (spectators, dead players).
- Choose worlds where Skoice is active.
- Create multiple teams that cannot communicate with each other.
- Select which messages from the plugin you want to display to your players.
- Toggle the synchronization of external plugins with Skoice (DiscordSRV, EssentialsX).

## Commands

### Minecraft

| Command                       | Description                                                                                                 | Permission                    |
|-------------------------------|-------------------------------------------------------------------------------------------------------------|-------------------------------|
| `/skoice configure`           | Get instructions to begin the configuration process.                                                        | Operator (or `skoice.manage`) |
| `/skoice token <token>`       | Link a Discord bot to your Minecraft server.                                                                | Operator (or `skoice.manage`) |
| `/skoice language <language>` | Change the language used by Skoice.                                                                         | Operator (or `skoice.manage`) |
| `/skoice tooltips`            | Toggle interactive messages, useful on Bedrock Edition or when tooltips are disabled in Minecraft settings. | Operator (or `skoice.manage`) |
| `/skoice link <code>`         | Link your Minecraft account to Discord.                                                                     | None                          |
| `/skoice unlink`              | Unlink your Minecraft account from Discord.                                                                 | None                          |

### Discord

| Command      | Description                                           | Permission      |
|--------------|-------------------------------------------------------|-----------------|
| `/configure` | Customize Skoice depending on your needs.             | `Manage Server` |
| `/link`      | Get a code to link your Discord account to Minecraft. | None            |
| `/unlink`    | Unlink your Discord account from Minecraft.           | None            |
| `/invite`    | Get external links related to Skoice.                 | None            |

> **Note:** The `Use Application Commands` permission is automatically granted to users since it is required to perform those commands.

## Getting Help

If you are experiencing any issues with Skoice, you may want to [join our Discord server](https://discord.gg/skoice-proximity-voice-chat-741375523275407461).

## Donating

Keep in mind that Skoice is a free software. If you like it, you can help us by [donating](https://opencollective.com/skoice).
