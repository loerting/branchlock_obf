<?php

namespace App\Models;

use App\Notifications\PasswordReset;
use Illuminate\Database\Eloquent\BroadcastsEvents;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Notifications\Notifiable;
use Illuminate\Support\Facades\Hash;
use Jenssegers\Agent\Agent;
use Laravel\Sanctum\HasApiTokens;
use MongoDB\Laravel\Auth\User as Authenticatable;


class User extends Authenticatable
{
    use HasApiTokens, HasFactory, Notifiable, BroadcastsEvents;


    public $timestamps = true;

    const STATUS_UNCONFIRMED = 'unconfirmed';
    const STATUS_CONFIRMED = 'confirmed';
    const STATUS_SUSPENDED = 'suspended';

    const PLAN_FREE = 'free';
    const PLAN_SOLO = 'solo';
    const PLAN_GROUP = 'team';
    const PLAN_ENTERPRISE = 'enterprise';

    const ROLE_SANDBOX = 'sandbox';
    const ROLE_USER = 'user';
    const ROLE_ADMIN = 'admin';


    protected $attributes = [
        'auth_type' => null,
        'auth_id' => null,

        'oauth_token' => null,
        'oauth_refresh_token' => null,
        'oauth_expires_in' => null,

        'username' => null,
        'name' => null,
        'email' => null,
        'avatar' => null,
        'password' => null,

        'role' => self::ROLE_USER,
        'status' => self::STATUS_UNCONFIRMED,
        'plan' => self::PLAN_FREE,

        'newsletters' => false,
        'referral' => null,
        'settings' => array(),

        'remember_token' => null,
    ];

    /**
     * The attributes that are mass assignable.
     *
     * @var array
     */
    protected $fillable = [
        'auth_type',
        'auth_id',
        'oauth_token',
        'oauth_refresh_token',
        'oauth_expires_in',
        'username',
        'name',
        'email',
        'avatar',
        'password',
        'role',
        'status',
        'plan',
        'referral',
        'newsletters',
        'updated_at',
        'created_at'
    ];

    /**
     * The attributes that should be hidden for serialization.
     *
     * @var array<int, string>
     */
    protected $hidden = [
        'password',
        'remember_token',
    ];


    /**
     * The attributes that should be cast.
     *
     * @var array<string, string>
     */
    protected $casts = [
        'email_verified_at' => 'datetime',
    ];

    /**
     * The attributes that should be cast to native types.
     *
     * @var array
     */
    protected $dates = ['email_verified_at'];


    public function setPasswordAttribute($value)
    {
        $this->attributes['password'] = Hash::make($value);
    }

    public function sendPasswordResetNotification($token): void
    {
        $url = config('settings.url') . '/legacy/reset-password/' . $token;

        // Get user agent information
        $userAgent = request()->header('User-Agent');

        $agent = new Agent();
        $agent->setUserAgent($userAgent);

        $operatingSystem = $agent->platform();
        $browserName = $agent->browser();

        $this->notify(new PasswordReset($url, $operatingSystem, $browserName));
    }

    public function getPlan(): array
    {
        return config('users.plans.' . $this->plan);
    }

    public function maxProjectsReached(): bool
    {
        $projects = Project::where('user_id', $this->id)->get();
        $maxProjects = $this->getPlan()['max_projects'];
        if (count($projects) < $maxProjects) {
            return false;
        }
        return true;
    }

    public static function getPlanVar($plan, $var): mixed
    {
        return config('users.plans.' . $plan)[$var];
    }

}
