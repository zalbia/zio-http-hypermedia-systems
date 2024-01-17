package com.github.zalbia.zhhs.web.templates

import com.github.zalbia.zhhs.domain.NewContactDto
import com.github.zalbia.zhhs.web.templates.NewContactFormData.ErrorField

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
  val empty: NewContactFormData = NewContactFormData(None, None, None, None, Map.empty)

  sealed trait ErrorField
  object ErrorField {
    // We only care about emails for validation
    case object Email extends ErrorField
  }
}
