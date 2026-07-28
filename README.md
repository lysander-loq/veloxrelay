## VeloxRelay
VeloxRelay is a lightweight Velocity plugin that relays player activity/server events to Discord through webhooks.
It integrates chat messages, proxy joins and leaves, and server switches, with customizable message formats. I plan to add more relayed events in the future, and perhaps some other features that allow smoother configuration.

<details>
<summary>config.yml</summary>

```yaml
# VeloxRelay configuration file
# if any format option is left blank (""), it's corresponding
# message broadcast will be disabled, if unspecified it will
# default to it's respective default format

webhooks:
- "URLHERE" # non-https prefixed strings are ignored

# Message format for any chat message received
# {server} will be replaced with the server name
# {player} will be replaced with the player's username
# {message} will be replaced with the message sent by the player
format: "`{server}` **{player}**: {message}"

# Message format for when a player joins the proxy
# {player} will be replaced with the player's username
joinformat: "**{player}** has connected to the proxy."

# Message format for when a player enters a server
# {server} will be replaced with the server name
# {player} will be replaced with the player's username
enterformat: "**{player}** has entered `{server}`."

# Message format for when a player leaves the proxy
# {player} will be replaced with the player's username
leaveformat: "**{player}** has disconnected from the proxy."
```
</details>

Feel free to open an [Issue](<https://github.com/lysander-loq/veloxrelay/issues>) if you wish to report a bug or give me suggestions on what i could add to this project :3 

For developers wanting to implement their own features using the webhooks registered on this plugin, they can cast messages using the method
`broadcastMessage(java.lang.String)` on the plugin's class
