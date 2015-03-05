/*
 * Copyright (c) 2015 Ink Applications, LLC.
 * Distributed under the MIT License (http://opensource.org/licenses/MIT)
 */
package com.inkapplications.groundcontrol;

/**
 * Factory for creating new workers that require lookup criteria at runtime.
 *
 * Since asynchronous data is invoked through a callback, criteria needed for
 * lookup needs to be provided to the worker at construction.
 * This service defines a factory that can, at runtime, create these workers.
 *
 * If the lookup needs multiple parameters, you should create an object to
 * wrap them and use that as the criteria.
 *
 * @param <YIELD> The type of data that the worker will lookup and return.
 * @param <CRITERIA> The lookup criteria to be provided to the worker constructor.
 * @author Maxwell Vandervelde (Max@MaxVandervelde.com)
 */
public interface CriteriaWorkerFactory<YIELD, CRITERIA>
{
    /**
     * Create a new worker.
     *
     * @param criteria The lookup criteria to be provided to the worker constructor.
     * @return The fully constructed worker to use for data lookup.
     */
    public Worker<YIELD> createWorker(CRITERIA criteria);
}
