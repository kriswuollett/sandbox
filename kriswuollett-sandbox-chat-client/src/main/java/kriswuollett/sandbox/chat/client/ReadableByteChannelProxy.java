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
