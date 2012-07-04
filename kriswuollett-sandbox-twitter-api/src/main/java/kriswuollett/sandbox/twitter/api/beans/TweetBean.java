/**
 * 
 */
package kriswuollett.sandbox.twitter.api.beans;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import kriswuollett.sandbox.twitter.api.Tweet;
import kriswuollett.sandbox.twitter.api.TwitterStreamObjectType;
import kriswuollett.sandbox.twitter.api.User;

/**
 *
 */
public class TweetBean implements Tweet
{
    private long createdAt;
    
    private String id;

    private User user;
    
    private String text;

    private boolean favorited;
    
    private boolean possiblySensitive;
    
    private int retweetCount;
    
    private boolean retweeted;
    
    private String source;
    
    private boolean truncated;
    
    private boolean withheldCopyright;
    
    private String withheldInCountries;
    
    private String withheldScope;
    
    @Override
    public TwitterStreamObjectType getType()
    {
        return TwitterStreamObjectType.TWEET;
    }

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
    public String getTextAsString()
    {
        return text;
    }
    
    @Override
    public void setTextFromString( String text )
    {
        this.text = text;
    }    
    
    @Override
    public User getUser()
    {
        return user;
    }

    @Override
    public void setUser( User user )
    {
        this.user = user;
    }

    @Override
    public void setFavorited( boolean favorited )
    {
        this.favorited = favorited;
    }

    @Override
    public boolean isFavorited()
    {
        return favorited;
    }

    @Override
    public boolean isPossiblySensitive()
    {
        return possiblySensitive;
    }

    @Override
    public void setPossiblySensitive( boolean possiblySensitive )
    {
        this.possiblySensitive = possiblySensitive;
    }

    @Override
    public int getRetweetCount()
    {
        return retweetCount;
    }

    @Override
    public void setRetweetCount( int retweetCount )
    {
        this.retweetCount = retweetCount;
    }

    @Override
    public boolean isRetweeted()
    {
        return retweeted;
    }

    @Override
    public void setRetweeted( boolean retweeted )
    {
        this.retweeted = retweeted;
    }

    @Override
    public String getSource()
    {
        return source;
    }

    @Override
    public void setSource( String source )
    {
        this.source = source;
    }

    @Override
    public boolean isTruncated()
    {
        return truncated;
    }

    @Override
    public void setTruncated( boolean truncated )
    {
        this.truncated = truncated;
    }

    @Override
    public boolean isWithheldCopyright()
    {
        return withheldCopyright;
    }

    @Override
    public void setWithheldCopyright( boolean withheldCopyright )
    {
        this.withheldCopyright = withheldCopyright;
    }

    @Override
    public String getWithheldInCountries()
    {
        return withheldInCountries;
    }

    @Override
    public void setWithheldInCountries( String withheldInCountries )
    {
        this.withheldInCountries = withheldInCountries;
    }

    @Override
    public String getWithheldScope()
    {
        return withheldScope;
    }

    @Override
    public void setWithheldScope( String withheldScope )
    {
        this.withheldScope = withheldScope;
    }
    
    @Override
    public void clear()
    {
        // Clear only object references
        createdAt = 0;
        id = null;
        user.clear();
        user = null;
        text = null;
        favorited = false;
        possiblySensitive = false;
        retweetCount = 0;
        retweeted = false;
        source = null;
        truncated = false;
        withheldCopyright = false;
        withheldInCountries = null;
        withheldScope = null;
    }
    
    @Override
    public Tweet clone()
    {
        try
        {
           final TweetBean other = ( TweetBean ) super.clone();
           
           other.user = other.user.clone();
           
           return other;
        }
        catch ( CloneNotSupportedException e )
        {
            throw new RuntimeException( e );
        }
    }

    private String formatMillis( final long theTime )
    {
        final DateFormat dateFormat = new SimpleDateFormat( "EEE MMM d HH:mm:ss Z yyyy" );
        
        dateFormat.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
        
        final GregorianCalendar cal = new GregorianCalendar();
        
        cal.setTimeInMillis( theTime );
        
        final String date = dateFormat.format( cal.getTime() );
        return date;
    }
    
    @Override
    public String toString()
    {
        return "TweetBean [createdAt=" + formatMillis( createdAt ) + ", id=" + id
                + ", text=" + text + ", favorited=" + favorited
                + ", possiblySensitive=" + possiblySensitive
                + ", retweetCount=" + retweetCount + ", retweeted=" + retweeted
                + ", source=" + source + ", truncated=" + truncated
                + ", withheldCopyright=" + withheldCopyright
                + ", withheldInCountries=" + withheldInCountries
                + ", withheldScope=" + withheldScope
                + ", user=" + user + "]";
    }    
}
