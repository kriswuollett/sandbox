package kriswuollett.sandbox.twitter.api;

public enum TwitterField
{
<#list twitterFields as field>
    ${field.name?upper_case},
</#list>
    __UNKNOWN__;
    
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
