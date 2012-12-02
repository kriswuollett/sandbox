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
import java.nio.channels.ReadableByteChannel;

/**
 * @author kris
 *
 */
public interface ReadableByteChannelSink
{
    /**
     * @param channel
     * @throws IOException
     */
    public void onBytesAvailable( ReadableByteChannel channel ) throws IOException;
    
    public int getBytesCapacity();
}
