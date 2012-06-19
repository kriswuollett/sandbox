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
package kriswuollett.sandbox.twitter.api.streaming.parameters;


/**
 * @author kris
 *
 */
public class Track extends TwitterStreamParameter
{

    private String track;

    public Track( final String track )
    {
        this.track = track;
    }
    
    @Override
    public TwitterStreamParameterType getType()
    {
        return TwitterStreamParameterType.TRACK;
    }

    public String getTrack()
    {
        return track;
    }
}