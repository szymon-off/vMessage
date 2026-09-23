# vMessage

A Velocity plugin that shows chat, joins, leaves and server switches to everyone on the network, no matter which backend they're on. It also adds `/msg`, `/reply` and `/broadcast` that work across servers. Every message format is a MiniMessage string in one config file.

![Release](https://img.shields.io/github/v/release/szymon-off/vMessage) ![Modrinth downloads](https://img.shields.io/modrinth/dt/ZIxTT2xI?logo=modrinth&color=%2300AF5C) ![License](https://img.shields.io/github/license/szymon-off/vMessage)

> Also by me: **[vHubs](https://modrinth.com/plugin/vhubs)**, for running several hub servers with player commands like `/hub`, `/lobby` and `/survival`.

## Requirements

| Dependency | Version |
| --- | --- |
| Velocity | 4.0.0 or newer                                                                                                                                                                                                                                        |
| Java | 25                                                                                                                                                                                                                                                    |
| [SignedVelocity](https://modrinth.com/plugin/signedvelocity) | On the proxy and on every backend. Only needed for chat: vMessage cancels the original chat event on the proxy and sends its own, and with signed chat that needs SignedVelocity. Without it, chat is unreliable; joins, leaves and switches are fine. |
| [LuckPerms](https://luckperms.net) | Optional. Gives you `$prefix$`, `$suffix$` and custom meta placeholders.                                                                                                                                                                              |
| [PAPIProxyBridge](https://modrinth.com/plugin/papiproxybridge) | Optional. Lets formats use PlaceholderAPI placeholders.                                                                                                                                                                                               |

## Installing

1. Drop `vMessage-<version>.jar` into the proxy's `plugins` folder.
2. Install SignedVelocity on the proxy and all backends.
3. Start the proxy. The config is created at `plugins/vmessage/config.yml`.
4. Edit it and run `/vmessage reload`.

Chat, join, leave and switch messages work without any permissions set up.

## Upgrading from 1.x

Version 2.0 is a rewrite, and the config changed with it. On first start, vMessage looks for a 1.x config in `plugins/vMessage/config.yml` (capital M, the old location) and converts it into `plugins/vmessage/config.yml`. The old file is renamed to `MIGRATED-config.yml` and kept for reference, next to a short `README.txt`. Once you've checked the result you can delete that folder. On Windows and macOS, folder names ignore case, so both locations are the same folder. If `plugins/vmessage/config.yml` already exists as a 2.0 config, the old one is left alone. If the conversion fails, the error is logged, the 2.0 config starts from defaults and your old settings stay in the `MIGRATED-config.yml` file.

What the conversion does:

- Placeholders change from `%player%` to `$player$`. Formats, enabled flags and permission defaults carry over.
- `server-aliases` moves to `settings.server-aliases`, and `luck-perms-meta` to `placeholders.luck-perms.custom-meta`.
- In `/msg` formats, `%sender-server%` and `%receiver-server%` become `$sender_server$` and `$receiver_server$`. `$prefix$` and `$suffix$` now mean the *other* player's, which is what `%receiver-prefix%` meant in the sender's copy and `%sender-prefix%` in the receiver's.

What no longer exists:

- Legacy `&` colour codes. Formats are MiniMessage only, and `§` codes in player messages are stripped.
- The LiteBans and LibertyBans hooks. Muted players are handled by event order instead: vMessage runs `LAST` by default and skips any chat message that another plugin has already denied. If you're changing `messages.chat.order`, keep the punishment plugin ahead of it.
- `/vmessage say`. Use `/vmessage fake chat` (below).

## Commands

| Command | Aliases | Permission | Default |
|---|---|---|---|
| `/vmessage` | `/vmsg`, `/vm` | none | everyone |
| `/vmessage help` | | `vmessage.command.vmessage.help` | denied |
| `/vmessage reload` | | `vmessage.command.vmessage.reload` | denied |
| `/vmessage fake <join\|leave\|change> <player>` | | `vmessage.command.vmessage.fake` | denied |
| `/vmessage fake chat <player> <message>` | | `vmessage.command.vmessage.fake` | denied |
| `/message <player> <message>` | `/msg`, `/tell`, `/whisper`, `/w` | `vmessage.command.message` | allowed |
| `/reply <message>` | `/r` | `vmessage.command.message.reply` | allowed |
| `/broadcast <message>` | `/bc`, `/bcast`, `/shout` | `vmessage.command.broadcast` | denied |

The console can run all of them except `/message` and `/reply`. For `/message`, `/reply` and `/broadcast` the Default column is the `allow-by-default` value in the config; a permission that's explicitly set (true or false) always beats it. `/reply` only exists while `/message` is enabled.

`/vmessage fake` sends the message the given player would have triggered, without the player doing anything.

### Silent joins, leaves and switches

Players who have the matching permission don't trigger that message. The permission names are set in `settings.silent-permissions`, and these are the defaults:

| Config key | Default permission | Suppresses |
|---|---|---|
| `join` | `vmessage-silent.join` | join message |
| `leave` | `vmessage-silent.leave` | leave message |
| `change` | `vmessage-silent.change` | server switch message |

```yaml
settings:
  silent-permissions:
    join: vmessage-silent.join
    leave: vmessage-silent.leave
    change: vmessage-silent.change
```

The default prefix is `vmessage-silent` and not `vmessage` so that a `vmessage.*` wildcard doesn't silence everyone. If you change the names, keep them outside `vmessage.*` for the same reason.

### Muted players

vMessage cancels the chat event on the proxy, so the punishment plugin (LiteBans, LibertyBans, ...) has to run on the proxy too, and its chat handling has to run ahead of vMessage's. With the default `order: LAST` that's normally the case. A mute that only exists on the backends won't hold, because the message never reaches them.

`/message`, `/reply` and `/broadcast` are commands, not chat, so a mute doesn't cover them and a muted player can still send private messages. Add every alias to your punishment plugin's list of commands blocked while muted:

```
message, msg, tell, whisper, w
reply, r
broadcast, bcast, bc, shout
```

Blocking `/msg` alone leaves `/w` and `/tell` open.

## Configuration

Everything lives in `plugins/vmessage/config.yml`, and the generated file has comments on each option. `/vmessage reload` reloads formats, server aliases, integrations and which message types are on. It doesn't register or remove commands, so turning `/message`, `/reply` or `/broadcast` on or off needs a proxy restart.

A format is a MiniMessage string with placeholders in `$dollar$` signs. This is the default chat format:

```yaml
messages:
  chat:
    enabled: true
    format: '$prefix$ <b>$player$</b>: $message$'
    allow-mini-message: false
    order: LAST
```

With `allow-mini-message: false`, tags typed by players are escaped, so nobody can colour their own messages or break your layout. Chat, `/message` and `/broadcast` each have their own `allow-mini-message`. Whatever the setting, legacy `§` colour codes and control characters are always stripped from player text. `order` is when vMessage handles the chat event: `FIRST`, `EARLY`, `NORMAL`, `LATE` or `LAST`.

Placeholders per format:

| Format | Placeholders |
|---|---|
| `messages.chat` | `$player$`, `$message$`, `$server$` |
| `messages.join`, `messages.leave` | `$player$`, `$server$` |
| `messages.change` | `$player$`, `$old_server$`, `$new_server$` |
| `commands.message` | `$sender$`, `$receiver$`, `$message$`, `$sender_server$`, `$receiver_server$` |
| `commands.broadcast.format.player` | `$player$`, `$message$`, `$server$` |
| `commands.broadcast.format.console` | `$message$` |

On top of these, every format that involves a player also accepts `$prefix$` and `$suffix$` (LuckPerms), your own `&meta&` placeholders (below) and PlaceholderAPI placeholders (when PAPIProxyBridge is installed).

Server names come from `settings.server-aliases`, which maps a server's name in `velocity.toml` to the text shown to players:

```yaml
settings:
  server-aliases:
    lobby1: Lobby
    survival: Survival
```

A server that isn't listed keeps its plain name from `velocity.toml`. `settings.default-server-name` (`Unknown` by default) is only used when vMessage can't work out which server a player is on.

### LuckPerms and PlaceholderAPI

For LuckPerms, `$prefix$` and `$suffix$` work as soon as it's installed. Any other meta value has to be mapped first, under `placeholders.luck-perms.custom-meta`. Each integration also has an `enabled` toggle (`placeholders.luck-perms.enabled`, `placeholders.placeholder-api.enabled`). Adding `rank: my_rank_meta` there lets you write `&rank&` in a format.

PlaceholderAPI works through PAPIProxyBridge, so the placeholders are resolved by the backend the player is on. Each lookup is given `placeholders.placeholder-api.bridge-timeout` milliseconds (500 by default); if the backend doesn't answer in time, the placeholder is left unresolved and a warning is logged.

## What it sends out

vMessage reports which features are enabled to [bStats](https://bstats.org/plugin/velocity/vMessage%20Velocity/27241); you can turn that off in bStats' own config. It also asks the Modrinth API for the latest version at startup and logs a line if you're behind. Set `settings.check-for-updates: false` to stop that. Dev builds never check.

## How this was built

Most of vMessage is written by hand, I'd say 90% or more. I use GitHub Copilot for code completion, Claude Chat for quick questions, and Claude Code for code review and small fixes. AI also handled a few repetitive jobs, and it wrote the Javadocs.

## Building from source

You need Java 25.

```bash
git clone https://github.com/szymon-off/vMessage.git
cd vMessage
./gradlew build
```

The jar ends up in `build/libs/`. Without `-PpluginVersion=x.y.z` it's named `vMessage-0.0.0-UNKNOWN.jar`. Gradle downloads the dependencies (Velocity API, Fishy API and others), so the build needs network access.

Bug reports and pull requests are welcome on [GitHub](https://github.com/szymon-off/vMessage/issues).

## License

Versions up to 1.6.1 are MIT. From 1.7.0 on, vMessage is GPL-3.0; the full text is in [LICENSE.md](https://github.com/szymon-off/vMessage/blob/master/LICENSE.md).
