pub mod web;

use anyhow::Result;
use clap::Subcommand;

#[derive(Subcommand)]
pub enum InstallCommands {
    Web(web::WebInstallArgs),
}

pub async fn execute(command: InstallCommands) -> Result<()> {
    match command {
        InstallCommands::Web(args) => web::execute(args).await,
    }
}
