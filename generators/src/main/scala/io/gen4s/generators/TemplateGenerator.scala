package io.gen4s.generators

import cats.*
import cats.syntax.all.*
import io.gen4s.generators.dsl.Dsl
import io.gen4s.generators.dsl.TemplateParser
import io.gen4s.generators.render.Renderrer
import io.gen4s.generators.render.Renderrer.given

trait TemplateGenerator[F[_]]:
  def process(template: String): F[String]

object TemplateGenerator:

  def make[F[_]: MonadThrow]: TemplateGenerator[F] = new:

    def process(template: String): F[String] =
      for {
        tokens       <- implicitly[MonadThrow[F]].catchNonFatal(TemplateParser.process(template))
        randomValues <- tokens.traverse(t => gen(t))
      } yield randomValues.mkString

    private def gen[A <: Dsl](obj: A)(using render: Renderrer[A]): F[String] =
      render.generate(obj).pure[F]
