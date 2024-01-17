package com.github.zalbia.zhhs.web

import com.github.zalbia.zhhs.domain.ContactService
import com.github.zalbia.zhhs.web.templates.{ContactFormData, NewContactTemplate}
import zio.http.*
import zio.http.Middleware.*

object AppRoutes {

  private val corsConfig = CorsConfig(allowedOrigin = _ => Some(Header.AccessControlAllowOrigin.All))

  private val staticPath = Middleware.serveResources(Path.empty / "static")

  val routes: HttpApp[ContactService] =
    Routes(
      Method.GET / ""                           -> Response.redirect(URL.root / "contacts").toHandler,
      Method.GET / "contacts"                   -> ContactController.contacts,
      Method.DELETE / "contacts"                -> ContactController.contactsDelete,
      Method.GET / "contacts" / "count"         -> ContactController.contactsCount,
      Method.GET / "contacts" / "new"           -> Handler.html(NewContactTemplate(ContactFormData.empty)),
      Method.POST / "contacts" / "new"          -> ContactController.contactsNewPost,
      Method.GET / "contacts" / string("id")    -> ContactController.contactView,
      Method.DELETE / "contacts" / string("id") -> ContactController.contactDelete,
    ).toHttpApp @@ cors(corsConfig) @@ staticPath
}
