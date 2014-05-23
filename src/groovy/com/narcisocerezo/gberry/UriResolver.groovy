package com.narcisocerezo.gberry

/**
 * Resolves URIs into actual files or commands
 *
 * User: Narciso
 * Date: 21/05/14
 * Time: 13:13
 * @author narciso.cerezo@gmail.com
 * @since 0.1
 */
class UriResolver {

    HttpServer server

    /**
     * Resolve a given URI into an HttpCommand, PageTemplate or File object.<br/>
     * This will first try to map the uri as it comes using the actionRoot to look for a matching command
     * (groovy script).<br/>
     * Then it will try again but adding the .groovy extension.<br/>
     * If that will not work, it will look then for direct files in the documentRoot, using the uri as it comes,
     * then trying with .gsp extension, then with .html, then with .htm.<br/>
     * If there's a match it will return a PageTemplate for files ending in .gsp, or a File object for any other
     * file.<br/>
     * If there isn't a match, it will return null.
     *
     * @param uri uri to resolve
     * @return HttpCommand, PageTemplate or File as appropriate if found, null if no match found.
     */
    def resolve( String uri ) {
        File file = new File( server.config.commandRoot, uri )
        if( file.isDirectory() ) {
            file = new File( file, "index" )
        }
        if( file.exists() ) {
            return server.commandForFile( file )
        }
        else {
            file = new File( file.parentFile, file.name + ".groovy" )
            if( file.exists() ) {
                return server.commandForFile( file )
            }
            else {
                return resolveView( uri )
            }
        }
    }

    /**
     * Resolve a given URI into a PageTemplate or File object.<br/>
     * It will for direct files in the documentRoot, using the uri as it comes,
     * then trying with .gsp extension, then with .html, then with .htm.<br/>
     * If there's a match it will return a PageTemplate for files ending in .gsp, or a File object for any other
     * file.<br/>
     * If there isn't a match, it will return null.
     *
     * @param uri uri to resolve
     * @return PageTemplate or File as appropriate if found, null if no match found.
     */
    def resolveView( String uri ) {
        File file = new File( server.config.documentRoot, uri )
        if( file.isDirectory() ) {
            file = new File( file, "index" )
        }
        if( file.exists() ) {
            if( file.name.endsWith( ".gsp" ) ) {
                return server.pageTemplateForFile( file )
            }
            else {
                return file
            }
        }
        else {
            file = new File( file.parentFile, file.name + ".gsp" )
            if( file.exists() ) {
                return server.pageTemplateForFile( file )
            }
            else {
                file = new File( file.parentFile, file.name + ".html" )
                if( file.exists() ) {
                    return file
                }
                else {
                    file = new File( file.parentFile, file.name + ".htm" )
                    if( file.exists() ) {
                        return file
                    }
                    else {
                        return null
                    }
                }
            }
        }
    }
}
