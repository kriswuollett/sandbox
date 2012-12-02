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

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;

/**
 * @author kris
 */
public class TwitterStreamClient
{

    private String consumerKey;
    private String consumerSecret;
    public String getConsumerKey()
    {
        return consumerKey;
    }

    public void setConsumerKey( String consumerKey )
    {
        this.consumerKey = consumerKey;
    }

    public String getConsumerSecret()
    {
        return consumerSecret;
    }

    public void setConsumerSecret( String consumerSecret )
    {
        this.consumerSecret = consumerSecret;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken( String token )
    {
        this.token = token;
    }

    public String getSecret()
    {
        return secret;
    }

    public void setSecret( String secret )
    {
        this.secret = secret;
    }

    private String token;
    private String secret;

    public void search( final String query )
            throws UnsupportedEncodingException
    {
        final String queryEncoded = URLEncoder.encode( query, Charset
                .defaultCharset().displayName() );
        System.out.printf( "Query: %s\n", query );
        System.out.printf( "Query Encoded: %s\n", queryEncoded );
    }

   
    public void stream() throws Exception
    {
        final OAuthConsumer consumer = new CommonsHttpOAuthConsumer( consumerKey, consumerSecret );

        consumer.setTokenWithSecret( token, secret );

        final HttpPost post = new HttpPost( "https://stream.twitter.com/1/statuses/filter.json" );

        // Query
        final List<NameValuePair> nvps = new LinkedList<NameValuePair>();
//        nvps.add( new BasicNameValuePair( "track", "twitter" ) );
//        nvps.add( new BasicNameValuePair( "locations", "-74,40,-73,41" ) );
      nvps.add( new BasicNameValuePair( "track", "'nyc','ny','new york','new york city'" ) );
        post.setEntity( new UrlEncodedFormEntity( nvps, HTTP.UTF_8 ) );
        post.getParams().setBooleanParameter( CoreProtocolPNames.USE_EXPECT_CONTINUE, false );

        consumer.sign( post );
        
        DefaultHttpClient httpClient = new DefaultHttpClient();
        
        HttpResponse response = httpClient.execute( post );
        
        int statusCode = response.getStatusLine().getStatusCode();

        if ( statusCode != 200 )
        {
            System.err.println( "Response statusCode=" + statusCode + " reason=" + response.getStatusLine().getReasonPhrase() );
            return;
        }
        
        final InputStream instream = response.getEntity().getContent();
        BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream( "sample.newYork.json" ) );
        byte[] buf = new byte[4096];
        try
        {
            int bytesRead;

            while (( bytesRead = instream.read( buf ) ) >= 0)
            {
                out.write( buf, 0, bytesRead );
                out.flush();
            }
        }
        finally
        {
            instream.close();
        }
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

        final Properties secrets = new Properties();

        secrets.load( new FileInputStream( "secrets.properties" ) );

        TwitterStreamClient client = new TwitterStreamClient();

        client.setConsumerKey( secrets.getProperty( "twitter.oauth.consumer.key" ) );
        client.setConsumerSecret( secrets.getProperty( "twitter.oauth.consumer.secret" ) );
        client.setToken( secrets.getProperty( "twitter.oauth.token" ) );
        client.setSecret( secrets.getProperty( "twitter.oauth.secret" ) );

        client.stream();
    }
}
