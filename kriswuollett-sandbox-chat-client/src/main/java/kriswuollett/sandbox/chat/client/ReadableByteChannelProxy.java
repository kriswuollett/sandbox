/*
 * Copyright (c) 2012, Kristopher Wuollett
 * All rights reserved.
 *
 * This file is part of kriswuollett/chat-client.
 *
 * kriswuollett/twitter-api is free software: you can redistribute it and/or modify
 * it under the terms of the BSD 3-Clause License as written in the COPYING
 * file.
 */
package kriswuollett.sandbox.chat.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * @author kris
 */
public class ReadableByteChannelProxy implements ReadableByteChannel
{
    private final ReadableByteChannel channel;

    private int lastRead;

    public ReadableByteChannelProxy( final ReadableByteChannel channel )
    {
        this.channel = channel;
    }
    
    public int read( ByteBuffer dst ) throws IOException
    {
        return lastRead = channel.read( dst );
    }

    public boolean isOpen()
    {
        return channel.isOpen();
    }

    public void close() throws IOException
    {
        channel.close();
    }

    public int getLastRead()
    {
        return lastRead;
    }
}
