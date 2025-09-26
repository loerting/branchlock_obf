<?php

return [
    'roles' => [
        'sandbox' => [

        ],
        'user' => [

        ],
        'admin' => [

        ],
    ],

    'status' => [
        'unconfirmed' => [

        ],
        'confirmed' => [

        ],
        'suspended' => [

        ],
    ],

    'plans' => [
        'free' => [
            'tier' => 0,
            'backend_name' => 'free',
            'name' => 'Demo',
            'color' => 'outline-secondary',
            'description' => 'Experience the power of our platform with the Demo plan – a free offering designed for testing purposes.',
            'price' => 0.00,
            'duration' => false,
            'limited' => false,
            'demo_mode' => true,
            'android' => true,
            'premium_tasks' => false,
            'experimental_tasks' => false,
            'max_projects' => 1,
            'concurrent_jobs' => 1,
            'cooldown' => 60,
            'maxFilesize' => 5,
            'queue_priority' => 'low',
            'features' => [
                'Commercial Use' => false,
                'Cooldown' => '60 seconds',
                'Max One Project' => 1,
                'One Concurrent Job' => 1,
                'Up to 5 MB per file' => 1,
                'Low Queue Priority' => 'low'
            ],
        ],

        'solo' => [
            'tier' => 1,
            'backend_name' => 'solo',
            'name' => 'Basic',
            'color' => 'primary',
            'description' => 'Perfect for solo developers and hobby projects, offers essential tools for your creative endeavors.',
            'price' => 106.80,
            'duration' => '365',
            'limited' => false,
            'demo_mode' => false,
            'android' => true,
            'premium_tasks' => true,
            'experimental_tasks' => true,
            'max_projects' => 5,
            'concurrent_jobs' => 1,
            'cooldown' => 2,
            'maxFilesize' => 250,
            'queue_priority' => 'medium',
            'features' => [
                'Commercial Use' => true,
                'No Cooldown' => true,
                'Max 5 Projects' => 5,
                'One Concurrent Job' => 1,
                'Up to 80 MB per file' => 1,
                'Medium Queue Priority' => 'medium'
            ],

        ],

        'team' => [
            'tier' => 2,
            'backend_name' => 'team',
            'name' => 'Professional',
            'color' => 'success',
            'description' => 'Ideal for teams and big projects, provides efficient and comprehensive resources for collaborative success.',
            'price' => 298.80,
            'duration' => '365',
            'limited' => false,
            'demo_mode' => false,
            'android' => true,
            'premium_tasks' => true,
            'experimental_tasks' => true,
            'max_projects' => 30,
            'concurrent_jobs' => 5,
            'cooldown' => 2,
            'maxFilesize' => 250,
            'queue_priority' => 'high',
            'features' => [
                'Commercial Use' => true,
                'No Cooldown' => true,
                'Max 30 Projects' => 30,
                '5 Concurrent Jobs' => 5,
                'Up to 250 MB per file' => 1,
                'High Queue Priority' => 'high'
            ],
        ],

        'enterprise' => [
            'tier' => 3,
            'backend_name' => 'enterprise',
            'name' => 'Enterprise',
            'color' => 'success',
            'description' => 'Tailored for companies, delivers a robust infrastructure for scalable and secure development operations.',
            'price' => 598.80,
            'duration' => '365',
            'limited' => false,
            'demo_mode' => false,
            'android' => true,
            'premium_tasks' => true,
            'experimental_tasks' => true,
            'max_projects' => 280,
            'concurrent_jobs' => 25,
            'cooldown' => 2,
            'maxFilesize' => 400,
            'queue_priority' => 'high',
            'features' => [
                'Commercial Use' => true,
                'No Cooldown' => true,
                'Unlimited Projects' => true,
                '25 Concurrent Jobs' => 25,
                'Up to 400 MB per file' => 1,
                'Highest Queue Priority' => 'high'
            ],
        ],

    ],

    'legacy_plans' => [
        'solo' => [
            'price' => 64.00,
        ],
        'team' => [
            'price' => 192.00,
        ],
        'enterprise' => [
            'price' => 385.00,
        ],
    ]
];
