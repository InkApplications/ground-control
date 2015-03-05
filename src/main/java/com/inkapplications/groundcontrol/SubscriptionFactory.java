/*
 * Copyright (c) 2014 Ink Applications, LLC.
 * Distributed under the MIT License (http://opensource.org/licenses/MIT)
 */
package com.inkapplications.groundcontrol;

import org.apache.commons.logging.Log;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import java.util.HashMap;
import java.util.List;

/**
 * Manages adding observers to subscriptions to prevent duplicate requests.
 *
 * Keeps a collection of requests that are run and will join an observer
 * to the previous subscription if one is "in flight" at the time.
 * This is intended to be used in a repository when looking up remote data.
 * Observers are run on Android's main thread, and background is run on IO.
 *
 * @param <ENTITY> The entity that this repository represents.
 */
public class SubscriptionFactory<ENTITY>
{
    final private Log logger;

    /** Stores in-flight requests. */
    final private HashMap<String, Observable<ENTITY>> requests;

    /** Stores in-flight requests when fetching a collection. */
    final private HashMap<String, Observable<List<ENTITY>>> collectionRequests;

    public SubscriptionFactory(Log logger)
    {
        this.logger = logger;
        this.requests = new HashMap<String, Observable<ENTITY>>();
        this.collectionRequests = new HashMap<String, Observable<List<ENTITY>>>();
    }

    /**
     * Create or join with previous subscription for a collection of the entity.
     *
     * Ensures that the request logic is only run once and additional calls made
     * before completion will be added as a subscriber causing the observer
     * callbacks to be invoked, but not the on-subscribe logic.
     *
     * @param onSubscribe Logic to run for the request.
     * @param observer Callback to invoke on request events.
     * @param key A unique key to identify this request type seperate from others.
     * @return A subscription for the observer that may be unsubscribed if necessary.
     */
    final public Subscription createCollectionSubscription(
        OnSubscribe<List<ENTITY>> onSubscribe,
        Observer<List<ENTITY>> observer,
        String key
    ) {
        this.logger.trace("Creating collection subscription.");
        Observable<List<ENTITY>> callback = Observable.create(onSubscribe);
        callback = callback.subscribeOn(Schedulers.io());
        callback = callback.observeOn(AndroidSchedulers.mainThread());

        Observable<List<ENTITY>> previousRequest = this.collectionRequests.get(key);
        Subscription subscription;
        if (null == previousRequest) {
            this.logger.debug("No previous request to join.");
            this.collectionRequests.put(key, callback);
            PublishSubject<List<ENTITY>> composite = PublishSubject.create();
            composite.subscribe(observer);
            composite.subscribe(new CleanupObserver<List<ENTITY>>(this.collectionRequests, key));
            subscription = callback.subscribe(composite);
        } else {
            this.logger.debug("Joining with previous request.");
            subscription = previousRequest.subscribe(observer);
        }

        return subscription;
    }

    /**
     * Create or join with previous subscription for an entity.
     *
     * Ensures that the request logic is only run once and additional calls made
     * before completion will be added as a subscriber causing the observer
     * callbacks to be invoked, but not the on-subscribe logic.
     *
     * @param onSubscribe Logic to run for the request.
     * @param observer Callback to invoke on request events.
     * @param key A unique key to identify this request type seperate from others.
     * @return A subscription for the observer that may be unsubscribed if necessary.
     */
    final public Subscription createSubscription(
        OnSubscribe<ENTITY> onSubscribe,
        Observer<ENTITY> observer,
        String key
    ) {
        this.logger.trace("Creating subscription.");
        Observable<ENTITY> callback = Observable.create(onSubscribe);
        callback = callback.subscribeOn(Schedulers.io());
        callback = callback.observeOn(AndroidSchedulers.mainThread());
        callback = callback.cache();

        Observable<ENTITY> previousRequest = this.requests.get(key);
        Subscription subscription;
        if (null == previousRequest) {
            this.logger.debug("No previous request to join.");
            this.requests.put(key, callback);
            PublishSubject<ENTITY> composite = PublishSubject.create();
            composite.subscribe(observer);
            composite.subscribe(new CleanupObserver<ENTITY>(this.requests, key));
            subscription = callback.subscribe(composite);
        } else {
            this.logger.debug("Joining with previous request.");
            subscription = previousRequest.subscribe(observer);
        }

        return subscription;
    }

    /**
     * Clears out all "in-flight" requests managed by this service.
     */
    public void emptySubscriptions()
    {
        this.collectionRequests.clear();
        this.requests.clear();
    }
}
