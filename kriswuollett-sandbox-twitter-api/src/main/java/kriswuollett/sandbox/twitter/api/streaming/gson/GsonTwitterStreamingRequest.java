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
package kriswuollett.sandbox.twitter.api.streaming.gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import kriswuollett.sandbox.twitter.api.Delete;
import kriswuollett.sandbox.twitter.api.Limit;
import kriswuollett.sandbox.twitter.api.ScrubGeo;
import kriswuollett.sandbox.twitter.api.Tweet;
import kriswuollett.sandbox.twitter.api.TwitterField;
import kriswuollett.sandbox.twitter.api.TwitterStreamObject;
import kriswuollett.sandbox.twitter.api.TwitterStreamReader;
import kriswuollett.sandbox.twitter.api.beans.DeleteBean;
import kriswuollett.sandbox.twitter.api.beans.LimitBean;
import kriswuollett.sandbox.twitter.api.beans.ScrubGeoBean;
import kriswuollett.sandbox.twitter.api.beans.TweetBean;
import kriswuollett.sandbox.twitter.api.streaming.TwitterStreamingRequest;
import kriswuollett.sandbox.twitter.api.streaming.TwitterStreamingResponse;
import kriswuollett.sandbox.twitter.api.streaming.parameters.Track;
import kriswuollett.sandbox.twitter.api.streaming.parameters.TwitterStreamParameter;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/**
 * TODO Eventually will allow for selective processing of fields on the twitter stream?
 * TODO Remove assumption that delete, scrub_geo, and limit is always first key?
 */
public class GsonTwitterStreamingRequest implements TwitterStreamingRequest
{
    private enum State
    {
        BEGIN, MESSAGE, TWEET, TWEET_FIELD, DELETE, SCRUB_GEO, LIMIT
    }

    private static class Response implements TwitterStreamingResponse
    {
        private boolean success;
        private String message;

        public Response( boolean success, String message )
        {
            this.success = success;
            this.message = message;
        }
        
        @Override
        public boolean isSuccess()
        {
            return success;
        }

        @Override
        public String getMessage()
        {
            return message;
        }
    }
    
    private final Logger log = Logger.getLogger( "TwitterStreamingService" );

    private CommonsHttpOAuthConsumer consumer;
    
    private TwitterStreamReader listener;
    private String track;

    private JsonReader reader;
    
    private TwitterField stateField;
    private State state;
    private Deque<TwitterStreamObject> stack = new ArrayDeque<TwitterStreamObject>();
    
    @Override
    public TwitterStreamingRequest setOAuthConsumer( final CommonsHttpOAuthConsumer consumer )
    {
        this.consumer = consumer;
        return this;
    }
    
    @Override
    public TwitterStreamingRequest setListener( TwitterStreamReader listener )
    {
        this.listener = listener;
        return this;
    }

    @Override
    public TwitterStreamingRequest setParameters( TwitterStreamParameter... parameters )
    {
        for ( final TwitterStreamParameter param : parameters )
        {
            switch ( param.getType() )
            {
                case TRACK:
                    track = ( ( Track ) param ).getTrack();
                    break;
            }
        }
        return this;
    }

