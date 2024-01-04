package com.github.zalbia.zhhs

import com.github.zalbia.zhhs.domain.ContactService
import com.github.zalbia.zhhs.web.templates.IndexTemplate
import zio.*
import zio.http.*
import zio.http.Middleware.*

import scala.util.Try

object Router {

  private val corsConfig = CorsConfig(allowedOrigin = _ => Some(Header.AccessControlAllowOrigin.All))

  private val staticPath = Middleware.serveResources(Path.empty / "static")

  val routes: HttpApp[ContactService] =
    Routes(
      Method.GET / ""         ->
        Response.redirect(URL.root / "contacts").toHandler,
      Method.GET / "contacts" ->
        Handler.fromFunctionZIO { (request: Request) =>
          (
            request.url.queryParams.get("q"),
            request.url.queryParams.get("page").flatMap(p => Try(p.toInt).toOption),
          ) match {
            case (Some(query), Some(page)) =>
              ZIO
                .serviceWithZIO[ContactService](_.search(Some(query), page))
                .map(contacts => Response.html(IndexTemplate(Some(query), contacts)))
            case (Some(query), None)       =>
              ZIO
                .serviceWithZIO[ContactService](_.search(Some(query), 1))
                .map(contacts => Response.html(IndexTemplate(Some(query), contacts)))
            case (None, Some(page))        =>
              ZIO
                .serviceWithZIO[ContactService](_.search(Some(""), page))
                .map(contacts => Response.html(IndexTemplate(None, contacts)))
            case (None, None)              =>
              ZIO
                .serviceWithZIO[ContactService](_.search(Some(""), 1))
                .map(contacts => Response.html(IndexTemplate(None, contacts)))
          }
        },
    ).toHttpApp @@ cors(corsConfig) @@ staticPath
}
