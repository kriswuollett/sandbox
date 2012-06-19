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
package kriswuollett.sandbox.twitter.api.streaming;

import java.util.concurrent.Callable;

import kriswuollett.sandbox.twitter.api.TwitterStreamReader;
import kriswuollett.sandbox.twitter.api.streaming.parameters.TwitterStreamParameter;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;


/**
 * @author kris
 */
public interface TwitterStreamingRequest extends Callable<TwitterStreamingResponse>
{
    public abstract TwitterStreamingRequest setOAuthConsumer( final CommonsHttpOAuthConsumer consumer );
    
    public abstract TwitterStreamingRequest setListener( final TwitterStreamReader listener );
    
    public abstract TwitterStreamingRequest setParameters( final TwitterStreamParameter... parameters );
}
