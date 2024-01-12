package com.github.zalbia.zhhs.web.templates

import zio.http.template.*

object NewContactTemplate {
  def apply(contactFormData: ContactFormData): Html =
    LayoutTemplate.noFlashedMessages(newContactForm(contactFormData) ++ p(a(hrefAttr := "/contacts", "Back")))

  private def newContactForm(contactFormData: ContactFormData): Html =
    form(
      actionAttr := "/contacts/new",
      methodAttr := "post",
      fieldSet(
        legend("Contact Values"),
        div(
          classAttr := List("table", "rows"),
          p(
            label(forAttr     := "email", "Email"),
            input(
              nameAttr        := "email",
              idAttr          := "email",
              typeAttr        := "text",
              placeholderAttr := "Email",
              valueAttr       := contactFormData.email.getOrElse(""),
            ),
            span(classAttr    := List("error"), s"${contactFormData.errors.getOrElse("email", "")}"),
          ),
          p(
            label(forAttr     := "first_name", "First Name"),
            input(
              nameAttr        := "first_name",
              idAttr          := "first_name",
              typeAttr        := "text",
              placeholderAttr := "First Name",
              valueAttr       := contactFormData.firstname.getOrElse(""),
            ),
            span(classAttr    := List("error"), s"${contactFormData.errors.getOrElse("firstname", "")}"),
          ),
          p(
            label(forAttr     := "last_name", "Last Name"),
            input(
              nameAttr        := "last_name",
              idAttr          := "last_name",
              typeAttr        := "text",
              placeholderAttr := "Last Name",
              valueAttr       := contactFormData.lastname.getOrElse(""),
            ),
            span(classAttr    := List("error"), s"${contactFormData.errors.getOrElse("lastname", "")}"),
          ),
          p(
            label(forAttr     := "phone", "Phone"),
            input(
              nameAttr        := "phone",
              idAttr          := "phone",
              typeAttr        := "text",
              placeholderAttr := "Phone",
              valueAttr       := contactFormData.phone.getOrElse(""),
            ),
            span(classAttr    := List("error"), s"${contactFormData.errors.getOrElse("phone", "")}"),
          ),
        ),
        button("Save"),
      ),
    )
}
