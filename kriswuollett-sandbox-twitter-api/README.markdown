Configuration
=============

Create a secrets.properties file that has the following contents:

	twitter.oauth.consumer.key=xxx
	twitter.oauth.consumer.secret=xxx
	twitter.oauth.token=xxx
	twitter.oauth.secret=xxx
	
Run Streaming Sample
====================

Connect to Twitter Streaming API using your application's credentials filtering on a track parameter:

	java ... kriswuollett.sandbox.twitter.api.TwitterStream <secrets.properties> <track parameter>
	
Use a sample of the Streaming API data:

	java ... kriswuollett.sandbox.twitter.api.TwitterStream <sample.json>
