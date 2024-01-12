package com.github.zalbia.zhhs.web

import com.github.zalbia.zhhs.domain.SaveContactError.*
import com.github.zalbia.zhhs.domain.{ContactService, SaveContactError}
import com.github.zalbia.zhhs.web.templates.{ContactFormData, IndexTemplate}
import zio.ZIO
import zio.http.*
import zio.durationInt

import scala.util.Try

private[web] object ContactController {

  private val defaultPage = 1

  val handleContacts: Handler[ContactService, Nothing, Request, Response] =
    Handler.fromFunctionZIO { (request: Request) =>
      val query = request.url.queryParams.get("q")
      val page  = request.url.queryParams.get("page").flatMap(p => Try(p.toInt).toOption)
      ZIO
        .serviceWithZIO[ContactService](_.search(query, page.getOrElse(defaultPage)))
        .map(contacts => Response.html(IndexTemplate(query, contacts, request.flashMessage)))
    }

  val handleDeleteContact: Handler[ContactService, Nothing, (String, Request), Response] =
    Handler.fromFunctionZIO { case (contactId, request) =>
      ZIO
        .serviceWithZIO[ContactService](_.delete(contactId))
        .as {
          if (request.headers.get("HX-Trigger").contains("delete-btn"))
            Response(status = Status.SeeOther, headers = Headers(Header.Location(URL.root / "contacts")))
              .addCookie(
                Cookie.Response(
                  name = "zio-http-flash",
                  content = "Deleted Contact!",
                  maxAge = Some(5.seconds),
                )
              )
          else
            Response.text("")
        }
        .catchAll(e =>
          ZIO.succeed(
            Response.error(
              Status.BadRequest,
              s"Contact with id '${e.contactId}' doesn't exist'",
            )
          )
        )
    }

  val handleNewContactSubmit: Handler[ContactService, Nothing, Request, Response] =
    Handler.fromFunctionZIO { (request: Request) =>
      saveNewContact(request).debug("save new contact").catchAll {
        case SaveContactError.DecodingError     =>
          ZIO.succeed(Response.error(Status.BadRequest, "New contact form contact form could be parsed from the request"))
        case EmailAlreadyExistsError(email)     =>
          ZIO.succeed(Response.error(Status.BadRequest, s"Email '$email' already exists."))
        case SaveContactError.MissingEmailError =>
          ZIO.succeed(Response.error(Status.BadRequest, "An email is required for adding contacts."))
      }
    }

  private def saveNewContact(request: Request) =
    for {
      form   <- request.body.asURLEncodedForm.orElseFail(DecodingError)
      contact = ContactFormData(
                  firstname = form.get("first_name").flatMap(_.stringValue),
                  lastname = form.get("last_name").flatMap(_.stringValue),
                  phone = form.get("phone").flatMap(_.stringValue),
                  email = form.get("email").flatMap(_.stringValue),
                )
      _      <- ZIO.serviceWithZIO[ContactService](_.save(contact))
    } yield Response
      .seeOther(URL.root / "contacts")
      .addCookie(
        Cookie.Response(
          name = "zio-http-flash",
          content = "Created New Contact",
          maxAge = Some(30.seconds),
        )
      )
}
