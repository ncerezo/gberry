package com.narcisocerezo.gberry

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Http server core.
 *
 * User: Narciso
 * Date: 21/05/14
 * Time: 10:39
 * @author narciso.cerezo@gmail.com
 * @since 0.1
 */
class HttpServer implements Runnable {

    public static String VERSION = "0.1"

    static Log log = LogFactory.getLog( HttpServer.class )

    public static MIME_TYPES = [
            'html': 'text/html',
            'gsp': 'text/html',
            'htm': 'text/html',
            'txt': 'text/plain',
            'xml': 'text/xml',
            'css': 'text/css',
            'js': 'text/javascript',
            'jpeg': 'image/jpeg',
            'jpg': 'image/jpeg',
            'png': 'image/png',
    ]

    UriResolver     uriResolver
    def             config

    private String[]        args
    private ServerSocket    serverSocket
    private boolean         isStopped    = false
    private Thread          runningThread
    private ExecutorService threadPool
    private SimpleCache     commandCache
    private SimpleCache     pageCache
    private SimpleCache     sessionCache

    @Override
    void run() {
        synchronized( this ) {
            runningThread = Thread.currentThread();
        }
        log.info( "Starting server" )
        configure()
        uriResolver = new UriResolver( server: this )
        threadPool = Executors.newFixedThreadPool( config.maxWorkers ?: 5 )
        log.info( "Starting connection" )
        openServerSocket()
        log.info( "gberry server v${VERSION} started. Listening on port: ${serverSocket.localPort}" )

        while( !isStopped()) {
            try {
                Socket clientSocket = serverSocket.accept()
                threadPool.execute( new HttpProcessor( server: this, socket: clientSocket ) )
            }
            catch( IOException e ) {
                if( isStopped() ) {
                    log.info( "Server Stopped." )
                    return
                }
                throw new RuntimeException( "Error accepting client connection", e )
            }
        }
        threadPool.shutdown()
        log.info( "Server Stopped." )
    }

    def configure() {
        def configFile = null
        if( args.length > 0 ) {
            configFile = new File( args[0] )
        }
        if( !configFile?.exists() ) {
            configFile = new File( new File( System.getProperty( "user.home" ) ), '.gberry/conf.groovy' )
        }

        if( !configFile?.exists() ) {
            configFile = new File( '/etc/gberry/conf.groovy' )
        }

        if( configFile.exists() ) {
            log.info( "Using configuration file: ${configFile.absolutePath}" )
            config = new ConfigSlurper().parse( configFile.toURI().toURL() )
        }
        else {
            log.info( "Using default configuration!" )
            config = [:]
        }

        checkFolder( "documentRoot", '/tmp/gberry' )

        if( config.serverPort == null ||
                !(config.serverPort instanceof Integer) ||
                config.serverPort < 80 ||
                config.serverPort > 65000
        ) {
            config.serverPort = 8100
        }

        config.maxWorkers = checkInteger( config.maxWorkers, 5, 1, 20 )

        if( config.mimeTypes == null ) {
            config.mimeTypes = MIME_TYPES
        }

        checkFolder( "commandRoot", "${config.documentRoot.absolutePath}/_commands" )

        config.commandCacheMaxSize = checkInteger( config.commandCacheMaxSize, 10, 5, 100 )
        config.pageCacheMaxSize = checkInteger( config.pageCacheMaxSize, 10, 5, 100 )
        config.sessionCacheMaxSize = checkInteger( config.sessionCacheMaxSize, 50, 10, 300 )

        commandCache = new SimpleCache( maxElements: config.commandCacheMaxSize )
        pageCache = new SimpleCache( maxElements: config.pageCacheMaxSize )
        sessionCache = new SimpleCache( maxElements: config.sessionCacheMaxSize )
    }

    private File checkFolder( String source, String defaultValue ) {
        def value = config."$source"
        File file = new File( defaultValue )
        if( !value && value != null && !(value instanceof File) ) {
            file = new File( source )
        }
        if( !file.exists() ) {
            file.mkdirs()
        }
        config."$source" = file
        return file
    }

    private static int checkInteger( def source, int defaultValue, int min, int max ) {
        if( source == null || !(source instanceof Integer) ) {
            defaultValue
        }
        else if( source < min ) {
            min
        }
        else if( source > max ) {
            max
        }
        else {
            source
        }
    }

    private synchronized boolean isStopped() {
        return isStopped;
    }

    public synchronized void stop() {
        log.info( "Shutting down server" )
        isStopped = true
        try {
            serverSocket.close()
        }
        catch( IOException e ) {
            e.printStackTrace()
        }
        log.info "Waiting up to 30 seconds for current threads to end"
        threadPool.shutdown()
        threadPool.awaitTermination( 30, TimeUnit.SECONDS )
        log.info "Server stopped"
    }

    private void openServerSocket() {
        try {
            log.info( "Starting server on port ${config.serverPort ?: 8100}" )
            serverSocket = new ServerSocket( config.serverPort ?: 8100 )
        }
        catch( IOException e ) {
            throw new RuntimeException("Cannot open port ${config.serverPort ?: 8100}", e)
        }
    }

    HttpCommand commandForFile( File file ) {
        HttpCommand command = (HttpCommand) commandCache.get( file.absolutePath )
        if( !command ) {
            command = new HttpCommand( file )
            commandCache.add( file.absolutePath, command )
        }
        return command
    }

    PageTemplate pageTemplateForFile( File file ) {
        PageTemplate page = (PageTemplate) pageCache.get( file.absolutePath )
        if( !page ) {
            page = new PageTemplate( file )
            pageCache.add( file.absolutePath, page )
        }
        return page
    }

    HttpSession getSession( String id = null ) {
        HttpSession session = (HttpSession) sessionCache.get( id )
        if( !session ) {
            session = new HttpSession()
            if( id ) {
                session.id = id
            }
            sessionCache.add( session.id, session )
        }
        return session
    }

    String getFileMimeType( File file ) {
        int dot = file.name.lastIndexOf( "." )
        def mimeType = null
        if( dot > -1 ) {
            mimeType = config.mimeTypes[file.name.substring( dot + 1 ).toLowerCase()]
        }
        return mimeType ?: "application/octet-stream"
    }
}
