# Mastodon Module

This module allows the bot to post games to mastodon (including pleroma, which uses a mastodon-compatible client-server API). Fediverse users can subscribe to game notifications via hashtags (as a substitute for channels that would be found in an IM context).

An "official" account for this bot can be found at @theswatbot@sacred.harpy.faith.

## Using the Mastodon instance

The running bot, initially, won't post anything to tell you it's working.
You have to start by mentioning it and including at least one #hashtag, and at least one non-mention, non-tag word. The bot will pick the first of those two and register them. The word should be a regex, as per the usual conventions of this bot. You cannot insert spaces in the regex, that will cause it to be treated as multiple, separate words, you should use \s instead.
If you wish to unregister a tag, provide a hashtag and no accompanying words.
The bot will attempt to register a tag that is already registered; it does not check. As far as I know, this will overwrite the existing tag's regex.

## Configuration

This module has two configuration properties:
`mastodon.instance`, which should be the domain (just the domain, eg. sacred.harpy.faith or mastodon.social) that the bot will log into.
`mastodon.token`, which is a bearer token for the bot; this token is associated with the account, so no account name is required.

## Getting a token

Thankfully, mastodon and pleroma both support oauth, making this a TRIVIAL AND PAINLESS process.
Please ignore the audience's laughter.

The following instructions will all use curl for the examples, with notes of what each aspect of it does; you are free to use whatever HTTP library you prefer.

`$instance` refers to the exact same value in the `mastodon.instance` config property; all calls should be to that instance.

First, register a client:
`curl https://$instance/api/v1/apps --json '{"client_name":"wc3-notification-bot","website":"http://rakka.au","scopes":"read write follow push","redirect_uris":"urn:ietf:wg:oauth:2.0:oob"}'`
`client_name` and `website` can be set to whatever you want, and will only be used in the extremely remote possibility that the administrator of the instance becomes aware of a vulnerability in the client, and purges all tokens using them. For this reason it's recommended to at least use the provided `client_name`.
`scopes` and `redirect_uris` should be exactly as shown here.
curl's `--json` option sets the `content-type` to `application/json` and passes the argument in as the body of the request.

The server will return a json blob from which you should extract the `client_id` and `client_secret`.

Second, do the actual login:
This part is easier to do with a proper web browser, like lynx or ladybird:
Load `https://$instance/oauth/authorize?client_id=$cid&scope=read+write+follow+push&redirect_uri=urn:ietf:wg:oauth:2.0:oob&response_type=code`, where $cid refers to the `client_id` from the previous step, then fill out the html form and submit it, and receive a token.

```
csrf=$(curl -s "https://$instance/oauth/authorize?client_id=$cid&scope=read+write+follow+push&redirect_uri=urn:ietf:wg:oauth:2.0:oob&response_type=code" | grep -Eo '<.*_csrf_token.*>' | grep -Eo 'value=".*"' | grep -Eo '".*"' | grep -Eo '[^"]+')
	toke1=$(curl -s "https://$instance/oauth/authorize" -F _csrf_token=$csrf -F 'authorization[scope][]'= -F 'authorization[scope][]'=read -F 'authorization[scope][]'= -F 'authorization[scope][]'=write -F 'authorization[scope][]'= -F 'authorization[scope][]'=follow -F 'authorization[scope][]'= -F 'authorization[scope][]'=push -F 'authorization[name]'=$user -F 'authorization[password]'=$pass -F 'authorization[client_id]'=$cid -F 'authorization[response_type]'=code -F 'authorization[redirect_uri]'='urn:ietf:wg:oauth:2.0:oob' -F 'authorization[state]'= | grep -Eo 'Token code is <br>.*<' | grep -Eo '>.*<' | grep -Eo '[^><]+')
```
I won't go into `grep`'s functions here. curl's `-s` simply quiets the output, and `-F` simulates form fields (with `content-type: multipart/form-data; boundary=[etc.]`, and method `POST`). The first call is to fetch the csrf that is needed when submitting the form (which, given that it does not use cookies, serves no purpose whatsoever).
Ah, and $user and $pass refer to the username and password of the account you are logging in as.

The token you receive is NOT the token you put in the configuration!
Third, and lastly, trade tokens:
`curl -s https://$instance/oauth/token -F client_id=$cid -F client_secret=$secret -F redirect_uri="urn:ietf:wg:oauth:2.0:oob" -F grant_type=authorization_code -F code=$toke1 -F scope='read write follow push'`
Where `$cid` and `$secret` are from the first step, and `$toke1` is from the second step.
This will produce both a `refresh_token` and an `access_token`, the latter is what you want to put in the configuration file.

## Implementation details

This bot takes advantage of pleroma's plaintext post contents property to more accurately read posts directed at it.
On mastodon, it will fall back to processing the post contents by first deleting all HTML tags. This might cause unexpected behaviour.

WARNING: This has NOT ACTUALLY BEEN TESTED ON MASTODON. It was developed and tested on pleroma.
There is a particular issue with pleroma that backslashes in the regexes have to be doubled before being posted back out (but after being saved to the DB), this may not be necessary on mastodon, and thus it may produce an incorrect response (but still behave correctly).
There may be other mastodon-specific bugs that I have not encountered, please reach out to @Zergling_man@sacred.harpy.faith if you wish to report any.