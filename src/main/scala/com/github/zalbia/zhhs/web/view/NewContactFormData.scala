package com.github.zalbia.zhhs.web.view

import com.github.zalbia.zhhs.domain.NewContactDto
import com.github.zalbia.zhhs.web.view.NewContactFormData.ErrorField
import zio.http.Form

final case class NewContactFormData(
  firstname: Option[String],
  lastname: Option[String],
  phone: Option[String],
  email: Option[String],
  errors: Map[ErrorField, String] = Map.empty,
) {
  def addError(errorString: String): NewContactFormData =
    copy(errors = errors + (ErrorField.Email -> errorString))

  def toNewContact: NewContactDto =
    NewContactDto(firstname, lastname, phone, email)
}

object NewContactFormData {
  def fromForm(form: Form): NewContactFormData =
    NewContactFormData(
      firstname = trimEmptyAsNone(form.get("first_name").flatMap(_.stringValue)),
      lastname = trimEmptyAsNone(form.get("last_name").flatMap(_.stringValue)),
      phone = trimEmptyAsNone(form.get("phone").flatMap(_.stringValue)),
      email = trimEmptyAsNone(form.get("email").flatMap(_.stringValue)),
    )

  val empty: NewContactFormData = NewContactFormData(None, None, None, None, Map.empty)

  sealed trait ErrorField
  object ErrorField {
    // We only care about emails for validation
    case object Email extends ErrorField
  }
}
