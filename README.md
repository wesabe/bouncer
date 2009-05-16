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

Configuration
-------------

Bouncer needs two pieces of information to run:

* a config file
* a port

You run bouncer something like this:
    
    `java -jar bouncer.jar /etc/bouncer.properties 8080`

The config file should look like this:
    
    # If true, an unhandled exceptions will return a debug message. Otherwise,
    # an exception report will be emailed to eng@wesabe.com
    bouncer.debug-errors=true
    
    # The backend, as a URI.
    bouncer.backend.uri=http://0.0.0.0:8081
    
    # The Basic Auth realm.
    bouncer.auth.realm=Wesabe API
    
    # The JDBC class, URI, username, and password.
    bouncer.jdbc.driver=com.mysql.jdbc.Driver
    bouncer.jdbc.uri=jdbc:mysql://localhost/pfc_development
    bouncer.jdbc.username=pfc
    bouncer.jdbc.password=blah
    
    # If true, gzip content encoding is enabled.
    bouncer.http.compression.enable=true
    
    # A comma-separated list of mime types which can be compressed.
    bouncer.http.compression.mime-types=application/xml,application/json
    
    # The minimum response entity size to compress, in bytes.
    bouncer.http.compression.minimum-size=100
    
    # A comma-separated list of memcache servers, with ports.
    bouncer.memcached.servers=memcache1:11211,memcache2:11212

    # anything prefixed with c3p0 is sent directly to the c3p0 data source
    c3p0.maxIdleTime=1800

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