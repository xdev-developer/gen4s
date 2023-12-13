package io.gen4s.outputs.avro

import java.io.{File, FileNotFoundException}

import org.apache.avro.{Schema, SchemaParseException}

import cats.implicits.*
import io.gen4s.core.Domain.Topic

import fs2.kafka.vulcan.SchemaRegistryClient

object SchemaLoader {

  def loadSchemaFromFile(file: File): Either[Exception, Schema] = {
    for {
      f <-
        Either.cond(file.exists(), file, new FileNotFoundException(s"Schema file not found ${file.getAbsolutePath}"))
      s <- Either
             .catchNonFatal(Schema.Parser().parse(f))
             .leftMap(ex => new SchemaParseException(s"Schema parsing error: ${ex.getMessage}"))
    } yield s
  }

  def loadLatestSchemaFromRegistry(
    topic: Topic,
    client: SchemaRegistryClient,
    sType: String): Either[Throwable, Schema] = {
    Either.catchNonFatal[Schema] {
      Schema.Parser().parse(client.getLatestSchemaMetadata(s"${topic.value}-$sType").getSchema)
    }
  }
}
