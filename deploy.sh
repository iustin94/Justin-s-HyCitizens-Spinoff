#!/usr/bin/env bash
set -e

CLIENT_MODS="$HOME/.var/app/com.hypixel.HytaleLauncher/data/Hytale/UserData/Mods"
SERVER_MODS="$HOME/HytaleServer/mods"

cd "$(dirname "$0")"

JAR_DIR="build/libs"

# Target selection
echo "Deploy to:"
echo "  1) Client  ($CLIENT_MODS)"
echo "  2) Server  ($SERVER_MODS)"
echo "  3) Both"
echo
read -rp "Select target: " target

case "$target" in
    1) targets=("$CLIENT_MODS") ;;
    2) targets=("$SERVER_MODS") ;;
    3) targets=("$CLIENT_MODS" "$SERVER_MODS") ;;
    *) echo "Invalid target."; exit 1 ;;
esac

# Verify target directories exist
for t in "${targets[@]}"; do
    if [[ ! -d "$t" ]]; then
        echo "Target directory does not exist: $t"
        exit 1
    fi
done

# Build
echo
echo "Building HyCitizens..."
./gradlew build

# Find the fat JAR (exclude -sources.jar and version-less duplicates)
jar=$(find "$JAR_DIR" -maxdepth 1 -name "HyCitizens-*.jar" ! -name "*-sources.jar" | head -1)
if [[ -z "$jar" ]]; then
    echo "No HyCitizens JAR found in $JAR_DIR after build."
    exit 1
fi

# Deploy
echo
for t in "${targets[@]}"; do
    cp "$jar" "$t/"
    label="server"
    [[ "$t" == "$CLIENT_MODS" ]] && label="client"
    echo "  $(basename "$jar") -> $label"
done

echo
echo "Done!"
