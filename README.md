Bouncer
=======

*"Get the hell out of here."*

Bouncer is an authenticating reverse proxy for Wesabe applications with
aspirations to be a web application firewall.

Bouncer accepts Basic HTTP authentication credentials, verifies them, and 
proxies the request to a backend with new, Wesabe credentials:
    
    Authorization: Wesabe ${base64(user_id + ":" + account_key)}

Along the way, it ensures that HTTP requests are well-formed, non-funky, and 
worth being in the same club with.

TODO
----
  
  * audit logging, with detail/log split
  * charset normalization
  * move authentication out to something like Grendel
  * accept OAuth credentials
  * ensure well-formedness of application/json, application/xml, and 
    application/x-www-form-urlencoded entities (maybe using     
    [VTD-XML](http://vtd-xml.sourceforge.net) and 
    [Jackson](http://jackson.codehaus.org/)?)
  * validate content-length, etc.
  * ingress/egress filtering with customizable responses
  * client-timeout tarpit for total losers, but we'd have to see how it behaves
  * IP filtering
  * URI/method blocking
  * on-the-wire entity rewriting (e.g., *** for password)
  * rate limiting based on IP, user, user-agent, OAuth, client cert
  * limit response entity size