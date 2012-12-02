/*
 * Copyright (c) 2012, Kristopher Wuollett
 * All rights reserved.
 *
 * This file is part of kriswuollett/twitter-api.
 *
 * kriswuollett/twitter-api is free software: you can redistribute it and/or modify
 * it under the terms of the BSD 3-Clause License as written in the COPYING
 * file.
 */
package kriswuollett.sandbox.twitter.api.beans;

import kriswuollett.sandbox.twitter.api.ScrubGeo;
import kriswuollett.sandbox.twitter.api.TwitterStreamObjectType;

/**
 * @author kris
 */
public class ScrubGeoBean implements ScrubGeo
{

    @Override
    public void clear()
    {
    }

    @Override
    public TwitterStreamObjectType getType()
    {
        return TwitterStreamObjectType.SCRUB_GEO;
    }
}
