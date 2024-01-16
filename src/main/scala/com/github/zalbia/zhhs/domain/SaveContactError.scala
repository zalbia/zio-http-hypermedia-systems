package com.github.zalbia.zhhs.domain

sealed trait SaveContactError extends Product with Serializable

object SaveContactError {
  final case class EmailAlreadyExistsError(email: String) extends SaveContactError
  case object MissingEmailError                           extends SaveContactError
}
