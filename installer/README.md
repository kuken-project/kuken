# Küken Installer

## Linux

Bash (Ubuntu, Debian, Fedora, etc.)
```shell
curl -fsSL https://raw.githubusercontent.com/devnatan/kuken/main/installer/installer.sh | bash
```
```shell
wget -qO- https://raw.githubusercontent.com/devnatan/kuken/main/installer/installer.sh | bash
```

Manual installation
```shell
curl -fsSL https://raw.githubusercontent.com/devnatan/kuken/main/installer/installer.sh
cat install.sh
chmod +x install.sh
./install.sh
```
 
System installation
```shell
sudo INSTALL_DIR=/opt/kuken BIN_DIR=/usr/local/bin \
  bash -c "$(curl -fsSL https://raw.githubusercontent.com/devnatan/kuken/main/installer/installer.sh)"
```

## macOS

```shell
curl -fsSL https://raw.githubusercontent.com/devnatan/kuken/main/installer/installer.sh | bash
```

## Windows

```powershell
Invoke-WebRequest -Uri "https://raw.githubusercontent.com/devnatan/kuken/main/installer/installer.sh" -OutFile "install.sh"
& bash install.sh
```