package io.gen4s.core

import io.circe.derivation.Configuration

package object generators {
  given Configuration = Configuration.default
}
