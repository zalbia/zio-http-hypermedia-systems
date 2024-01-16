package com.github.zalbia.zhhs.web

import com.github.zalbia.zhhs.domain.SaveContactError.*
import com.github.zalbia.zhhs.domain.{ContactService, SaveContactError}
import com.github.zalbia.zhhs.web.templates.*
import zio.ZIO
import zio.http.*
import zio.durationInt

private[web] object ContactController {

  

  val handleContacts: Handler[ContactService, Nothing, Request, Response] =
    Handler.fromFunctionZIO { (request: Request) =>
      val search = request.url.queryParams.get("q")
      val page   = request.url.queryParams.get("page").map(_.toInt).getOrElse(1) // unsafe!
      for {
        contactService <- ZIO.service[ContactService]
        contactsFound  <- search match {
                            case None => contactService.all
                            case _    => contactService.search(search, page)
                          }
      } yield {
        if (request.headers.get("HX-Trigger").contains("search"))
          Response.html(RowsTemplate(contactsFound))
        else
          Response.html(IndexTemplate(search, contactsFound, request.flashMessage))
      }
    }

  val handleDeleteContact: Handler[ContactService, Nothing, (String, Request), Response] =
    Handler.fromFunctionZIO { case (contactId, request) =>
      ZIO
        .serviceWithZIO[ContactService](_.delete(contactId))
        .as(
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
        )
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
