#!/usr/bin/env bash
#
# Adds the OpenRocket macOS Quick Look extension to an install4j-built DMG, then signs, notarizes and staples the result.
#
# install4j cannot build or sign Quick Look extensions, so the extension is built separately with Xcode and merged into
# the finished DMG. Adding files to OpenRocket.app invalidates the code signature that install4j applied, so the app is
# re-signed after the merge and the app and DMG are both notarized again.
#
# Usage: add-macos-quicklook.sh <plugins-dir> <input.dmg> <output.dmg>
#
#   <plugins-dir>  The PlugIns folder of the archived Quick Look host app (contains the .appex).
#
# Required environment:
#   SIGN_IDENTITY       Code signing identity (name or SHA-1) of the "Developer ID Application" certificate.
#   NOTARY_KEY_PATH     Path to the App Store Connect API key (.p8) used by notarytool.
#   NOTARY_KEY_ID       Key ID of that API key.
#   NOTARY_ISSUER_ID    Issuer ID of that API key.
# Optional environment:
#   KEYCHAIN_PATH       Keychain that holds the signing identity (omit to use the default search list).

set -euo pipefail

if [ "$#" -ne 3 ]; then
  echo "Usage: $0 <plugins-dir> <input.dmg> <output.dmg>" >&2
  exit 2
fi

PLUGINS_SRC=$1
INPUT_DMG=$2
OUTPUT_DMG=$3

: "${SIGN_IDENTITY:?SIGN_IDENTITY is required}"
: "${NOTARY_KEY_PATH:?NOTARY_KEY_PATH is required}"
: "${NOTARY_KEY_ID:?NOTARY_KEY_ID is required}"
: "${NOTARY_ISSUER_ID:?NOTARY_ISSUER_ID is required}"

