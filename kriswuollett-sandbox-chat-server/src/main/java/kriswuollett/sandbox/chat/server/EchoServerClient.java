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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
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
    private final ByteBuffer bufIn;
    private final ByteBuffer bufOut;
    
    private State state = State.NA;
    private final String name;
    private int curLimit = 0;
    
    public State getState()
    {
        return state;
    }

    public EchoServerClient( final String name, int inBufferSize, int outBufferSize )
    {
        this.name = "EchoServerClient-" + name;
        log = Logger.getLogger( name );
        bufIn  = ByteBuffer.allocateDirect( inBufferSize );
        bufOut = ByteBuffer.allocateDirect( outBufferSize );
    }
    
    public String getName()
    {
        return name;
    }    

    public int getReadCapacity()
    {
        return bufIn.capacity();
    }
    
    public int getPendingWriteSize()
    {
        return bufOut.position();
    }
    
    /**
     * @param channel
     */
    public void read( ReadableByteChannel channel )
    {
        int bytesRead = 0;
        
        if ( bufIn.remaining() == 0 )
        {
            // If there is still data to be written we are okay for now
            if ( bufOut.position() > 0 )
                return;

            // If we didn't find the end of message yet, we have an error
            // since we didn't detect it earlier.
            if ( curLimit == bufIn.position() )
            {
                log.severe( "No room in bufIn and couldn't process current message" );
                state = State.PENDING_DISCONNECT;
                return;
            }
        }
        else
        {
            try
            {
                bytesRead = channel.read( bufIn );
                
                log.info( String.format( "Read %d bytes.  %d bytes remaining in inBuf", bytesRead, bufIn.remaining() ) );
            }
            catch ( IOException e )
            {
                bytesRead = 0;
                state = State.PENDING_DISCONNECT;
                log.warning( "Failed to read: " + e.getMessage() );
                return;
            }

            if ( bytesRead < 0 )
            {
                log.info( "Disconnecting client since EOS was reached" );
                state = State.PENDING_DISCONNECT;
                return;
            }            
        }
        
        processReadBuffer();
    }

    private void processReadBuffer()
    {
        bufIn.flip();
        
        int dataPos   = bufIn.position();

        final int dataLimit = bufIn.limit();
        
        // Search for EOL character(s)

        log.info( String.format( "Data starts at %d.  Searching for EOL from position %d stopping before %d.", dataPos, curLimit, dataLimit ) );
        
        while ( curLimit < dataLimit )
        {
            // Find next eom byte
            if ( !isEom( bufIn.get( curLimit ) ) )
            {
                do
                {
                    ++curLimit;
                } 
                while ( curLimit < dataLimit
                        && !isEom( bufIn.get( curLimit ) ) );
            }
                    
            if ( curLimit == dataLimit )
                break;
//            {
//                // Did not find eom
//                log.info( String.format( "Did not find eom position %d limit %d capacity %d", bufIn.position(), bufIn.limit(), bufIn.capacity() ) );
//
//                if ( bufIn.limit() == bufIn.capacity() )
//                {
//                    // No more room to read
//                    if ( bufIn.position() > 0 )
//                    {
//                        // We still have capacity to read if we compact
//                        log.info( String.format( "Compacated buffer, new remaining %d", bufIn.remaining() ) );
//                        bufIn.compact();
//                        return;
//                    }
//                    
//                    // In buffer is full and no eom found so we have to disconnect the client
//                    log.info( "Disconnecting client since EOM was not found and bufIn was completely utilized." );
//                    state = State.PENDING_DISCONNECT;
//                    return;
//                }
//            }
            
            // The end of a message was found so process it
            
            bufIn.position( dataPos );
            bufIn.limit( curLimit );
            
            if ( bufIn.remaining() > 0
                    && !onMessage( bufIn ) )
            {
                if ( bufOut.position() == 0 )
                {
                    // Nothing was written.  We didn't have enough room, but the out buffer was empty... that's odd.
                    // We only support messages that can eventually fit entirely in the out buffer
                    state = State.PENDING_DISCONNECT;
                    log.info( "Disconnecting client because onMessage failed and write buffer is empty" );
                    return;
                }
                
                
                // There was not enough room to write this message, so just compact the input buffer if possible
                if ( dataPos > 0 )
                {
                    log.info( "Compacting input buffer because onMessage failed." );
                    curLimit -= dataPos;
                    bufIn.position( dataPos );
                    bufIn.limit( dataLimit );
                    bufIn.compact();
                }
                else
                {
                    bufIn.limit( bufIn.capacity() );
                    bufIn.position( dataLimit );
                }
                return;
            }
            
            // Message was successfully processed (or skipped because it was empty), 
            // so reset the buffer to try processing another message
            bufIn.limit( dataLimit );
            bufIn.position( ++curLimit );
            dataPos = curLimit;
        }
        
        // Did not find eom
        log.info( String.format( "Did not find eom position %d limit %d capacity %d", bufIn.position(), bufIn.limit(), bufIn.capacity() ) );

        if ( dataPos == 0 && dataLimit == bufIn.capacity() )
        {
            // Completely filled read buffer but found no newline
            log.info( "Disconnecting client since EOM was not found and bufIn was completely utilized." );
            state = State.PENDING_DISCONNECT;
            return;            
        }
        
        if ( bufIn.remaining() == 0 )
        {
            // All data from bufIn was used
            curLimit = 0;
            bufIn.clear();
        }
        else if ( dataLimit == bufIn.capacity() )
        {
            // Only compact if there isn't any more room in the in buffer (hopefully reduces copying)
            curLimit -= dataPos;
            bufIn.compact();
        }
    }

    private boolean isEom( byte val )
    {
        log.info( String.format( "isEom %c", ( char ) val ) );
        
        return val == '\n' || val == '\r';
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
     * @throws IOException 
     */
    public void write( WritableByteChannel channel )
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
            return;
        }
        
        log.info( String.format( "Wrote %d bytes.  %d bytes remaining in outBuf", bytesWritten, bufOut.remaining() ) );
        
        if ( bufOut.hasRemaining() )
            bufOut.compact();
        else
            bufOut.clear();
        
        // Process read buffer again just in case we had to pause writing to the out buffer earlier
        processReadBuffer();
    }
    
    // TODO make routine that logs state of buffer
    
    @SuppressWarnings("unused")
    private void printState()
    {
        System.out.printf( "name=%s state=%s\n", name, state.toString() );
        System.out.printf( "  bufIn=%s",  toString( bufIn  ) );
        System.out.printf( "  bufOut=%s", toString( bufOut ) );
    }
    
    private String toString( ByteBuffer buf )
    {
        StringBuilder strBuf = new StringBuilder();
        strBuf.append( String.format( "position=%d limit=%d remaining=%d:\n", buf.position(), buf.limit(), buf.remaining() ) );
        
        for ( int i = buf.position(); i < buf.limit(); ++i )
            strBuf.append( ( char ) buf.get( i ) );
        
        strBuf.append( "\n" );
        return strBuf.toString();
    }
}
