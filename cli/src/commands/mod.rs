pub mod install;

use anyhow::Result;
use clap::Subcommand;

#[derive(Subcommand)]
pub enum Commands {
    Install {
        #[command(subcommand)]
        command: install::InstallCommands,
    },
}

pub async fn execute(command: Commands) -> Result<()> {
    match command {
        Commands::Install { command } => install::execute(command).await,
    }
}
