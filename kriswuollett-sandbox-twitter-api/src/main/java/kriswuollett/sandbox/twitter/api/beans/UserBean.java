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
package kriswuollett.sandbox.twitter.api.beans;

import kriswuollett.sandbox.twitter.api.User;

/**
 * @author kris
 */
public class UserBean implements User
{
    
    private long createdAt;

    private int createdAtOffset;

    private String id;
    
    private String screenName;
    
    @Override
    public long getCreatedAt()
    {
        return createdAt;
    }

    @Override
    public void setCreatedAt( long createdAt )
    {
        this.createdAt = createdAt;
    }

    @Override
    public int getCreatedAtOffset()
    {
        return createdAtOffset;
    }

    @Override
    public void setCreatedAtOffset( int createdAtOffset )
    {
        this.createdAtOffset = createdAtOffset;
    }

    @Override
    public String getIdAsString()
    {
        return id;
    }
    
    @Override
    public void setIdFromString( String id )
    {
        this.id = id;
    }
    
    @Override
    public String getScreenNameAsString()
    {
        return screenName;
    }
    
    @Override
    public void setScreenNameAsString( String screenName )
    {
        this.screenName = screenName;
    }    

    @Override
    public void clear()
    {
        createdAt = 0;
        createdAtOffset = 0;
    }
    
    @Override
    public User clone()
    {
        try
        {
           final User other = ( User ) super.clone();
           
           return other;
        }
        catch ( CloneNotSupportedException e )
        {
            throw new RuntimeException( e );
        }
    } 
}
