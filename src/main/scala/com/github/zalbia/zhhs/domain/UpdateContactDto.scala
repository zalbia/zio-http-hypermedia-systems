package com.github.zalbia.zhhs.domain

// A DTO representing an attempt to update an existing contact
final case class UpdateContactDto(
  contactId: String,
  firstname: Option[String],
  lastname: Option[String],
  phone: Option[String],
  email: Option[String],
)
