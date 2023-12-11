package com.github.zalbia.zhhs

import com.github.zalbia.zhhs.templates.*
import zio.http.*
import zio.http.Middleware.*

object Router {

  private val corsConfig = CorsConfig(allowedOrigin = _ => Some(Header.AccessControlAllowOrigin.All))

  private val staticPath = Middleware.serveResources(Path.empty / "static")

  val routes =
    Routes(
      Method.GET / ""         ->
        Handler.response(Response.redirect(URL.root / "contacts")),
      Method.GET / "contacts" ->
        Handler.html(Layout.noFlashedMessages("")),
    ).toHttpApp @@ cors(corsConfig) @@ staticPath
}
