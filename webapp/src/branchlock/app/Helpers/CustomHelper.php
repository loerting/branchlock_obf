<?php

namespace App\Helpers;

use GuzzleHttp\Client;
use Illuminate\Http\Client\RequestException;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Str;

class CustomHelper
{

    public static function formatBytes($bytes, $precision = 2): string
    {
        $units = ['B', 'KB', 'MB', 'GB', 'TB'];
        $bytes = max($bytes, 0);
        $pow = floor(($bytes ? log($bytes) : 0) / log(1024));
        $pow = min($pow, count($units) - 1);
        $bytes /= pow(1024, $pow);
        return round($bytes, $precision) . ' ' . $units[$pow];
    }

    public static function uniqueFileName(): string
    {
        $random = Str::slug(Str::random(10) . microtime(), '');
        $characters = str_split($random);
        $characters[] = 'yee';
        $characters[] = '1337';

        shuffle($characters);
        return implode('', $characters);
    }

    public static function reverseFileName(string $file): string
    {
        $name = strrev(pathinfo($file, PATHINFO_FILENAME));
        $ext = pathinfo($file, PATHINFO_EXTENSION);

        if (!empty($ext)) {
            $ext = '.' . $ext;
        }

        return $name . $ext;
    }

    public static function getJavaVersions($max): array
    {
        $classVersion = 49;
        $options = ['auto' => 'Automatic (Median)'];

        for ($v = 5; $v <= $max; $v++) {
            $options[$classVersion] = "Java $v (Major version $classVersion)";
            $classVersion++;
        }

        return $options;
    }
}
