#!/bin/bash

# Read version and repository from gradle.properties
if [ ! -f gradle.properties ]; then
    echo "Could not find gradle.properties"
    exit 1
fi

VERSION=$(grep '^mod_version=' gradle.properties | cut -d'=' -f2)
REPO=$(grep '^repository=' gradle.properties | cut -d'=' -f2)

# Fallback to origin if no repo is found in gradle.properties
REPO=${REPO:-origin}

# Get short git hash
GIT_HASH=$(git rev-parse --short HEAD)

# Clean version for tag-safe characters
SAFE_VERSION=$(echo "$VERSION" | tr -cd '[:alnum:]._-')

# Create dev tag with version + hash
TAG="v${SAFE_VERSION}-dev-${GIT_HASH}"

# Create tag locally
git tag -a "$TAG" -m "Dev build $TAG"

# Push the tag
git push "$REPO" "$TAG"

echo "Tag created and pushed: $TAG to $REPO"
