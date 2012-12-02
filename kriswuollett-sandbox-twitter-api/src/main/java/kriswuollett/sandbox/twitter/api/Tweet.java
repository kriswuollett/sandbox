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
public interface Tweet extends TwitterStreamObject
{

    public abstract void clear();

    public abstract Tweet clone();

    public abstract void setIdFromString( String id );

    public abstract String getIdAsString();

    public abstract void setCreatedAt( long createdAt );

    public abstract long getCreatedAt();

    public abstract void setTextFromString( String text );

    public abstract String getTextAsString();
    
    public abstract void setFavorited( boolean favorited );
    
    public abstract boolean isFavorited();

    public abstract void setWithheldScope( String withheldScope );

    public abstract String getWithheldScope();

    public abstract void setWithheldInCountries( String withheldInCountries );

    public abstract String getWithheldInCountries();

    public abstract void setWithheldCopyright( boolean withheldCopyright );

    public abstract boolean isWithheldCopyright();

    public abstract void setTruncated( boolean truncated );

    public abstract boolean isTruncated();

    public abstract void setSource( String source );

    public abstract String getSource();

    public abstract void setRetweeted( boolean retweeted );

    public abstract boolean isRetweeted();

    public abstract void setRetweetCount( int retweetCount );

    public abstract int getRetweetCount();

    public abstract void setPossiblySensitive( boolean possiblySensitive );

    public abstract boolean isPossiblySensitive();

    public abstract void setUser( User user );

    public abstract User getUser();
}
