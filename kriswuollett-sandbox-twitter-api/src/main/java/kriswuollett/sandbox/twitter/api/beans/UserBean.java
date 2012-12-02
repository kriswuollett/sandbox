/*
 * Copyright (c) 2012, Kristopher Wuollett
 * All rights reserved.
 *
 * This file is part of kriswuollett/twitter-api.
 *
 * kriswuollett/twitter-api is free software: you can redistribute it and/or modify
 * it under the terms of the BSD 3-Clause License as written in the COPYING
 * file.
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
