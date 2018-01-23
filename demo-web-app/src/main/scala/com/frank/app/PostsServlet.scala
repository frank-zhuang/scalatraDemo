package com.frank.app

import org.scalatra.ScalatraServlet

class PostsServlet extends ScalatraServlet{

  get("/users/:uid/posts/:pid") {  //  <= this is a route matcher
    // this is an action
    // this action would show the user which has the specified :id
  }

  post("/users/:uid/posts") {
    // submit/create an user
  }

  put("/users/:uid/posts/:pid") {
    // update the user which has the specified :uid
  }

  delete("/users/:uid/posts/:pid") {
    // delete the user with the specified :id
  }


}
