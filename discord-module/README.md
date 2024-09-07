# Discord Module

This module allows notifications to be registered on Discord, and messages sent to channels about WC3 games. To use this
module, you will need to create a Discord bot, and then add the bot to your server.

## Adding the bot to your server

If you simply wish to use the existing Discord bot, and don't want to host the application yourself, you can
use [this link](https://discord.com/oauth2/authorize?client_id=1270671002346590291) to add the bot to your server.

## Configuration

This module has only a single configuration property; `discord.privateToken`. This property is the client secret for
your Discord bot.

## Using the Discord bot

The Discord bot has 4 slash commands that can be used: `/notify`, `/stopnotify`, `/help`, and `/about`. Please use these
commands
to see the documentation for them.