<?php

namespace App\Http\Controllers;

use App\Helpers\Payment\Sellix;
use App\Models\User;
use Carbon\Carbon;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Facades\Validator;

class LicenseController extends Controller
{

    public function show()
    {
        $licenses = config('users.plans');
        $licenses = array_map(function ($license) {
            $license['price'] = number_format($license['price'], 2);
            if (is_numeric($license['price'])) {
                $price_per_month = $license['price'] / 12;
                $license['price_month'] = number_format($price_per_month, 2);
            } else {
                $license['price_month'] = null;
            }

            return $license;
        }, $licenses);


        return view('home.sites.licenses', [
            'licenses' => $licenses,
        ]);
    }

    public function get(Request $request, $license, $duration)
    {
        $licenses = config('users.plans');
        $license = $licenses[$license] ?? null;

        if (!$license) {
            abort(404);
        }
        if ($duration != 'monthly') {
            $duration = 'yearly';
        }

        if ($request->user()->role == 'sandbox') {
            return redirect()->route('app');
        }

        $userLicense = $request->user()->getPlan();

        if ($userLicense && $userLicense['tier'] >= $license['tier']) {
            return redirect()->route('app');
        }

        return view('app.sites.get-license', [
            'license' => $license,
            'duration' => $duration,
            'paymentMethods' => config('settings.payment_methods'),
        ]);

    }

    public function createPayment(Request $request, $license, $duration)
    {
        $validator = Validator::make($request->all(), [
            'name' => 'required|string|max:255',
            'address' => 'required|string|max:255',
            'payment_method' => 'required',
        ]);

        if ($validator->fails()) {
            return redirect()->back()->withErrors($validator)->withInput();
        }

        $validated = $validator->validated();

        $license = config('users.plans')[$license] ?? null;

        if (!$license) {
            abort(404);
        }
        if ($duration != 'monthly') {
            $duration = 'yearly';
        }

        $request->user()->full_name = $validated['name'];
        $request->user()->address = $validated['address'];
        $request->user()->save();

        $amount = $duration == 'monthly' ? $license['price'] / 12 * 3 : $license['price'];
        $duration = $duration == 'monthly' ? 30 : 365;

        $paymentUrl = (new Sellix())->createPayment($request->payment_method, $license, $license['name'] . ' License', $request->user()->email, $amount, $duration);

        return redirect($paymentUrl);
    }

    // sellix webhook
    public function sellixWebhook(Request $request)
    {
        $signature = $request->header('X-Sellix-Signature');

        if ($signature) {
            $payload = $request->getContent();

            $signature2 = hash_hmac('sha512', $payload, config('settings.sellix_webhook_secret'));
            if (hash_equals($signature2, $signature)) {
                $data = json_decode($payload, true);
                $event = $data['event'];
                switch ($event) {
                    case "order:paid":
                        $customerEmail = $data['data']['customer_email'];
                        $productId = $data['data']['custom_fields']['license'];
                        $duration = $data['data']['custom_fields']['duration'];
                        $orderID = $data['data']['uniqid'];

                        $user = User::where('email', $customerEmail)->first();
                        $user->plan = $productId;
                        $user->order_id = $orderID;
                        $user->purchase_date = date('Y-m-d H:i:s');
                        $user->purchase_end_date = Carbon::parse($user->purchase_date)->addDays($duration)->format('Y-m-d H:i:s');
                        $user->save();
                        break;
                    case "order:created":
                        // nothing
                        break;
                    default:
                        break;
                }
            }
        }
    }

}
