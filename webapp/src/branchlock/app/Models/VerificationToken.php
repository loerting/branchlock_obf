<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use MongoDB\Laravel\Eloquent\Model;

class VerificationToken extends Model
{
    use HasFactory;

    protected $table = 'verification_tokens';

    protected $fillable = [
        'user_id',
        'token',
    ];
}
