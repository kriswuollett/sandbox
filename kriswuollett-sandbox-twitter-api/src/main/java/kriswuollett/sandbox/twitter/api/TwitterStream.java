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

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import kriswuollett.sandbox.twitter.api.streaming.TwitterStreamingRequest;
import kriswuollett.sandbox.twitter.api.streaming.TwitterStreamingResponse;
import kriswuollett.sandbox.twitter.api.streaming.gson.GsonTwitterStreamingRequest;
import kriswuollett.sandbox.twitter.api.streaming.parameters.TwitterStreamParameter;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

/**
 * The <code>secrets.properties</code> file contains the following (replace with your own values):
 * <pre>
twitter.oauth.consumer.key=xxx
twitter.oauth.consumer.secret=xxx
twitter.oauth.token=xxx
twitter.oauth.secret=xxx
</pre>
 *
 * @author kris
 */
public class TwitterStream implements TwitterStreamReader
{

    @Override
    public void onTweet( Tweet tweet )
    {
        System.err.printf( "onTweet: %s\n", tweet );
    }

    @Override
    public void onDelete( Delete delete )
    {
        System.err.printf( "onDelete: %s\n", delete );
        
    }

    @Override
    public void onScrubGeo( ScrubGeo scrubGeo )
    {
        System.err.printf( "onScrubGeo: %s\n", scrubGeo );
    }

    @Override
    public void onLimit( Limit limit )
    {
        System.err.printf( "onLimit: %s\n", limit );
    } 
    
    /**
     * @param args
     */
    public static void main( String[] args ) throws Exception
    {
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel( Level.FINEST );
        LogManager.getLogManager().getLogger( Logger.GLOBAL_LOGGER_NAME ).addHandler( consoleHandler );
        LogManager.getLogManager().getLogger( "" ).addHandler( consoleHandler );

        final TwitterStream app = new TwitterStream();

        final TwitterStreamingRequest req;
        
        if ( args.length == 2 )
        {
            final InputStream secretsInputStream = new FileInputStream( args[ 0 ] );
            final String track = args[ 1 ];
            
            final Properties secrets = new Properties();
    
            secrets.load( secretsInputStream );
            
            final CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer( secrets.getProperty( "twitter.oauth.consumer.key" ), secrets.getProperty( "twitter.oauth.consumer.secret" ) );
            
            consumer.setTokenWithSecret( secrets.getProperty( "twitter.oauth.token" ), secrets.getProperty( "twitter.oauth.secret" ) );
            
            req = new GsonTwitterStreamingRequest()
                .setListener( app )
                .setOAuthConsumer( consumer )
                .setParameters( TwitterStreamParameter.Track( track ) );
        }
        else if ( args.length == 1 )
        {
            req = new GsonTwitterStreamingRequest()
                .setListener( app )
                .setInputStream( new FileInputStream( args[ 0 ] ) );
        }
        else
        {
            throw new IllegalArgumentException( "Usage: <secrets.properties> <track> | <input file>" ); 
        }
        
        final ExecutorService execSvc = Executors.newFixedThreadPool( 1 );
        
        final Future<TwitterStreamingResponse> rspTask = execSvc.submit( req ); 
        
        final TwitterStreamingResponse rsp = rspTask.get();
        
        if ( !rsp.isSuccess() )
            System.err.printf( "Streaming failed: %s\n", rsp.getMessage() );
        
        execSvc.shutdownNow();
    }
}
