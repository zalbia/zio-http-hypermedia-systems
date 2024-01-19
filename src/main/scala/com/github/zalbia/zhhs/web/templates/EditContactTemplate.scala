package com.github.zalbia.zhhs.web.templates

import com.github.zalbia.zhhs.web.templates.EditContactFormData.ErrorField
import com.github.zalbia.zhhs.web.templates.ExtraAttributes.*
import zio.http.template.*

object EditContactTemplate {

  def apply(contact: EditContactFormData): Html =
    LayoutTemplate.noFlashedMessages(editForm(contact) ++ deleteButton(contact.id) ++ backLink)

  private def editForm(contactFormData: EditContactFormData): Html =
    form(
      actionAttr := s"/contacts/${contactFormData.id}/edit",
      methodAttr := "post",
      fieldSet(
        legend("Contact Values"),
        div(
          classAttr := List("table", "rows"),
          p(
            label(forAttr       := "email", "Email"),
            input(
              nameAttr          := "email",
              idAttr            := "email",
              typeAttr          := "email",
              hxAttr("get")     := s"/contacts/${contactFormData.id}/email",
              hxAttr("target")  := "next .error",
              hxAttr("trigger") := "change, keyup delay:200ms",
              placeholderAttr   := "email@example.com",
              valueAttr         := contactFormData.email.getOrElse(""),
            ),
            span(classAttr      := List("error"), s"${contactFormData.errors.getOrElse(ErrorField.Email, "")}"),
          ),
          p(
            label(forAttr     := "first_name", "First Name"),
            input(
              nameAttr        := "first_name",
              idAttr          := "first_name",
              typeAttr        := "text",
              placeholderAttr := "Juan",
              valueAttr       := contactFormData.firstname.getOrElse(""),
            ),
            span(classAttr    := List("error")),
          ),
          p(
            label(forAttr     := "last_name", "Last Name"),
            input(
              nameAttr        := "last_name",
              idAttr          := "last_name",
              typeAttr        := "text",
              placeholderAttr := "de la Cruz",
              valueAttr       := contactFormData.lastname.getOrElse(""),
            ),
            span(classAttr    := List("error")),
          ),
          p(
            label(forAttr     := "phone", "Phone"),
            input(
              nameAttr        := "phone",
              idAttr          := "phone",
              typeAttr        := "text",
              placeholderAttr := "+63 123 456 7890",
              valueAttr       := contactFormData.phone.getOrElse(""),
            ),
            span(classAttr    := List("error")),
          ),
        ),
        button("Save"),
      ),
    )

  private def deleteButton(contactId: String): Html =
    button(
      idAttr("delete-btn"),
      hxAttr("delete")   := s"/contacts/$contactId",
      hxAttr("push-url") := "true",
      hxAttr("confirm")  := "Are you sure you want to delete this contact?",
      hxAttr("target")   := "body",
      "Delete Contact",
    )

  private lazy val backLink: Html =
    p(a(hrefAttr := "/contacts", "Back"))
}
