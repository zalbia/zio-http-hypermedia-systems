package com.github.zalbia.zhhs.web

import com.github.zalbia.zhhs.domain.ContactService
import zio.http.*
import zio.http.Middleware.*

object AppRoutes {

  private val corsConfig = CorsConfig(allowedOrigin = _ => Some(Header.AccessControlAllowOrigin.All))

  private val staticPath = Middleware.serveResources(Path.empty / "static")

  val routes: HttpApp[ContactService] =
    ContactController.contactRoutes.toHttpApp @@ cors(corsConfig) @@ staticPath
}
