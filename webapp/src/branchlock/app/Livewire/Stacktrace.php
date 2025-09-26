<?php

namespace App\Livewire;

use App\Branchlock;
use App\BranchlockRunType;
use App\Helpers\CustomHelper;
use Illuminate\Support\Facades\Redis;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Str;
use Livewire\Component;

class Stacktrace extends Component
{
    public $textarea;
    public $decryptionKey;

    public function render()
    {
        return view('livewire.stacktrace');
    }

    public function decrypt()
    {
        $lockKey = 'stacktrace:' . auth()->user()->id;

        if (Redis::get($lockKey)) {
            return response()->json([
                'success' => false,
                'message' => 'Please try again later.',
            ]);
        }

        $id = Str::random(15);
        $decrypted = CustomHelper::reverseFileName($id);
        Storage::disk('uploads')->put($id, $this->textarea);

        $bl = new Branchlock(BranchlockRunType::STACKTRACE_DECRYPTION);
        $bl->setInput($id);
        $bl->setOutput($decrypted);
        $bl->setStacktraceKey($this->decryptionKey);
        $outputLog = $bl->run();

        Redis::set($lockKey, true);
        Redis::expire($lockKey, 5);

        $this->textarea = file_get_contents(Storage::disk('uploads')->path($decrypted));
    }
}
