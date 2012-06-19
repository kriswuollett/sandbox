/**
 * 
 */
package kriswuollett.sandbox.twitter.api.beans;

import kriswuollett.sandbox.twitter.api.Tweet;
import kriswuollett.sandbox.twitter.api.TwitterStreamObjectType;
import kriswuollett.sandbox.twitter.api.User;

/**
 *
 */
public class TweetBean implements Tweet
{
    private long createdAt;
    
    private int createdAtOffset;
    
    private String id;

    private User user;
    
    private String text;
    
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
    public String getTextAsString()
    {
        return text;
    }
    
    @Override
    public void setTextFromString( String text )
    {
        this.text = text;
    }    
    
    public User getUser()
    {
        return user;
    }

    public void setUser( User user )
    {
        this.user = user;
    }

    
    
    @Override
    public String toString()
    {
        return "TweetBean [createdAt=" + createdAt + ", createdAtOffset="
                + createdAtOffset + ", id=" + id + ", user=" + user + ", text="
                + text + "]";
    }

    @Override
    public void clear()
    {
        createdAt = 0;
        createdAtOffset = 0;
    }
    
    @Override
    public Tweet clone()
    {
        try
        {
           final TweetBean other = ( TweetBean ) super.clone();
           
           return other;
        }
        catch ( CloneNotSupportedException e )
        {
            throw new RuntimeException( e );
        }
    }
}
