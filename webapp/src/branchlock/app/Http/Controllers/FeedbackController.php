<?php

namespace App\Http\Controllers;

use App\Models\Feedback;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Redis;

class FeedbackController extends Controller
{

    public function sendFeedback()
    {
        $this->validate(request(), [
            'feedbackMessage' => 'required|string|max:2000|min:5',
        ]);

        $message = request('feedbackMessage');
        $lockKey = 'feedback:' . auth()->user()->id;

        if (Redis::get($lockKey)) {
            return response()->json([
                'success' => false,
                'message' => 'You have already sent feedback recently. Please try again later.',
            ]);
        }

        $feedback = new Feedback();
        $feedback->user_id = auth()->user()->id;
        $feedback->message = $message;
        $feedback->save();


        Redis::set($lockKey, true);
        Redis::expire($lockKey, 60 * 5);


        return response()->json([
            'success' => true,
            'message' => 'Feedback sent successfully!',
        ]);
    }

}
