package kriswuollett.sandbox.twitter.api;

public enum TwitterField
{
<#list twitterFields as field>
    /** ${field.description} */ 
    ${field.name?upper_case},

</#list>
    /** An unrecognized field  */
    __UNKNOWN__;

    // TODO log warning about first time seeing unrecognized field and use own map to avoid try catch    
    public static TwitterField lookup( final String name )
    {
    	try
    	{
    		return TwitterField.valueOf( name.toUpperCase() );
    	}
    	catch ( Exception e )
    	{
    		return TwitterField.__UNKNOWN__;
    	}
    }
}
