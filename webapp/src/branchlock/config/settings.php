<?php

return [
    // url from APP_URL
    'url' => env('APP_URL', 'http://localhost'),

    'name' => env('APP_NAME', 'Branchlock'),

    'news_email' => 'news@branchlock.net',

    'max_storage_time' => 6,

    'postmark_secret' => env('POSTMARK_TOKEN'),
    'h_captcha_secret' => env('H_CAPTCHA_SECRET', '0x0000000000000000000000000000000000000000'),
    'h_captcha_sitekey' => env('H_CAPTCHA_SITEKEY', '10000000-ffff-ffff-ffff-000000000001'),

    'sellix_key' => env('SELLIX_KEY'),
    'sellix_webhook_secret' => env('SELLIX_WEBHOOK_SECRET'),

    'payment_methods' => [
        'PAYPAL' => [
            'name' => 'PayPal',
            'icon' => '/img/payment/paypal.png',
        ],
        /*'STRIPE' => [
            'name' => 'Credit card (Stripe)',
            'icon' => '/img/payment/creditcard.png',
        ],
        'BITCOIN' => [
            'name' => 'Bitcoin',
            'icon' => '/img/payment/bitcoin.png',
        ],*/
        /*'ETHEREUM' => [
            'name' => 'Ethereum',
            'icon' => '/img/payment/ethereum.png',
        ],
        'BITCOIN_CASH ' => [
            'name' => 'Bitcoin Cash',
            'icon' => '/img/payment/bitcoin-cash.png',
        ],
        'LITECOIN ' => [
            'name' => 'Litecoin',
            'icon' => '/img/payment/litecoin.png',
        ],
        'MONERO' => [
            'name' => 'Monero',
            'icon' => '/img/payment/monero.png',
        ],*/
    ],

    'oauth_providers' => [
        'google' => [
            'provider' => 'google',
            'name' => 'Google',
            'icon' => '/img/social/google.png',
        ],
        'github' => [
            'provider' => 'github',
            'name' => 'GitHub',
            'icon' => '/img/social/github.png',
        ],
        'gitlab' => [
            'provider' => 'gitlab',
            'name' => 'GitLab',
            'icon' => '/img/social/gitlab.svg',
        ],
        'bitbucket' => [
            'provider' => 'bitbucket',
            'name' => 'Bitbucket',
            'icon' => '/img/social/bitbucket.png',
        ],
        /*'twitter-oauth-2' => [
            'provider' => 'twitter-oauth-2',
            'name' => 'Twitter',
            'icon' => '/img/social/twitter.png',
        ],*/
    ],
];
