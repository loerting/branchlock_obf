<?php

return [
    'general' => [
        'target_version' => [
            'type' => 'select',
            'frontend_name' => 'Target Java Version',
            'premium' => false,
            'desktop' => true,
            'android' => true,
            'options' => \App\Helpers\CustomHelper::getJavaVersions(23),
            'value' => 65,
        ],
        'name_iterator' => [
            'type' => 'select',
            'frontend_name' => 'Member Name Generation Mode',
            'premium' => false,
            'desktop' => true,
            'android' => true,
            'options' => [
                'alphabetic' => 'Uppercase and Lowercase Letters (ABCabc)',
                'ls-and-is' => 'Lowercase Ls and Is (lIli)',
                'non-printable' => 'Non-Printable Characters',
                'arabic' => 'Arabic Character Set (عخئ)',
                'chinese' => 'Chinese Character Set (勇和乐)',
                'keywords' => 'Java Keywords (switch, if)',
                'ideographic' => 'Ideographs (豈 更 車)',
                'rtl' => 'RTL & LTR (Mixes right-to-left characters into the latin alphabet)'
            ],
            'value' => 'chinese',
        ],
        'random_seed' => [
            'type' => 'text',
            'frontend_name' => 'Random Seed for Generation',
            'premium' => false,
            'desktop' => true,
            'android' => true,
            'value' => \Illuminate\Support\Str::random(10),
        ],
        'no_watermark' => [
            'type' => 'checkbox',
            'frontend_name' => 'Disable Watermarking',
            'premium' => true,
            'desktop' => true,
            'android' => true,
            'value' => false,
        ],
        'remove_empty_directories' => [
            'type' => 'checkbox',
            'frontend_name' => 'Remove Empty Directories',
            'premium' => false,
            'desktop' => true,
            'android' => true,
            'value' => false,
        ],
        'no_compress' => [
            'type' => 'checkbox',
            'frontend_name' => 'Disable Compression',
            'premium' => false,
            'desktop' => true,
            'android' => true,
            'value' => false,
        ],
        'load_inner_jars' => [
            'type' => 'checkbox',
            'frontend_name' => 'Load inner jar files from the input file as libraries',
            'premium' => false,
            'desktop' => true,
            'android' => false,
            'value' => false,
        ],
        'disable_class_limit' => [
            'type' => 'checkbox',
            'frontend_name' => 'Disable Class Limitation',
            'premium' => true,
            'desktop' => true,
            'android' => true,
            'value' => false,
        ],
    ]
];
