<div id="hcaptcha"
     class="h-captcha"
     data-sitekey="{{ config('settings.h_captcha_sitekey') }}"
     data-callback="onSubmit"
     data-close-callback="onClose"
     data-size="invisible"></div>

<div class="d-grid gap-2 col-md-12 mx-auto">
    <button type="button" class="w-100 btn btn-lg btn-primary shadow" id="submit_btn">Continue</button>
    <div class="text-center text-muted opacity-75 mt-1" style="font-size: 12px;">
        This site is protected by hCaptcha and its
        <a href="https://hcaptcha.com/privacy" class="text-primary-emphasis" target="_blank">Privacy Policy</a> and
        <a href="https://hcaptcha.com/terms" class="text-primary-emphasis" target="_blank">Terms of Service</a>
        apply.
    </div>
</div>
