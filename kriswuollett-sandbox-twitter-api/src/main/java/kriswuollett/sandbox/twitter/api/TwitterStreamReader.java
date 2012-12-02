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
package kriswuollett.sandbox.twitter.api;

/**
 * Support for receving callbacks with Twitter stream objects.  Ownership of objects remain
 * with the caller.  If you wish to store the objects use the <code>clone</code> method.
 */
public interface TwitterStreamReader
{
    /**
     * @param tweet the new tweet message
     */
    public void onTweet( Tweet tweet );
    
    /**
     * @param delete the delete request that should be honored
     */
    public void onDelete( Delete delete );
    
    /**
     * @param scrubGeo the scrubbing request that should be honored
     */
    public void onScrubGeo( ScrubGeo scrubGeo );
    
    /**
     * @param limit the API limit info that should be recognized
     */
    public void onLimit( Limit limit );
}
