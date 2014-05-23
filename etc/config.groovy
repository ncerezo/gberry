// **********************************************************************
// Basic settings
// **********************************************************************

// Server port to listen for connections
// default 8100
serverPort = 80

// Maximum number of worker threads
// default 5, min 1, max 20
maxWorkers = 10

// **********************************************************************
// Folders
// **********************************************************************

// Where to find static content and views
// default /tmp/gberry
documentRoot = '/usr/share/gberry/documentRoot'

// Where to find commands
// default ${docmentRoot}/_commands
commandRoot = '/usr/share/gberry/commandRoot'

// **********************************************************************
// Caching
// **********************************************************************

// How many commands to keep in memory precompiled
// default 10, min 5, max 1000
commandCacheMaxSize = 30

// How many page templates to keep in memory precompiled
// default 10, min 5, max 1000
commandCacheMaxSize = 30

// How many sessions to keep (in memory)
// default 50, min 10, max 300
commandCacheMaxSize = 30

// **********************************************************************
// Mime types
// **********************************************************************

mimeTypes = [
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
        'xls': 'application/vnd-ms-excel',
]
