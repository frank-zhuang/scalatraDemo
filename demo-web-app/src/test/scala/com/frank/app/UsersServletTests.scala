package com.frank.app

import org.scalatra.test.scalatest._

class UsersServletTests extends ScalatraFunSuite {

  addServlet(classOf[UsersServlet], "/*")

  test("GET / on DemoServlet should return status 200"){
    get("/"){
      status should equal (200)
    }
  }

}
