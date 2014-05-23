package com.narcisocerezo.gberry

/**
 * Very simple LRU cache
 *
 * User: Narciso
 * Date: 21/05/14
 * Time: 15:35
 * @author narciso.cerezo@gmail.com
 * @since 0.1
 */
class SimpleCache {

    int maxElements = 10
    def sorted = []
    def map = [:]

    synchronized void add( key, content ) {
        map[key] = content
        touch( key )
        if( map.size() > maxElements ) {
            def keyToRemove = sorted.last()
            sorted.remove( keyToRemove )
            map.remove( keyToRemove )
        }
    }

    synchronized touch( key ) {
        if( sorted.contains( key ) ) {
            sorted.remove( key )
        }
        sorted.add( 0, key )
    }

    def get( key ) {
        def object = map[key]
        if( object ) {
            touch( key )
        }
        return object
    }
}
