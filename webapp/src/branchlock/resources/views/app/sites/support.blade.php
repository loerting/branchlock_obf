@extends('app.layout')

@section('meta')
    <title>Support &bull; {{ config('app.name') }}</title>
@endsection

@section('content')
    <p class="mb-3">Make sure you read the <a href="{{ route('home.docs') }}">documentation.</a></p>
    <p class="mb-5">Do not hesitate to contact us at <a href="mailto:support@branchlock.net">support@branchlock.net</a>
        if you have any questions or problems with our service. Please provide us with as much
        information as possible (e.g. stacktrace, file size of the Java archive, number of libraries, selected tasks).</p>

    <iframe src="https://discord.com/widget?id=830779956338360370&theme=dark" width="350" height="500" allowtransparency="true" frameborder="0"
            sandbox="allow-popups allow-popups-to-escape-sandbox allow-same-origin allow-scripts" class="rounded-4"></iframe>
@endsection
