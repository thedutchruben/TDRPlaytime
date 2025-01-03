#!/bin/bash

# GitHub repository details
REPO_OWNER="thedutchruben"
REPO_NAME="tdrplaytime"

# GitHub API URL for the latest release (including pre-releases)
API_URL="https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/releases?per_page=1"

# Get the latest release data (including pre-releases)
release_data=$(curl -s $API_URL)
assets_data=$(curl -s $(echo $release_data | jq -r '.[0].assets_url'))

# Extract the download URL for the JAR file
jar_url=$(echo $assets_data | jq -r '.[0].browser_download_url')

# Check if the URL was found
if [ -z "$jar_url" ]; then
  echo "No JAR file found in the latest release or pre-release."
  exit 1
fi

mkdir -p plugins

# Download the JAR file
echo "Downloading JAR file from $jar_url"
curl -L -o ./plugins/latest-release.jar "$jar_url"

# Check if download was successful
if [ $? -eq 0 ]; then
  echo "Download successful: latest-release.jar"
else
  echo "Download failed."
  exit 1
fi
