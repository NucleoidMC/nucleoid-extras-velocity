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
    }
}
```
