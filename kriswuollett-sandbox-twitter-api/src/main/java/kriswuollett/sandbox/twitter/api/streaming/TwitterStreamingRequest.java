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
package kriswuollett.sandbox.twitter.api.streaming;

import java.io.InputStream;
import java.util.concurrent.Callable;

import kriswuollett.sandbox.twitter.api.TwitterStreamReader;
import kriswuollett.sandbox.twitter.api.streaming.parameters.TwitterStreamParameter;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;


/**
 * @author kris
 */
public interface TwitterStreamingRequest extends Callable<TwitterStreamingResponse>
{
    /* TODO replace separate input stream and oauthconsumer calls with a setSource method */
    public abstract TwitterStreamingRequest setInputStream( final InputStream inputStream );
    
    public abstract TwitterStreamingRequest setOAuthConsumer( final CommonsHttpOAuthConsumer consumer );
    
    public abstract TwitterStreamingRequest setListener( final TwitterStreamReader listener );
    
    public abstract TwitterStreamingRequest setParameters( final TwitterStreamParameter... parameters );
}
