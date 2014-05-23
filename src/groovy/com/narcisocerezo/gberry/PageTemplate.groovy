package com.narcisocerezo.gberry

import groovy.text.GStringTemplateEngine
import groovy.text.Template

/**
 * A page template that uses GStringTemplateEngine.
 *
 * User: Narciso
 * Date: 21/05/14
 * Time: 13:18
 * @author narciso.cerezo@gmail.com
 * @since 0.1
 */
class PageTemplate {

    private long        timestamp
    private File        file
    private Template    template

    /**
     * Load, parse and create a template from the given file.
     *
     * @param file template file
     */
    PageTemplate( File file ) {
        this.file = file
        timestamp = file.lastModified()
        template = new GStringTemplateEngine().createTemplate( file.text )
    }

    /**
     * Render the template with the given model, and return the result as a String.
     *
     * @param model model to pass onto the template
     * @return String with template rendered
     */
    String render( model ) {
        checkReload()
        template.make( model ).toString()
    }

    /**
     * Render the template with the given model, and write it to the given output stream.
     *
     * @param model model to pass onto the template
     * @param outputStream output stream to write the result to
     */
    void render( model, OutputStream outputStream ) {
        String string = render( model )
        outputStream.write( string.bytes )
    }

    private checkReload() {
        if( file.lastModified() > timestamp ) {
            timestamp = file.lastModified()
            template = new GStringTemplateEngine().createTemplate( file.text )
        }
    }
}
