package com.github.zalbia.zhhs

import com.github.zalbia.zhhs.domain.ContactService
import com.github.zalbia.zhhs.templates.*
import zio.*
import zio.http.*
import zio.http.Middleware.*

import scala.util.Try

object Router {

  private val corsConfig = CorsConfig(allowedOrigin = _ => Some(Header.AccessControlAllowOrigin.All))

  private val defaultPage = 1

  private val staticPath = Middleware.serveResources(Path.empty / "static")

  val routes: HttpApp[ContactService] =
    Routes(
      Method.GET / ""         ->
        Response.redirect(URL.root / "contacts").toHandler,
      Method.GET / "contacts" ->
        Handler.fromFunctionZIO { (request: Request) =>
          val query = request.url.queryParams.get("q")
          val page  = request.url.queryParams.get("page").flatMap(p => Try(p.toInt).toOption)
          ZIO
            .serviceWithZIO[ContactService](_.search(query, page.getOrElse(defaultPage)))
            .map(contacts => Response.html(Index(query, contacts)))
        },
    ).toHttpApp @@ cors(corsConfig) @@ staticPath
}
