#!/bin/bash

# Variables
OLD_APP_NAME="OpenRocket"
NEW_APP_NAME="OpenRocket 24.12.RC.01"
DMG_TEMPLATE="macOS_resources/OpenRocket-23.09-macOS-AppleSilicon.dmg"
OUTPUT_DIR="./macOS_resources"

eject_rw_volume=true
manual_positioning=false

# Check for install4j being closed
if pgrep -x "install4j" > /dev/null; then
    echo "Please close install4j before running this script."
    exit 1
fi

# Step 1: Convert DMG to read/write format
echo "Converting DMG to read/write format..."
FORMATTED_DMG="${DMG_TEMPLATE%.*}_rw.dmg"
hdiutil convert "$DMG_TEMPLATE" -format UDRW -o "$FORMATTED_DMG"

# Step 2: Resize the DMG
echo "Resizing DMG..."
CURRENT_SIZE=$(hdiutil imageinfo "$FORMATTED_DMG" | grep "Total Bytes:" | awk '{print $3}')
if [ -z "$CURRENT_SIZE" ]; then
    echo "Failed to determine current DMG size. Exiting."
    exit 1
fi

# Convert bytes to sectors (1 sector = 512 bytes)
CURRENT_SECTORS=$((CURRENT_SIZE / 512))
RESIZE_SECTORS=$((CURRENT_SECTORS + 100000))

hdiutil resize -sectors $RESIZE_SECTORS "$FORMATTED_DMG"

# Step 3: Mount the DMG
echo "Mounting DMG..."
MOUNT_POINT=$(hdiutil attach "$FORMATTED_DMG" | grep "Volumes" | awk '{print $3}')
if [ -z "$MOUNT_POINT" ]; then
    echo "Failed to mount DMG. Exiting."
    exit 1
fi

# Step 4: Update the app name
echo "Updating app name from $OLD_APP_NAME to $NEW_APP_NAME..."
APP_OLD="${MOUNT_POINT}/${OLD_APP_NAME}.app"
APP_NEW="${MOUNT_POINT}/${NEW_APP_NAME}.app"

if [ -d "$APP_OLD" ]; then
    mv "$APP_OLD" "$APP_NEW"
else
    echo "App not found in mounted DMG at $APP_OLD. Exiting."
    if $eject_rw_volume ; then
        hdiutil detach "$MOUNT_POINT"
    fi
    exit 1
fi

# Step 5.1: Position the app automatically
echo "Positioning the app icon automatically..."
osascript <<EOF
tell application "Finder"
    set diskName to "$(basename $MOUNT_POINT)"
    set appName to "$(basename "${APP_NEW}")"
    tell disk diskName
        set position of file appName to {135, 150} -- Desired x, y position
    end tell
end tell
EOF

# Step 5.2: Wait until the app is moved to the correct location
if $manual_positioning ; then
    open $MOUNT_POINT
    read -p "Move the app to the correct location. Press any key to continue... " -n1 -s
fi

# Step 6: Copy the .DS_Store
echo "Copying .DS_Store to $OUTPUT_DIR..."
mkdir -p "$OUTPUT_DIR"
cp "$MOUNT_POINT/.DS_Store" "$OUTPUT_DIR/DS_Store"

# Step 7: Eject the DMG
echo "Ejecting DMG..."
if $eject_rw_volume ; then
    hdiutil detach "$MOUNT_POINT"
fi

# Step 8: Clean up temporary files
echo "Cleaning up temporary DMG..."
rm -f "$FORMATTED_DMG"

echo "All done!"
