package com.github.zalbia.zhhs.web

import com.github.zalbia.zhhs.domain.*
import com.github.zalbia.zhhs.domain.ContactServiceError.*
import com.github.zalbia.zhhs.web.templates.*
import zio.ZIO
import zio.http.*
import zio.http.extensions.*

private[web] object ContactController {
  lazy val contactRoutes: Routes[ContactService, Nothing] = Routes(
    Method.GET / ""                                  ->
      Response.redirect(URL.root / "contacts").toHandler,
    Method.GET / "contacts"                          ->
      Handler.fromFunctionZIO { (request: Request) =>
        val search = request.url.queryParams.get("q")
        val page   = request.url.queryParams.get("page").map(_.toInt).getOrElse(1) // unsafe!
        (search match {
          case None => contactService(_.all(page))
          case _    => contactService(_.search(search, page))
        }).map { contactsFound =>
          if (request.headers.get("HX-Trigger").contains("search"))
            Response.twirl(partials.html.rows(contactsFound))
          else
            Response.twirl(views.html.index(search, contactsFound, request.flashMessage, page = page))
        }
      },
    Method.DELETE / "contacts"                       ->
      Handler.fromFunctionZIO { (request: Request) =>
        for {
          selectedIds <- request.body.asURLEncodedForm.orDie
                           .map(_.formData.collect { case FormField.Simple("selected_contact_ids", value) => value })
          contactIds   = selectedIds.flatMap(_.split(',')).toSet
          _           <- contactService(_.deleteAll(contactIds))
        } yield Response.seeOther(URL.root / "contacts").addExpiringFlashMessage("Deleted Contacts")
      },
    Method.GET / "contacts" / "count"                ->
      Handler.responseZIO(contactService(_.count).map(count => Response.text(s"($count total contacts)"))),
    Method.GET / "contacts" / "new"                  ->
      Handler.response(Response.twirl(views.html.newContact(NewContactFormData.empty))),
    Method.POST / "contacts" / "new"                 ->
      Handler.fromFunctionZIO { (request: Request) =>
        request.body.asURLEncodedForm.foldZIO(
          _ => ZIO.succeed(Response.error(Status.BadRequest, "Contact form data could not be parsed from the request")),
          form => {
            val contactFormData = NewContactFormData.fromForm(form)
            contactService(_.save(contactFormData.toNewContact))
              .as(Response.seeOther(URL.root / "contacts").addExpiringFlashMessage("Created New Contact"))
              .catchAll {
                case EmailAlreadyExistsError(email) =>
                  val formDataWithError = contactFormData.addError(s"""Email "$email" already exists""")
                  ZIO.succeed(Response.twirl(views.html.newContact(formDataWithError)))
                case MissingEmailError              =>
                  val formDataWithError = contactFormData.addError(s"An email is required")
                  ZIO.succeed(Response.twirl(views.html.newContact(formDataWithError)))
              }
          },
        )
      },
    Method.GET / "contacts" / string("id")           ->
      Handler.fromFunctionZIO[(String, Request)] { case (contactId, _) =>
        contactService(_.find(contactId)).map {
          case Some(contact) =>
            Response.twirl(views.html.showContact(contact))
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
                .addExpiringFlashMessage("Deleted Contact")
            else
              Response.text("")
          )
          .catchAll { case ContactIdDoesNotExist(contactId) =>
            ZIO.succeed(Response.error(Status.BadRequest, s"Contact with id '$contactId' doesn't exist'"))
          }
      },
    Method.GET / "contacts" / string("id") / "edit"  ->
      Handler.fromFunctionZIO[(String, Request)] { case (contactId, _) =>
        contactService(_.find(contactId))
          .map {
            case Some(contact) =>
              Response.html(EditContactTemplate(EditContactFormData.fromContact(contact)))
            case None          =>
              Response.notFound(s"Contact with ID '$contactId' not found")
          }
      },
    Method.POST / "contacts" / string("id") / "edit" ->
      Handler.fromFunctionZIO[(String, Request)] { case (contactId, request) =>
        request.body.asURLEncodedForm.foldZIO(
          _ => ZIO.succeed(Response.error(Status.BadRequest, "Contact form data could not be parsed from the request")),
          form => {
            val contactFormData = EditContactFormData.fromForm(contactId, form)
            contactService(_.update(contactFormData.toUpdateContactDto))
              .as(Response.seeOther(URL.root / "contacts" / contactId).addExpiringFlashMessage("Contact Updated"))
              .catchAll {
                case ContactIdDoesNotExist(contactId) =>
                  ZIO.succeed(Response.notFound(s"Contact with id '$contactId' doesn't exist'"))
                case EmailAlreadyExistsError(email)   =>
                  val formDataWithError = contactFormData.addError(s"""Email "$email" already exists""")
                  ZIO.succeed(Response.html(EditContactTemplate(formDataWithError)))
                case MissingEmailError                =>
                  val formDataWithError = contactFormData.addError("An email is required")
                  ZIO.succeed(Response.html(EditContactTemplate(formDataWithError)))
              }
          },
        )
      },
    Method.GET / "contacts" / string("id") / "email" ->
      Handler.fromFunctionZIO[(String, Request)] { case (contactId, request) =>
        val email = trimEmptyAsNone(request.url.queryParams.get("email"))
        contactService(_.validateEmail(contactId, email)).map {
          case Some(ContactIdDoesNotExist(contactId)) =>
            Response.notFound(s"Contact with id '$contactId' doesn't exist'")
          case Some(EmailAlreadyExistsError(email))   =>
            Response.text(s"""Email "$email" already exists""")
          case Some(MissingEmailError)                =>
            Response.text("An email is required")
          case None                                   =>
            Response.text("")
        }
      },
  )

  private lazy val contactService = ZIO.serviceWithZIO[ContactService]
}
