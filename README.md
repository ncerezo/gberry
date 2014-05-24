gberry
======

Minimalistic http server based on groovy with some grails in it, designed for embedded usage in Raspberry PI or similar environments.

While working on some projects with Raspberry PI I wanted to provide a simple administration interface, and what better than a web app.

Being used to Grails and Groovy, I was really glad when raspbian included an ARM optimized version of Oracle Java by default. That meant that I could use Groovy. But sadly grails is too much for that little computer, being RAM the most limiting resource.

I had previously used Groovy string templates (GStringTemplateEngine) to create gsp-like html files with dynamic content, so I decided to give it a try and create a small web server with Groovy and some Grails inspiration in it.

And so gberry come into existence.

I hope you find it useful.

Check the wiki for detailed information about installing, running, configuring, developing...

### Installing and running

The basic installation is as simple as downloading the zip file and exploding it in your preferred folder, or cloning the repository.

To start gberry just type this from the gberry folder:

`bin/gberry.sh`

It will automatically create the folder for the documentRoot if does not exists (`/tmp/gberry`) and a default index.gsp file.

So just point your web browser to `http://localhost:8100`, you should see something like:

> It works!

> gberry server at Fri May 23 10:16:41 CEST 2014
