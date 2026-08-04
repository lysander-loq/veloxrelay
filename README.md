## VeloxRelay
VeloxRelay is a lightweight Velocity plugin that relays player activity/server events to Discord through webhooks.
It integrates chat messages, proxy joins and leaves, and server switches, with customizable message formats. I plan to add more relayed events in the future, and perhaps some other features that allow smoother configuration.

## Building
VeloxRelay uses Gradle to handle dependencies & building.

#### Requirements
* Java 21 JDK or newer
* Git

#### Compiling from source
```sh
git clone https://github.com/lysander-loq/veloxrelay.git
cd veloxrelay
./gradlew build
```
And you should be good to go! :P

## Looking for ideas

Feel free to open an [Issue](<https://github.com/lysander-loq/veloxrelay/issues>) if you wish to report a bug or give me suggestions on what i could add to this project :3 

For developers wanting to implement their own features using the webhooks registered on this plugin, they can cast messages using the method
`broadcastMessage(java.lang.String)` on the plugin's running instance
