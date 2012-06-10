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
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

/**
 * TODO Compact only when necessary to avoid extra copies
 */
public class EchoServerClient
{
    public enum State
    {
        NA, PENDING_DISCONNECT
    }
    
    private final Logger     log;
    private final ByteBuffer bufIn  = ByteBuffer.allocateDirect( 1024 );
    private final ByteBuffer bufOut = ByteBuffer.allocateDirect( 1024 );
    
    private State state = State.NA;
    private int checkedPos = 0;
    private final String name;
    
    public State getState()
    {
        return state;
    }

    public EchoServerClient( final String name )
    {
        this.name = "EchoServerClient-" + name;
        log = Logger.getLogger( name );
    }
    
    public String getName()
    {
        return name;
    }    

    
    /**
     * @param channel
     * @return true if there still is data that needs to be written to the client
     */
    public boolean read( SocketChannel channel )
    {
        int bytesRead;
        
        try
        {
            if ( bufIn.remaining() == 0 )
            {
                state = State.PENDING_DISCONNECT;
                log.severe( "Client should have already been disconnected?  No more room to read" );
                return false;
            }
            
            bytesRead = channel.read( bufIn );
            
            log.info( String.format( "Read %d bytes.  %d bytes remaining in inBuf", bytesRead, bufIn.remaining() ) );
        }
        catch ( IOException e )
        {
            bytesRead = 0;
            state = State.PENDING_DISCONNECT;
            log.warning( "Failed to read: " + e.getMessage() );
            return false;
        }
        
        if ( bytesRead > 0 )
        {
            bufIn.flip();
            
            final int newLimit = bufIn.limit();
            
            // Search for EOL character(s)

            log.info( String.format( "Searching for EOL from position %d through %d", checkedPos, newLimit - 1 ) );
            
            while ( checkedPos < bufIn.limit() )
            {
                byte b = bufIn.get( checkedPos++ );
                
                if ( b == '\n' || b == '\r' )
                {
                    log.info( String.format( "Found EOL at position %d", checkedPos - 1 ) );
                    
                    bufIn.limit( checkedPos - 1 );
                    
                    // If we can't process the message now then quit processing for now
                    
                    if ( !onMessage( bufIn ) )
                    {
                        //  TODO Create timer to check for clients that stop reading instead of dropping them here
                        state = State.PENDING_DISCONNECT;
                        log.info( "Disconnecting client because write buffer is full" );
                        return false;
                    }
                
                    bufIn.limit( newLimit );
                    bufIn.position( checkedPos );
                    
                    // Strip EOL character(s) and empty lines
                    
                    int stripCount = 0;
                    
                    while ( bufIn.hasRemaining()
                            && ( b = bufIn.get( bufIn.position() ) ) == '\n' || b == '\r' )
                    {
                        ++stripCount;
                        bufIn.get();
                    }
                    
                    if ( stripCount > 0 )
                    {
                        checkedPos = bufIn.position();
                        
                        log.info( String.format( "Stripped %d extra EOL bytes", stripCount ) );
                    }
                }
            }
            
            // Create more room for reading and preserve the length for which we already checked for no EOL character(s)
            
            if ( bufIn.hasRemaining() )
            {
                checkedPos = checkedPos - bufIn.position();
                bufIn.compact();
            }
            else
            {
                bufIn.clear();
                checkedPos = 0;
            }
        }
        else if ( bytesRead < 0 )
        {
            log.info( "Disconnecting client since EOS was reached" );
            state = State.PENDING_DISCONNECT;
            return false;
        }
        
        return bufOut.position() > 0;
    }

    /**
     * @param buf
     * @return true if message was processed
     */
    private boolean onMessage( ByteBuffer buf )
    {
        log.info( String.format( "Processing message at position %d with length %d.  Remaining bufOut is %d.", buf.position(), buf.remaining(), bufOut.remaining() ) );

        if ( bufOut.remaining() > buf.remaining() )
        {
            bufOut.put( buf );
            bufOut.put( ( byte ) '\n' );
            return true;
        }
        
        return false;
    }
    
    /**
     * @param channel
     * @return true if there still is data that needs to be written to the client
     * @throws IOException 
     */
    public boolean write( SocketChannel channel )
    {
        bufOut.flip();
        
        final int bytesWritten;

        try
        {
            bytesWritten = channel.write( bufOut );
        }
        catch ( IOException e )
        {
            log.info( String.format( "Failed to write: %s", e.getMessage() ) );
            state = State.PENDING_DISCONNECT;
            return false;
        }
        
        log.info( String.format( "Wrote %d bytes.  %d bytes remaining in outBuf", bytesWritten, bufOut.remaining() ) );
        
        if ( bufOut.hasRemaining() )
        {
            bufOut.compact();
            return true;
        }
        
        bufOut.clear();
        return false;
    }
}
