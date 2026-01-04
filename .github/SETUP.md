# GitHub Setup Guide

This guide explains how to configure the GitHub repository for automatic releases and Discord notifications.

## Setup Steps

### 1. Discord Webhook Setup

1. Go to your Discord server settings
2. Navigate to **Integrations** → **Webhooks**
3. Click **New Webhook**
4. Name it "AethorQuests Commits" (or similar)
5. Select the channel where commits should be posted
6. Copy the webhook URL
7. In your GitHub repository:
   - Go to **Settings** → **Secrets and variables** → **Actions**
   - Click **New repository secret**
   - Name: `DISCORD_WEBHOOK`
   - Value: Paste the webhook URL
   - Click **Add secret**

### 2. GitHub Releases Setup

Releases are created automatically when you push a version tag:

```bash
git tag v1.0.0
git push origin v1.0.0
```

The workflow will:
- Build the plugin with Maven
- Create a GitHub release
- Upload the JAR file
- Generate release notes

### 3. Build Artifacts

Every push to main/master/develop branches:
- Builds the plugin
- Uploads the JAR as an artifact (available for 7 days)
- Can be downloaded from the Actions tab

## Workflow Files

- `.github/workflows/build.yml` - Build on every commit
- `.github/workflows/release.yml` - Create releases from tags
- `.github/workflows/discord-notify.yml` - Send commit notifications to Discord

## Creating a Release

1. Update version in `pom.xml` if needed
2. Commit your changes
3. Create and push a tag:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
4. The release workflow will automatically:
   - Build the plugin
   - Create a GitHub release
   - Upload the JAR file

## Discord Notification Format

Commits to main/master/develop will send an embed to Discord with:
- Commit message
- Author
- Branch
- Commit SHA with link
- Files changed count
- Timestamp

## Troubleshooting

### Discord notifications not working
- Check if `DISCORD_WEBHOOK` secret is set correctly
- Verify the webhook URL is valid in Discord
- Check the Actions tab for error logs

### Release not creating
- Ensure you're pushing a tag (not a branch)
- Tag must start with `v` (e.g., `v1.0.0`)
- Check Actions tab for build errors

### Build failing
- Ensure Java 21 is specified in workflows
- Check Maven wrapper files are committed
- Verify `aethornpcs-api-1.0.0.jar` is in the repository root
