# 🚀 Publishing Checklist

## ✅ Files Created

All necessary files for GitHub publishing, CI/CD, and Discord integration have been created:

### GitHub Actions Workflows
- ✅ `.github/workflows/build.yml` - Auto-build on every commit
- ✅ `.github/workflows/release.yml` - Auto-release on version tags
- ✅ `.github/workflows/discord-notify.yml` - Discord commit notifications

### Documentation
- ✅ `.github/SETUP.md` - Detailed setup instructions
- ✅ `PUBLISHING.md` - Step-by-step publishing guide
- ✅ `CONTRIBUTING.md` - Contribution guidelines
- ✅ `LICENSE` - MIT License
- ✅ `README.md` - Updated with badges and links

### Helper Scripts
- ✅ `publish.ps1` - PowerShell script to automate publishing

---

## 📋 Pre-Publish Checklist

Before running the publish script:

- [ ] Create repository on GitHub (https://github.com/organizations/YOUR_ORG/repositories/new)
  - Name: `AethorQuests`
  - Description: `Production-quality quest system for Paper Minecraft 1.21.8`
  - Visibility: Public or Private
  - **DO NOT** check "Initialize with README"

- [ ] Update `publish.ps1` with your organization name
  - Open `publish.ps1`
  - Change `$ORG_NAME = "YOUR_ORG"` to your actual org name

- [ ] Verify all files are ready
  - Run: `git status` to see what will be committed
  - Make sure `aethornpcs-api-1.0.0.jar` is in the repo root

---

## 🎬 Quick Start

### Option 1: Use PowerShell Script (Recommended)

```powershell
# 1. Edit publish.ps1 and set your org name
# 2. Run the script
.\publish.ps1
```

### Option 2: Manual Commands

```bash
# Initialize and commit
git init
git add .
git commit -m "Initial commit: AethorQuests production-quality quest system"

# Add remote (replace YOUR_ORG)
git remote add origin https://github.com/YOUR_ORG/AethorQuests.git

# Push
git branch -M main
git push -u origin main
```

---

## ⚙️ Post-Publish Setup

### 1. Configure Discord Webhook (Required for notifications)

**In Discord:**
1. Server Settings → Integrations → Webhooks
2. New Webhook → Name: "AethorQuests Commits"
3. Select channel → Copy webhook URL

**In GitHub:**
1. Repository → Settings → Secrets and variables → Actions
2. New repository secret
3. Name: `DISCORD_WEBHOOK`
4. Value: Paste webhook URL
5. Add secret

### 2. Create First Release

```bash
git tag v1.0.0
git push origin v1.0.0
```

This triggers the release workflow automatically!

### 3. Update README Links

Find and replace in `README.md`:
- `YOUR_ORG` → Your actual organization name

Then commit:
```bash
git add README.md
git commit -m "Update README with organization links"
git push
```

---

## 🎯 What Happens Next

### On Every Commit to main/master/develop:
1. ✅ Build workflow runs
2. ✅ Discord notification sent with commit details
3. ✅ JAR artifact uploaded (available 7 days)

### On Version Tag Push (e.g., `v1.0.0`):
1. ✅ Release workflow runs
2. ✅ Plugin built with Maven
3. ✅ GitHub release created
4. ✅ JAR file attached to release
5. ✅ Release notes generated

---

## 📊 Monitoring

**View Workflows:**
- Go to: `https://github.com/YOUR_ORG/AethorQuests/actions`
- See build status, logs, and artifacts

**Download Builds:**
- Development builds: Actions tab → Build workflow → Artifacts
- Releases: Releases page → Latest release → Download JAR

**Discord Notifications:**
- Every commit will post to your configured channel
- Includes: commit message, author, branch, files changed

---

## 🐛 Troubleshooting

### Build Fails
- **Check**: Is `aethornpcs-api-1.0.0.jar` in the repo?
- **Check**: Are Maven wrapper files (`.mvn/`, `mvnw`, `mvnw.cmd`) committed?
- **Check**: View Actions logs for detailed error messages

### Discord Not Working
- **Check**: Is `DISCORD_WEBHOOK` secret set correctly?
- **Check**: Is the webhook URL still valid in Discord?
- **Check**: View Actions logs for webhook errors

### Can't Push to GitHub
- **Check**: Does the repository exist on GitHub?
- **Check**: Do you have write access to the organization?
- **Check**: Is your Git authentication configured? (Use GitHub CLI or PAT)

---

## 📚 Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Discord Webhooks Guide](https://support.discord.com/hc/en-us/articles/228383668)
- [Semantic Versioning](https://semver.org/)
- [Git Tagging](https://git-scm.com/book/en/v2/Git-Basics-Tagging)

---

## ✨ Success Indicators

You'll know everything is working when:

1. ✅ Repository is visible on GitHub
2. ✅ Build badge shows "passing" in README
3. ✅ Discord receives commit notifications
4. ✅ First release appears in Releases page
5. ✅ JAR file is downloadable from release

---

**Ready to publish? Run `.\publish.ps1` to get started!**
