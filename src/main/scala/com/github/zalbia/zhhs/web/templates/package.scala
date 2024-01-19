package com.github.zalbia.zhhs.web

package object templates {
  def trimEmptyAsNone(string: Option[String]): Option[String] =
    string.flatMap(s => if (s.trim.isEmpty) None else Some(s.trim))
}
