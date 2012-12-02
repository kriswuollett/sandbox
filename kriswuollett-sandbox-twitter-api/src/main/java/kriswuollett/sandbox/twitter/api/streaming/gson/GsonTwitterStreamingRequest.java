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
package kriswuollett.sandbox.twitter.api.streaming.gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
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
import kriswuollett.sandbox.twitter.api.User;
import kriswuollett.sandbox.twitter.api.beans.DeleteBean;
import kriswuollett.sandbox.twitter.api.beans.LimitBean;
import kriswuollett.sandbox.twitter.api.beans.ScrubGeoBean;
import kriswuollett.sandbox.twitter.api.beans.TweetBean;
import kriswuollett.sandbox.twitter.api.beans.UserBean;
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
        BEGIN, MESSAGE, TWEET, TWEET_FIELD, DELETE, SCRUB_GEO, LIMIT, USER, USER_FIELD
    }
    
    private static class ParseState
    {
        public State state;
        public TwitterStreamObject object;
        public TwitterField field;

        public ParseState( State state, TwitterStreamObject object, TwitterField field )
        {
            this.state = state;
            this.object = object;
            this.field = field;
        }

        @Override
        public String toString()
        {
            return "[state=" + state + ", object=" + object
                    + ", field=" + field + "]";
        }
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

    private InputStream inputStream;
    private CommonsHttpOAuthConsumer consumer;
    
    private TwitterStreamReader listener;
    private String track;

    private JsonReader reader;
    
    private Deque<ParseState> stack1 = new ArrayDeque<ParseState>();
    
    private DateFormat dateFormat = new SimpleDateFormat( "EEE MMM d HH:mm:ss Z yyyy" );
    
    @Override
    public TwitterStreamingRequest setInputStream( InputStream inputStream )
    {
        this.inputStream = inputStream;
        return this;
    }

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
        if ( listener == null )
            throw new IllegalStateException( "The listener was null" );

        final InputStream in;
        
        if ( inputStream == null )
        {
            if ( consumer == null )
                throw new IllegalStateException( "The consumer was null" );
    
            if ( track == null )
                throw new IllegalStateException( "The track was null" );
            
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
            
            in = response.getEntity().getContent();
        }
        else
        {
            if ( consumer != null )
                throw new IllegalStateException( "The consumer parameter cannot be used with inputStream" );
    
            if ( track != null )
                throw new IllegalStateException( "The track parameter cannot be used with inputStream" );
            
            in = inputStream;
        }
        
        reader = new JsonReader( new InputStreamReader( in, "UTF-8" ) );
        reader.setLenient( true );

        try
        {
            stack1.clear();
            
            ParseState state = new ParseState( State.BEGIN, null, null );

            stack1.push( state );
            
            while (true)
            {
                final JsonToken token = reader.peek();

                switch (token)
                {
                    case BEGIN_ARRAY:
                        reader.beginArray();
                        if ( log.isLoggable( Level.FINEST ) ) log.finest( String.format("%sBEGIN_ARRAY STATE=%s STACK_SIZE=%d", padding( stack1.size() ), state, stack1.size() ) );
                        onBeginArray();
                        break;
                    case END_ARRAY:
                        reader.endArray();
                        if ( log.isLoggable( Level.FINEST ) ) log.finest( String.format("%sEND_ARRAY STATE=%s STACK_SIZE=%d", padding( stack1.size() ), state, stack1.size() ) );
                        onEndArray();
                        break;
                    case BEGIN_OBJECT:
                        reader.beginObject();
                        if ( log.isLoggable( Level.FINEST ) ) log.finest( String.format("%sBEGIN_OBJECT STATE=%s STACK_SIZE=%d", padding( stack1.size() ), state, stack1.size() ) );
                        onBeginObject();
                        break;
                    case END_OBJECT:
                        reader.endObject();
                        if ( log.isLoggable( Level.FINEST ) ) log.finest( String.format("%sEND_OBJECT STATE=%s STACK_SIZE=%d", padding( stack1.size() ), state, stack1.size() ) );
                        onEndObject();
                        break;
                    case NAME:
                        String name = reader.nextName();
                        if ( log.isLoggable( Level.FINEST ) ) log.finest( String.format( "%sNAME - %s", padding( stack1.size() ), name ) );
                        onName( name );
                        break;
                    case STRING:
                        String s = reader.nextString();
                        if ( log.isLoggable( Level.FINEST ) ) log.finest( String.format( "%sSTRING - %s", padding( stack1.size() ), s ) );
                        onString( s );
                        break;
                    case NUMBER:
                        String n = reader.nextString();
                        if ( log.isLoggable( Level.FINEST ) ) log.finest( String.format( "%sNUMBER - %s", padding( stack1.size() ), n ) );
                        onNumber( n );
                        break;
                    case BOOLEAN:
                        boolean b = reader.nextBoolean();
                        if ( log.isLoggable( Level.FINEST ) ) log.finest( String.format( "%sBOOLEAN - %b", padding( stack1.size() ), b ) );
                        onBoolean( b );
                        break;
                    case NULL:
                        reader.nextNull();
                        if ( log.isLoggable( Level.FINEST ) ) log.finest( String.format( "%sNULL", padding( stack1.size() ) ) );
                        onNull();
                        break;
                    case END_DOCUMENT:
                        if ( log.isLoggable( Level.FINEST ) ) log.finest( String.format( "%sEND_DOCUMENT", padding( stack1.size() ) ) );
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
        final ParseState state = stack1.peek();
        
        switch ( state.state )
        {
            case BEGIN:
            {
                stack1.push( new ParseState( State.MESSAGE, null, null ) );
                return;
            }
            case TWEET_FIELD:
            {
                switch ( state.field )
                {
                    case USER:
                    {
                        stack1.push( new ParseState( State.USER, new UserBean(), null ) );
                        return;
                    }
                }
            }
            default:
            {
                throw new RuntimeException( "Got BEGIN_OBJECT in state " + state );
            }
        }
    }

    private void onEndObject()
    {
        ParseState state = stack1.peek();
        
        switch ( state.state )
        {
            case TWEET:
            {
                listener.onTweet( ( Tweet ) state.object );
                stack1.pop();
                break;
            }
            case DELETE:
            {
                listener.onDelete( ( Delete  ) state.object );
                stack1.pop();
                break;
            }
            case SCRUB_GEO:
            {
                listener.onScrubGeo( ( ScrubGeo ) state.object );
                stack1.pop();
                break;
            }
            case LIMIT:
            {
                listener.onLimit( ( Limit ) state.object );
                stack1.pop();
                break;
            }
            case USER:
            {
                ParseState oldState = stack1.pop();
                
                state = stack1.peek();
                
                if ( state.state != State.TWEET_FIELD )
                    throw new RuntimeException( "Got END_OBJECT USER in state " + state + " with prev state " + state );
                
                switch ( state.field )
                {
                    case USER:
                    {
                        ( ( Tweet ) state.object ).setUser( ( User ) oldState.object );
                        break;
                    }
                    default:
                    {
                        throw new RuntimeException( "Got END_OBJECT USER for TWEET_FIELD for unsupported field " + state.field );
                    }
                }
                
                state.state = State.TWEET;
                
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
        final ParseState state = stack1.peek();
        
        state.field = TwitterField.lookup( name );
        
        switch ( state.state )
        {
            case MESSAGE:
            {
                // Determine the type of message

                assert stack1.size() == 0;
                
                switch ( state.field )
                {
                    case DELETE:
                    {
                        state.object = new DeleteBean();
                        state.state = State.DELETE;
                        reader.skipValue();
                        break;
                    }
                    case SCRUB_GEO:
                    {
                        state.object = new ScrubGeoBean();
                        state.state = State.SCRUB_GEO;
                        reader.skipValue();
                        break;
                    }
                    case LIMIT:
                    {
                        state.object = new LimitBean();
                        state.state = State.LIMIT;
                        reader.skipValue();
                        break;
                    }
                    default:
                    {
                        // assume is the first field of a tweet
                        state.object = new TweetBean();
                        onTweetField( state );
                        break;
                    }
                }
                
                break;
            }
            case TWEET:
            {
                onTweetField( state );
                break;
            }
            case USER:
            {
                onUserField( state );
                break;
            }
            default:
            {
                reader.skipValue();
            }
        }        
    }

    private void onTweetField( ParseState state ) throws IOException
    {
        switch ( state.field )
        {
            case ID_STR:
            case TEXT:
            case CREATED_AT:
            case FAVORITED:
            case WITHHELD_SCOPE:
            case WITHHELD_IN_COUNTRIES:
            case WITHHELD_COPYRIGHT:
            case TRUNCATED:
            case SOURCE:
            case RETWEETED:
            case RETWEET_COUNT:
            case POSSIBLY_SENSITIVE:
            case USER:
                state.state = State.TWEET_FIELD;
                break;
            default:
                state.state = State.TWEET;
                reader.skipValue();
        }
    }

    private void onUserField( ParseState state ) throws IOException
    {
        switch ( state.field )
        {
            case NAME:
            case DESCRIPTION:
            case SCREEN_NAME:
                state.state = State.USER_FIELD;
                break;
            default:
                state.state = State.USER;
                reader.skipValue();
        }
    }

    private void onBeginArray()
    {
        // TODO actually handle arrays
        final ParseState state = stack1.peek();
        
        switch ( state.state )
        {
            default:
            {
                throw new RuntimeException( "Got BEGIN_ARRAY in state " + state );
            }
        }
    }
    
    private void onEndArray()
    {
        final ParseState state = stack1.peek();
        
        switch ( state.state )
        {
            default:
            {
                throw new RuntimeException( "Got END_ARRAY in state " + state );
            }
        }
    }
    
    private void onNull()
    {
        final ParseState state = stack1.peek();
        
        switch ( state.state )
        {
            case TWEET_FIELD:
                state.state = State.TWEET;
                break;
            case USER_FIELD:
                state.state = State.USER;
                break;
            default:
                throw new RuntimeException( "Got NULL in state " + state );
        }  
    }

    private void onBoolean( boolean b )
    {
        final ParseState state = stack1.peek();
        
        switch ( state.state )
        {
            case TWEET_FIELD:
            {
                switch ( state.field )
                {
                    case FAVORITED:
                        ( ( Tweet ) state.object ).setFavorited( b ); 
                        break;
                    case WITHHELD_COPYRIGHT:
                        ( ( Tweet ) state.object ).setWithheldCopyright( b ); 
                        break;
                    case TRUNCATED:
                        ( ( Tweet ) state.object ).setTruncated( b ); 
                        break;
                    case RETWEETED:
                        ( ( Tweet ) state.object ).setRetweeted( b ); 
                        break;
                }
                
                state.state = State.TWEET;
                break;
            }
            default:
            {
                throw new RuntimeException( "Got BOOLEAN in state " + state );
            }
        }             
    }

    private void onNumber( String n )
    {
        final ParseState state = stack1.peek();
        
        switch ( state.state )
        {
            case TWEET_FIELD:
            {
                switch ( state.field )
                {
                    case RETWEET_COUNT: 
                        ( ( Tweet ) state.object ).setRetweetCount( Integer.parseInt( n ) );
                        break;
                }
                
                state.state = State.TWEET;
                break;
            }
            
            default:
            {
                throw new RuntimeException( "Got NUMBER in state " + state );
            }
        }          
    }

    private void onString( String s )
    {
        final ParseState state = stack1.peek();
        
        switch ( state.state )
        {
            case TWEET_FIELD:
            {
                switch ( state.field )
                {
                    case TEXT:   
                        ( ( Tweet ) state.object ).setTextFromString( s ); 
                        break;
                    case ID_STR: 
                        ( ( Tweet ) state.object ).setIdFromString( s );
                        break;
                    case CREATED_AT:
                    {
                        try
                        {
                            final Date date = dateFormat.parse( s );
                            final Tweet t = ( Tweet ) state.object;
                            t.setCreatedAt( date.getTime() );
                        }
                        catch ( ParseException e )
                        {
                            throw new RuntimeException( e );
                        }
                        break;
                   }
                   case WITHHELD_SCOPE: 
                        ( ( Tweet ) state.object ).setWithheldScope( s );
                        break;
                   case WITHHELD_IN_COUNTRIES: 
                       ( ( Tweet ) state.object ).setWithheldInCountries( s );
                       break;
                   case SOURCE: 
                       ( ( Tweet ) state.object ).setSource( s );
                       break;
                }
                
                state.state = State.TWEET;
                break;
            }
            case USER_FIELD:
            {
                switch ( state.field )
                {
                    case SCREEN_NAME:
                        ( ( User ) state.object ).setScreenName( s );
                        break;
                    case DESCRIPTION:
                        ( ( User ) state.object ).setDescription( s );
                        break;
                    case NAME:
                        ( ( User ) state.object ).setName( s );
                        break;
                }
                
                state.state = State.USER;
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
