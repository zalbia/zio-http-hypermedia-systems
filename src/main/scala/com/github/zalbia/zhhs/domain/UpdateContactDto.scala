package com.github.zalbia.zhhs.domain

final case class UpdateContactDto(
  contactId: String,
  firstname: Option[String],
  lastname: Option[String],
  phone: Option[String],
  email: Option[String],
)
