package com.github.zalbia.zhhs.web

import com.github.zalbia.zhhs.domain.ContactService
import com.github.zalbia.zhhs.web.templates.{ContactFormData, NewContactTemplate}
import zio.http.*
import zio.http.Middleware.*

object Router {

  private val corsConfig = CorsConfig(allowedOrigin = _ => Some(Header.AccessControlAllowOrigin.All))

  private val staticPath = Middleware.serveResources(Path.empty / "static")

  val routes: HttpApp[ContactService] =
    Routes(
      Method.GET / ""                           -> Response.redirect(URL.root / "contacts").toHandler,
      Method.POST / "contacts" / "new"          -> ContactController.handleNewContactSubmit,
      Method.GET / "contacts" / "new"           -> Handler.html(NewContactTemplate(ContactFormData.empty)),
      Method.GET / "contacts"                   -> ContactController.handleContacts,
      Method.DELETE / "contacts" / string("id") -> ContactController.handleDeleteContact,
    ).toHttpApp @@ cors(corsConfig) @@ staticPath
}
