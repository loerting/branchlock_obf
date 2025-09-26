FROM php:8.4-fpm-alpine as base

# Arguments defined in docker-compose.yml
ARG user
ARG uid


# Install system dependencies
RUN apk update && apk add --no-cache \
    openssl \
    git \
    curl \
    libzip-dev \
    libpng-dev \
    oniguruma-dev \
    icu-dev \
    libxml2-dev \
    autoconf \
    pkgconf \
    openssl-dev \
    zip \
    unzip \
    supervisor \
    wget \
    gnupg \
    libmcrypt-dev \
    hiredis-dev \
    zlib-dev

# Add Amazon Corretto repository and install Corretto
RUN wget -O /etc/apk/keys/amazoncorretto.rsa.pub https://apk.corretto.aws/amazoncorretto.rsa.pub && \
    echo "https://apk.corretto.aws/" >> /etc/apk/repositories && \
    apk update
RUN apk add amazon-corretto-24

# Clear cache
RUN rm -rf /var/cache/apk/*

# Install build tools and compilers
RUN apk add --no-cache build-base

# Install PHP extensions
RUN docker-php-ext-install bcmath mbstring intl opcache exif pcntl gd zip

# Install additional PHP extensions for Laravel, Redis, and MongoDB
RUN pecl install redis mongodb && \
    docker-php-ext-enable redis mongodb

# Get latest Composer
COPY --from=composer:latest /usr/bin/composer /usr/bin/composer

RUN echo 'pm.max_children = 15' >> /usr/local/etc/php-fpm.d/zz-docker.conf

# Create system user to run Composer and Artisan Commands
RUN adduser -u $uid -D -h /home/$user $user \
    && adduser $user www-data \
    && mkdir -p /home/$user/.composer \
    && chown -R $user:$user /home/$user /var/www/html

# Set working directory
WORKDIR /var/www/html

USER $user