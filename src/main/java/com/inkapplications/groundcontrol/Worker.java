/*
 * Copyright (c) 2015 Ink Applications, LLC.
 * Distributed under the MIT License (http://opensource.org/licenses/MIT)
 */
package com.inkapplications.groundcontrol;

import rx.Observable;

import java.sql.SQLException;

/**
 * A service that looks up data locally.
 *
 * @param <YIELD> The type of data that the worker will lookup and return.
 * @author Maxwell Vandervelde (Max@MaxVandervelde.com)
 */
public interface Worker<YIELD> extends Observable.OnSubscribe<YIELD>
{
    /**
     * Synchronously lookup data locally.
     *
     * @return The data found locally.
     * @throws SQLException If any problems occur during lookup.
     */
    public YIELD lookupLocal() throws SQLException;
}
