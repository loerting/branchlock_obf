<?php

namespace App\Models;

use MongoDB\Laravel\Eloquent\Model;

class Queue extends Model
{
    protected $table = 'obf_queue';

    const STATUS_PENDING = 0;
    const STATUS_PROCESSING = 1;
    const STATUS_COMPLETED = 2;
    const STATUS_FAILED = 3;

}
