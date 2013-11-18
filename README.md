# Thundr Proxy

An intercepting reverse proxy module for Thundr.

## Usage

First add Thundr Proxy to your `module.properties`

```ini
com.threewks.thundr.proxy=
```

### Configuring Routes

Thundr Proxy registers a new ActionResolver which means that you register routes you're interested proxying in your 
`routes.json` like so:

```json
{
  "/foo/**": {
    "GET": "proxy",
    "POST": "proxy",
    "PUT": "proxy",
    "DELETE": "proxy"
  }
}
```

Of course you can omit any HTTP verbs that you'd prefer to ignore.

### Adding Rules

Once you've defined a route you'd like to proxy you'll need to define a proxy rule. A rule allows you to define what to
do when your app receives a request for one of the routes you've registered.

Let's say you wanted to reverse proxy requests beginning with the path /foo over to the upstream server 
http://www.example.org/ instead of handling it in your app. Using the included [SimpleProxyRule] 
class in your injection configuration add the following:

```java
SimpleProxyRule rule = new SimpleProxyRule("/foo", "http://www.example.org/");
ProxyActionResolver resolver = injectionContext.get(ProxyActionResolver.class);
resolver.registerProxyRule(rule);
```

Now with this rule registered any requests to /foo will be proxied to the upstream server. So therefore:

`/foo/bar/baz -> http://www.example.org/bar/baz`

### Registering interceptors

Rules also support interceptors. Let's say you want to block all requests to the path /foo/bar/qux and 
return a 403 without hitting the upstream server. 

Expanding on the code in our injection configuration above we can create a new [ProxyInterceptor]
to intercept those calls and modify the response. For this purpose we will extend the [BaseProxyInterceptor]
which provides noop stubs for the ProxyInterceptor interface so we only have to implement the interceptor method we're 
interested in:

```java
ProxyInterceptor interceptor = new BaseProxyInterceptor() {
  /* Here we use the 'before' method which intercepts the request before it has been 
   * proxied to the upstream server. */
  public Response before(Request inboundRequest, Request proxyRequest) {
    if (inboundRequest.path().startsWith("/foo/bar/qux")) {
      return new Response().status(403);  // terminate proxying immediately and return 403 response to user
    }
    return null;  // null response signals that we should continue proxying request
  }
};
rule.addInterceptor(interceptor);
```

#### Interceptor events

The interceptor framework supports 3 events.

* before     - intercepts request before being proxied to the upstream server
* after      - intercepts response from upstream server before it has been proxied back to the requestor
* exception  - intercepts exceptions occurring in any rule or interceptor

### Custom rule classes

If the SimpleProxyRule class just isn't cutting it then creating your own is easy. Just extend [BaseProxyRule] or 
implement the [ProxyRule] interface. 


[ProxyRule]: https://github.com/kuhnza/thundr-proxy/blob/master/src/main/java/com/threewks/thundr/proxy/rule/ProxyRule.java
[BaseProxyRule]: https://github.com/kuhnza/thundr-proxy/blob/master/src/main/java/com/threewks/thundr/proxy/rule/BaseProxyRule.java
[SimpleProxyRule]: https://github.com/kuhnza/thundr-proxy/blob/master/src/main/java/com/threewks/thundr/proxy/rule/SimpleProxyRule.java
[ProxyInterceptor]: https://github.com/kuhnza/thundr-proxy/blob/master/src/main/java/com/threewks/thundr/proxy/intercept/ProxyInterceptor.java
[BaseProxyInterceptor]: https://github.com/kuhnza/thundr-proxy/blob/master/src/main/java/com/threewks/thundr/proxy/intercept/BaseProxyInterceptor.java
