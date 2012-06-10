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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 *
 */
public class EchoServer implements Runnable
{
    private Logger log = Logger.getLogger( EchoServer.class.getName() ); 
    
    private final Status status = new Status();
    
    private ServerSocketChannel serverChannel;
    
    private InetSocketAddress serverAddress;
    
    private Selector selector;
    
    private volatile boolean done = false;
    
    private int port = -1;
    
    private long selectPass = 0;
    
    public EchoServer()
    {
        
    }

    public class Status
    {
        private volatile boolean open = false;
        
        public boolean isOpen()
        {
            return open;
        }
    }

    public Status getStatus()
    {
        return status;
    }
    
    public int getPort()
    {
        return port;
    }
    
    @Override
    public void run()
    {
        openSelector();        
        
        openServerSocket();
        
        // TODO change this so users of this class can't block open / close
        synchronized ( status )
        {
            status.open = true;
            status.notifyAll();
        }
        
        while ( !done )
        {
            final int numKeys;
            
            try
            {
                if ( ( numKeys = selector.select( 100 ) ) == 0 )
                    continue;
            }
            catch ( IOException e )
            {
                throw new RuntimeException( "Failed to select", e );
            }
            
            selectPass++;
            
            log.info( String.format( "Select pass %d numKeys %d", selectPass, numKeys ) );
            
            final Iterator<SelectionKey> keysReady = selector.selectedKeys().iterator();
            
            while ( keysReady.hasNext() )
            {
                final SelectionKey key = keysReady.next();
                
                if ( key.isAcceptable() )
                    accept( key );
                
                if ( key.isValid() && key.isReadable() )
                    read( key );
                
                if ( key.isValid() && key.isWritable() )
                    write( key );
                
                keysReady.remove();
            }
        }
        
        synchronized ( status )
        {
            status.open = false;
            status.notifyAll();
        }        
    }

    public void close()
    {
        done = true;
    }
    
    private void accept( SelectionKey key )
    {
        final ServerSocketChannel serverSocketChannel = ( ServerSocketChannel ) key.channel();

        final SocketChannel socketChannel;

        try
        {
            if ( ( socketChannel = serverSocketChannel.accept() ) == null )
            {
                log.info( "Got accept client notification but no client could be accepted" );             
                return;
            }
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Failed to accept client connection", e );
        }

        try
        {
            socketChannel.configureBlocking( false );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Failed to configure blocking on client socket channel", e );
        }
        
        try
        {
            final Socket           socket     = socketChannel.socket();
            final String           clientName = String.format( "%s:%d", socket.getInetAddress().getHostAddress(), socket.getPort() );
            final EchoServerClient client     = new EchoServerClient( clientName );
            
            socketChannel.register( selector, SelectionKey.OP_READ, client );
            
            log.info( String.format( "Accepted client %s", client.getName() ) );
            
        }
        catch ( ClosedChannelException e )
        {
            throw new RuntimeException( "Failed to register read interest for client socket channel", e );
        }
    }

    private void read( SelectionKey key )
    {
        final EchoServerClient client        = ( EchoServerClient ) key.attachment();
        final SocketChannel    socketChannel = ( SocketChannel    ) key.channel();
        final int              interestOps   = key.interestOps();
        
        if ( client.read( socketChannel ) 
                && ( interestOps & SelectionKey.OP_WRITE ) == 0 )
        {
            log.info( String.format( "Registering for write on client %s", client.getName() ) );
            
            key.interestOps( interestOps | SelectionKey.OP_WRITE );
        }
        
        checkPostConditions( client, socketChannel );
    }

    private void write( SelectionKey key )
    {
        final EchoServerClient client = ( EchoServerClient ) key.attachment();
        final SocketChannel    socketChannel = ( SocketChannel    ) key.channel();
        final int              interestOps   = key.interestOps();
        
        if ( !client.write( socketChannel ) )
        {
            log.info( String.format( "Unregistering from write on client %s", client.getName() ) );
            
            key.interestOps( interestOps & ~SelectionKey.OP_WRITE );
        }

        checkPostConditions( client, socketChannel );
    }

    private void checkPostConditions( EchoServerClient client,  SocketChannel socketChannel )
    {
        switch ( client.getState() )
        {
            case PENDING_DISCONNECT:
            {
                log.info( String.format( "Disconnecting client %s due to PENDING_DISCONNECT", client.toString() ) );

                try
                {
                    socketChannel.close();
                }
                catch ( IOException e )
                {
                    System.err.println( "Failed to close client socket" );
                    e.printStackTrace( System.err );
                }
            }
        }
    }
    
    private void openSelector()
    {
        // Create selector and register for select
        
        try
        {
            selector = SelectorProvider.provider().openSelector();
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Failed to open selector", e );
        }
    }

    private void openServerSocket()
    {
        // Create socket
        
        try
        {
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking( false );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Failed to set up server channel", e );
        }

        
        // Determine port and address
        
        serverAddress = new InetSocketAddress( 0 );

        
        // Bind to socket
        
        try
        {
            serverChannel.socket().bind( serverAddress );
        }
        catch ( IOException e )
        {
            try { serverChannel.close(); } catch ( IOException e1 ) {}
            
            throw new RuntimeException( "Failed to bind to socket", e );
        }
        
        
        // Create selector and register for select
        
        try
        {
            serverChannel.register( selector, SelectionKey.OP_ACCEPT );
        }
        catch ( IOException e )
        {
            try { serverChannel.close(); } catch ( IOException e1 ) {}

            throw new RuntimeException( "Failed to register accept with selector", e );
        }
        
        port = serverChannel.socket().getLocalPort();
        
        log.info( String.format( "Listening on port %d", port ) );
    }
    
    public static void main( final String[] args )
    {
        final EchoServer server = new EchoServer();
        
        server.run();
    }
}
