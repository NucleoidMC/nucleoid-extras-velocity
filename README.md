# nucleoid-extras-velocity

## Example Config
Replace `plugins/nucleoid-extras-velocity/config.json` with this, and fill in the settings: (note that this is not valid JSON, you need to remove the comments)
```json5
{
    "integrations": {
        "host": "localhost",
        "port": 12345,
        "channel_name": "proxy",
        "enabled": [
            // Support receiving commands from the backend to execute.
            "commands",
            // Send started/stopped messages to the backend.
            "lifecycle",
            // Allow the backend to send players between servers.
            "server"
        ]
    },
    // These are appended onto the server MOTD if the host matches.
    // Like the velocity motd option, this supports both JSON text components (wrapped in a string)
    // AND legacy text formatting using &
    "forced_motds": {
      "building.example.com": "Building server"
    },
    // Optional
    // The base address of a nucleoid-backend web api if self-hosting
    "nucleoid_api_base": "https://api.nucleoid.xyz",
    // These are used to show the open games in a list on the server status.
    // The value of each pair is the backend channel name of the server to query
    "forced_channels": {
      "play.example.com": "play"
    },
    // Used to format each line of the currently open games in the player list. Supports legacy formatting with '&'
    // In this example the game name is in blue and bold, and the player count is in gold
    // $GAME_NAME$ is replaced with the name of the game and $PLAYER_COUNT$ is replaced with the amount of players.
    "open_game_format": "&9&l$GAME_NAME$&r: &6$PLAYER_COUNT$",
    // The text shown in the player list when no games are open. Supports legacy formatting with '&'
    // In this example the message is red.
    "no_games_message": "&cNo games are open right now!",
    // Allows receiving custom payload packets ('plugin channels') from backend servers to switch players around
    "enable_switch_packets": false
}
```
