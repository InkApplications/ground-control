/*
 * Copyright (c) 2015 Ink Applications, LLC.
 * Distributed under the MIT License (http://opensource.org/licenses/MIT)
 */
package com.inkapplications.groundcontrol;

import rx.Subscription;

import java.util.LinkedList;

/**
 * Keeps track of active rx subscriptions so that we can manipulate them as a group.
 *
 * @author Maxwell Vandervelde (Max@MaxVandervelde.com)
 */
public class SubscriptionManager
{
    /** A collection of open/active subscriptions. */
    final private LinkedList<Subscription> subscriptions = new LinkedList<Subscription>();

    /** Start tracking a new subscription. */
    public void add(Subscription subscription) {
        this.subscriptions.add(subscription);
    }

    /**
     * Unsubscribe from each of the tracked subscriptions.
     */
    public final void unsubscribeAll() {
        for (Subscription subscription : this.subscriptions) {
            subscription.unsubscribe();
        }
        this.subscriptions.clear();
    }
}
