# Fake AFK
Allows players to create fake players from [Carpet Mod](https://modrinth.com/mod/carpet) without significantly changing the games balance

The mod only needs to exist server-side, and it requires Carpet Mod to work

## Commands

`/afk:ready` after running, a Fake Player will be created at your position when you log off, and disappear once you log back on

`/afk:summon` summons your Fake Player for 5 minutes, so you can give it items, or kill it to get any items it picked up - the limited time means some simple tasks that require two people can be done, but anything complex cant

`/afk:name` rename your Fake Player, all names need to include a "-", which regular usernames can't, so its always clear who is a real player

## Config

Fake AFK's config and data can be found in `config/fake_afk.txt`, and looks like this:

```
name_permission_level: 0             | permission level needed for /afk:name to be usable
ready_permission_level: 0            | permission level needed for /afk:ready to be usable
summon_permission_level: 0           | permission level needed for /afk:summon to be usable
allow_real_names_permission_level: 3 | permission level needed for /afk:name to allow names without -
max_afk_ticks: -1                    | max time in ticks people can fake afk for, -1 is unlimited
max_summon_ticks: 6000               | max time in ticks people can /afk:summon their fake players for, -1 is unlimited
names:                               | list of what people have named their fake players:
9c3adf8d-a723-... nettakrim-is-afk   | <uuid> <name>
...
```

For reference, this is what the permission levels are:

- `ALL = 0` - everyone
- `MODERATORS = 1` - people who can build in spawn chunks
- `GAMEMASTERS = 2` - people with /gamerule, /fill, /execute, /tp, ... (also command blocks, datapacks etc.)
- `ADMINS = 3` - people with /kick, /ban, /op, ...
- `OWNERS = 4` - people with /stop, /save