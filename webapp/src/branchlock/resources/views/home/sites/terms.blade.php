@extends('home.layout')

@section('meta')
    <title>Terms of Service &bull; {{ config('app.name') }}</title>

    <meta name="description" content="Terms of Service">
@endsection

@section('bg', 'bg-light')

@section('content')
    @include('home.partials.header')
    <div class="container pb-5">
        <div class="row justify-content-center">
            <div class="col-12">
                <h3 class="fw-semibold mb-4">Terms of Service</h3>

                <p class="mb-5">Welcome to Branchlock! These Terms of Service ("Terms") govern your use of our Obfuscator & Optimizer services
                    ("Services"). By accessing or using our Services, you agree to comply with and be bound by these Terms. If you disagree with any part
                    of the terms, you may not access the Services.</p>

                <h5 class="fw-semibold mb-4">1. Description of Services</h5>

                <p class="mb-5">Our Services offer code obfuscation and optimization for Java Desktop, Android, and Server applications. Users upload their
                    code for processing, after which it's protected from reverse engineering and optimized for performance.</p>

                <h5 class="fw-semibold mb-4">2. User Responsibilities</h5>

                <ul class="mb-5">
                    <li>Users are solely responsible for the code they upload.</li>
                    <li>Users must comply with all applicable laws and regulations.</li>
                    <li>Users shall not use the Services for any unlawful purposes or to infringe upon any third-party rights.</li>
                    <li>You are responsible for all activity that occurs under your account.</li>
                    <li>You may not use the Services for any illegal or unauthorized purpose. You must not, in the use of the Services, violate any laws in
                        your jurisdiction (including but not limited to copyright or trademark laws).
                    </li>
                    <li>You must not upload any code that is illegal, harmful, threatening, abusive, harassing, tortious, defamatory, vulgar, obscene,
                        libelous, invasive of another's privacy, hateful, or racially,
                        ethnically, or otherwise objectionable.
                    </li>
                    <li>You must not upload any code that contains software viruses or any other computer code, files, or programs designed to interrupt,
                        destroy, or limit the functionality of any computer software or
                        hardware or telecommunications equipment.
                    </li>
                </ul>

                <h5 class="fw-semibold mb-4">3. Data and Privacy</h5>

                <p class="mb-3">Uploaded input files: When you upload code to our Services, we collect and temporarily store it. We guarantee that it
                    will never be shared with third parties. User data protection is our top priority. Depending on the settings, user input files are
                    either deleted manually or automatically once the process is complete. In cases where users opt not to automatically delete files (to
                    prevent re-uploading for multiple obfuscation tasks, debugging, testing etc.), the
                    maximum storage duration is {{ config('settings.max_storage_time') }} hours. Beyond this period, all files are permanently and irreversibly deleted, without the option for
                    recovery.</p>

                <p class="mb-3">In rare cases, we access your source code with your explicit consent or upon support request for troubleshooting and
                    ensuring
                    smooth application function. This access prioritizes your data privacy, used strictly for optimization or necessary troubleshooting.
                    Regarding support or bug reports for your uploaded code, it's exclusively used to diagnose and resolve issues, enhancing our
                    techniques' effectiveness. Your code remains confidential, employed solely for service enhancement upon your explicit support
                    engagement.</p>

                <p class="mb-5">Furthermore, we can only access files that are actively displayed in the user's web interface. If files are deleted before
                    a support
                    request, we have no means to restore them, potentially hindering our ability to address issues.</p>

                <h5 class="fw-semibold mb-4">4. Intellectual Property</h5>

                <p class="mb-5">We claim no intellectual property rights over the code you upload to our Services. Your code remains exclusively yours.</p>

                <h5 class="fw-semibold mb-4">5. Service Limitations</h5>

                <p class="mb-5">While we strive to provide reliable Services, we do not guarantee that the service will be uninterrupted, timely, secure,
                    or error-free.</p>

                <h5 class="fw-semibold mb-4">6. Termination</h5>

                <p class="mb-5">We may terminate or suspend your account immediately, without prior notice or liability, for any reason whatsoever,
                    including without limitation if you breach the Terms. Upon termination, your right to use the Services will immediately cease. If you
                    wish to terminate your account, you may simply discontinue using the Services.</p>

                <h5 class="fw-semibold mb-4">7. Disclaimers</h5>

                <p class="mb-5">The Services are provided "as is" without warranties of any kind, whether express or implied. We shall not be liable for
                    any indirect, incidental, special, consequential, or punitive damages arising out of or relating to the use of our Services.</p>

                <h5 class="fw-semibold mb-4">8. Governing Law</h5>

                <p class="mb-5">These Terms shall be governed by and construed in accordance with the laws of Germany and Austria, without regard to its
                    conflict of law principles.</p>

                <h5 class="fw-semibold mb-4">9. Changes to Terms</h5>

                <p class="mb-5">We reserve the right to modify or replace these Terms at any time. Your continued use of the Services after changes to the
                    Terms signifies acceptance of those changes.</p>

                <h5 class="fw-semibold mb-4">10. Licenses</h5>

                <p class="mb-3">Each license purchased for our Services is valid for 1 Year. New purchases automatically follow this validity period.
                    "Legacy licenses" (prior to the update) are lifetime licenses. However, upon upgrading to a higher license, the legacy license terms
                    will be overwritten with the new license terms. Legacy licenses may have certain restrictions or limitations, and users are encouraged
                    to review the terms and conditions specific to their legacy license.</p>

                <p class="mb-5">Commercial usage of our Services is only permitted with paid licenses. Any commercial usage under the Free Trial Plan is
                    strictly prohibited.</p>


                <h5 class="fw-semibold mb-4">11. Contact Us</h5>

                <p class="mb-5">If you have any questions about these Terms, please contact us at <a
                        href="mailto:legal@branchlock.net">legal@branchlock.net</a>.</p>

                <p>By using our Services, you agree to abide by these Terms. Please review these Terms periodically for any updates.</p>

            </div>
        </div>
    </div>
@endsection
