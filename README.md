# vMessage

> ⚠️ **Warning:** LiteBans mute support hasn't been tested yet. If you encounter any issues, please reach out to me on Discord: `szymon.off`

**vMessage** is the best Velocity plugin for synchronizing chat and player events across your entire proxy network. It is designed for server administrators who want seamless, reliable, and configurable message syncing without unnecessary complexity.

## Features

- **Global Chat Sync:** Instantly syncs chat messages across all servers connected to your Velocity proxy.
- **Join/Leave/Change-Server Broadcasts:** Notifies all players network-wide when someone joins, leaves, or switches servers.
- **Powerful Configuration:** Comes with a robust, easy-to-use config file so you can tailor the plugin to your network's needs.
- **Lightweight & Fast:** No unnecessary features or bloat—just efficient, reliable message syncing.

## Usage

1. Place `vMessage.jar` into your Velocity `plugins` folder.
2. Start or restart your Velocity proxy.
3. Edit the generated configuration file (`plugins/vMessage/config.yml`) to suit your preferences.

Once installed and configured, vMessage will automatically:

- Sync chat messages across all servers
- Broadcast join, leave, and server switch events to all players

No commands or permissions are required for basic functionality.

## Commands

vMessage provides several administrative commands for advanced usage and configuration:

- `/vmessage say <player> <message>`  
  Sends a message as the specified player across the network.  
  **Permission:** `vmessage.command.say`

- `/vmessage reload`  
  Reloads the plugin configuration without restarting the proxy.  
  **Permission:** `vmessage.command.reload`

- `/vmessage help`  
  Displays the help message with available commands.  
  **Permission:** `vmessage.command.help`

Make sure to assign the appropriate permissions to your staff or admin roles in your Velocity configuration.

## Configuration

vMessage provides a powerful and easy-to-use configuration file. You can customize message formats, toggle features, and more. See the comments in `config.yml` for details.

## Why vMessage?

- **Purpose-built for Velocity:** Designed specifically for Velocity, making it the most reliable and feature-rich solution for network-wide messaging.
- **Simple Setup:** Drop it in, configure, and go. No complicated dependencies or setup steps.
- **Actively Maintained:** Built with modern best practices and open to community feedback.

## Contributing

Contributions are welcome! Please open issues or submit pull requests for improvements or bug fixes.

## Building from Source

If you want to build vMessage yourself:

- Prerequisites: Java 17 or higher, Maven
- Clone the repository and build:
  ```bash
  git clone https://github.com/szymon-off/vMessage.git
  cd vMessage
  mvn clean package
  ```
- The built jar will be in the `target` directory.

## License

This project is licensed under the [MIT License](https://opensource.org/licenses/MIT). You are free to use, modify, and distribute it at no cost.