    @Override
    public TwitterStreamingResponse call() throws Exception
    {
        // Strict settings for now
        
        if ( consumer == null )
            throw new IllegalStateException( "The consumer was null" );

        if ( track == null )
            throw new IllegalStateException( "The track was null" );
        
        if ( listener == null )
            throw new IllegalStateException( "The listener was null" );
        
        final HttpPost post = new HttpPost( "https://stream.twitter.com/1/statuses/filter.json" );
        
        final List<NameValuePair> nvps = new LinkedList<NameValuePair>();
        
        // nvps.add( new BasicNameValuePair( "track", "twitter" ) );
        // nvps.add( new BasicNameValuePair( "locations", "-74,40,-73,41" ) );
        //nvps.add( new BasicNameValuePair( "track", "'nyc','ny','new york','new york city'" ) );
        nvps.add( new BasicNameValuePair( "track", track ) );
        post.setEntity( new UrlEncodedFormEntity( nvps, HTTP.UTF_8 ) );
        post.getParams().setBooleanParameter( CoreProtocolPNames.USE_EXPECT_CONTINUE, false );

        consumer.sign( post );

        DefaultHttpClient httpClient = new DefaultHttpClient();

        HttpResponse response = httpClient.execute( post );
      
        int statusCode = response.getStatusLine().getStatusCode();
        
        if ( statusCode != HttpStatus.SC_OK )
            return new Response( false, response.getStatusLine().getReasonPhrase() );
        
        final InputStream in = response.getEntity().getContent();
        
        reader = new JsonReader( new InputStreamReader( in, "UTF-8" ) );
        reader.setLenient( true );

        try
        {
            state = State.BEGIN;
            stateField = null;
            stack.clear();
            
            while (true)
            {
                final JsonToken token = reader.peek();

                switch (token)
                {
                    case BEGIN_ARRAY:
                        reader.beginArray();
                        if ( log.isLoggable( Level.FINEST ) ) log.finest( String.format("%sBEGIN_ARRAY STATE=%s STACK_SIZE=%d", padding( stack.size() ), state, stack.size() ) );
                        onBeginArray();
                        break;
                    case END_ARRAY:
                        reader.endArray();
                        if ( log.isLoggable( Level.FINEST ) ) log.finest( String.format("%sEND_ARRAY STATE=%s STACK_SIZE=%d", padding( stack.size() ), state, stack.size() ) );
                        onEndArray();
                        break;
                    case BEGIN_OBJECT:
                        reader.beginObject();
                        if ( log.isLoggable( Level.FINEST ) ) log.finest( String.format("%sBEGIN_OBJECT STATE=%s STACK_SIZE=%d", padding( stack.size() ), state, stack.size() ) );
                        onBeginObject();
                        break;
                    case END_OBJECT:
                        reader.endObject();
                        if ( log.isLoggable( Level.FINEST ) ) log.finest( String.format("%sEND_OBJECT STATE=%s STACK_SIZE=%d", padding( stack.size() ), state, stack.size() ) );
                        onEndObject();
                        break;
                    case NAME:
                        String name = reader.nextName();
                        if ( log.isLoggable( Level.FINEST ) ) log.finest( String.format( "%sNAME - %s", padding( stack.size() ), name ) );
                        onName( name );
                        break;
                    case STRING:
                        String s = reader.nextString();
                        if ( log.isLoggable( Level.FINEST ) ) log.finest( String.format( "%sSTRING - %s", padding( stack.size() ), s ) );
                        onString( s );
                        break;
                    case NUMBER:
                        String n = reader.nextString();
                        if ( log.isLoggable( Level.FINEST ) ) log.finest( String.format( "%sNUMBER - %s", padding( stack.size() ), n ) );
                        onNumber( n );
                        break;
                    case BOOLEAN:
                        boolean b = reader.nextBoolean();
                        if ( log.isLoggable( Level.FINEST ) ) log.finest( String.format( "%sBOOLEAN - %b", padding( stack.size() ), b ) );
                        onBoolean( b );
                        break;
                    case NULL:
                        reader.nextNull();
                        if ( log.isLoggable( Level.FINEST ) ) log.finest( String.format( "%sNULL", padding( stack.size() ) ) );
                        onNull();
                        break;
                    case END_DOCUMENT:
                        if ( log.isLoggable( Level.FINEST ) ) log.finest( String.format( "%sEND_DOCUMENT", padding( stack.size() ) ) );
                        return new Response( true, "OK" );
                }
            }
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }   
    }

    private void onBeginObject()
    {
        switch ( state )
        {
            case BEGIN:
            {
                state = State.MESSAGE;
                break;
            }
            default:
            {
                throw new RuntimeException( "Got BEGIN_OBJECT in state " + state );
            }
        }
    }

