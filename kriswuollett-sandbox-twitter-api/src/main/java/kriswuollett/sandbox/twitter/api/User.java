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
package kriswuollett.sandbox.twitter.api;

/**
 * @author kris
 */
public interface User extends TwitterStreamObject
{

    public abstract void setIdFromString( String id );

    public abstract String getIdAsString();

    public abstract void setScreenName( String screenName );

    public abstract String getScreenName();

	public abstract User clone();

    public abstract void setName( String name );

    public abstract String getName();

    public abstract void setDescription( String description );

    public abstract String getDescription();
}
