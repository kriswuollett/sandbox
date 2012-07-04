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

import kriswuollett.sandbox.twitter.api.TwitterStreamObjectType;
import kriswuollett.sandbox.twitter.api.User;

/**
 * @author kris
 */
public class UserBean implements User
{

    private String id;
    
    private String screenName;

    private String description;
    
    private String name;
    
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
    public String getScreenName()
    {
        return screenName;
    }
    
    @Override
    public void setScreenName( String screenName )
    {
        this.screenName = screenName;
    }    

    @Override
    public void clear()
    {
        id = null;
        screenName = null;
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

    @Override
    public TwitterStreamObjectType getType()
    {
        return TwitterStreamObjectType.USER;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public void setDescription( String description )
    {
        this.description = description;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setName( String name )
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return "UserBean [id=" + id + ", screenName=" + screenName
                + ", description=" + description + ", name=" + name + "]";
    } 
}
