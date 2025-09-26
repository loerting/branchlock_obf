<?php

namespace App\Rules;

use Closure;
use Illuminate\Contracts\Validation\ValidationRule;

class ValidJarFile implements ValidationRule
{
    /**
     * Run the validation rule.
     *
     * @param \Closure(string): \Illuminate\Translation\PotentiallyTranslatedString $fail
     */
    public function validate(string $attribute, mixed $value, Closure $fail): void
    {
        if (!$this->isNotEmpty($value) || !$this->isValidContent($value)) {
            $fail('The ' . $attribute . ' must be a valid jar file.');
        }
    }

    private function isNotEmpty($file): bool
    {
        return $file->getSize() > 0;
    }

    private function isValidExtension($file): bool
    {
        return strtolower(pathinfo($file->getClientOriginalName(), PATHINFO_EXTENSION)) === 'jar';
    }

    private function isValidMimeType($file): bool
    {
        $allowedMimeTypes = ['application/java-archive', 'application/x-java-archive'];

        return in_array($file->getClientMimeType(), $allowedMimeTypes, true);
    }

    private function isValidContent($file): bool
    {
        $fileContent = file_get_contents($file->getRealPath());

        return str_starts_with($fileContent, 'PK');
    }
}
