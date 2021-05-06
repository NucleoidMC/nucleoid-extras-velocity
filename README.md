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
    }
}
```
