use anyhow::{Context, Result};
use clap::Args;
use console::style;
use dialoguer::{Input, theme::ColorfulTheme};
use indicatif::{MultiProgress, ProgressBar, ProgressStyle};
use reqwest;
use serde::Deserialize;
use std::fs;
use std::path::PathBuf;
use std::process::Command;
use std::time::Duration;

#[derive(Args)]
pub struct WebInstallArgs {
    /// Install in development mode
    #[arg(long)]
    dev: bool,

    /// Küken API server URL
    #[arg(long, value_name = "URL")]
    api_url: Option<String>,
}

#[derive(Deserialize)]
struct ApiHealthResponse {
    organization: Organization,
    production: bool,
    version: String,
}

#[derive(Deserialize)]
struct Organization {
    name: String,
}

const REPO_URL: &str = "https://github.com/devnatan/kuken.git";
const KUKEN_ORANGE: (u8, u8, u8) = (255, 116, 56); // #ff7438

pub async fn execute(args: WebInstallArgs) -> Result<()> {
    print_fancy_header();

    let multi = MultiProgress::new();

    let pb = create_spinner(&multi, "Checking Docker installation...");
    check_docker()?;
    pb.finish_and_clear();

    let mode = if args.dev {
        "development"
    } else {
        "production"
    };

    print_box(&format!(
        "Installing Web UI in {} mode",
        style(mode).bold().fg(custom_color(KUKEN_ORANGE))
    ));

    let api_url = get_api_url(args.api_url).await?;
    test_api_connection(&api_url, &multi).await;

    let pb = create_spinner(&multi, "Preparing installation directory...");
    let install_dir = get_install_dir()?;

    // Remove old installation if exists
    if install_dir.exists() {
        fs::remove_dir_all(&install_dir).context("Failed to remove old installation")?;
    }

    fs::create_dir_all(&install_dir).context("Failed to create installation directory")?;
    pb.finish_and_clear();
    println!(
        "{}  Directory: {}",
        style("✔").green(),
        style(install_dir.display()).dim()
    );

    clone_web_directory(&install_dir, &multi)?;

    let pb = create_spinner(&multi, "Generating configuration files...");
    create_env_file(&install_dir, &api_url)?;
    pb.finish_and_clear();
    println!("{}  Configuration files created", style("✔").green());

    let compose_file = if args.dev {
        "docker-compose.dev.yml"
    } else {
        "docker-compose.prod.yml"
    };
    start_docker_compose(&install_dir, compose_file, &multi)?;

    print_success_message();

    Ok(())
}

fn print_fancy_header() {
    let orange = custom_color(KUKEN_ORANGE);

    println!();
    println!(
        "{}",
        style("╔═════════════════════════════════════════════════════════════╗").fg(orange)
    );
    println!(
        "{}",
        style("║                                                             ║").fg(orange)
    );
    println!(
        "{}{}{}",
        style("║           ").fg(orange),
        style("       🐤 Küken Web UI Installer").bold().fg(orange),
        style("                  ║").fg(orange)
    );
    println!(
        "{}",
        style("║                                                             ║").fg(orange)
    );
    println!(
        "{}",
        style("╚═════════════════════════════════════════════════════════════╝").fg(orange)
    );
    println!();
}

fn print_box(message: &str) {
    let orange = custom_color(KUKEN_ORANGE);
    let width = 61;
    let padding = (width - console::measure_text_width(message)) / 2;

    println!(
        "{}",
        style("┌─────────────────────────────────────────────────────────────┐").fg(orange)
    );
    println!(
        "{}{}{}",
        style("│").fg(orange),
        " ".repeat(padding)
            + message
            + &" ".repeat(width - padding - console::measure_text_width(message)),
        style("│").fg(orange)
    );
    println!(
        "{}",
        style("└─────────────────────────────────────────────────────────────┘").fg(orange)
    );
}

fn print_success_message() {
    let orange = custom_color(KUKEN_ORANGE);

    println!();
    println!(
        "{}",
        style("╔═════════════════════════════════════════════════════════════╗")
            .fg(orange)
            .bold()
    );
    println!(
        "{}",
        style("║                                                             ║").fg(orange)
    );
    println!(
        "{}{}{}",
        style("║     ").fg(orange),
        style("    ⭐ Küken Web UI installed successfully! ⭐")
            .bold()
            .fg(orange),
        style("          ║").fg(orange)
    );
    println!(
        "{}",
        style("║                                                             ║").fg(orange)
    );
    println!(
        "{}",
        style("╚═════════════════════════════════════════════════════════════╝")
            .fg(orange)
            .bold()
    );
    println!();

    println!(
        "  {} Access the Web UI at: {}",
        style("→").fg(orange).bold(),
        style("http://localhost:3000")
            .cyan()
            .white()
            .underlined()
            .for_stdout()
    );
    println!();
}

