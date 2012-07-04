/**
 * Copyright 2012 Kristopher Wuollett
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
