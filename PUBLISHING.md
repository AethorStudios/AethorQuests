# Publishing AethorQuests to GitHub

## Quick Publish Guide

### Step 1: Initialize Git Repository (if not done)

```bash
cd C:\Users\jkakj\Desktop\Aethor\AethorQuests
git init
git add .
git commit -m "Initial commit: AethorQuests production-quality quest system"
```

### Step 2: Create GitHub Repository

1. Go to your GitHub organization
2. Click **New Repository**
3. Name: `AethorQuests`
4. Description: `Production-quality quest system for Paper Minecraft 1.21.8`
5. Visibility: Choose **Public** or **Private**
6. **DO NOT** initialize with README (we already have one)
7. Click **Create repository**

### Step 3: Push to GitHub

Replace `YOUR_ORG` with your actual organization name:

```bash
git remote add origin https://github.com/YOUR_ORG/AethorQuests.git
git branch -M main
git push -u origin main
```

### Step 4: Configure Discord Webhook

1. **In Discord:**
   - Go to Server Settings → Integrations → Webhooks
   - Click "New Webhook"
   - Name it "AethorQuests Commits"
   - Select the channel for commit notifications
   - Copy the Webhook URL

2. **In GitHub:**
   - Go to your repository
   - Settings → Secrets and variables → Actions
   - Click "New repository secret"
   - Name: `DISCORD_WEBHOOK`
   - Value: Paste the webhook URL
   - Click "Add secret"

### Step 5: Create First Release

```bash
# Update version in pom.xml if needed
git tag v1.0.0
git push origin v1.0.0
```

This will automatically:
- Build the plugin
- Create a GitHub release
- Upload the JAR file
- Generate release notes

### Step 6: Update README Links

After publishing, update these in README.md:
- Replace `YOUR_ORG` with your organization name
- Update Discord invite link (if applicable)

Example:
```bash
# Find and replace in README.md
YOUR_ORG → AethorNetwork
```

Then commit:
```bash
git add README.md
git commit -m "Update README with correct organization links"
git push
```

---

## Automated Workflows

Once published, the following workflows will run automatically:

### 🔨 Build Workflow
- **Triggers**: Every push to main/master/develop
- **Actions**: Builds plugin, uploads artifact
- **Location**: `.github/workflows/build.yml`

### 🚀 Release Workflow
- **Triggers**: When you push a tag (e.g., `v1.0.0`)
- **Actions**: Creates GitHub release with JAR
- **Location**: `.github/workflows/release.yml`

### 💬 Discord Notifications
- **Triggers**: Every push to main/master/develop
- **Actions**: Posts commit info to Discord
- **Location**: `.github/workflows/discord-notify.yml`

---

## Version Tags

Follow semantic versioning:

```bash
# Major release (breaking changes)
git tag v2.0.0

# Minor release (new features)
git tag v1.1.0

# Patch release (bug fixes)
git tag v1.0.1

# Push tag to trigger release
git push origin v1.0.1
```

---

## Troubleshooting

### Build fails on GitHub Actions
- Check if `aethornpcs-api-1.0.0.jar` is committed to the repo
- Verify Java 21 is specified in workflow files
- Check Maven wrapper files are present

### Discord webhook not working
- Verify the secret is named exactly `DISCORD_WEBHOOK`
- Check the webhook URL is valid in Discord
- Look at Actions logs for errors

### Release not creating
- Ensure tag starts with `v` (e.g., `v1.0.0`)
- Check if tag was pushed: `git push origin v1.0.0`
- View Actions tab for errors

---

## Next Steps

1. ✅ Publish repository to GitHub
2. ✅ Configure Discord webhook
3. ✅ Create first release
4. 📝 Update README with correct links
5. 📝 Add collaborators (Settings → Collaborators)
6. 📝 Set up branch protection rules (optional)
7. 📝 Create GitHub Pages for documentation (optional)

---

For detailed setup instructions, see [.github/SETUP.md](.github/SETUP.md)
