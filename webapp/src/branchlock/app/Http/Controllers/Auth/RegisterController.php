<?php

namespace App\Http\Controllers\Auth;

use App\Helpers\CustomHelper;
use App\Http\Controllers\Controller;
use App\Models\User;
use App\Models\VerificationToken;
use App\Notifications\AccountVerification;
use App\Rules\ValidHCaptcha;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\App;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Str;
use Illuminate\Validation\Rules\Password;

class RegisterController extends Controller
{

    public function __invoke(Request $request): RedirectResponse
    {
        // Beta registration is disabled

        /*if (App::environment('prod-beta')) {
            return redirect()->back()->withErrors(['email' => 'Registration is disabled. Only whitelisted users can login.']);
        }*/

        $validator = Validator::make($request->all(), [
            'email' => ['required', 'email:rfc,dns', 'max:255', 'unique:users'],
            'password' => ['required', 'string', Password::min(8)->uncompromised()],
            'password_confirm' => ['required', 'string', 'same:password'],
            'consent' => ['accepted'],
            'h-captcha-response' => ['required', new ValidHCaptcha()]
        ]);

        if ($validator->fails()) {
            return redirect()->back()->withErrors($validator->errors());
        }

        $validated = $validator->safe()->only(['email', 'password']);

        $user = User::create([
            'auth_type' => 'legacy',
            'email' => $validated['email'],
            'password' => hash('sha256', $validated['password']),
        ]);

        OAuthController::createDefaultProject($user);

        Auth::login($user, true);

        // Generate a token
        $token = Str::random(60);

        // Store token in database
        VerificationToken::create([
            'user_id' => $user->id,
            'token' => $token,
        ]);
        // send email verification
        $user->notify(new AccountVerification(config('settings.url') . '/verify-email/' . $token));

        return redirect()->route('app');
    }

    public function verificationNotice()
    {
        $user = Auth::user();

        if (!$user) {
            return redirect()->route('app');
        }

        if ($user->status === User::STATUS_CONFIRMED) {
            return redirect()->route('app');
        }

        return view('app.sites.email-verification');
    }

    public function verifyEmail(Request $request, $token): RedirectResponse
    {
        $verificationToken = VerificationToken::where('token', $token)->first();

        if (!$verificationToken) {
            return redirect()->route('app');
        }

        $user = User::find($verificationToken->user_id);

        if (!$user) {
            return redirect()->route('app');
        }

        $user->status = User::STATUS_CONFIRMED;
        $user->save();

        $verificationToken->delete();

        return redirect()->route('app');
    }
}
