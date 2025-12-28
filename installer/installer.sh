#!/usr/bin/env bash

set -e

# Colors and Formatting
CL_RED='\033[0;31m'
CL_RESET='\033[0m'
CL_BLUE='\033[0;34m'
CL_BOLD='\033[1m'
IC_ARROW='￫'
IC_X='✖'

OS=""
ARCH=""
INSTALL_DIR="${INSTALL_DIR:-$HOME/.kuken}"
BIN_DIR="${BIN_DIR:-$HOME/.local/bin}"

# --- Logging ---
print() {
	echo -e "${CL_BLUE}${CL_BOLD}${IC_ARROW}${CL_RESET} $1"
}

err() {
	echo -e "${CL_RED}${CL_BOLD}${IC_X}${CL_RESET} $1"
}

# --- OS/Architecture detection ---
detect_os() {
	case "$(uname -s)" in
		Linux*)
			OS="linux"
			;;
		Darwin*)
			OS="darwin"
			;;
		CYGWIN*|MINGW*|MSYS*)
			OS="windows"
			;;
		*)
			err "Unsupported operating system: $(uname -s)"
			exit 1
			;;
	esac
}

detect_arch() {
	case "$(uname -m)" in
		x86_64|amd64)
			ARCH="amd64"
			;;
		aarch64|arm64)
			ARCH="arm64"
			;;
		armv7l|armhf)
			ARCH="armv7"
			;;
		*)
			err "Unsupported architecture: $(uname -m)"
			exit 1
			;;
	esac
}

# --- Dependencies ---
check_java() {
	if command -v java &> /dev/null; then
		local java_version
		java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)

		if [[ "$java_version" -ge 26 ]]; then
			print "Java $java_version found, but Java 25+ is recommended to run Küken"
		fi

		print "Detected Java version $java_version"
	else
		err ""
		err "Java not found! Java 25 or higher is required to run Küken"
		err ""

		read -p "Continue installation anyway? [y/N] " -n 1 -r
		echo
		if [[ ! $REPLY =~ ^[Yy]$ ]]; then
			exit 1
		fi
	fi
}

check_docker() {
	if command -v docker &> /dev/null; then
		if docker ps &> /dev/null; then
			local docker_version
			docker_version=$(docker --version | awk '{print $3}' | sed 's/,//')
			print "Detected Docker version: $docker_version"
			return 0
		else
			err "Docker is installed but not running"
			return 0
		fi
	else
		err ""
		err "Docker not found! Docker is required to run Küken"
		err ""

		read -p "Continue installation anyway? [y/N] " -n 1 -r
		echo
		if [[ ! $REPLY =~ ^[Yy]$ ]]; then
			exit 1
		fi
	fi
}

check_curl() {
	if ! command -v curl &> /dev/null; then
		err "cURL is required to install Küken"
		exit 1
	fi
}

# --- Installation ---
get_latest_release() {
	local response
	response=$(curl -s "https://api.github.com/repos/devnatan/kuken/releases/latest")
	local tag
	tag=$(echo "$response" | grep '"tag_name":' | sed -E 's/.*"tag_name": "([^"]+)".*/\1/')

	if [[ -z "$tag" ]]; then
		err "Could not fetch latest release information"
		exit 1
	fi

	echo "$tag"
}

get_download_url() {
	local version=$1
	local response
	response=$(curl -s "https://api.github.com/repos/devnatan/kuken/releases/tags/$version")
	local download_url
	download_url=$(echo "$response" | grep -o '"browser_download_url": "[^"]*\.jar"' | grep -o 'http[^"]*' | head -n1)

	echo "$download_url"
}

download_and_install() {
	local version=$1
	local download_url
	download_url=$(get_download_url "$version")

	print "Downloading Küken ${version}..."
	print "Installation directory: ${INSTALL_DIR}"

	mkdir -p "$INSTALL_DIR"
	local jar_file="$INSTALL_DIR/kuken.jar"
	curl -L --progress-bar "$download_url" -o "$jar_file"

	mkdir -p "$BIN_DIR"
	local launcher="$BIN_DIR/kuken"
	cat > "$launcher" << 'EOF'
#!/usr/bin/env bash

KUKEN_JAR="${KUKEN_INSTALL_DIR:-$HOME/.kuken}/kuken.jar"

if [[ ! -f "$KUKEN_JAR" ]]; then
	echo "Küken executable not found at $KUKEN_JAR"
	exit 1
fi

java -jar "$KUKEN_JAR" "$@"
EOF

	chmod +x "$launcher"
	print "Launcher created at $launcher"
}

main() {
  detect_os
  detect_arch

  print "Operating System: $OS ($ARCH)"

	check_curl
	check_java
	check_docker

  local version="$(get_latest_release)"
	download_and_install "$version"
}

main