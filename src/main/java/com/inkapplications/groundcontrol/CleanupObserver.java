/*
 * Copyright (c) 2015 Ink Applications, LLC.
 * Distributed under the MIT License (http://opensource.org/licenses/MIT)
 */
package com.inkapplications.groundcontrol;

import rx.Observable;
import rx.Observer;

import java.util.HashMap;

/**
 * Observer for removing completed requests in a collection.
 *
 * this is used to clean up the collection of "in-flight" requests after it is
 * completed in the subscription factory.
 * The requests collection here is stateful, an this observer will modify it
 * on completion of the request.
 *
 * @param <ENTITY> The subscription entity type that this is bound to.
 */
final class CleanupObserver<ENTITY> implements Observer<ENTITY>
{
    /** Stateful storage of in-flight requests. */
    final private HashMap<String, Observable<ENTITY>> requests;

    /** Key to remove observables of on completion. */
    final private String key;

    /**
     * @param requests Stateful storage of in-flight requests.
     * @param key Key to remove observables of on completion.
     */
    public CleanupObserver(HashMap<String, Observable<ENTITY>> requests, String key)
    {
        this.requests = requests;
        this.key = key;
    }

    @Override
    public void onCompleted()
    {
        this.requests.remove(this.key);
    }

    @Override public void onError(Throwable e) {}
    @Override public void onNext(ENTITY entity) {}
}
