<?php

namespace App\Http\Controllers\Auth;

use App\Helpers\CustomHelper;
use App\Http\Controllers\Controller;
use App\Models\Sanctum\PersonalAccessToken;
use App\Models\User;
use App\Rules\ValidHCaptcha;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\App;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Password;
use Illuminate\Support\Facades\Redirect;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Str;

class LoginController extends Controller
{

    public function __invoke(Request $request): RedirectResponse
    {
        $validator = Validator::make($request->all(), [
            'email' => 'required|email|exists:users,email',
            'password' => 'required|min:6',
            'h-captcha-response' => ['required', new ValidHCaptcha()]
        ]);

        if ($validator->fails()) {
            return redirect()->back()->withErrors($validator->errors());
        }

        $validated = $validator->safe()->only(['email', 'password']);

        $lowercaseEmail = strtolower($validated['email']);

        $user = User::where('email', $lowercaseEmail)->first();

        if ($user && !$user->updated_at && !$user->created_at) {
            if (Hash::check($validated['password'], $user->password)) {
                $user->password = hash('sha256', $validated['password']);
                $user->updated_at = now();
                $user->created_at = now();
                $user->save();
            }
        }

        if (!Auth::attempt([
            'auth_type' => 'legacy',
            'email' => $lowercaseEmail,
            'password' => hash('sha256', $validated['password'])
        ], true)) {
            return Redirect::back()->withErrors(['email' => 'Invalid credentials']);
        }

        OAuthController::createDefaultProject($user);

        return redirect()->route('app');
    }


    public function sendResetLinkEmail(Request $request): RedirectResponse
    {
        $validator = Validator::make($request->all(), [
            'email' => 'required|email',
            'h-captcha-response' => ['required', new ValidHCaptcha()]
        ]);

        if ($validator->fails()) {
            return redirect()->back()->withErrors($validator->errors());
        }

        $validated = $validator->safe()->only(['email']);

        $user = User::where('email', $validated['email'])->first();

        $message = 'A password reset link has been sent if there is an existing account.';
        if (!$user) {
            return Redirect::back()->with('success', $message);
        }

        Password::sendResetLink($validated);

        return back()->with('success', $message);
    }

    public function resetPassword(Request $request): RedirectResponse
    {
        $validator = Validator::make($request->all(), [
            'token' => 'required',
            'email' => 'required|email|exists:users,email',
            'password' => ['required', 'string', \Illuminate\Validation\Rules\Password::min(8)->uncompromised()],
            'password_confirm' => ['required', 'string', 'same:password'],
            'h-captcha-response' => ['required', new ValidHCaptcha()]
        ]);

        if ($validator->fails()) {
            return redirect()->back()->withErrors($validator->errors());
        }

        $validated = $validator->safe()->only(['email', 'password', 'password_confirm', 'token']);

        $status = Password::reset($validated,
            function (User $user, string $password) {
                $user->forceFill([
                    'password' => hash('sha256', $password)
                ])->setRememberToken(Str::random(60));

                $user->save();
            }
        );

        return $status === Password::PASSWORD_RESET
            ? Auth::attempt([
                'auth_type' => 'legacy',
                'email' => $validated['email'],
                'password' => hash('sha256', $validated['password'])
            ], true) ? redirect()->route('app') : back()->withErrors(['email' => 'Invalid credentials'])
            : back()->withErrors(['email' => [__($status)]]);
    }

    public function changePassword(Request $request): RedirectResponse
    {
        $validator = Validator::make($request->all(), [
            'old_password' => 'required|min:6',
            'password' => ['required', 'string', \Illuminate\Validation\Rules\Password::min(8)->uncompromised()],
            'password_confirm' => ['required', 'string', 'same:password'],
        ]);

        if ($validator->fails()) {
            return redirect()->back()->withErrors($validator->errors());
        }

        $validated = $validator->safe()->only(['old_password', 'password', 'password_confirm']);

        $user = Auth::user();

        if ($user->auth_type !== 'legacy') {
            return Redirect::back()->withErrors(['old_password' => 'Invalid password']);
        }

        // check old password
        if (!Hash::check(hash('sha256', $validated['old_password']), $user->password)) {
            return Redirect::back()->withErrors(['old_password' => 'Invalid password']);
        }

        $user->password = hash('sha256', $validated['password']);

        $user->save();

        $tokenCount = PersonalAccessToken::where('tokenable_id', Auth::user()->id)->count();
        if ($tokenCount > 0) {
            PersonalAccessToken::where('tokenable_id', Auth::user()->id)->delete();
        }

        //Auth::logoutOtherDevices($validated['password']);

        return redirect()->back()->with('success', 'Password changed successfully');
    }

}
