@extends('home.layout')

@section('meta')
    <title>Frequently Asked Questions &bull; {{ config('app.name') }}</title>

    <meta name="description"
          content="These are the questions we are asked most often. If you are interested in other things, do not hesitate to contact us.">
@endsection

@section('bg', 'bg-pattern-wavy')

@section('content')
    @include('home.partials.header')
    <section id="content" class="pb-5">
        <div class="container">
            <div class="accordion" id="accordionFaq">
                <div class="accordion-item">
                    <h2 class="accordion-header h2" id="heading1">
                        <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapse1" aria-expanded="false"
                                aria-controls="collapse1">
                            What does Branchlock do?
                        </button>
                    </h2>
                    <div id="collapse1" class="accordion-collapse collapse" aria-labelledby="heading1" data-bs-parent="#accordionFaq">
                        <div class="accordion-body text-muted">
                            Branchlock is a technology that encrypts the compiled code of Java and Android projects so that they cannot be
                            restored to the original code afterwards (so-called "obfuscators").
                            With conventional obfuscators usually only methods, fields and class names are renamed. All constants like API keys or
                            private URLs are left completely unchanged in the bytecode (= the final product after compilation). These can be
                            easily viewed with decompilers, which convert bytecode back to source code, or scanned by automated bots (especially
                            if your application is listed on third party markets). Branchlock encrypts class pool constants such as strings,
                            numbers and even references, providing strong protection for your Java project. The original code structure cannot be
                            recovered at all. Branchlock offers different encryption modes that have different effects on the security and
                            performance of your application.
                        </div>
                    </div>
                </div>
                <div class="accordion-item">
                    <h2 class="accordion-header h2" id="heading2">
                        <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapse2"
                                aria-expanded="false" aria-controls="collapse2">
                            Why do I need to provide libraries?
                        </button>
                    </h2>
                    <div id="collapse2" class="accordion-collapse collapse" aria-labelledby="heading2" data-bs-parent="#accordionFaq">
                        <div class="accordion-body text-muted">
                            Java uses frames to check the validity of bytecode. To find known parents of two classes, the libraries are needed.
                            Also, Branchlock needs the libraries to generate a dependency tree. Without libraries, obfuscation errors could occur
                            because, for example, methods in the superclass could be unintentionally overwritten.
                        </div>
                    </div>
                </div>
                <div class="accordion-item">
                    <h2 class="accordion-header h2" id="heading3">
                        <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapse3"
                                aria-expanded="false" aria-controls="collapse3">
                            Will Branchlock log my activities?
                        </button>
                    </h2>
                    <div id="collapse3" class="accordion-collapse collapse" aria-labelledby="heading3" data-bs-parent="#accordionFaq">
                        <div class="accordion-body text-muted">
                            Branchlock will store your jar files temporarily on the server and obfuscate them, and then delete them instantly. No
                            files or logs are kept.
                        </div>
                    </div>
                </div>
                <div class="accordion-item">
                    <h2 class="accordion-header h2" id="heading4">
                        <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapse4"
                                aria-expanded="false" aria-controls="collapse4">
                            Does it work for all java versions / android api levels?
                        </button>
                    </h2>
                    <div id="collapse4" class="accordion-collapse collapse" aria-labelledby="heading4" data-bs-parent="#accordionFaq">
                        <div class="accordion-body text-muted">
                            It depends on which modes you use. Tasks that use the <code>invokedynamic</code> opcode require Java 8+. However,
                            Branchlock
                            provides modes for all Java versions and Android API levels.
                        </div>
                    </div>
                </div>
                <div class="accordion-item">
                    <h2 class="accordion-header h2" id="heading5">
                        <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapse5"
                                aria-expanded="false" aria-controls="collapse5">
                            Who made Branchlock?
                        </button>
                    </h2>
                    <div id="collapse5" class="accordion-collapse collapse" aria-labelledby="heading5" data-bs-parent="#accordionFaq">
                        <div class="accordion-body text-muted">
                            We are a small team from Europe, focused on Java bytecode for several years. Our goal was to revolutionize Java
                            obfuscation and create a modern product that is also profitable for smaller developers.
                        </div>
                    </div>
                </div>
                <div class="accordion-item">
                    <h2 class="accordion-header h2" id="heading6">
                        <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapse6"
                                aria-expanded="false" aria-controls="collapse6">
                            Can I obfuscate with another obfuscator after I obfuscated with Branchlock?
                        </button>
                    </h2>
                    <div id="collapse6" class="accordion-collapse collapse" aria-labelledby="heading6" data-bs-parent="#accordionFaq">
                        <div class="accordion-body text-muted">
                            You can, but there is no guarantee given that the jar file is still runnable afterwards. Renaming classes or class
                            members after obfuscating using branchlock will not work.
                        </div>
                    </div>
                </div>
                <div class="accordion-item">
                    <h2 class="accordion-header h2" id="heading7">
                        <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapse7"
                                aria-expanded="false" aria-controls="collapse7">
                            My obfuscated jar file is not runnable after obfuscating!
                        </button>
                    </h2>
                    <div id="collapse7" class="accordion-collapse collapse" aria-labelledby="heading7" data-bs-parent="#accordionFaq">
                        <div class="accordion-body text-muted">
                            Please contact the support at <code>support@branchlock.net</code> if you need help or want to report bugs.
                        </div>
                    </div>
                </div>
                <div class="accordion-item">
                    <h2 class="accordion-header h2" id="heading8">
                        <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapse8"
                                aria-expanded="false" aria-controls="collapse8">
                            Will my project be totally secure?
                        </button>
                    </h2>
                    <div id="collapse8" class="accordion-collapse collapse" aria-labelledby="heading8" data-bs-parent="#accordionFaq">
                        <div class="accordion-body text-muted">
                            Branchlock tries to make it as hard as possible for attackers to reverse engineer your project. Total protection is
                            impossible, but it's safe to say that many hours will be wasted trying to untangle the mess Branchlock creates.
                            Cracking (= removing access restrictions in your project) also becomes more difficult, as Branchlock uses integrity
                            checks and "crashers" (exploits that crash reverse engineering tools) to protect everything as much as possible.
                        </div>
                    </div>
                </div>
                <div class="accordion-item">
                    <h2 class="accordion-header h2" id="heading9">
                        <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapse9"
                                aria-expanded="false" aria-controls="collapse9">
                            Will my license expire?
                        </button>
                    </h2>
                    <div id="collapse9" class="accordion-collapse collapse" aria-labelledby="heading9" data-bs-parent="#accordionFaq">
                        <div class="accordion-body text-muted">
                            Once you have purchased a branchlock license, you can use the obfuscator anytime. Your license will never expire, as
                            long as you don't share your account or violate the ToS.
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>
@endsection
