# Publishing Guide

This guide explains how to publish the AI Mock Data Generator library to Maven Central and other repositories.

## Prerequisites

1. **Sonatype Account**: Create an account at https://issues.sonatype.org/
2. **GPG Key**: Generate a GPG key for signing artifacts
3. **Maven Settings**: Configure `~/.m2/settings.xml` with Sonatype credentials

## Step 1: Configure Maven Settings

Create or update `~/.m2/settings.xml`:

```xml
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>YOUR_SONATYPE_USERNAME</username>
      <password>YOUR_SONATYPE_PASSWORD</password>
    </server>
  </servers>
</settings>
```

## Step 2: Generate GPG Key (Optional for Maven Central)

GPG signing is **optional by default** in the pom.xml. To enable it for Maven Central publishing:

```bash
# Generate a new GPG key
gpg --gen-key

# List your keys
gpg --list-keys

# Export your public key
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
```

**Note:** The build will skip GPG signing by default. To enable signing, use:
```bash
mvn clean deploy -Dgpg.skip=false
```

Or set the property in your `~/.m2/settings.xml` or command line.

## Step 3: Update pom.xml

Before publishing, update the following in `pom.xml`:

1. Replace `yourusername` with your actual GitHub username
2. Update developer information (name, email)
3. Update repository URLs if needed

## Step 4: Build and Test

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package the library
mvn package

# Install to local repository
mvn install
```

## Step 5: Publish to Maven Central

### For Snapshot Releases

```bash
mvn clean deploy
```

### For Release Versions

1. **Update Version**: Change version in `pom.xml` (e.g., from `1.0.0-SNAPSHOT` to `1.0.0`)

2. **Create Release Tag**:
   ```bash
   git tag -a v1.0.0 -m "Release version 1.0.0"
   git push origin v1.0.0
   ```

3. **Deploy to Staging**:
   ```bash
   # Without GPG signing (for testing)
   mvn clean deploy
   
   # With GPG signing (required for Maven Central)
   mvn clean deploy -Dgpg.skip=false
   ```

4. **Verify in Sonatype Nexus**: 
   - Go to https://oss.sonatype.org/
   - Login and check "Staging Repositories"
   - Close and release the staging repository

5. **Update Version for Next Development**:
   - Change version back to `1.0.1-SNAPSHOT` in `pom.xml`

## Publishing to Other Repositories

### GitHub Packages

Add to `pom.xml`:

```xml
<distributionManagement>
   <repository>
     <id>github</id>
     <name>GitHub Packages</name>
     <url>https://maven.pkg.github.com/YOUR_USERNAME/ai-mock-data-gen</url>
   </repository>
</distributionManagement>
```

Configure `~/.m2/settings.xml`:

```xml
<servers>
  <server>
    <id>github</id>
    <username>YOUR_GITHUB_USERNAME</username>
    <password>YOUR_GITHUB_TOKEN</password>
  </server>
</servers>
```

Then deploy:
```bash
mvn clean deploy
```

### JitPack

JitPack automatically builds from GitHub releases. Just:

1. Create a GitHub release
2. Users can add to their `pom.xml`:
   ```xml
   <repository>
     <id>jitpack.io</id>
     <url>https://jitpack.io</url>
   </repository>
   ```
   
   ```xml
   <dependency>
     <groupId>com.github.YOUR_USERNAME</groupId>
     <artifactId>ai-mock-data-gen</artifactId>
     <version>v1.0.0</version>
   </dependency>
   ```

## Versioning

Follow Semantic Versioning (MAJOR.MINOR.PATCH):
- **MAJOR**: Breaking changes
- **MINOR**: New features, backward compatible
- **PATCH**: Bug fixes, backward compatible

## Checklist Before Publishing

- [ ] All tests pass
- [ ] Code compiles without warnings
- [ ] Javadoc is complete
- [ ] README.md is up to date
- [ ] Version number is correct
- [ ] License information is correct
- [ ] Developer information is correct
- [ ] Repository URLs are correct
- [ ] GPG key is configured
- [ ] Maven settings are configured

## Troubleshooting

### GPG Signing Issues

**GPG signing is optional by default.** The build will skip signing if GPG is not available.

If you encounter GPG signing errors when trying to sign:
```bash
# Check if GPG is installed
gpg --version

# Check if GPG agent is running
gpg-agent --daemon

# Test signing
echo "test" | gpg --clearsign

# Skip GPG signing if not needed
mvn clean deploy -Dgpg.skip=true
```

**To enable GPG signing:**
```bash
# Enable GPG signing for deployment
mvn clean deploy -Dgpg.skip=false
```

**Common GPG Issues:**
- If GPG is not installed, the build will skip signing automatically
- If you get "gpgVersion is null", ensure GPG is installed: `brew install gnupg` (macOS) or `apt-get install gnupg` (Linux)
- For Maven Central, GPG signing is required, so ensure GPG is properly configured

### Authentication Issues

Verify your credentials in `~/.m2/settings.xml` and ensure your Sonatype account has the necessary permissions.

### Staging Repository Issues

If staging fails:
1. Check Sonatype Nexus for error messages
2. Ensure all required files are present (sources, javadoc, signatures)
3. Verify GPG key is published to keyservers

## Additional Resources

- [Maven Central Publishing Guide](https://central.sonatype.org/publish/publish-guide/)
- [Sonatype OSSRH](https://central.sonatype.org/publish/publish-guide/)
- [GPG Documentation](https://www.gnupg.org/documentation/)

