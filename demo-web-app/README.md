# Demo Web App #
only very basic functions (GET/POST/PUT/DELETE) implemented with Scalatra, Slick and Scala Test.
Did not use Scala Query coz it seems like it's discontinued.
Haven't got a chance to make it Dockerized yet.
Will add Swagger later.

Almost everything is in UsersServlet.scala at the moment. Will move db and trait related stuff to separated files 
## Build & Run ##

```sh
$ cd Demo_Web_App
$ sbt
> jetty:start
> browse
```

If `browse` doesn't launch your browser, manually open [http://localhost:8080/](http://localhost:8080/) in your browser.
