import zio._
import zio.http._

object Main extends ZIOAppDefault {
  val app: HttpApp[Any] =
    Routes(
      Method.GET / "text" -> handler(Response.text("Hello World!"))
    ).toHttpApp

  override val run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] =
    Server.serve(app).provide(Server.default)
}