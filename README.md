
# AutoDimReset

Automatically reset the nether and/or end dimensions once a month on server startup.

Developed for Minecraft Forge 1.12.2.

## Usage

The mod doesn't do anything until you add dimension names to the configuration file `auto_dim_reset.txt` which can be found in the config folder.

The default reset interval is a month, but can be configured in `auto_dim_reset_time.txt`.

Example of format: `0y3m0d0h`

Which would correspond to resetting every 3 months.

The file `last_dim_reset.txt` in the world folder will be updated with the current date when a dimension reset is triggered.

## Important

This mod only resets dimensions on server load, which means you will need to have the server automatically restart on a regular basis.

Additionally, players who log out in a dimension that is then reset will most likely experience kinetic energy, or suffocate in a wall.
