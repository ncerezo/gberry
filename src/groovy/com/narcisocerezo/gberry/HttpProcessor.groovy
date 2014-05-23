package com.narcisocerezo.gberry

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * Processes a client request.
 *
 * User: Narciso
 * Date: 21/05/14
 * Time: 11:04
 * @author narciso.cerezo@gmail.com
 * @since 0.1
 */
class HttpProcessor implements Runnable {

    static Log log = LogFactory.getLog( HttpProcessor.class )

    HttpServer  server
    Socket      socket

    @Override
    void run() {
        long start = System.currentTimeMillis()
        log.debug( "$this : Processing socket $socket (${socket.closed})" )
        if( !socket.closed ) {
            socket.withStreams { inputStream, outputStream ->
                HttpRequest request = new HttpRequest( server, socket )
                request.parse( inputStream )
                HttpResponse response = new HttpResponse( outputStream )
                response.server = server
                response.request = request
                if( request.validRequest ) {
                    if( request.method == "GET" || request.method == "POST" ) {
                        def target = server.uriResolver.resolve( request.uri )
                        if( target ) {
                            response.content = target
                        }
                        else {
                            response.responseCode = 404
                            response.responseMessage = "Not found"
                            response.content = error(404)
                            response.contentType = "text/html"
                        }
                    }
                    else {
                        response.content = error(501)
                        response.responseCode = 501
                        response.responseMessage = "Not implemented"
                        response.contentType = "text/html"
                    }
                }
                else {
                    response.responseCode = 400
                    response.responseMessage = "Invalid request"
                    response.content = error(400)
                    response.contentType = "text/html"
                }
                response.send()
            }
        }
        long elapsed = System.currentTimeMillis() - start
        log.info( "Response handled in $elapsed ms." )
    }

    static String error( int code ) {
        // ToDo: improve error reporting
        """<html>
<head>
    <title>Error $code</title>
</head>
<body>
    Error $code
</body>
</html>"""
    }

}
