from .seed import seed_data
import click
from flask.cli import AppGroup

def init_seed_cli(app):
    # Configure CLI command for seeding
    seed_cli = AppGroup('seed', help='Commands for populating the database with initial data')

    @seed_cli.command('init', help='Populates the database with initial data')
    @click.option('--force', is_flag=True, help='Drops existing data before seeding')
    def seed_init(force):
        result = seed_data(app, force=force)
        if result:
            click.echo(click.style('Data seeding completed successfully', fg='green'))
        else:
            click.echo(click.style('Error during data seeding', fg='red'))
            raise click.Abort()

    app.cli.add_command(seed_cli)
