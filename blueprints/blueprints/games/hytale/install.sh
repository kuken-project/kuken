#!/bin/ash
set -e

mkdir -p /mnt/server
cd /mnt/server

echo -e "Installing dependencies..."
apk add --no-cache curl unzip

DOWNLOAD_URL="https://downloader.hytale.com/hytale-downloader.zip"

echo -e "Downloading Hytale Downloader CLI..."
rm -f hytale-downloader.zip
curl -L --progress-bar -o hytale-downloader.zip $DOWNLOAD_URL
unzip -o hytale-downloader.zip -d hytale-downloader
mv hytale-downloader/hytale-downloader-linux-amd64 hytale-downloader/hytale-downloader-linux
chmod 555 hytale-downloader/hytale-downloader-linux

echo -e "Verifying Hytale Downloader installation..."
echo -e "Hytale Downloader version: $(./hytale-downloader/hytale-downloader-linux -version)"
echo -e "Hytale Downloader installed successfully."