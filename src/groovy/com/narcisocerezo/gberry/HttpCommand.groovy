package com.narcisocerezo.gberry

/**
 * Encapsulate a command, i.e. a groovy script.
 *
 * User: Narciso
 * Date: 21/05/14
 * Time: 12:33
 * @author narciso.cerezo@gmail.com
 * @since 0.1
 */
class HttpCommand {

    def             commandResult
    byte[]          commandOutput

    private ByteArrayOutputStream   output = new ByteArrayOutputStream()
    private Script                  script
    private Binding                 binding
    private long                    timestamp
    private File                    file

    HttpCommand( File file ) {
        this.file = file
        timestamp = file.lastModified()
        binding = new Binding()
        def shell = new GroovyShell( binding )
        shell.setProperty( "out", new PrintStream( output ) )
        script = shell.parse( file )
    }

    def void run( HttpRequest request, HttpResponse response ) {
        // Checking for changes on each run is non-optimal, it should be a separate thread with configurable timing
        checkReload()
        binding.setVariable( "request", request )
        binding.setVariable( "response", response )
        binding.setVariable( "params", request.params )
        output.reset()
        commandResult = script.run()
        commandOutput = output.toByteArray()
    }

    private checkReload() {
        if( file.lastModified() > timestamp ) {
            timestamp = file.lastModified()
            def shell = new GroovyShell( binding )
            shell.setProperty( "out", new PrintStream( output ) )
            script = shell.parse( file )
        }
    }
}
