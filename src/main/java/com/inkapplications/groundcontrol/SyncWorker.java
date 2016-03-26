/*
 * Copyright (c) 2015 Ink Applications, LLC.
 * Distributed under the MIT License (http://opensource.org/licenses/MIT)
 */
package com.inkapplications.groundcontrol;

import rx.Subscriber;

import java.sql.SQLException;
import java.util.Collection;

/**
 * A worker that synchronizes a remote data source with a local one.
 *
 * Implements the reactive callback in three phases:
 *  - Look up local data and inform subscriber
 *  - fetch remote data and save it locally
 *  - Look up local data again and inform subscriber
 *
 *  This will complete the subscriber when finished.
 *
 * @param <YIELD> The type of data that the worker will lookup and return.
 * @author Maxwell Vandervelde (Max@MaxVandervelde.com)
 */
abstract public class SyncWorker<YIELD> implements Worker<YIELD>
{
    @Override
    final public void call(Subscriber<? super YIELD> subscriber)
    {
        try {
            this.lookup(subscriber);
        } catch (Exception e) {
            subscriber.onError(e);
        }

        subscriber.onCompleted();
    }

    /**
     * Look up local data and sync remote data if needed.
     *
     * This will first lookup the local data and inform the subscriber.
     * If the local data is stale, it will look up the remote data and save it.
     * After saving remote data, it will do another local lookup.
     */
    private void lookup(Subscriber<? super YIELD> subscriber) throws Exception
    {
        YIELD currentEvents = this.lookupLocal();
        if (currentEvents instanceof Collection) {
            if (false == ((Collection) currentEvents).isEmpty()) {
                subscriber.onNext(currentEvents);
            }
        }

        if (false == this.dataIsStale()) {
            return;
        }

        this.syncRemote();
        YIELD newEvents = this.lookupLocal();
        subscriber.onNext(newEvents);
    }

    /**
     * Synchronize the remote API data with the local data.
     *
     * This looks up the remote entities and saves them to the local database.
     *
     * @throws SQLException If anything goes wrong with the local data lookup.
     * @throws Exception catch-all for if anything goes wrong in the lookup. Not
     *                   considered fatal.
     */
    protected void syncRemote() throws Exception
    {
        YIELD events = lookupRemote();
        this.saveLocal(events);
    }

    /**
     * Check if the local database is out of date.
     *
     * @return Whether a query should be run to update the local data with the
     *         remote API.
     * @throws SQLException If something goes wrong looking up local data.
     */
    abstract public boolean dataIsStale() throws SQLException;

    /**
     * Fetch the remote data entities.
     *
     * @return the entity or entities to be saved in the local database.
     * @throws Exception catch-all for if anything goes wrong in the lookup. Not
     *                   considered fatal.
     */
    abstract public YIELD lookupRemote() throws Exception;

    /**
     * Save data entities into the local database.
     *
     * @param yield The entities to be saved to the local database.
     * @throws SQLException If something goes wrong saving the local data.
     */
    abstract public void saveLocal(YIELD yield) throws SQLException;
}
