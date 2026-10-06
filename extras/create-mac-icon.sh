#!/bin/bash

################################################################
# Create ICNS file from PNG image with sips and iconutil
################################################################

# Find script dir and cd as working dir
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd "$SCRIPT_DIR"
# Find and copy PlanetHour-icon.png to current dir
find .. -type f -name 'PlanetHour-icon.png' | xargs -I{} cp {} .
# Make iconset folder and generate all sizes
mkdir PlanetHour.iconset
sips -z 16 16     PlanetHour-icon.png --out PlanetHour.iconset/icon_16x16.png
sips -z 32 32     PlanetHour-icon.png --out PlanetHour.iconset/icon_16x16@2x.png
sips -z 32 32     PlanetHour-icon.png --out PlanetHour.iconset/icon_32x32.png
sips -z 64 64     PlanetHour-icon.png --out PlanetHour.iconset/icon_32x32@2x.png
sips -z 128 128   PlanetHour-icon.png --out PlanetHour.iconset/icon_128x128.png
sips -z 256 256   PlanetHour-icon.png --out PlanetHour.iconset/icon_128x128@2x.png
sips -z 256 256   PlanetHour-icon.png --out PlanetHour.iconset/icon_256x256.png
sips -z 512 512   PlanetHour-icon.png --out PlanetHour.iconset/icon_256x256@2x.png
sips -z 512 512   PlanetHour-icon.png --out PlanetHour.iconset/icon_512x512.png
sips -z 1024 1024 PlanetHour-icon.png --out PlanetHour.iconset/icon_512x512@2x.png
# Convert all images in iconset to icns file
iconutil -c icns PlanetHour.iconset
# Cleanup
rm -R PlanetHour.iconset
rm PlanetHour-icon.png
