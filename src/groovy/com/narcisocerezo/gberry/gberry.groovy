package com.narcisocerezo.gberry

/**
 * Main entry point.
 *
 * User: Narciso
 * Date: 21/05/14
 * Time: 10:37
 * @author narciso.cerezo@gmail.com
 * @since 0.1
 */
class gberry implements Runnable {

    HttpServer server

    public gberry( String[] args ) {
        server = new HttpServer( args: args )
    }

    @Override
    void run() {

        addShutdownHook {
            server.stop()
        }

        Thread thread = new Thread( server )
        thread.start()
        thread.join()
    }
}