fn clone_web_directory(install_dir: &PathBuf, multi: &MultiProgress) -> Result<()> {
    let pb = create_spinner(multi, "Cloning Web UI from repository...");

    let init_output = Command::new("git")
        .arg("init")
        .current_dir(install_dir)
        .output()
        .context("Failed to initialize git repository")?;

    if !init_output.status.success() {
        pb.finish_and_clear();
        anyhow::bail!("Failed to initialize git repository");
    }

    let remote_output = Command::new("git")
        .arg("remote")
        .arg("add")
        .arg("origin")
        .arg(REPO_URL)
        .current_dir(install_dir)
        .output()
        .context("Failed to add git remote")?;

    if !remote_output.status.success() {
        pb.finish_and_clear();
        anyhow::bail!("Failed to add git remote");
    }

    let sparse_output = Command::new("git")
        .arg("config")
        .arg("core.sparseCheckout")
        .arg("true")
        .current_dir(install_dir)
        .output()
        .context("Failed to enable sparse checkout")?;

    if !sparse_output.status.success() {
        pb.finish_and_clear();
        anyhow::bail!("Failed to enable sparse checkout");
    }

    // Configure sparse-checkout to only get /web directory
    let sparse_info_dir = install_dir.join(".git").join("info");
    fs::create_dir_all(&sparse_info_dir).context("Failed to create sparse-checkout directory")?;

    let sparse_checkout_file = sparse_info_dir.join("sparse-checkout");
    fs::write(&sparse_checkout_file, "web/*\n").context("Failed to write sparse-checkout file")?;

    pb.set_message("Downloading Web UI files...");

    // Pull only the web directory
    let pull_output = Command::new("git")
        .arg("pull")
        .arg("origin")
        .arg("main")
        .current_dir(install_dir)
        .output()
        .context("Failed to pull from repository")?;

    if !pull_output.status.success() {
        let stderr = String::from_utf8_lossy(&pull_output.stderr);
        pb.finish_and_clear();
        anyhow::bail!("Failed to pull from repository: {}", stderr);
    }

    // Move files from web/ subdirectory to root of install_dir
    let web_subdir = install_dir.join("web");
    if web_subdir.exists() {
        // Move all files from web/ to parent directory
        for entry in fs::read_dir(&web_subdir)? {
            let entry = entry?;
            let src = entry.path();
            let dst = install_dir.join(entry.file_name());

            // Skip if destination already exists (like .git)
            if !dst.exists() {
                fs::rename(&src, &dst)
                    .context(format!("Failed to move {:?}", entry.file_name()))?;
            }
        }

        // Remove empty web/ directory
        fs::remove_dir(&web_subdir).context("Failed to remove web subdirectory")?;
    }

    pb.finish_and_clear();
    println!("{}  Web UI files cloned successfully", style("✔").green());

    Ok(())
}

fn create_spinner(multi: &MultiProgress, message: &str) -> ProgressBar {
    let pb = multi.add(ProgressBar::new_spinner());
    pb.set_style(
        ProgressStyle::default_spinner()
            .tick_strings(&["⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"])
            .template("{spinner:.orange} {msg}")
            .unwrap(),
    );
    pb.set_message(message.to_string());
    pb.enable_steady_tick(Duration::from_millis(80));
    pb
}

fn custom_color(rgb: (u8, u8, u8)) -> console::Color {
    console::Color::Color256(16 + 36 * (rgb.0 / 51) + 6 * (rgb.1 / 51) + (rgb.2 / 51))
}

fn check_docker() -> Result<()> {
    if !Command::new("docker").arg("--version").output().is_ok() {
        anyhow::bail!(
            "{} Docker is not installed. Please install Docker first.",
            style("✗").red()
        );
    }

    if !Command::new("docker")
        .arg("info")
        .output()
        .map(|o| o.status.success())
        .unwrap_or(false)
    {
        anyhow::bail!(
            "{} Docker daemon is not running. Please start Docker.",
            style("✗").red()
        );
    }

    Ok(())
}

