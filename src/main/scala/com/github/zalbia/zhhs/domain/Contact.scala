package com.github.zalbia.zhhs.domain

final case class Contact(
  id: String,
  firstname: Option[String],
  lastname: Option[String],
  phone: Option[String],
  email: String,
)
