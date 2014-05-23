package com.narcisocerezo.gberry

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory


/**
 * Parses and encapsulates the request sent from the browser,
 * and provides utility methods that can be used from commands and views.
 *
 * User: Narciso
 * Date: 21/05/14
 * Time: 10:54
 * @author narciso.cerezo@gmail.com
 * @since 0.1
 */
class HttpRequest {

    static Log log = LogFactory.getLog( HttpRequest.class )

    public static final String SESSION_COOKIE_NAME = "GSESSIONID"

    def     method
    def     uri
    def     protocolVersion
    def     headers = [:]
    byte[]  body
    boolean validRequest
    def     params = [:]
    def     cookies = [:]

    InetAddress remoteAddress
    InetAddress localAddress
    int         localPort
    int         remotePort
    String      hostName
    String      protocol = "http" // https not currently supported

    private HttpServer server
    private HttpSession session

    protected HttpRequest( HttpServer server, Socket socket ) {
        this.server = server

        remoteAddress = socket.inetAddress
        localAddress = socket.localAddress
        localPort = socket.localPort
        remotePort = socket.port
    }

    HttpSession getSession() {
        if( !session ) {
            HttpCookie sessionCookie = (HttpCookie) cookies[SESSION_COOKIE_NAME]
            if( sessionCookie ) {
                session = server.getSession( sessionCookie.value )
            }
            else {
                session = server.getSession()
            }
        }
        return session
    }

    protected hasSession() {
        return session != null || cookies[SESSION_COOKIE_NAME] != null
    }

    protected void parse( InputStream input ) {

        validRequest = false

        String line = readLine( input )
        if( line ) {
            def pos1 = line.indexOf( ' ' )
            def pos2 = line.lastIndexOf( ' ' )
            if( pos1 > 0 ) {
                // Parse main request line
                method = line.substring( 0, pos1 )
                if( pos2 > pos1 ) {
                    uri = line.substring( pos1 + 1, pos2 ).trim()
                    def questionMarkPosition = uri.indexOf( '?' )
                    if( questionMarkPosition > -1 ) {
                        parseParams( uri.substring( questionMarkPosition + 1 ) )
                        uri = uri.substring( 0, questionMarkPosition )
                    }
                    protocolVersion = line.substring( pos2 ).trim()
                }
                else {
                    uri = line.substring( pos1 + 1 )
                }
                log.info( "method: [$method] , uri: [$uri], protocolVersion: [$protocolVersion]" )
                // Parse headers
                line = readLine( input )
                while( line != null ) {
                    if( line.trim().length() == 0 ) {
                        break
                    }
                    else {
                        def pos = line.indexOf( ':' )
                        if( pos > 0 ) {
                            def headerName = line.substring( 0, pos )
                            def headerValue = line.substring( pos + 1 ).trim()
                            def currentValue = headers[headerName]
                            if( currentValue != null ) {
                                if( currentValue instanceof List ) {
                                    currentValue << headerValue
                                    headers[headerName] = currentValue
                                }
                                else {
                                    headers[headerName] = [ currentValue, headerValue ]
                                }
                            }
                            else {
                                headers[headerName] = headerValue
                            }
                        }
                    }
                    line = readLine( input )
                }
                // Normalize hostName
                hostName = headers['Host'] ?: localAddress.hostName
                if( hostName.indexOf( ':' ) > -1 ) {
                    hostName = hostName.substring( 0, hostName.indexOf( ':' ) )
                }
                // Parse cookies
                if( headers['Cookie'] ) {
                    def cookieHeaders = headers['Cookie']
                    if( !(cookieHeaders instanceof List) ) {
                        cookieHeaders = [ cookieHeaders ]
                    }
                    cookieHeaders.each { cookieHeader ->
                        HttpCookie.parseCookies( cookieHeader )?.each { cookie ->
                            cookies[cookie.name] = cookie
                        }
                    }
                }
                // If there's a body get it
                // ShouldDo: leave input untouched for the commands/views to handle it when contents are not a form
                if( input.available() > 0 ) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream()
                    byte[] buffer = new byte[1024]
                    int count = input.read( buffer )
                    while( count != -1 ) {
                        if( count > 0 ) {
                            baos.write( buffer, 0, count )
                        }
                        count = input.read( buffer )
                    }
                    body = baos.toByteArray()
                    if( method == "POST" && headers["Content-Type"] == 'application/x-www-form-urlencoded' ) {
                        parseParams( new String( body ) )
                        body = null
                    }
                    // ToDo: add support for multipart/form-data
                }
                log.debug( "body size: ${body?.length ?: 0}" )
                validRequest = true
            }
        }
    }

    /**
     * Parse and decode parameters, either from the query string in a GET operation, or from the body of a POST.
     *
     * @param queryString string with the parameters
     * @return map of parameters (name,value). Value can be a list when parameter appears more than once.
     */
    private def parseParams( final String queryString ) {
        queryString.split( "&" ).each { element ->
            def parts = element.split( "=" )
            if( parts.length > 1 ) {
                def name = parts[0]
                def value = URLDecoder.decode( parts[1], "UTF-8" )
                def currentValue = params[name]
                if( currentValue == null ) {
                    params[name] = value
                }
                else if( currentValue instanceof List ) {
                    currentValue << value
                    params[name] = currentValue
                }
                else {
                    params[name] = [ currentValue, value ]
                }
            }
        }
    }

    /**
     * Read a line of ASCII characters (8 bit) from the InputStream. We don't use a reader or any other wrapper
     * around the stream since that could lead to the wrapper reading information from the request body, with no
     * safe way to prevent or recover it.
     *
     * @param input input stream
     * @return a new line (terminated with new line, discarding carriage returns), or null if no more lines available
     */
    private static def readLine( InputStream input ) {
        StringBuilder builder = new StringBuilder()
        int c = input.read()
        while( c != -1 && c != '\n'.charAt( 0 ) ) {
            if( c != '\r'.charAt( 0 ) ) {
                builder.append( (char) c )
            }
            c = input.read()
        }
        return c == -1 && builder.length() == 0 ? null : builder.toString()
    }
}
