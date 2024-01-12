package com.github.zalbia.zhhs.domain

sealed trait SaveContactError extends Product with Serializable

object SaveContactError {
  case object DecodingError                               extends SaveContactError
  final case class EmailAlreadyExistsError(email: String) extends SaveContactError
  case object MissingEmailError                           extends SaveContactError
}
