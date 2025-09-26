@extends('app.layout')

@section('meta')
    <title>Acquire license &bull; {{ config('app.name') }}</title>
@endsection

@section('content')
    @if ($errors->any())
        <div class="alert alert-danger py-3">
            <p class="fw-bold">Whoops! Something went wrong:</p>
            <ul class="m-0">
                @foreach ($errors->all() as $error)
                    <li>{{ $error }}</li>
                @endforeach
            </ul>
        </div>
    @endif
    @if(Session::has('success'))
        <div class="alert alert-success mb-3">
            {{ Session::get('success') }}
        </div>
    @endif

    <form method="post"
          action="{{ route('app.license.buy', ['license' => $license['backend_name'], 'duration' => $duration]) }}">
        @csrf
        <h5 class="mb-4">{{ $license['name'] }} License</h5>

        <div class="mb-3">
            <label for="nameField" class="form-label">Name</label>
            <input type="text" class="form-control" id="nameField" name="name" placeholder="John Doe">
        </div>
        <div class="mb-3">
            <label for="addressField" class="form-label">Address</label>
            <textarea class="form-control" id="addressField" name="address" rows="3"></textarea>
        </div>

        <div class="row row-cols-4 g-3 mx-1 my-4">
            @foreach($paymentMethods as $index => $paymentMethod)
                <input type="radio" class="btn-check" name="payment_method" value="{{ $index }}" id="{{ $index }}"
                       autocomplete="off" checked>
                <label class="btn btn-outline-primary rounded-4 me-3" for="{{ $index }}">
                    {{ $paymentMethod['name'] }}
                    <img class="ms-3" src="{{ asset($paymentMethod['icon']) }}" width="24" height="24"
                         alt="{{ $paymentMethod['name'] }}">
                </label>
            @endforeach
        </div>

        @if($duration === 'yearly')
            <h5 class="text-muted my-3 mt-5">€{{ $license['price'] }}</h5>
        @elseif($duration === 'monthly')
            <h5 class="text-muted my-3 mt-5">€{{ $license['price'] / 12 * 3 }}</h5>
        @endif

        <button type="submit" class="btn btn-primary rounded-4 mt-2"><i class="fa-solid fa-cart-shopping me-1"></i>
            Checkout
        </button>
    </form>
@endsection

@section('modals')

@endsection
