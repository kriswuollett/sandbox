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
package kriswuollett.sandbox.twitter.api.streaming.parameters;


/**
 * @author kris
 *
 */
public class Track extends TwitterStreamParameter
{

    private String track;

    public Track( final String track )
    {
        this.track = track;
    }
    
    @Override
    public TwitterStreamParameterType getType()
    {
        return TwitterStreamParameterType.TRACK;
    }

    public String getTrack()
    {
        return track;
    }
}