if [ ! -d "$PLUGINS_SRC" ] || ! ls "$PLUGINS_SRC"/*.appex >/dev/null 2>&1; then
  echo "No .appex found in $PLUGINS_SRC" >&2
  exit 1
fi
if [ ! -f "$INPUT_DMG" ]; then
  echo "Input DMG not found: $INPUT_DMG" >&2
  exit 1
fi

KEYCHAIN_ARGS=()
if [ -n "${KEYCHAIN_PATH:-}" ]; then
  KEYCHAIN_ARGS=(--keychain "$KEYCHAIN_PATH")
fi

WORK=$(mktemp -d "${RUNNER_TEMP:-${TMPDIR:-/tmp}}/ql-inject.XXXXXX")
MOUNT="$WORK/mnt"
mkdir -p "$MOUNT"

cleanup() {
  hdiutil detach "$MOUNT" -force >/dev/null 2>&1 || true
  rm -rf "$WORK"
}
trap cleanup EXIT

log() { echo "==> $*"; }

# Submits a file to Apple's notary service and waits for the verdict. Prints Apple's log when the submission is rejected.
notarize() {
  local file=$1 result status id
  log "Notarizing $(basename "$file")"
  result=$(xcrun notarytool submit "$file" \
    --key "$NOTARY_KEY_PATH" --key-id "$NOTARY_KEY_ID" --issuer "$NOTARY_ISSUER_ID" \
    --wait --timeout 45m --output-format json) || {
    echo "$result"
    echo "notarytool submit failed for $file" >&2
    return 1
  }
  echo "$result"
  status=$(printf '%s' "$result" | python3 -c 'import json,sys; print(json.load(sys.stdin).get("status", ""))')
  id=$(printf '%s' "$result" | python3 -c 'import json,sys; print(json.load(sys.stdin).get("id", ""))')
  if [ "$status" != "Accepted" ]; then
    echo "Notarization of $file finished with status '$status'. Apple's log:" >&2
    xcrun notarytool log "$id" \
      --key "$NOTARY_KEY_PATH" --key-id "$NOTARY_KEY_ID" --issuer "$NOTARY_ISSUER_ID" >&2 || true
    return 1
  fi
}

log "Converting $(basename "$INPUT_DMG") to a writable image"
hdiutil convert "$INPUT_DMG" -format UDRW -o "$WORK/rw.dmg" -quiet

# The install4j DMG has no free space, so grow the image (and its file system) to make room for the extension.
hdiutil imageinfo -plist "$WORK/rw.dmg" > "$WORK/info.plist"
CURRENT_SECTORS=$(/usr/libexec/PlistBuddy -c 'Print :Size\ Information:Sector\ Count' "$WORK/info.plist")
hdiutil resize -sectors $((CURRENT_SECTORS + 100000)) "$WORK/rw.dmg"

log "Mounting the writable image"
hdiutil attach "$WORK/rw.dmg" -nobrowse -noverify -noautoopen -mountpoint "$MOUNT" -quiet

APPS=()
while IFS= read -r app; do
  APPS+=("$app")
done < <(find "$MOUNT" -maxdepth 1 -name '*.app' -type d)
if [ "${#APPS[@]}" -ne 1 ]; then
  echo "Expected exactly one .app in the DMG, found ${#APPS[@]}." >&2
  exit 1
fi
APP=${APPS[0]}
log "Application bundle: $(basename "$APP")"

log "Copying the Quick Look extension into the application bundle"
ditto "$PLUGINS_SRC" "$APP/Contents/$(basename "$PLUGINS_SRC")"

# Adding files to a signed bundle breaks its seal, so sign the app again. Nested code (launcher, JRE, native libraries)
# keeps the signatures install4j gave it; only the outer bundle is re-sealed, so --deep is deliberately not used.
log "Re-signing the application bundle"
# install4j puts vmoptions.txt directly in Contents/, which codesign treats as a nested code location. install4j's own
# signer seals it as a plain resource, but codesign refuses to seal the bundle unless that file is signed itself (the
# signature of a non-Mach-O file is stored in its extended attributes).
find "$APP/Contents" -maxdepth 1 -type f ! -name Info.plist ! -name PkgInfo -print0 |
  while IFS= read -r -d '' file; do
    codesign --force --timestamp --sign "$SIGN_IDENTITY" ${KEYCHAIN_ARGS[@]+"${KEYCHAIN_ARGS[@]}"} "$file"
  done
codesign --force --timestamp --options runtime \
  --preserve-metadata=identifier,entitlements,flags,runtime \
  --sign "$SIGN_IDENTITY" ${KEYCHAIN_ARGS[@]+"${KEYCHAIN_ARGS[@]}"} "$APP"
codesign --verify --deep --strict --verbose=2 "$APP"

# Notarize the app on its own so the ticket can be stapled to it. The app then passes Gatekeeper offline after it has
# been copied out of the DMG.
ditto -c -k --sequesterRsrc --keepParent "$APP" "$WORK/app.zip"
notarize "$WORK/app.zip"
xcrun stapler staple "$APP"

log "Unmounting"
for attempt in 1 2 3 4 5; do
  if hdiutil detach "$MOUNT" -quiet; then
    break
  fi
  if [ "$attempt" -eq 5 ]; then
    hdiutil detach "$MOUNT" -force
  else
    sleep 3
  fi
done

log "Creating the final compressed DMG"
mkdir -p "$(dirname "$OUTPUT_DMG")"
rm -f "$OUTPUT_DMG"
hdiutil convert "$WORK/rw.dmg" -format UDZO -imagekey zlib-level=9 -o "$OUTPUT_DMG" -quiet

log "Signing, notarizing and stapling the DMG"
codesign --force --timestamp --sign "$SIGN_IDENTITY" ${KEYCHAIN_ARGS[@]+"${KEYCHAIN_ARGS[@]}"} "$OUTPUT_DMG"
notarize "$OUTPUT_DMG"
xcrun stapler staple "$OUTPUT_DMG"

log "Verifying the result"
xcrun stapler validate "$OUTPUT_DMG"
spctl --assess --type open --context context:primary-signature -vv "$OUTPUT_DMG"

hdiutil attach "$OUTPUT_DMG" -readonly -nobrowse -noverify -noautoopen -mountpoint "$MOUNT" -quiet
FINAL_APP=$(find "$MOUNT" -maxdepth 1 -name '*.app' -type d | head -n 1)
codesign --verify --deep --strict "$FINAL_APP"
spctl --assess --type execute -vv "$FINAL_APP"
ls "$FINAL_APP"/Contents/[Pp]lug[Ii]ns/*.appex
hdiutil detach "$MOUNT" -quiet

log "Done: $OUTPUT_DMG"
