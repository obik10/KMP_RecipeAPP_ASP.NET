# Setup Instructions

## Requirements
- Docker Desktop
- DBeaver
- MySQL 8 (via Docker)

## Run MySQL
docker run --name mysql_recipes -e MYSQL_ROOT_PASSWORD=secret123 -e MYSQL_DATABASE=recipe_app -p 3306:3306 -v mysql_recipes_data:/var/lib/mysql -d mysql:8.0

## Database Schema
See docs/schema.sql
