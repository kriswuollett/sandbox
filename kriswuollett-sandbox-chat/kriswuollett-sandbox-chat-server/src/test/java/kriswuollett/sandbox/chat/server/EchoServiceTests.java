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
package kriswuollett.sandbox.chat.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;

public class EchoServiceTests
{
    private ExecutorService execSvc = Executors.newFixedThreadPool( 3 );
    
    @Test
    public void testSimpleMessage() throws Exception
    {
        final EchoServer server = new EchoServer();
        final Thread serverThread = new Thread( server, "EchoServiceServer" );

        serverThread.start();

        Thread.yield();

        try
        {
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
            
            final Socket serviceSocket = new Socket( serviceAddress, server.getPort() );
            
            Assert.assertTrue( "service socket not connected", serviceSocket.isConnected() );
            
            final BufferedReader in = new BufferedReader( new InputStreamReader( serviceSocket.getInputStream() ) );
            final BufferedWriter out = new BufferedWriter( new OutputStreamWriter( serviceSocket.getOutputStream() ) );
            
            final String expectedMessage = "Hello World!";
            
            sendMessage( out, expectedMessage );
            Assert.assertEquals( "message did not match", expectedMessage, recvMessage( in ) );
            
            serviceSocket.close();
        }
        finally
        {
            server.stop();

            serverThread.join( 5000 );

            if ( serverThread.isAlive() )
                throw new RuntimeException( "The thread " + serverThread.getName() + " is still alive" );
        }

        Assert.assertFalse( "server is still open", server.getStatus().isOpen() );
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
