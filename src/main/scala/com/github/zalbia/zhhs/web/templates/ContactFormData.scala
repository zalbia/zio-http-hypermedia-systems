package com.github.zalbia.zhhs.web.templates

import com.github.zalbia.zhhs.web.templates.ContactFormData.ErrorField

final case class ContactFormData(
  firstname: Option[String],
  lastname: Option[String],
  phone: Option[String],
  email: Option[String],
  errors: Map[ErrorField, String] = Map.empty,
) {
  def addError(errorString: String): ContactFormData =
    copy(errors = errors + (ErrorField.Email -> errorString))
}

object ContactFormData {
  val empty: ContactFormData = ContactFormData(None, None, None, None, Map.empty)

  sealed trait ErrorField
  object ErrorField {
    // We only care about emails for validation
    case object Email extends ErrorField
  }
}
