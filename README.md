gberry
======

Minimalistic http server based on groovy with some grails in it, designed for embedded usage in Raspberry PI or similar environments.
While working on some projects with Raspberry PI I wanted to provide a simple administration interface, and what better than a web app.

Being used to Grails and Groovy, I was really glad when raspbian included an ARM optimized version of Oracle Java by default. That meant that I could use Groovy. But sadly grails is too much for that little computer, being RAM the most limiting resource.

I had previously used Groovy string templates (GStringTemplateEngine) to create gsp-like html files with dynamic content, so I decided to give it a try and create a small web server with Groovy and some Grails inspiration in it.

And so gberry come into existence.

I hope you find it useful.

Check the wiki for detailed information about installing, running, configuring, developing...
