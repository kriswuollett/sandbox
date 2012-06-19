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
package kriswuollett.sandbox.twitter.api;

/**
 * Support for receving callbacks with Twitter stream objects.  Ownership of objects remain
 * with the caller.  If you wish to store the objects use the <code>clone</code> method.
 */
public interface TwitterStreamReader
{
    /**
     * @param tweet the new tweet message
     */
    public void onTweet( Tweet tweet );
    
    /**
     * @param delete the delete request that should be honored
     */
    public void onDelete( Delete delete );
    
    /**
     * @param scrubGeo the scrubbing request that should be honored
     */
    public void onScrubGeo( ScrubGeo scrubGeo );
    
    /**
     * @param limit the API limit info that should be recognized
     */
    public void onLimit( Limit limit );
}
