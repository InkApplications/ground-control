/*
 * Copyright (c) 2015 Ink Applications, LLC.
 * Distributed under the MIT License (http://opensource.org/licenses/MIT)
 */
package com.inkapplications.groundcontrol;

import java.sql.SQLException;

/**
 * A remote synchronized worker that provides an API for updated removed entities.
 *
 * @param <YIELD> The type of data that the worker will lookup and return.
 * @author Maxwell Vandervelde (Max@MaxVandervelde.com)
 */
abstract public class RemovableSyncWorker<YIELD> extends SyncWorker<YIELD>
{
    @Override
    protected void syncRemote() throws Exception
    {
        super.syncRemote();

        YIELD removed = this.lookupRemovedRemote();
        this.removeLocal(removed);
    }

    /**
     * Lookup the entities to be removed locally.
     *
     * @return Any entities that have been removed on the remote API.
     * @throws Exception catch-all for if anything goes wrong in the lookup. Not
     *                   considered fatal.
     */
    abstract public YIELD lookupRemovedRemote() throws Exception;

    /**
     * Remove local entities.
     *
     * Invoked to synchronize any entities that have been removed from the API
     * and should therefore be removed locally as well.
     *
     * @param yield The entity or entities to remove.
     * @throws SQLException If an error occurs when removing the entities from
     *                      the local database.
     */
    abstract public void removeLocal(YIELD yield) throws SQLException;
}
