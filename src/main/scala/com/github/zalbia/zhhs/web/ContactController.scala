package com.github.zalbia.zhhs.web

import com.github.zalbia.zhhs.Settings
import com.github.zalbia.zhhs.domain.ContactServiceError.*
import com.github.zalbia.zhhs.domain.*
import com.github.zalbia.zhhs.web.templates.*
import zio.ZIO
import zio.http.*
import zio.durationInt

private[web] object ContactController {
  lazy val contactRoutes: Routes[ContactService, Nothing] = Routes(
    Method.GET / ""                                  ->
      Response.redirect(URL.root / "contacts").toHandler,
    Method.GET / "contacts"                          ->
      Handler.fromFunctionZIO { (request: Request) =>
        val search = request.url.queryParams.get("q")
        val page   = request.url.queryParams.get("page").map(_.toInt).getOrElse(1) // unsafe!
        for {
          contactsFound <- search match {
                             case None => contactService(_.all)
                             case _    => contactService(_.search(search, page))
                           }
        } yield {
          if (request.headers.get("HX-Trigger").contains("search"))
            Response.html(RowsTemplate(contactsFound))
          else
            Response.html(IndexTemplate(search, contactsFound, request.flashMessage))
        }
      },
    Method.DELETE / "contacts"                       ->
      Handler.fromFunctionZIO { (request: Request) =>
        for {
          selectedIds <- request.body.asURLEncodedForm.orDie
                           .map(_.formData.collect { case FormField.Simple("selected_contact_ids", value) => value })
          contactIds   = selectedIds.map(_.split(',')).flatten.toSet
          _           <- contactService(_.deleteAll(contactIds))
        } yield Response
          .seeOther(URL.root / "contacts")
          .addCookie(
            Cookie.Response(
              name = "zio-http-flash",
              content = "Deleted Contacts",
              maxAge = Some(5.seconds),
            )
          )
      },
    Method.GET / "contacts" / "count"                ->
      Handler.responseZIO {
        contactService(_.count).map(count => Response.text(s"($count total contacts)"))
      },
    Method.GET / "contacts" / "new"                  ->
      Handler.html(NewContactTemplate(NewContactFormData.empty)),
    Method.POST / "contacts" / "new"                 ->
      Handler.fromFunctionZIO { (request: Request) =>
        request.body.asURLEncodedForm.foldZIO(
          _ => ZIO.succeed(Response.error(Status.BadRequest, "Contact form data could not be parsed from the request")),
          form => {
            val contactFormData = NewContactFormData(
              firstname = trimEmptyAsNone(form.get("first_name")),
              lastname = trimEmptyAsNone(form.get("last_name")),
              phone = trimEmptyAsNone(form.get("phone")),
              email = trimEmptyAsNone(form.get("email")),
            )
            contactService(_.save(contactFormData.toNewContact))
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
              .catchAll {
                case EmailAlreadyExistsError(email) =>
                  val formDataWithError = contactFormData.addError(s"""Email "$email" already exists""")
                  ZIO.succeed(Response.html(NewContactTemplate(formDataWithError)))
                case MissingEmailError              =>
                  val formDataWithError = contactFormData.addError(s"An email is required")
                  ZIO.succeed(Response.html(NewContactTemplate(formDataWithError)))
              }
          },
        )
      },
    Method.GET / "contacts" / string("id")           ->
      Handler.fromFunctionZIO[(String, Request)] { case (contactId, _) =>
        contactService(_.find(contactId)).map {
          case Some(contact) =>
            Response.html(ShowContactTemplate(contact))
          case None          =>
            Response.notFound(s"Contact with ID '$contactId' not found")
        }
      },
    Method.DELETE / "contacts" / string("id")        ->
      Handler.fromFunctionZIO[(String, Request)] { case (contactId, request) =>
        contactService(_.delete(contactId))
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
          .catchAll { case ContactIdDoesNotExist(contactId) =>
            ZIO.succeed(
              Response.error(
                Status.BadRequest,
                s"Contact with id '$contactId' doesn't exist'",
              )
            )
          }
      },
    Method.GET / "contacts" / string("id") / "edit"  ->
      Handler.fromFunctionZIO[(String, Request)] { case (contactId, _) =>
        contactService(_.find(contactId))
          .map {
            case Some(contact) =>
              Response.html(EditContactTemplate(EditContactFormData.from(contact)))
            case None          =>
              Response.notFound(s"Contact with ID '$contactId' not found")
          }
      },
    Method.POST / "contacts" / string("id") / "edit" ->
      Handler.fromFunctionZIO[(String, Request)] { case (contactId, request) =>
        request.body.asURLEncodedForm.foldZIO(
          _ => ZIO.succeed(Response.error(Status.BadRequest, "Contact form data could not be parsed from the request")),
          form => {
            val contactFormData = EditContactFormData(
              id = contactId,
              firstname = trimEmptyAsNone(form.get("first_name")),
              lastname = trimEmptyAsNone(form.get("last_name")),
              phone = trimEmptyAsNone(form.get("phone")),
              email = trimEmptyAsNone(form.get("email")),
            )
            contactService(_.update(contactFormData.toUpdateContactDto))
              .as(
                Response
                  .seeOther(URL.root / "contacts" / contactId)
                  .addCookie(
                    Cookie.Response(
                      name = "zio-http-flash",
                      content = "Contact Updated",
                      maxAge = Some(Settings.flashMessageMaxAge),
                    )
                  )
              )
              .catchAll {
                case ContactIdDoesNotExist(contactId) =>
                  ZIO.succeed(Response.notFound(s"Contact with id '$contactId' doesn't exist'"))
                case EmailAlreadyExistsError(email)   =>
                  val formDataWithError = contactFormData.addError(s"""Email "$email" already exists""")
                  ZIO.succeed(Response.html(EditContactTemplate(formDataWithError)))
                case MissingEmailError                =>
                  val formDataWithError = contactFormData.addError(s"An email is required")
                  ZIO.succeed(Response.html(EditContactTemplate(formDataWithError)))
              }
          },
        )
      },
  )

  private def trimEmptyAsNone(formField: Option[FormField]) =
    formField.flatMap(_.stringValue.flatMap(s => if (s.trim.isEmpty) None else Some(s.trim)))

  private lazy val contactService = ZIO.serviceWithZIO[ContactService]
}
