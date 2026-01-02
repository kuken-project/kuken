# Security Policy

## Supported Versions

Küken is currently under active development. The following versions receive security updates:

| Version | Supported          |
| ------- | ------------------ |
| main    | :white_check_mark: |
| < 1.0   | :x:                |

## Reporting a Vulnerability

The security of Küken and its users is our top priority. We appreciate your efforts to responsibly disclose your findings
and will make every effort to acknowledge your contributions.

### How to Report

**Please do not report security vulnerabilities through public GitHub issues.**

Instead, please report security vulnerabilities by emailing the maintainer directly or through GitHub's private vulnerability reporting feature:

1. **GitHub Security Advisories** (Recommended)
    - Go to the [Security tab](https://github.com/devnatan/Küken/security/advisories)
    - Click "Report a vulnerability"
    - Fill out the form with details about the vulnerability

2. **Direct Email**
    - Contact the maintainer directly through their GitHub profile
    - Include "Küken Security" in the subject line

### What to Include

Please include the following information in your report:

- Type of vulnerability (e.g., authentication bypass, SQL injection, XSS, server command injection)
- Full paths of source file(s) related to the vulnerability
- Location of the affected source code (tag/branch/commit or direct URL)
- Step-by-step instructions to reproduce the issue
- Proof-of-concept or exploit code (if possible)
- Impact of the vulnerability, including how an attacker might exploit it
- Any potential mitigations you've identified

### What to Expect

- **Acknowledgment**: We will acknowledge receipt of your vulnerability report within 48 hours
- **Updates**: We will keep you informed about the progress of fixing the vulnerability
- **Timeline**: We aim to patch critical vulnerabilities within 7 days and other vulnerabilities within 30 days
- **Credit**: We will credit you in the security advisory (unless you prefer to remain anonymous)
- **Disclosure**: We follow a coordinated disclosure process and will work with you to determine an appropriate disclosure timeline

## Security Considerations for Game Server Management

When deploying Küken to manage game servers, please consider the following security best practices:

### Authentication & Authorization

- Always use strong passwords or SSH keys for authentication
- Implement proper role-based access control (RBAC)
- Regularly review user permissions and remove unnecessary access
- Enable two-factor authentication when available

### Network Security

- Use firewalls to restrict access to management interfaces
- Isolate game servers in separate network segments when possible
- Use VPNs or secure tunnels for remote management
- Keep all ports closed except those required for game operation

### Server Configuration

- Run game servers with minimal privileges (non-root users)
- Keep server software and dependencies up to date
- Regularly audit server configurations
- Disable unnecessary services and features
- Use encrypted connections (TLS/SSL) for the Web UI

### Monitoring & Logging

- Enable comprehensive logging for all management operations
- Monitor for suspicious activity or unauthorized access attempts
- Set up alerts for critical security events
- Regularly review audit logs

### Data Protection

- Encrypt sensitive data at rest and in transit
- Regularly backup server configurations and data
- Implement proper key management practices
- Follow data retention policies

### Blueprint Security

When using Küken blueprints to automate server deployment:

- Review blueprint scripts before execution
- Only use blueprints from trusted sources
- Validate input parameters to prevent injection attacks
- Use version control for blueprint management
- Test blueprints in isolated environments first

## Known Security Limitations

As Küken is in active development, please be aware of the following:

- Pre-1.0 versions should not be used in production environments without thorough security review
- The API authentication mechanism is still evolving
- Some features may not have complete input validation

## Security Updates

Security updates will be announced through:

- GitHub Security Advisories
- Release notes
- The project's README

We strongly recommend watching the repository for security updates.

## Scope

This security policy applies to:

- Küken server backend
- Küken web interface
- Küken CLI tools
- Official blueprints
- Documentation that could lead to security issues

## Bug Bounty Program

Currently, Küken does not have a bug bounty program. However, we deeply appreciate security researchers who help improve
our security posture. Significant contributions may be recognized in our documentation and release notes.

## Contact

For general security questions or concerns that are not vulnerabilities, please open a discussion in the [GitHub Discussions](https://github.com/devnatan/kuken/discussions) section.

Thank you for helping keep Küken and its users safe!