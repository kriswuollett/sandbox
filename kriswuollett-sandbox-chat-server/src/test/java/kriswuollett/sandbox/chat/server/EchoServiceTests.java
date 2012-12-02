/*
 * Copyright (c) 2012, Kristopher Wuollett
 * All rights reserved.
 *
 * This file is part of kriswuollett/chat-server.
 *
 * kriswuollett/twitter-api is free software: you can redistribute it and/or modify
 * it under the terms of the BSD 3-Clause License as written in the COPYING
 * file.
 */
package kriswuollett.sandbox.chat.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
//import java.util.logging.ConsoleHandler;
//import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO Set up workers that push or pull as much data from the socket during tests instead of synchronous reads/writes
 */
public class EchoServiceTests
{
    private ExecutorService execSvc = Executors.newFixedThreadPool( 3 );
    private BufferedReader in;
    private BufferedWriter out;
    private Socket serviceSocket;
    private EchoServer server;
    private Thread serverThread;
    
    @Before
    public void setUp()
    {
        LogManager.getLogManager().reset();
    }
    
    @Test
    public void testSimpleMessage() throws Exception
    {
//        ConsoleHandler consoleHandler = new ConsoleHandler();
//        consoleHandler.setLevel( Level.INFO );
//        LogManager.getLogManager().getLogger( Logger.GLOBAL_LOGGER_NAME ).addHandler( consoleHandler );
//        LogManager.getLogManager().getLogger( "" ).addHandler( consoleHandler );        
        
        connect();
        
        try
        {
            final String expectedMessage = "Hello World!";
            
            sendMessage( out, expectedMessage );
            Assert.assertEquals( "message did not match", expectedMessage, recvMessage( in ) );
            
            serviceSocket.close();
        }
        finally
        {
            server.close();

            serverThread.join( 5000 );

            if ( serverThread.isAlive() )
                throw new RuntimeException( "The thread " + serverThread.getName() + " is still alive" );
        }

        Assert.assertFalse( "server is still open", server.getStatus().isOpen() );
    }

    @Test
    public void testMultipleMessages() throws Exception
    {
        connect();
        
        try
        {
            final String[] expectedMessages = new String[] { 
                    "Hello World!",
                    "Welcome!",
                    "This is my third message.",
                    "Did you get them all?"
            };
            
            sendMessages( out, expectedMessages );
            
            for ( final String expectedMessage : expectedMessages )
                Assert.assertEquals( "message did not match", expectedMessage, recvMessage( in ) );
            
            serviceSocket.close();
        }
        finally
        {
            server.close();

            serverThread.join( 5000 );

            if ( serverThread.isAlive() )
                throw new RuntimeException( "The thread " + serverThread.getName() + " is still alive" );
        }

        Assert.assertFalse( "server is still open", server.getStatus().isOpen() );
    }

    @Test
    public void testMultipleMessagesBatches() throws Exception
    {
        connect();
        
        try
        {
            final String[] expectedMessages = new String[] { 
                    "Hello World!",
                    "Welcome!",
                    "This is my third message.",
                    "Did you get them all?"
            };
            
            sendMessages( out, expectedMessages );
            
            for ( final String expectedMessage : expectedMessages )
                Assert.assertEquals( "message did not match", expectedMessage, recvMessage( in ) );
            
            sendMessages( out, expectedMessages );
            
            for ( final String expectedMessage : expectedMessages )
                Assert.assertEquals( "message did not match", expectedMessage, recvMessage( in ) );

            sendMessages( out, expectedMessages );
            
            for ( final String expectedMessage : expectedMessages )
                Assert.assertEquals( "message did not match", expectedMessage, recvMessage( in ) );

            serviceSocket.close();
        }
        finally
        {
            server.close();

            serverThread.join( 5000 );

            if ( serverThread.isAlive() )
                throw new RuntimeException( "The thread " + serverThread.getName() + " is still alive" );
        }

        Assert.assertFalse( "server is still open", server.getStatus().isOpen() );
    }
  
