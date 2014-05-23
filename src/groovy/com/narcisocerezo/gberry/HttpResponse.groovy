package com.narcisocerezo.gberry

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * Encapsulates the response to be sent to the browser.
 *
 * User: Narciso
 * Date: 21/05/14
 * Time: 10:59
 * @author narciso.cerezo@gmail.com
 * @since 0.1
 */
class HttpResponse {

    static Log log = LogFactory.getLog( HttpResponse.class )

    HttpRequest     request
    HttpServer      server
    def             headers = [:]
    def             cookies = [:]
    def             content
    String          contentType
    int             responseCode = 200
    String          responseMessage = "OK"
    def             view
    ByteArrayOutputStream   output = new ByteArrayOutputStream()

    private boolean         headersSent
    private OutputStream    outputStream

    HttpResponse( OutputStream outputStream ) {
        this.outputStream = outputStream
    }

    /**
     * Add a cookie to the response.
     *
     * @param name cookie name
     * @param value cookie value
     * @param maxAgeInSeconds max age in seconds (optional, default null)
     * @param path cookie path (optional, default null)
     * @param domain cookie domain (optional, default null)
     * @return the HttpCookie object that holds the cookie
     */
    HttpCookie addCookie( name, value, maxAgeInSeconds = null, path = null, domain = null ) {
        HttpCookie cookie = new HttpCookie(
                name: name,
                value: value,
                maxAgeInSeconds: maxAgeInSeconds,
                path: path,
                domain: domain
        )
        cookies[cookie.name] = cookie
        return cookie
    }

    /**
     * Execute the request and sent its contents to the browser.
     */
    protected void send() {
        if( content ) {
            if( content instanceof HttpCommand ) {
                HttpCommand command = (HttpCommand) content
                command.run( request, this )
                def _view = null
                if( view ) {
                    _view = server.uriResolver.resolveView( view )
                }
                if( !_view ) {
                    _view = server.uriResolver.resolveView( request.uri )
                }
                if( _view ) {
                    if( _view instanceof PageTemplate ) {
                        sendPageTemplate( _view, command )
                    }
                    else {
                        sendFile( _view )
                    }
                }
                else if( command.commandOutput ) {
                    writeHeaders()
                    fixContentHeaders( command.commandOutput.length, "text/html" )
                    endHeaders()
                    outputStream.write( command.commandOutput )
                }
                else {
                    sendEmptyResponse()
                }
            }
            else if( content instanceof PageTemplate ) {
                sendPageTemplate( content )
            }
            else if( content instanceof File ) {
                sendFile( content )
            }
            else if( content instanceof byte[] ) {
                byte[] bytes = (byte[]) content
                writeHeaders()
                fixContentHeaders( bytes.length, contentType ?: "application/octet-stream" )
                endHeaders()
                outputStream.write( bytes )
            }
            else {
                String string = content.toString()
                writeHeaders()
                fixContentHeaders( string.length(), contentType ?: "text/html" )
                endHeaders()
                outputStream.write( string.bytes )
            }
        }
        else {
            sendEmptyResponse()
        }
    }

    /**
     * Write HTTP response start and HTTP headers to the browser.
     */
    private synchronized void writeHeaders() {
        if( !headersSent ) {
            outputStream.write( "HTTP/1.1 $responseCode $responseMessage\n".getBytes() )
            headers.each { name, value ->
                outputStream.write( "$name: $value\n".getBytes() )
            }
            if( request.hasSession() ) {
                HttpCookie sessionCookie = new HttpCookie(
                        name: HttpRequest.SESSION_COOKIE_NAME,
                        value: request.getSession().id,
                        maxAgeInSeconds: server.config.cookieMaxAgeInSeconds ?: (30 * 60)
                )
                cookies[sessionCookie.name] = sessionCookie
            }
            if( cookies.size() > 0 ) {
                outputStream.write( "Set-Cookie: ${cookies.values().join( "," )}\n".getBytes() )
            }
        }
    }

    /**
     * Send a new blank line, marking the end of headers.
     */
    private synchronized void endHeaders() {
        if( !headersSent ) {
            outputStream.write( "\n".getBytes() )
            headersSent = true
        }
    }

    /**
     * Send Content-Length and Content-Type headers if they have not been already defined.
     *
     * @param contentLength content lenght
     * @param contentType content type (optional, default null means do nothing)
     */
    private void fixContentHeaders( long contentLength, String contentType = null ) {
        if( !headersSent ) {
            if( !headers['Content-Length'] ) {
                outputStream.write( "Content-Length: ${contentLength}\n".getBytes() )
            }
            if( !headers['Content-Type'] ) {
                if( contentType ) {
                    outputStream.write( "Content-Type: ${contentType}\n".getBytes() )
                }
                else {
                    outputStream.write( "Content-Type: application/octet-stream\n".getBytes() )
                }
            }
        }
    }

    /**
     * Send an empty but valid response
     */
    private void sendEmptyResponse() {
        writeHeaders()
        endHeaders()
    }

    /**
     * Send a file contents, untouched. Mime type is guessed from the extension using the configured mimeType list.
     *
     * @param file file to send
     */
    private void sendFile( File file ) {
        writeHeaders()
        fixContentHeaders( file.length(), server.getFileMimeType( file ) )
        endHeaders()
        file.withInputStream { input ->
            byte[] buffer = new byte[1024]
            int count = input.read( buffer )
            while( count != -1 ) {
                if( count > 0 ) {
                    outputStream.write( buffer, 0, count )
                }
                count = input.read( buffer )
            }
        }
    }

    /**
     * Render a page template, and send the results to the browser.
     *
     * @param template template to render
     * @param command optional command that might have been executed before (and thus provides information)
     */
    private void sendPageTemplate( PageTemplate template, HttpCommand command = null ) {
        def model = [
                request: request,
                response: this
        ]
        if( command ) {
            if( command.commandResult instanceof Map ) {
                model.putAll( command.commandResult )
            }
            else if( command.commandResult ) {
                model.commandResult = command.commandResult
            }
            if( command.commandOutput ) {
                model.commandOutput = command.commandOutput
            }
        }
        template.render( model, output )
        writeHeaders()
        byte[] raw = output.toByteArray()
        fixContentHeaders( raw.length, "text/html" )
        endHeaders()
        outputStream.write( raw )
    }

}
