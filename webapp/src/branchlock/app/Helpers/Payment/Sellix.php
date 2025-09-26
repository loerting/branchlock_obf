<?php

namespace App\Helpers\Payment;

class Sellix
{

    public function createPayment($payment_method, $license, $title, $email, $amount, $duration)
    {
        $client = new \Sellix\PhpSdk\Sellix(config('settings.sellix_key'), "Branchlock");

        $response = $client->create_payment([
            'title' => $title,
            'white_label' => false,
            'return_url' => config('settings.url') . '/app/paid',
            'email' => $email,
            'gateway' => $payment_method,
            'currency' => 'EUR',
            'value' => $amount,
            'custom_fields' => [
                'license' => $license['backend_name'],
                'duration' => $duration,
            ],
        ]);

        return $response->url;
    }

}
