package com.github.zalbia.zhhs.web.templates

import com.github.zalbia.zhhs.domain.Contact
import zio.http.template.*
import zio.http.template.Html.fromDomElement

object ShowContactTemplate {
  def apply(contact: Contact): Html =
    LayoutTemplate.noFlashedMessages(
      h1(
        (contact.firstname, contact.lastname) match {
          case (Some(firstname), Some(lastname)) =>
            if (firstname.nonEmpty && lastname.nonEmpty)
              s"$firstname $lastname"
            else if (firstname.nonEmpty && lastname.isEmpty)
              firstname
            else if (firstname.isEmpty && lastname.nonEmpty)
              lastname
            else
              em("Unnamed Contact")

          case (Some(firstname), None) => firstname
          case (None, Some(lastname))  => lastname
          case (None, None)            => em("Unnamed Contact")
        }
      ) ++
        div(
          div(contact.phone match {
            case Some(phone) if phone.nonEmpty => s"Phone: $phone"
            case _                             => span("Phone number: ") ++ em("N/A")
          }),
          div(s"Email: ${contact.email}"),
        ) ++
        p(
          a(hrefAttr := s"/contacts/${contact.id}/edit", "Edit"),
          a(hrefAttr := s"/contacts", "Back"),
        )
    )
}
