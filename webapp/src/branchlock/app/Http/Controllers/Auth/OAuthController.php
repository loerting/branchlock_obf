<?php

namespace App\Http\Controllers\Auth;

use App\Helpers\CustomHelper;
use App\Http\Controllers\Controller;
use App\Models\Project;
use App\Models\User;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\App;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Str;
use Laravel\Socialite\Facades\Socialite;

class OAuthController extends Controller
{
    public function show(Request $request, $plan = null)
    {
        if ($plan) {
            $request->session()->put('plan', $plan);
        }

        return view('auth.sites.oauth', [
            'providers' => config('settings.oauth_providers'),
        ]);
    }

    public function redirectToProvider(Request $request)
    {
        $validator = Validator::make($request->route()->parameters(), [
            'provider' => ['required', 'string'],
        ]);

        $validated = $validator->safe()->only(['provider']);

        return Socialite::driver($validated['provider'])
            ->with(["prompt" => "select_account"])
            ->redirect();
    }

    public function providerCallback(Request $request)
    {
        $validator = Validator::make($request->route()->parameters(), [
            'provider' => ['required', 'string'],
        ]);

        $validated = $validator->safe()->only(['provider']);

        $provider = $validated['provider'];

        $user = Socialite::driver($provider)->user();

        if (User::where('email', $user->getEmail())->doesntExist()) {
            // Beta registration is disabled
            /*
            if (App::environment('prod-beta')) {
                return redirect()->route('login.show')->withErrors(['email' => 'Registration is disabled. Only whitelisted users can login.']);
            }*/

            $validator = Validator::make((array)$user, [
                'email' => ['required', 'email:rfc,dns'],
            ]);

            if ($validator->fails()) {
                return redirect()
                    ->route('oauth.show')
                    ->withErrors($validator);
            }

        } else {
            $userDb = User::where('email', $user->getEmail())->first();
            if ($userDb->auth_type !== $provider) {
                return redirect()
                    ->route('login.show')
                    ->withErrors(['email' => 'This email address is already registered with another OAuth provider.
                    If the account you registered with no longer exists or you want to change the provider,
                    please contact us to verify your identity at support@branchlock.net ']);
            }
        }

        $user = User::updateOrCreate([
            'auth_type' => $provider,
            'auth_id' => $user->getId(),
        ], [
            'username' => $user->getNickname(),
            'name' => $user->getName(),
            'email' => $user->getEmail(),
            'avatar' => $user->getAvatar(),
            'oauth_token' => $user->token,
            'oauth_refresh_token' => $user->refreshToken,
            'oauth_expires_in' => $user->expiresIn,
            'status' => User::STATUS_CONFIRMED,
        ]);

        Auth::login($user, true);

        return self::afterLogin();
    }

    public function sandboxLogin(Request $request): false|RedirectResponse
    {
        $sandboxUsers = User::where('role', User::ROLE_SANDBOX)->get();
        if (count($sandboxUsers) > 15) {
            return redirect()->route('login.show')->withErrors(['captcha' => 'All sandbox accounts are currently in use. Please try again later.']);
        }

        $user = User::updateOrCreate([
            'auth_type' => 'sandbox',
            'auth_id' => Str::random(32),
        ], [
            'username' => 'Sandbox',
            'name' => 'Sandbox User',
            'email' => 'sandbox' . Str::random(5) . '@branchlock.net',
            'avatar' => '/img/social/offline.png',
            'status' => User::STATUS_CONFIRMED,
            'role' => User::ROLE_SANDBOX,
            'created_at' => now(),
            'updated_at' => now()
        ]);

        Auth::login($user, true);

        return self::afterLogin();
    }

    public function offlineLogin(Request $request): false|RedirectResponse
    {
        if (!app()->environment('local')) return false;

        $user = User::updateOrCreate([
            'auth_type' => 'offline',
            'auth_id' => 'BranchlockDev',
        ], [
            'username' => 'Dev',
            'name' => 'Local Developer',
            'email' => 'dev@localhost.local',
            'avatar' => '/img/social/offline.png',
            'status' => User::STATUS_CONFIRMED,
            'role' => User::ROLE_ADMIN,
            'plan' => User::PLAN_GROUP,
            'created_at' => now(),
            'updated_at' => now()
        ]);

        Auth::login($user, true);

        return self::afterLogin();
    }

    public static function afterLogin()
    {
        self::createDefaultProject(auth()->user());
        if (session()->has('plan')) {
            $plan = session()->get('plan');
            session()->forget('plan');
            return redirect()->route('app.license.get', ['license' => $plan]);
        }

        return redirect()->route('app');
    }

    public function logout(Request $request)
    {
        $user = auth()->user();

        if ($user['role'] === User::ROLE_SANDBOX) {
            //Project::where('user_id', $user['id'])->forceDelete();
            $user->delete();
        }

        Auth::logout();
        $request->session()->invalidate();
        $request->session()->regenerateToken();
        $request->session()->flush();

        return redirect()->back();
    }

    public static function createDefaultProject(User $user): void
    {
        if (Project::where('user_id', $user->id)->doesntExist()) {
            $androidProject = new Project(['android' => true]);
            $androidProject['user_id'] = $user->id;
            $androidProject['name'] = 'Android project';
            $androidProject['project_id'] = 'default-android';
            $androidProject['android'] = true;
            $androidProject->save();

            $project = new Project();
            $project['user_id'] = $user->id;
            $project['name'] = 'Project';
            $project['project_id'] = 'default';
            $project['android'] = false;
            $project->save();

            if ($user->role === User::ROLE_SANDBOX) {
                $jarFile = Storage::path('private/branchlock-files/demo.jar');
                $fileInstance = new UploadedFile($jarFile, 'demo.jar');

                $project->addInput($fileInstance);
            }
        }
    }
}
