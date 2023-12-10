package com.github.zalbia.zhhs

import zio.http.*
import zio.http.Middleware.*

object Router {

  private val corsConfig: CorsConfig = CorsConfig(allowedOrigin = _ => Some(Header.AccessControlAllowOrigin.All))

  val routes: HttpApp[Any] =
    Routes(
      Method.GET / "" -> Handler.text("Hello World!")
    ).toHttpApp @@ cors(corsConfig)
}
