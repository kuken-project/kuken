# cli

> [!CAUTION]
> - Always download from official [GitHub Releases](https://github.com/devnatan/kuken/releases)
> - Verify checksums for security (SHA256 available in releases)

Download the latest release for your platform from [GitHub Releases](https://github.com/devnatan/kuken/releases).

## Manual Installation

### Linux

```bash
curl -LO https://github.com/devnatan/kuken/releases/download/v0.1.0/kuken-linux-amd64
chmod +x kuken-linux-amd64
sudo mv kuken-linux-amd64 /usr/local/bin/kuken
kuken --version
```

### macOS

```bash
curl -LO https://github.com/devnatan/kuken/releases/download/v0.1.0/kuken-darwin-arm64

chmod +x kuken-darwin-arm64
sudo mv kuken-darwin-arm64 /usr/local/bin/kuken
kuken --version
```

### Windows

```powershell
# Download (replace VERSION with the latest version)
Invoke-WebRequest -Uri "https://github.com/devnatan/kuken/releases/download/v0.1.0/kuken-windows.exe" -OutFile "kuken.exe"

# Move to a directory in PATH (e.g., C:\Program Files\Kuken)
Move-Item kuken.exe "C:\Program Files\Kuken\kuken.exe"

# Add to PATH if needed
[Environment]::SetEnvironmentVariable("Path", $env:Path + ";C:\Program Files\Kuken", "Machine")

# Verify installation (restart PowerShell first)
kuken --version
```


## Uninstallation

### Linux / macOS

```bash
sudo rm /usr/local/bin/kuken
```

### Windows

```powershell
Remove-Item "C:\Program Files\Kuken\kuken.exe"
```

## Getting Started

After installation, initialize your Kuken configuration:

```bash
kuken init
```

For available commands:

```bash
kuken help
```

## Troubleshooting

### Permission Denied (Linux/macOS)

If you get a permission denied error:
```bash
chmod +x kuken
```

### Command Not Found

Make sure the installation directory is in your PATH:

**Linux/macOS:**
```bash
echo $PATH
```

**Windows:**
```powershell
$env:Path
```

### macOS "Cannot be opened because the developer cannot be verified"

Run:
```bash
xattr -d com.apple.quarantine /usr/local/bin/kuken
```