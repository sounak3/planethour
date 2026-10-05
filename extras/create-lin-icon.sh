#!/bin/bash

################################################################
# Copy the PNG file to be used as icon
################################################################

# Find script dir and cd as working dir
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd "$SCRIPT_DIR"
# Find and copy PlanetHour-icon.png to current dir
find .. -type f -name 'PlanetHour-icon.png' | xargs -I{} cp {} ./PlanetHour.png
