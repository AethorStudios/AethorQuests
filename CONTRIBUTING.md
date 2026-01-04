# Contributing to AethorQuests

Thank you for your interest in contributing to AethorQuests! This document provides guidelines for contributing to the project.

## Getting Started

1. Fork the repository
2. Clone your fork: `git clone https://github.com/YOUR_USERNAME/AethorQuests.git`
3. Create a feature branch: `git checkout -b feature/amazing-feature`
4. Make your changes
5. Test your changes thoroughly
6. Commit your changes: `git commit -m "Add amazing feature"`
7. Push to your fork: `git push origin feature/amazing-feature`
8. Open a Pull Request

## Development Requirements

- Java 21 or higher
- Maven 3.6+
- Paper 1.21.8 test server (recommended)
- AethorNPCS plugin for testing

## Code Style

- Follow Java naming conventions
- Use 4 spaces for indentation (not tabs)
- Add JavaDoc comments for public methods
- Keep methods focused and concise
- Use meaningful variable names

## Testing

Before submitting a PR:
1. Build the plugin: `./mvnw clean package`
2. Test on a Paper 1.21.8 server
3. Verify quest creation, editing, and completion
4. Test NPC interactions
5. Check for console errors

## Pull Request Process

1. Update the README.md if you're adding features
2. Update QUICK_REFERENCE.md if adding commands
3. Ensure your code builds without errors
4. Provide a clear description of changes
5. Link any related issues

## Reporting Bugs

Use the [GitHub Issues](https://github.com/YOUR_ORG/AethorQuests/issues) page:

**Include:**
- Server version (Paper build number)
- Plugin version
- Steps to reproduce
- Expected behavior
- Actual behavior
- Error logs (if any)

## Feature Requests

We welcome feature ideas! Open an issue with:
- Clear description of the feature
- Use case / why it's needed
- Possible implementation ideas (optional)

## Code of Conduct

- Be respectful and constructive
- Welcome newcomers
- Focus on what's best for the project
- Accept constructive criticism gracefully

## Questions?

Feel free to open an issue or reach out on Discord!
