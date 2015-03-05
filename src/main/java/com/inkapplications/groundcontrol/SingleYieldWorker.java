/*
 * Copyright (c) 2015 Ink Applications, LLC.
 * Distributed under the MIT License (http://opensource.org/licenses/MIT)
 */
package com.inkapplications.groundcontrol;

import rx.Subscriber;

/**
 * A simple worker with no criteria and a single local lookup.
 *
 * Implements the reactive callback to invoke our local lookup method and
 * provide the result to the subscriber. If any error occurs it will inform
 * the subscriber and will complete it when it's done.
 *
 * @param <YIELD> The type of data that the worker will lookup and return.
 * @author Maxwell Vandervelde (Max@MaxVandervelde.com)
 */
abstract public class SingleYieldWorker<YIELD> implements Worker<YIELD>
{
    @Override
    final public void call(Subscriber<? super YIELD> subscriber)
    {
        try {
            YIELD yield = this.lookupLocal();
            subscriber.onNext(yield);
        } catch (Exception e) {
            subscriber.onError(e);
        }

        subscriber.onCompleted();
    }
}
