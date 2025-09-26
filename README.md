# Branchlock

Java Obfuscator by Roman Kotov (Web-Dev) and Leonhard Kohl-Lörting (Backend)

# Installation Instructions

### Install Docker Runtime for your Distribution

Example: https://mpolinowski.github.io/docs/DevOps/Linux/2021-10-24--arch-linux-install-docker/2021-10-24/

### Configure Docker to run without sudo/root

Example for Arch: `sudo usermod -aG docker $USER`  
(Log in again afterwards)

### Install Docker Compose V2 (Important: as a plugin, since only that provides the latest version)

https://docs.docker.com/compose/install/linux/#install-using-the-repository

Check version: `docker compose version`  
It should show v2.2x.x

-----------

### Open a terminal in the project folder (webapp).

### Run once, or whenever dockerfiles change, or to update the images:

`docker compose pull`

`docker compose build app`

`docker compose build`

-------

### Start the Project

`docker compose up -d`

Give it some time to load (libraries will be installed, database will be imported, assets will be compiled automatically).

------------------

Accessible at http://127.0.0.1:8080/

### Stop the Project

`docker compose down`

-------

## Good to Know

### Clear Cache

`docker compose exec app php artisan optimize:clear`

### Container logs

`docker compose logs -f`

### Container logs (single container only)

`docker compose logs -f <container-name>`

### Show logs immediately during runtime start

`docker compose up`

### Laravel logs

src/branchlock/storage/logs/

### Branchlock files

src/branchlock/storage/app/private/branchlock-files/

-------

### Already automated (when starting with docker compose)

#### Import Database:

`docker compose exec mongo mongorestore --uri="mongodb://branchlock:password@mongo:27017/branchlock" --authenticationDatabase branchlock --drop /mongodump/branchlock`

#### Install Libraries

`docker compose exec app composer update`

#### Run Database Migrations

`docker compose exec app php artisan migrate`

#### Compile Assets

`docker compose run --rm npm`
