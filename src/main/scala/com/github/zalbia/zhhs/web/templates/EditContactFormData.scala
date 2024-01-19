package com.github.zalbia.zhhs.web.templates

import com.github.zalbia.zhhs.domain.{Contact, UpdateContactDto}
import com.github.zalbia.zhhs.web.templates.EditContactFormData.ErrorField
import zio.http.Form

final case class EditContactFormData(
  id: String,
  firstname: Option[String],
  lastname: Option[String],
  phone: Option[String],
  email: Option[String],
  errors: Map[ErrorField, String] = Map.empty,
) {
  def addError(errorString: String): EditContactFormData =
    copy(errors = errors + (ErrorField.Email -> errorString))

  def toUpdateContactDto: UpdateContactDto =
    UpdateContactDto(
      contactId = id,
      firstname = firstname,
      lastname = lastname,
      phone = phone,
      email = email,
    )
}

object EditContactFormData {
  def fromContact(contact: Contact): EditContactFormData =
    EditContactFormData(
      id = contact.id,
      firstname = contact.firstname,
      lastname = contact.lastname,
      phone = contact.phone,
      email = Some(contact.email),
    )

  def fromForm(contactId: String, form: Form): EditContactFormData = EditContactFormData(
    id = contactId,
    firstname = trimEmptyAsNone(form.get("first_name").flatMap(_.stringValue)),
    lastname = trimEmptyAsNone(form.get("last_name").flatMap(_.stringValue)),
    phone = trimEmptyAsNone(form.get("phone").flatMap(_.stringValue)),
    email = trimEmptyAsNone(form.get("email").flatMap(_.stringValue)),
  )

  sealed trait ErrorField
  object ErrorField {
    // We only care about emails for validation
    case object Email extends ErrorField
  }
}