    private void onEndObject()
    {
        switch ( state )
        {
            case TWEET:
            {
                listener.onTweet( ( Tweet ) stack.pop() );
                state = State.BEGIN;
                break;
            }
            case DELETE:
            {
                listener.onDelete( ( Delete  ) stack.pop() );
                state = State.BEGIN;
                break;
            }
            case SCRUB_GEO:
            {
                listener.onScrubGeo( ( ScrubGeo ) stack.pop() );
                state = State.BEGIN;
                break;
            }
            case LIMIT:
            {
                listener.onLimit( ( Limit ) stack.pop() );
                state = State.BEGIN;
                break;
            }
            default:
            {
                throw new RuntimeException( "Got END_OBJECT in state " + state );
            }
        }
    }

    private void onName( String name ) throws IOException
    {
        stateField = TwitterField.lookup( name );
        
        switch ( state )
        {
            case MESSAGE:
            {
                // Determine the type of message

                assert stack.size() == 0;
                
                switch ( stateField )
                {
                    case DELETE:
                    {
                        stack.push( new DeleteBean() );
                        state = State.DELETE;
                        reader.skipValue();
                        break;
                    }
                    case SCRUB_GEO:
                    {
                        stack.push( new ScrubGeoBean() );
                        state = State.SCRUB_GEO;
                        reader.skipValue();
                        break;
                    }
                    case LIMIT:
                    {
                        stack.push( new LimitBean() );
                        state = State.LIMIT;
                        reader.skipValue();
                        break;
                    }
                    default:
                    {
                        // assume is the first field of a tweet
                        stack.push( new TweetBean() );
                        onTweetField();
                        break;
                    }
                }
                
                break;
            }
            case TWEET:
            {
                onTweetField();
                break;
            }
            default:
            {
                reader.skipValue();
            }
        }        
    }

    private void onTweetField() throws IOException
    {
        switch ( stateField )
        {
            case ID_STR:
            case TEXT:
                state = State.TWEET_FIELD;
                break;
            default:
                state = State.TWEET;
                reader.skipValue();
        }
    }

    private void onBeginArray()
    {
        // TODO actually handle arrays
        
        switch ( state )
        {
            default:
            {
                throw new RuntimeException( "Got BEGIN_ARRAY in state " + state );
            }
        }
    }
    
    private void onEndArray()
    {
        switch ( state )
        {
            default:
            {
                throw new RuntimeException( "Got END_ARRAY in state " + state );
            }
        }
    }
    
    private void onNull()
    {
        switch ( state )
        {
            default:
            {
                throw new RuntimeException( "Got NULL in state " + state );
            }
        }  
    }

    private void onBoolean( boolean b )
    {
        switch ( state )
        {
            default:
            {
                throw new RuntimeException( "Got BOOLEAN in state " + state );
            }
        }             
    }

    private void onNumber( String n )
    {
        switch ( state )
        {
            default:
            {
                throw new RuntimeException( "Got NUMBER in state " + state );
            }
        }          
    }

    private void onString( String s )
    {
        switch ( state )
        {
            case TWEET_FIELD:
            {
                switch ( stateField )
                {
                    case TEXT:   
                        ( ( Tweet ) stack.peek() ).setTextFromString( s ); 
                        break;
                    case ID_STR: 
                        ( ( Tweet ) stack.peek() ).setIdFromString( s );
                        break;
                }
                
                state = State.TWEET;
                break;
            }
            default:
            {
                throw new RuntimeException( "Got STRING in state " + state );
            }
        }
    }

    private String padding( int amount )
    {
        switch ( amount )
        {
            case 0: return "";
            case 1: return "  ";
            case 2: return "    ";
            case 3: return "      ";
            case 4: return "        ";
            case 5: return "          ";
            default:
                StringBuilder buf = new StringBuilder();
                for ( int i = 0; i < amount; ++i )
                    buf.append( "  " );
                return buf.toString();

        }
    }
}
