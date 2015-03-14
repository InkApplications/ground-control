/*
 * Copyright (c) 2015 Ink Applications, LLC.
 * Distributed under the MIT License (http://opensource.org/licenses/MIT)
 */
package com.inkapplications.groundcontrol;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Action0;
import rx.subjects.ReplaySubject;

/**
 * Handles subscribing, unsubscribing and tracking of combined requests.
 *
 * The in-flight requests for the library are stored as composite subjects that
 * contain multiple subscriptions. When one of these subscriptions is canceled,
 * we need to check if there are any other subscriptions in the composite
 * before the entire subject can be canceled.
 * RxJava's subjects lack the ability to gain insights on the state of the
 * subject. So, as a solution, this class manages the main subscription
 * and the subscriptions within it.
 * We keep track of the number of subscriptions here so that a safe unsubscribe
 * can be done as a cleanup when one of the children unsubscribes.
 *
 * @param <ENTITY> The type of data being managed in the composite request.
 * @author Maxwell Vandervelde (Max@MaxVandervelde.com)
 */
public class CompositeRequestManager<ENTITY>
{
    /** The subject containing multiple child subjects. */
    final private ReplaySubject<ENTITY> composite;

    /** The subscription to cancel the main composite subject. */
    final private Subscription subscription;

    /** Action performed when a child unsubscribes. */
    final private Action0 unsubscribeAction;

    /** The number of child subjects in the composite. */
    private int subscriptions = 0;

    /**
     * @param composite The subject containing multiple child subjects.
     * @param subscription The subscription to cancel the main composite subject.
     * @param unsubscribeAction Action performed when a child unsubscribes.
     */
    public CompositeRequestManager(
        ReplaySubject<ENTITY> composite,
        Subscription subscription,
        Action0 unsubscribeAction
    ) {
        this.composite = composite;
        this.subscription = subscription;
        this.unsubscribeAction = unsubscribeAction;
    }

    /**
     * Subscribe a new observer to the composite request.
     *
     * This will subscribe the observer to the bound subject and add the bound
     * unsubscribe action.
     *
     * @param observer The observer to subscribe to the request.
     * @return A subscription to cancel updates for the specified observer, not the whole subject.
     */
    public Subscription subscribe(Observer<ENTITY> observer)
    {
        this.subscriptions++;

        Observable<ENTITY> observable = this.composite.doOnUnsubscribe(this.unsubscribeAction);

        return observable.subscribe(observer);
    }

    /**
     * Cancel the subscription for the main composite subject.
     */
    final public void unsubscribe()
    {
        this.subscription.unsubscribe();
    }

    /**
     * Notify that a child subscription has been cancelled.
     *
     * This decreases the subscription count that is tracked by this object. It
     * should be called from the unsubscribe action bound to this class.
     */
    final public void removeSubscription()
    {
        this.subscriptions--;
    }

    /**
     * Get the number of subscribers that are still observing the composite subject.
     *
     * @return The number of subscribers observing the subject.
     */
    final public int subscriberCount()
    {
        return this.subscriptions;
    }
}