async fn get_api_url(provided_url: Option<String>) -> Result<String> {
    if let Some(url) = provided_url {
        return Ok(url);
    }

    println!();
    let theme = ColorfulTheme {
        values_style: console::Style::new()
            .for_stderr()
            .fg(custom_color(KUKEN_ORANGE)),
        active_item_style: console::Style::new()
            .for_stderr()
            .fg(custom_color(KUKEN_ORANGE))
            .bold(),
        success_prefix: style("✔".to_string() + " ").for_stderr().green(),
        ..ColorfulTheme::default()
    };

    let url: String = Input::with_theme(&theme)
        .with_prompt("Enter your Küken API server URL")
        .default("https://example.com".to_string())
        .interact_text()?;

    if url.is_empty() {
        anyhow::bail!("API URL is required");
    }

    Ok(url)
}

async fn test_api_connection(api_url: &str, multi: &MultiProgress) {
    let pb = create_spinner(
        multi,
        &format!("Testing connection to {}...", style(api_url).cyan()),
    );

    let client = reqwest::Client::builder()
        .danger_accept_invalid_certs(true)
        .timeout(Duration::from_secs(5))
        .build()
        .unwrap();

    match client.get(api_url).send().await {
        Ok(response) => {
            if response.status().is_success() {
                match response.json::<ApiHealthResponse>().await {
                    Ok(health) => {
                        pb.finish_and_clear();

                        println!();
                        let orange = custom_color(KUKEN_ORANGE);
                        println!(
                            "{}",
                            style(
                                "┌─────────────────────────────────────────────────────────────┐"
                            )
                            .fg(orange)
                        );
                        println!(
                            "{}{}{}",
                            style("│ ").fg(orange),
                            style(" API Connection successful!").white().bold(),
                            style("                                 │").fg(orange)
                        );
                        println!(
                            "{}",
                            style(
                                "├─────────────────────────────────────────────────────────────┤"
                            )
                            .fg(orange)
                        );
                        println!(
                            "{}  {} {}{}",
                            style("│").fg(orange),
                            style("Organization:").dim(),
                            style(&health.organization.name).bold(),
                            " ".repeat(62 - 17 - health.organization.name.len())
                                + &style("│").fg(orange).to_string()
                        );
                        println!(
                            "{}  {} {}{}",
                            style("│").fg(orange),
                            style("Version:     ").dim(),
                            style(&health.version).bold(),
                            " ".repeat(62 - 17 - health.version.len())
                                + &style("│").fg(orange).to_string()
                        );
                        let mode_text = if health.production {
                            "Production"
                        } else {
                            "Development"
                        };
                        println!(
                            "{}  {} {}{}",
                            style("│").fg(orange),
                            style("Environment: ").dim(),
                            style(mode_text).bold(),
                            " ".repeat(62 - 17 - mode_text.len())
                                + &style("│").fg(orange).to_string()
                        );
                        println!(
                            "{}",
                            style(
                                "└─────────────────────────────────────────────────────────────┘"
                            )
                            .fg(orange)
                        );
                        println!();
                    }
                    Err(_) => {
                        pb.finish_with_message(format!(
                            "{} API responded but format is unexpected",
                            style("⚠").yellow()
                        ));
                    }
                }
            } else {
                pb.finish_with_message(format!(
                    "{} API responded with status: {}",
                    style("⚠").yellow(),
                    response.status()
                ));
            }
        }
        Err(e) => {
            pb.finish_and_clear();
            println!(
                "{}  Could not reach API: {}",
                style("⚠").yellow(),
                style(e.to_string()).dim()
            );
            println!("   Continuing anyway... You can configure this later.",);
        }
    }
}

fn get_install_dir() -> Result<PathBuf> {
    let home = dirs::home_dir().context("Could not determine home directory")?;
    Ok(home.join(".kuken").join("web"))
}

fn create_env_file(install_dir: &PathBuf, api_url: &str) -> Result<()> {
    let env_content = format!("VITE_KUKEN_API={}\n", api_url);
    let env_path = install_dir.join(".env");

    fs::write(&env_path, env_content).context("Failed to create .env file")?;

    Ok(())
}

fn start_docker_compose(
    install_dir: &PathBuf,
    compose_file: &str,
    multi: &MultiProgress,
) -> Result<()> {
    let pb = create_spinner(multi, "Starting Docker containers...");

    let output = Command::new("docker")
        .arg("compose")
        .arg("-f")
        .arg(compose_file)
        .arg("up")
        .arg("-d")
        .current_dir(install_dir)
        .output()
        .context("Failed to start Docker Compose")?;

    if !output.status.success() {
        let stderr = String::from_utf8_lossy(&output.stderr);
        pb.finish_with_message(format!("{} Failed to start containers", style("✗").red()));
        anyhow::bail!("Docker compose error: {}", stderr);
    }

    pb.finish_and_clear();
    println!("{}  Docker containers started", style("✔").green());

    Ok(())
}
