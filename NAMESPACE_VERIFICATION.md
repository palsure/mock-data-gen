# Namespace Verification Guide

## Current Namespace: `com.mock.data.gen`

To verify ownership of this namespace for Maven Central publishing, you need to create a DNS TXT record.

## DNS TXT Record Setup

### Option 1: If you own the domain `mock.data.gen`

1. **Create DNS TXT Record:**
   - **Host/Name**: `_maven.com.mock.data.gen` (or as instructed by Sonatype)
   - **Type**: TXT
   - **Value**: `m46mmmpd5w`
   - **TTL**: 3600 (or default)

2. **Verify the record:**
   ```bash
   dig TXT _maven.com.mock.data.gen
   # or
   nslookup -type=TXT _maven.com.mock.data.gen
   ```

3. **Complete verification in Sonatype OSSRH:**
   - Go to https://issues.sonatype.org/
   - Complete the namespace verification process

### Option 2: Use GitHub Namespace (Recommended - Easier)

If you don't own the domain `mock.data.gen`, use the GitHub namespace instead:

**Change groupId in pom.xml:**
```xml
<groupId>io.github.palsure</groupId>
```

**Benefits:**
- No DNS configuration needed
- Verification is done through GitHub account ownership
- Easier setup process

**Verification Steps for GitHub Namespace:**
1. Create a public repository on GitHub (if not exists)
2. Request namespace ownership through Sonatype JIRA
3. Sonatype will verify your GitHub account ownership
4. No DNS records needed!

### Option 3: Use Your Own Domain

If you own a domain (e.g., `example.com`), you can use:
```xml
<groupId>com.example.mockdata</groupId>
```

Then create DNS TXT record:
- **Host**: `_maven.com.example.mockdata`
- **Value**: `m46mmmpd5w`

## Current Verification Key

**Verification Key**: `m46mmmpd5w`

## Recommended Action

Since you're using GitHub (`palsure/mock-data-gen`), I recommend switching to:

```xml
<groupId>io.github.palsure</groupId>
```

This eliminates the need for DNS configuration and is the easiest path for Maven Central publishing.

## Next Steps

1. **If using `com.mock.data.gen`**: Create the DNS TXT record as described above
2. **If switching to `io.github.palsure`**: Update pom.xml and request namespace through Sonatype JIRA
3. Complete the Sonatype OSSRH setup process
4. Verify namespace ownership
5. Proceed with publishing

## Sonatype OSSRH Resources

- **JIRA**: https://issues.sonatype.org/
- **Documentation**: https://central.sonatype.org/publish/publish-guide/
- **Namespace Verification**: https://central.sonatype.org/namespace/


