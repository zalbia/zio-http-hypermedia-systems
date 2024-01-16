package com.github.zalbia.zhhs.web

import com.github.zalbia.zhhs.Settings
import com.github.zalbia.zhhs.domain.SaveContactError.*
import com.github.zalbia.zhhs.domain.{ContactService, SaveContactError}
import com.github.zalbia.zhhs.web.templates.*
import zio.ZIO
import zio.http.*
import zio.durationInt

private[web] object ContactController {
  val contacts: Handler[ContactService, Nothing, Request, Response] =
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

  val contactsCount: Handler[ContactService, Nothing, Request, Response] =
    Handler.responseZIO {
      ZIO.serviceWithZIO[ContactService](_.count).map(count => Response.text(s"($count total contacts)"))
    }

  val contactDelete: Handler[ContactService, Nothing, (String, Request), Response] =
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

  val contactsDelete: Handler[ContactService, Nothing, Request, Response] =
    Handler.fromFunctionZIO { (request: Request) =>
      for {
        contactService <- ZIO.service[ContactService]
        selectedIds    <- request.body.asURLEncodedForm.orDie
                            .map(_.formData.collect { case FormField.Simple("selected_contact_ids", value) => value })
        contactIds      = selectedIds.map(_.split(',')).flatten.toSet
        _              <- contactService.deleteAll(contactIds)
      } yield Response
        .seeOther(URL.root / "contacts")
        .addCookie(
          Cookie.Response(
            name = "zio-http-flash",
            content = "Deleted Contacts",
            maxAge = Some(5.seconds),
          )
        )
    }

  val contactsNewPost: Handler[ContactService, Nothing, Request, Response] =
    Handler.fromFunctionZIO { (request: Request) =>
      request.body.asURLEncodedForm.foldZIO(
        _ => ZIO.succeed(Response.error(Status.BadRequest, "Contact form data could not be parsed from the request")),
        form => {
          val contactFormData = parseContactFormData(form)
          saveNewContact(form).catchAll {
            case EmailAlreadyExistsError(email)     =>
              val formDataWithError = contactFormData.addError(s"""Email "$email" already exists""")
              ZIO.succeed(Response.html(NewContactTemplate(formDataWithError)))
            case SaveContactError.MissingEmailError =>
              val formDataWithError = contactFormData.addError(s"An email is required")
              ZIO.succeed(Response.html(NewContactTemplate(formDataWithError)))
          }
        },
      )
    }

  private def saveNewContact(form: Form) = {
    val contact = ContactFormData(
      firstname = form.get("first_name").flatMap(_.stringValue),
      lastname = form.get("last_name").flatMap(_.stringValue),
      phone = form.get("phone").flatMap(_.stringValue),
      email = form.get("email").flatMap(_.stringValue),
    )
    ZIO
      .serviceWithZIO[ContactService](_.save(contact))
      .as(
        Response
          .seeOther(URL.root / "contacts")
          .addCookie(
            Cookie.Response(
              name = "zio-http-flash",
              content = "Created New Contact",
              maxAge = Some(Settings.flashMessageMaxAge),
            )
          )
      )
  }

  private def parseContactFormData(form: Form) =
    ContactFormData(
      firstname = form.get("first_name").flatMap(_.stringValue),
      lastname = form.get("last_name").flatMap(_.stringValue),
      phone = form.get("phone").flatMap(_.stringValue),
      email = form.get("email").flatMap(_.stringValue),
    )
}
