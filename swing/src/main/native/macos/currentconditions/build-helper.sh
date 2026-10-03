#!/bin/sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
OUTPUT_ZIP=${1:?output zip path is required}
SIGNING_IDENTITY=${OPENROCKET_MAC_SIGNING_IDENTITY:--}
BUILD_DIR=$(mktemp -d "${TMPDIR:-/tmp}/openrocket-location-helper.XXXXXX")
trap 'rm -rf "$BUILD_DIR"' EXIT INT TERM

APP_DIR="$BUILD_DIR/OpenRocketLocationHelper.app"
mkdir -p "$APP_DIR/Contents/MacOS"
cp "$SCRIPT_DIR/Info.plist" "$APP_DIR/Contents/Info.plist"
for ARCH in arm64 x86_64; do
	xcrun swiftc -O -target "$ARCH-apple-macos12.0" -framework AppKit -framework CoreLocation \
		"$SCRIPT_DIR/OpenRocketLocationHelper.swift" \
		-o "$BUILD_DIR/OpenRocketLocationHelper-$ARCH"
done
xcrun lipo -create "$BUILD_DIR/OpenRocketLocationHelper-arm64" \
	"$BUILD_DIR/OpenRocketLocationHelper-x86_64" \
	-output "$APP_DIR/Contents/MacOS/OpenRocketLocationHelper"

if [ "$SIGNING_IDENTITY" = "-" ]; then
	codesign --force --sign - "$APP_DIR"
else
	codesign --force --options runtime --timestamp --sign "$SIGNING_IDENTITY" "$APP_DIR"
fi

mkdir -p "$(dirname -- "$OUTPUT_ZIP")"
ditto -c -k --keepParent "$APP_DIR" "$OUTPUT_ZIP"
