# Get version and repository from gradle.properties
$gradleFile = "gradle.properties"
if (-Not (Test-Path $gradleFile)) {
    Write-Error "Could not find gradle.properties"
    exit 1
}

$gradleContent = Get-Content $gradleFile
$versionLine = $gradleContent | Where-Object { $_ -match '^mod_version=' }
$repoLine = $gradleContent | Where-Object { $_ -match '^repository=' }

if (-Not $versionLine) {
    Write-Error "Could not find mod_version in gradle.properties"
    exit 1
}

$version = ($versionLine -split '=')[1].Trim()
# Use repo from gradle.properties or fallback to origin
$repo = if ($repoLine) { ($repoLine -split '=')[1].Trim() } else { "origin" }

# Get short git hash
try {
    $gitHash = git rev-parse --short HEAD
} catch {
    Write-Error "Could not get git hash. Are you in a git repository?"
    exit 1
}

# Clean version for tag-safe characters
$safeVersion = -join ($version.ToCharArray() | Where-Object { $_ -match '[a-zA-Z0-9._-]' })

# Build dev tag
$tag = "v${safeVersion}-dev-${gitHash}"

# Create tag locally
git tag -a $tag -m "Dev build $tag"
git push $repo $tag
Write-Host "Tag created and pushed: $tag to $repo"

