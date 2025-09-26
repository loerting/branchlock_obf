<?php

namespace App\Rules;

use Illuminate\Contracts\Validation\InvokableRule;
use Illuminate\Support\Facades\Log;

class ValidHCaptcha implements InvokableRule
{
    /**
     * Run the validation rule.
     *
     * @param string $attribute
     * @param mixed $value
     * @param \Closure(string): \Illuminate\Translation\PotentiallyTranslatedString $fail
     * @return void
     */
    public function __invoke($attribute, $value, $fail)
    {
        $data = array(
            'secret' => config('settings.h_captcha_secret'),
            'response' => $value
        );

        $verify = curl_init();

        curl_setopt($verify, CURLOPT_URL, "https://hcaptcha.com/siteverify");
        curl_setopt($verify, CURLOPT_POST, true);
        curl_setopt($verify, CURLOPT_POSTFIELDS, http_build_query($data));
        curl_setopt($verify, CURLOPT_RETURNTRANSFER, true);

        $response = curl_exec($verify);
        $responseData = json_decode($response, true);

        if (!$responseData['success']) {
            Log::error('Captcha: ' . $responseData['error-codes'][0]);
            $fail('Captcha: ' . $responseData['error-codes'][0]);
        }
    }
}
