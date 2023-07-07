
# AutoDimReset

Automatically reset the nether and/or end dimensions once a month on server startup.

Developed for Minecraft Forge 1.12.2.

## Usage

The mod doesn't do anything until you edit the configuration file `dimresetcfg.txt` which can be found in the world folder.

The configration file formatting is as follows.

```
n?e?[0-9]+y[0-9]+m[0-9]+d[0-9]+h
```

For example:

`ne2023y1m2d0h` will reset the both the nether and end dimensions when the server starts on or after January 2nd 2023 at the 0th hour UTC.

The configuration file will be updated with the current date plus a month when a dimension reset is triggered.

## Important

This mod only resets dimensions on server load, which means you will need to have the server automatically restart on a regular basis.

Additionally, players who log out in a dimension that is then reset will most likely experience kinetic energy, or suffocate in a wall.
