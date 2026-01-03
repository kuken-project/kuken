use anyhow::Result;
use clap::Parser;

mod cli;
mod commands;

#[tokio::main]
async fn main() -> Result<()> {
    let cli = cli::Cli::parse();
    commands::execute(cli.command).await
}
