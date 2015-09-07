/*
 * Copyright (c) 2015 Ink Applications, LLC.
 * Distributed under the MIT License (http://opensource.org/licenses/MIT)
 */
package com.inkapplications.groundcontrol;

import rx.functions.Action0;

/**
 * Observer for removing completed requests in a collection.
 *
 * this is used to clean up the collection of "in-flight" requests after it is
 * completed in the subscription factory.
 * The requests collection here is stateful, an this observer will modify it
 * on completion of the request.
 *
 * @param <ENTITY> The subscription entity type that this is bound to.
 * @author Maxwell Vandervelde (Max@MaxVandervelde.com)
 */
final class UnsubscribeCleanup<ENTITY> implements Action0
{
    /** Log cleanup callback events. */
    final private Log logger;

    /** Stateful storage of in-flight requests. */
    final private RequestCollection<ENTITY> requests;

    /** Key to remove observables of on completion. */
    final private String key;

    /**
     * @param requests Stateful storage of in-flight requests.
     * @param key Key to remove observables of on completion.
     */
    public UnsubscribeCleanup(
        Log logger,
        RequestCollection<ENTITY> requests,
        String key
    ) {
        this.logger = logger;
        this.requests = requests;
        this.key = key;
    }

    @Override
    public void call()
    {
        this.logger.trace("Subject Unsubscribed.");

        CompositeRequestManager<ENTITY> manager = this.requests.get(this.key);
        if (null == manager) {
            this.logger.debug("Key already clean.");
            return;
        }

        if (manager.subscriberCount() > 1) {
            this.logger.debug("Still observing. Keeping subject alive.");
            manager.removeSubscription();
            return;
        }

        this.logger.trace("Cleaning up key: " + key);
        this.requests.remove(this.key);
        manager.unsubscribe();
    }
}
