<?php

namespace App;

enum BranchlockRunType
{
    case WEB_OBFUSCATION;
    case ANDROID_OBFUSCATION;
    case INTERACTIVE_DEMO;
    case STACKTRACE_DECRYPTION;


    public function getRunClass(): string
    {
        return match ($this) {
            self::WEB_OBFUSCATION, self::ANDROID_OBFUSCATION => 'net.branchlock.BranchlockFileRunner',
            self::INTERACTIVE_DEMO => 'net.branchlock.BranchlockInteractiveDemoRunner',
            self::STACKTRACE_DECRYPTION => 'net.branchlock.BranchlockStacktraceRunner',
        };
    }
}
