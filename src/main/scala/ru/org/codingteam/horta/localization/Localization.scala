package ru.org.codingteam.horta.localization

import java.nio.file.{Files, Paths}

import ru.org.codingteam.horta.configuration.Configuration
import ru.org.codingteam.horta.security.Credential

import scala.collection.JavaConversions._

case class LocaleDefinition(name: String)

object Localization {

  private val locales: Map[LocaleDefinition, LocalizationMap] = loadLocales()

  def localize(key: String)(implicit credential: Credential): String = localize(key, credential.locale)
  def random(key: String)(implicit credential: Credential): String = random(key, credential.locale)

  def localize(key: String, locale: LocaleDefinition): String = {
    withLocale(locale) { case map =>
      map.get(key)
    } getOrElse key
  }

  def random(key: String, locale: LocaleDefinition): String = {
    withLocale(locale) { case map =>
      map.random(key)
    } getOrElse key
  }

  private def loadLocales() = {
    Files.newDirectoryStream(Paths.get(Configuration.localizationPath)).toStream.map { case filePath =>
      val regex = "^(.*)\\.conf$".r
      val fileName = filePath.getFileName.toString
      val localeName = fileName match {
        case regex(name) => name
        case _ => sys.error(s"Invalid path entry $fileName")
      }

      (LocaleDefinition(localeName), new LocalizationMap(localeName))
    }.toMap
  }

  private def withLocale(locale: LocaleDefinition)
                        (action: LocalizationMap => Option[String]): Option[String] = {
    locales.get(locale).flatMap(action)
  }

}