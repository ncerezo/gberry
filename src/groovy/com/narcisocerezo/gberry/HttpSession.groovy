package com.narcisocerezo.gberry

/**
 * Holds the http session.
 *
 * User: Narciso
 * Date: 21/05/14
 * Time: 10:58
 * @author narciso.cerezo@gmail.com
 * @since 0.1
 */
class HttpSession extends HashMap {

    HttpSession() {
        this.id = UUID.randomUUID().toString()
    }

}
