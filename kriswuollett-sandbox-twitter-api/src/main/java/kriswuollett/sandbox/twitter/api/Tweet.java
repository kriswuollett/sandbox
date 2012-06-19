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
 * @author kris
 */
public interface Tweet extends TwitterStreamObject
{

    public abstract void clear();

    public abstract Tweet clone();

    public abstract void setIdFromString( String id );

    public abstract String getIdAsString();

    public abstract void setCreatedAtOffset( int createdAtOffset );

    public abstract int getCreatedAtOffset();

    public abstract void setCreatedAt( long createdAt );

    public abstract long getCreatedAt();

    public abstract void setTextFromString( String text );

    public abstract String getTextAsString();

}
