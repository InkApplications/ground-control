/*
 * Copyright (c) 2015 Ink Applications, LLC.
 * Distributed under the MIT License (http://opensource.org/licenses/MIT)
 */
package com.inkapplications.groundcontrol;

import java.util.HashMap;

/**
 * Defines the collection type used for storing key/value pairs to lookup request managers.
 *
 * @param <ENTITY> The type of data being managed in the composite request.
 * @author Maxwell Vandervelde (Max@MaxVandervelde.com)
 */
final class RequestCollection<ENTITY> extends HashMap<String, CompositeRequestManager<ENTITY>> {}
