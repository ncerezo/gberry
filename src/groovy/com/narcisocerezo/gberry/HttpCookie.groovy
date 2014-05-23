package com.narcisocerezo.gberry

/**
 * An HTTP Cookie, as per rfc2109.
 *
 * User: Narciso
 * Date: 21/05/14
 * Time: 18:27
 * @author narciso.cerezo@gmail.com
 * @since 0.1
 */
class HttpCookie {

    String  name
    String  value
    String  comment
    String  domain
    Long    maxAgeInSeconds
    String  path
    boolean secure

    static parseCookies( String string ) {
        def cookies = []
        HttpCookie cookie = null
        string.split( ",|;" ).each { pairString ->
            def parts = pairString.split( "=" )
            def key = parts[0].trim()
            def value = parts[1].trim()
            switch( key.toLowerCase() ) {

                case "\$version":
                    break

                case "path":
                    if( cookie ) {
                        cookie.path = value
                    }
                    break

                case "domain":
                    if( cookie ) {
                        cookie.domain = value
                    }
                    break

                default:
                    if( cookie ) {
                        cookies << cookie
                    }
                    cookie = new HttpCookie( name: key, value: value )
                    break
            }
        }
        if( cookie ) {
            cookies << cookie
        }
        return cookies
    }

    @Override
    String toString() {
        StringBuilder builder = new StringBuilder()
        builder.append( "$name=$value" )
        if( comment ) {
            builder.append( ";Comment=$comment" )
        }
        if( domain ) {
            builder.append( ";Domain=" )
            if( !domain.startsWith( "." ) ) {
                builder.append( "." )
            }
            builder.append( domain )
        }
        if( maxAgeInSeconds != null && maxAgeInSeconds >= 0 ) {
            builder.append( ";Max-Age=$maxAgeInSeconds" )
        }
        if( path ) {
            builder.append( ";Path=$path" )
        }
        if( secure ) {
            builder.append( ";Secure" )
        }
        builder.append( ";Version=1" )
        return builder.toString()
    }
}
