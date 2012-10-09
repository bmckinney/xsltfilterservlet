xsltfilterservlet
=================

ServletFilter Implementation that uses Saxon to transform Servlet Output XML w.r.t. &lt;?xml-stylesheet PI

We use this at BaseX as a drop-in replacement for client-side `&lt;?xml-stylesheet` transformations, as those tend to be slow in Chrome and are limited to XSL 1.0 as well.

Usage
-----

To include the filter, add the following dependency to your `pom.xml`

```xml
 <dependency>
   <groupId>org.basex</groupId>
   <artifactId>XSLTSaxonServletFilter</artifactId>
   <version>0.0.1-SNAPSHOT</version>
 </dependency>
```

In case you do not yet have the BaseX Repository make sure to add the following to `pom.xml`

```xml
<repositories>
…
    <repository>
      <id>basex</id>
      <name>BaseX Maven Repository</name>
      <url>http://files.basex.org/maven</url>
    </repository>
…
```

Now you are ready to add the filter to `WEB-INF/web.xml`:

```xml
<filter>
    <filter-name>XSLTFilter</filter-name>
    <filter-class>org.basex.XSLTSaxonServletFilter.XSLTFilter</filter-class>
    <init-param>
        <!-- set to true to avoid reparsing the same stylesheet over and over 
				again -->
        <param-name>cache</param-name>
        <param-value>true</param-value>
    </init-param>
</filter>
<filter-mapping>
    <filter-name>XSLTFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
````

For Feedback please feel free to use the issue tracker or check out http://basex.org. 

Thanks.

