@extends('home.layout')

@section('meta')
    <title>Privacy Policy &bull; {{ config('app.name') }}</title>
    <meta name="description" content="Privacy Policy">
@endsection

@section('bg', 'bg-light')

@section('content')
    @include('home.partials.header')
    <div class="container pb-5">
        <div class="row justify-content-center">
            <div class="col-12">
                <h3 class="fw-semibold mb-4">Privacy Policy</h3>

                <p class="mb-5">This Privacy Policy describes how Branchlock collects, uses, and protects the information you provide when using
                    our Obfuscator & Optimizer services ("Services"). Please read this policy carefully to understand our practices regarding your data and
                    how we handle it.</p>

                <h5 class="fw-semibold mb-4">1. Information We Collect</h5>

                <ul class="mb-5">
                    <li><strong>Account Information:</strong> When you sign up for a Branchlock account, we collect your name, email address, and password.
                        You may also provide us with additional information such as your company name, phone number, and billing address.
                        If you sign in with OAuth (Google, GitHub, GitLab, Bitbucket), we collect your username, name, avatar image and email address from
                        the OAuth provider.
                    </li>
                    <li><strong>Payment Information:</strong> When you purchase a subscription, we collect your payment information, including your credit
                        card number, expiration date, and CVV code.
                    </li>
                    <li><strong>Log Data:</strong> When you use our Services, we automatically collect certain information, including your IP address,
                        browser
                        type, operating system, the referring web page, pages visited, location, your mobile carrier, device information (including device
                        and
                        application IDs), search terms, and cookie information.
                    </li>
                    <li><strong>Uploaded Code:</strong> When you upload code to our Services, we collect and temporarily store it. We guarantee that it
                        will never be shared with third parties.
                    </li>
                    <li><strong>User Input:</strong> Any user input collected is used solely for the purposes of providing and improving our Services.</li>
                </ul>

                <h5 class="fw-semibold mb-4">2. Use of Information</h5>

                <p class="mb-5">We use the information we collect to provide, maintain, and improve our Services, such as to administer your account and
                    process payments. We may also use the information to communicate with you, including responding to your comments, questions, and
                    requests; providing customer service and support; providing you with information about our Services; and sending you technical notices,
                    updates, security alerts, and administrative messages.</p>

                <h5 class="fw-semibold mb-4">3. Data Retention</h5>

                <p class="mb-5">We retain your information for as long as necessary to provide you with our Services and as described in this Privacy
                    Policy. We also retain your information for as long as necessary to support our business operations and for purposes of fraud
                    prevention
                    or to comply with our legal obligations, resolve disputes, or enforce our agreements.</p>

                <h5 class="fw-semibold mb-4">4. Data Protection</h5>

                <p class="mb-5">We take the security of your data very seriously. We use industry-standard physical, technical, and administrative
                    security measures and safeguards to protect the confidentiality and security of your personal information. However, since the Internet
                    is not a 100% secure environment, we cannot guarantee, ensure, or warrant the security of any information you transmit to us. There is
                    no guarantee that information may not be accessed, disclosed, altered, or destroyed by breach of any of our physical, technical, or
                    managerial safeguards. It is your responsibility to protect the security of your login information.</p>

                <h5 class="fw-semibold mb-4">5. Cookies and Tracking</h5>

                <p class="mb-3">We use cookies for certain functionalities, including but not limited to:</p>

                <ul>
                    <li>Login Persistence: Cookies may be used to remember user login sessions for convenience, allowing users to remain logged in across
                        pages.
                    </li>
                    <li>Theme Cache: Cookies may store theme preferences or settings to enhance user experience.</li>
                    <li>Understanding how you use our Services</li>
                    <li>Providing you with a better experience</li>
                </ul>

                <p class="mb-5">These cookies are essential for the proper functioning of our Services and do not collect personal information or track
                    user activities
                    beyond the scope of these functionalities.</p>


                <h5 class="fw-semibold mb-4">6. Third-Party Services</h5>

                <p class="mb-5">Our Services may contain links or integrations with third-party services. Please note that this Privacy Policy applies
                    solely to the Services provided by Branchlock. We are not responsible for the privacy practices or content of third-party
                    services.</p>

                <h5 class="fw-semibold mb-4">7. Compliance with Laws</h5>

                <p class="mb-5">We comply with all applicable laws and regulations regarding data privacy and protection, including the General Data
                    Protection Regulation (GDPR) in the European Union.</p>

                <h5 class="fw-semibold mb-4">8. Changes to this Policy</h5>

                <p class="mb-5">We reserve the right to update or modify this Privacy Policy at any time. Any changes will be effective immediately upon
                    posting the revised Privacy Policy on our website.</p>

                <h5 class="fw-semibold mb-4">9. Contact Us</h5>

                <p class="mb-5">If you have any questions about this Privacy Policy, please contact us at <a
                        href="mailto:legal@branchlock.net">legal@branchlock.net</a>.</p>

                <p>By using our Services, you agree to abide by this Privacy Policy. Please review this Privacy Policy periodically for any updates.</p>

            </div>
        </div>
    </div>
@endsection
