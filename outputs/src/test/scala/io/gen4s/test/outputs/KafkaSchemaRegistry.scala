package io.gen4s.test.outputs

import org.testcontainers.containers.Network

import com.dimafeng.testcontainers.{GenericContainer, KafkaContainer, SchemaRegistryContainer}

import scala.jdk.CollectionConverters.*

import io.gen4s.core.Domain.BootstrapServers

trait KafkaSchemaRegistry {
  private val brokerId                         = 1
  private val kafkaHost                        = s"kafka_broker_$brokerId"
  private val network: Network                 = Network.newNetwork()
  protected val kafkaContainer: KafkaContainer = KafkaContainer.Def().createContainer()

  protected val schemaRegistryContainer: GenericContainer =
    SchemaRegistryContainer.Def(network, kafkaHost).createContainer()

  kafkaContainer.container
    .withNetwork(network)
    .withNetworkAliases(kafkaHost)
    .withEnv(
      Map[String, String](
        "KAFKA_BROKER_ID"                 -> brokerId.toString,
        "KAFKA_HOST_NAME"                 -> kafkaHost,
        "KAFKA_AUTO_CREATE_TOPICS_ENABLE" -> "true"
      ).asJava
    )

  def bootstrapServers: BootstrapServers = BootstrapServers(kafkaContainer.bootstrapServers)

  def getSchemaRegistryAddress: String =
    s"http://${schemaRegistryContainer.container.getHost}:${schemaRegistryContainer.container.getMappedPort(SchemaRegistryContainer.defaultSchemaPort)}"

}
