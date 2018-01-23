package com.frank.app

import org.scalatra._
import org.scalatra.{ScalatraBase, FutureSupport, ScalatraServlet}

import slick.jdbc.H2Profile.api._
import scala.concurrent._
import scala.concurrent.duration.Duration
// JSON-related libraries
import org.json4s.{DefaultFormats, Formats}
import org.json4s._
import org.json4s.jackson.JsonMethods._

// JSON handling support from Scalatra
import org.scalatra.json._

object Tables {

  // Definition of the USERS table
  class Users(tag: Tag) extends Table[(Int, String, String)](tag, "USERS") {
    def id = column[Int]("USER_ID", O.PrimaryKey) // This is the primary key column
    def name = column[String]("USER_NAME")
    def email = column[String]("EMAIL")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, name, email)
  }

  // Definition of the POSTS table
  class Posts(tag: Tag) extends Table[(String, Int, Int)](tag, "POSTS") {
    def content = column[String]("POST_CONTENT")
    def userID = column[Int]("USER_ID")
    def id = column[Int]("POST_ID", O.PrimaryKey)
    def * = (content, userID, id)

    // A reified foreign key relation that can be navigated to create a join
    def user = foreignKey("PST_FK", userID, users)(_.id)
  }

  // Table query for the USERS table, represents all tuples
  val users = TableQuery[Users]

  def selectUser(id: Rep[Int]) = users.filter(_.id === id)
  def selectPost(id: Rep[Int]) = posts.filter(_.id === id)
  def selectPostByUser(uid: Rep[Int]) = posts.filter(_.userID === uid)

  def insertUser(u: User) =  DBIO.seq(Tables.users += (u.id, u.name, u.email))

  def updateUser(u: User) = users.filter(_.id === u.id).update(u.id, u.name, u.email)

  def deleteUser(id: Rep[Int]) = users.filter(_.id === id).delete

  // Table query for the POSTS table
  val posts = TableQuery[Posts]

  // Query, implicit inner join coffes and users, return their names
  val findPostsWithUser = {
    for {
      p <- posts
      u <- p.user
    } yield (p.id, u.id)
  }

  // DBIO Action which runs several queries inserting sample data
  val insertSupplierAndCoffeeData = DBIO.seq(
    Tables.users += (101, "User A", "a@b.c"),
    Tables.users += (102, "User B", "d@e.f"),
    Tables.users += (103, "User C", "g@h.i"),
    Tables.posts ++= Seq(
      ("New Post", 101, 10),
      ("First Post", 102, 11),
      ("Latest Post", 103, 12),
      ("Positive Post", 101, 13),
      ("Second Post", 102, 15)
    )
  )

  // DBIO Action which creates the schema
  val createSchemaAction = (users.schema ++ posts.schema).create

  // DBIO Action which drops the schema
  val dropSchemaAction = (users.schema ++ posts.schema).drop

  // Create database, composing create schema and insert sample data actions
  val createDatabase = DBIO.seq(createSchemaAction, insertSupplierAndCoffeeData)

}
case class User(id: Int, name: String, email: String)

case class Post(id: Int, userId: Int, content: String)

trait SlickRoutes extends ScalatraBase with FutureSupport {
  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  // Before every action runs, set the content type to be in JSON format.
  before() {
    //contentType = jsonFormats
    db.run(Tables.createDatabase)
    contentType = "application/json"
  }
  def db: Database

  get("/db/create-db") {
    db.run(Tables.createDatabase)

    "created database"
  }

  get("/db/drop-db") {
    db.run(Tables.dropSchemaAction)

    "dropped database"
  }

  get("/posts") {
    //contentType = "text/plain"
    db.run(Tables.posts.result) map { xs =>
      xs map { case (content, uid, id) => Post(id, uid, content)}
    }
  }

  get("/users/:id") {  //  <= this is a route matcher
    // this is an action
    // this action would show the user which has the specified :id

    val id:Int = params.getOrElse("id",halt(404)).toInt
    db.run(Tables.selectUser(id).result) map { xs =>
      xs map { case (id, name, email) => User(id, name, email)
      }

    }
  }

  post("/users") {
    // submit/create an user
    val parsedBody = parse(request.body)
    val user = parsedBody.extract[User]
    db.run(Tables.insertUser(user))
  }

  put("/users/:id") {
    // update the user which has the specified :id
    val parsedBody = parse(request.body)
    val user = parsedBody.extract[User]
    db.run(Tables.updateUser(user))
  }

  delete("/users/:id") {
    // delete the user with the specified :id
    val id:Int = params.getOrElse("id",halt(404)).toInt
    db.run(Tables.deleteUser(id))
  }

  get("/users/:uid/posts/:pid") {  //  <= this is a route matcher
    // this is an action
    // this action would show the user which has the specified :id
    val uid:Int = params.getOrElse("uid",halt(404)).toInt
    //val pid:Int = params.getOrElse("pid",halt(404)).toInt
    db.run(Tables.selectPostByUser(uid).result) map { xs =>
      xs map { case (content, uid, id) => Post(id, uid, content)}
    }
  }

  post("/users/:uid/posts") {
    // submit/create an user
    val parsedBody = parse(request.body)
    parsedBody.extract[Post]
  }

  put("/users/:uid/posts/:pid") {
    // update the user which has the specified :uid
  }

  delete("/users/:uid/posts/:pid") {
    // delete the user with the specified :id
  }

  get("/"){
    contentType = "text/html"
    redirect("/list")
  }
}

class UsersServlet(val db: Database) extends ScalatraServlet with FutureSupport with SlickRoutes with JacksonJsonSupport {

  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global

}
