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
mspt_kick_limit: -1                  | kicks afk players if the mspt goes above the limit, by default minecraft aims for 50mspt (-1 for disabled)
mspt_kick_type: 1                    | every minute, if the mspt is over the limit, what should happen?: 1 - kick player whose been fake afk the longest, 2 - kick all fake afk players
max_afk_limit: -1                    | max number of people who can be fake afk at once
max_afk_type: 1                      | when a new person tries to afk, what should happen?: 1 - kick player whose been fake afk the longest, 2 - dont allow more fake afk players
max_afk_ticks: -1                    | max time in ticks people can fake afk for, -1 is unlimited
max_summon_ticks: 6000               | max time in ticks people can /afk:summon their fake players for, -1 is unlimited
names:                               | list of what people have named their fake players:
9c3adf8d-a723-... nettakrim-is-afk   | <uuid> <name>
...
```

For reference, this is what the permission levels are:

- `ALL = 0` - everyone
- `MODERATORS = 1` - people who can build in spawn chunks
- `GAMEMASTERS = 2` - people with /gamerule, /fill, /execute, /tp, ... (also the level command blocks, datapacks etc. have)
- `ADMINS = 3` - people with /kick, /ban, /op, ...
- `OWNERS = 4` - people with /stop, /save