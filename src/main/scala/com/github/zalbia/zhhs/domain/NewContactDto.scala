package com.github.zalbia.zhhs.domain

// A DTO representing an attempt to save a new contact
final case class NewContactDto(
  firstname: Option[String],
  lastname: Option[String],
  phone: Option[String],
  email: Option[String],
)
