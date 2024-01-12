package com.github.zalbia.zhhs.web.templates

final case class ContactFormData(
  firstname: Option[String],
  lastname: Option[String],
  phone: Option[String],
  email: Option[String],
  errors: Map[String, String] = Map.empty,
)

object ContactFormData {
  val empty: ContactFormData = ContactFormData(None, None, None, None, Map.empty)
}
