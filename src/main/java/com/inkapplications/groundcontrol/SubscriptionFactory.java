/*
 * Copyright (c) 2014 Ink Applications, LLC.
 * Distributed under the MIT License (http://opensource.org/licenses/MIT)
 */
package com.inkapplications.groundcontrol;

import org.apache.commons.logging.Log;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;
import rx.subjects.ReplaySubject;

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
@SuppressWarnings("unused")
public class SubscriptionFactory<ENTITY>
{
    final private Log logger;
    final private Scheduler subscribeScheduler;
    final private Scheduler observeScheduler;

    /** Stores in-flight requests. */
    final private RequestCollection<ENTITY> requests;

    /** Stores in-flight requests when fetching a collection. */
    final private RequestCollection<List<ENTITY>> collectionRequests;

    public SubscriptionFactory(Log logger, Scheduler subscribeOn, Scheduler observeOn)
    {
        this.logger = logger;
        this.subscribeScheduler = subscribeOn;
        this.observeScheduler = observeOn;
        this.requests = new RequestCollection<ENTITY>();
        this.collectionRequests = new RequestCollection<List<ENTITY>>();
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
     * @param key A unique key to identify this request type separate from others.
     * @return A subscription for the observer that may be unsubscribed if necessary.
     */
    final public Subscription createCollectionSubscription(
        OnSubscribe<List<ENTITY>> onSubscribe,
        Observer<List<ENTITY>> observer,
        String key
    ) {
        this.logger.trace("Creating collection subscription for Key: " + key);
        Observable<List<ENTITY>> callback = Observable.create(onSubscribe);
        callback = callback.subscribeOn(this.subscribeScheduler);
        callback = callback.observeOn(this.observeScheduler);


        CompositeRequestManager<List<ENTITY>> previousRequest = this.collectionRequests.get(key);
        Subscription subscription;
        if (null == previousRequest) {
            this.logger.debug("No previous request to join.");
            ReplaySubject<List<ENTITY>> composite = ReplaySubject.create();
            Action0 unsubscribeCleanup = new UnsubscribeCleanup<List<ENTITY>>(this.logger, this.collectionRequests, key);
            Action0 completeCleanup = new CompleteCleanup<List<ENTITY>>(this.logger, this.collectionRequests, key);

            callback = callback.doOnCompleted(completeCleanup);

            Subscription mainSubscription = callback.subscribe(composite);
            CompositeRequestManager<List<ENTITY>> requestManager = new CompositeRequestManager<List<ENTITY>>(
                composite,
                mainSubscription,
                unsubscribeCleanup
            );
            this.collectionRequests.put(key, requestManager);
            subscription = requestManager.subscribe(observer);
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
     * @param key A unique key to identify this request type separate from others.
     * @return A subscription for the observer that may be unsubscribed if necessary.
     */
    final public Subscription createSubscription(
        OnSubscribe<ENTITY> onSubscribe,
        Observer<ENTITY> observer,
        String key
    ) {
        this.logger.trace("Creating subscription for Key: " + key);
        Observable<ENTITY> callback = Observable.create(onSubscribe);
        callback = callback.subscribeOn(this.subscribeScheduler);
        callback = callback.observeOn(this.observeScheduler);

        CompositeRequestManager<ENTITY> previousRequest = this.requests.get(key);
        Subscription subscription;
        if (null == previousRequest) {
            this.logger.debug("No previous request to join.");
            ReplaySubject<ENTITY> composite = ReplaySubject.create();
            Action0 unsubscribeCleanup = new UnsubscribeCleanup<ENTITY>(this.logger, this.requests, key);
            Action0 completeCleanup = new CompleteCleanup<ENTITY>(this.logger, this.requests, key);

            callback = callback.doOnCompleted(completeCleanup);

            Subscription mainSubscription = callback.subscribe(composite);
            CompositeRequestManager<ENTITY> manager = new CompositeRequestManager<ENTITY>(
                composite,
                mainSubscription,
                unsubscribeCleanup
            );
            this.requests.put(key, manager);
            subscription = manager.subscribe(observer);
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
