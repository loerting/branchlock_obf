<?php

namespace App\Models;

use MongoDB\Laravel\Eloquent\Model;

class Changelog extends Model
{
    protected $table = 'changelog';

    public static function getRunningVersion()
    {
        return self::where('running', true)->first();
    }

}
