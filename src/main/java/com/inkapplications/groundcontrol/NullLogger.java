/*
 * Copyright (c) 2015 Ink Applications, LLC.
 * Distributed under the MIT License (http://opensource.org/licenses/MIT)
 */
package com.inkapplications.groundcontrol;

/**
 * Empty Log class, here as a default for compatibility.
 *
 * @author Maxwell Vandervelde (Max@MaxVandervelde.com)
 */
final class NullLogger implements Log
{
    @Override public void debug(Object message) {}
    @Override public void debug(Object message, Throwable t) {}
    @Override public void error(Object message) {}
    @Override public void error(Object message, Throwable t) {}
    @Override public void fatal(Object message) {}
    @Override public void fatal(Object message, Throwable t) {}
    @Override public void info(Object message) {}
    @Override public void info(Object message, Throwable t) {}
    @Override public void trace(Object message) {}
    @Override public void trace(Object message, Throwable t) {}
    @Override public void warn(Object message) {}
    @Override public void warn(Object message, Throwable t) {}
}