    @Test
    public void testTwoTogetherTooBig() throws Exception
    {
//        ConsoleHandler consoleHandler = new ConsoleHandler();
//        consoleHandler.setLevel( Level.INFO );
//        LogManager.getLogManager().getLogger( Logger.GLOBAL_LOGGER_NAME ).addHandler( consoleHandler );
//        LogManager.getLogManager().getLogger( "" ).addHandler( consoleHandler );
        
        final Logger log = Logger.getAnonymousLogger();
        
        connect();
        
        try
        {
            final int inBufSize = server.getInBufferSize();
            final int msgSizes  = inBufSize * 2 / 3;
            final String msg    = String.format( "%0" + msgSizes + "d", 1 );
            
            log.info( "Using msgSize " + msgSizes );
            
            sendMessages( out, msg, msg );
            Assert.assertEquals( "message 1.1 did not match", msg, recvMessage( in ) );
            Assert.assertEquals( "message 1.2 did not match", msg, recvMessage( in ) );
            
            sendMessages( out, msg, msg );
            Assert.assertEquals( "message 2.1 did not match", msg, recvMessage( in ) );
            Assert.assertEquals( "message 2.2 did not match", msg, recvMessage( in ) );

            serviceSocket.close();
        }
        finally
        {
            server.close();

            serverThread.join( 5000 );

            if ( serverThread.isAlive() )
                throw new RuntimeException( "The thread " + serverThread.getName() + " is still alive" );
        }

        Assert.assertFalse( "server is still open", server.getStatus().isOpen() );
    }
    
    @Test
    public void testAllMessageSizes() throws Exception
    {
//        ConsoleHandler consoleHandler = new ConsoleHandler();
//        consoleHandler.setLevel( Level.INFO );
//        LogManager.getLogManager().getLogger( Logger.GLOBAL_LOGGER_NAME ).addHandler( consoleHandler );
//        LogManager.getLogManager().getLogger( "" ).addHandler( consoleHandler );
        
        final Logger log = Logger.getAnonymousLogger();
        
        connect();
        
        try
        {
            final int inBufSize  = server.getInBufferSize();
            final int dataToSend = inBufSize * 3;

            for ( int msgSize = 1; msgSize < inBufSize; ++msgSize )
            {
                final int msgCount = ( dataToSend / msgSize ) + 1;
                final String expectedMessage = String.format( "%0" + msgSize + "d", 1 );
                final String[] expectedMessages = new String[ msgCount ];
                
                Arrays.fill( expectedMessages, expectedMessage );
                
                sendMessages( out, expectedMessages );
                
                for ( int i = 0; i < msgCount; ++i )
                    Assert.assertEquals( "message " + i + " with size " + msgSize + " did not match", expectedMessage, recvMessage( in ) );
            }

            serviceSocket.close();
        }
        finally
        {
            server.close();

            serverThread.join( 5000 );

            if ( serverThread.isAlive() )
                throw new RuntimeException( "The thread " + serverThread.getName() + " is still alive" );
        }

        Assert.assertFalse( "server is still open", server.getStatus().isOpen() );
    }
    
    private void connect() throws InterruptedException, UnknownHostException,
            IOException
    {
        server = new EchoServer();
        serverThread = new Thread( server, "EchoServiceServer" );

        serverThread.start();

        Thread.yield();
        if ( !server.getStatus().isOpen() )
        {
            synchronized (server.getStatus())
            {
                server.getStatus().wait( 5000 );
            }
        }
        
        Assert.assertTrue( "server not open", server.getStatus().isOpen() );
        Assert.assertTrue( "server port <= 0", server.getPort() > 0 );
        
        // Time to exercise some old-school Java network programming
        
        final InetAddress serviceAddress = InetAddress.getByName( "127.0.0.1" );
        
        serviceSocket = new Socket( serviceAddress, server.getPort() );
        
        Assert.assertTrue( "service socket not connected", serviceSocket.isConnected() );
        
        in = new BufferedReader( new InputStreamReader( serviceSocket.getInputStream() ) );
        out = new BufferedWriter( new OutputStreamWriter( serviceSocket.getOutputStream() ) );
    }

    private void sendMessage( final BufferedWriter out, final String message )
            throws InterruptedException, ExecutionException, TimeoutException
    {
        final Future<Void> task = execSvc.submit( new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                out.write( message + "\n" );
                out.flush();
                return null;
            }
        } );
        
        task.get( 1000, TimeUnit.MILLISECONDS );
    }
    
    private void sendMessages( final BufferedWriter out, final String... messages )
            throws InterruptedException, ExecutionException, TimeoutException
    {
        final Future<Void> task = execSvc.submit( new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                for ( final String message : messages )
                    out.write( message + "\n" );    
                
                out.flush();
                return null;
            }
        } );
        
        task.get( 1000, TimeUnit.MILLISECONDS );
    }

    private String recvMessage( final BufferedReader in )
            throws InterruptedException, ExecutionException, TimeoutException
    {
        final Future<String> task = execSvc.submit( new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return in.readLine();
            }
        } );
        
        return task.get( 1000, TimeUnit.MILLISECONDS );
    }
    
}
