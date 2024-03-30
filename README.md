# Gen4s - data generator tool for developers and QA engineers.

[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)
[![Coverage Status](https://coveralls.io/repos/github/xdev-developer/gen4s/badge.svg?branch=main)](https://coveralls.io/github/xdev-developer/gen4s?branch=main)

Gen4s is a powerful data generation tool designed for developers and QA engineers. 

Features:

- **Data Generation**: Gen4s allows users to generate up-to-date data and publish it to their systems. This is particularly useful for testing and development purposes.

-  **Maintain Test Data**: Gen4s enables users to maintain test data in the file system or repository, ensuring that the data is always accessible and up-to-date.

-  **Data Sharing**: With Gen4s, users can easily share test data with their team, improving collaboration and efficiency.

-  **Support for Different Profiles**: Gen4s supports different profiles such as dev, local, QA, etc. This allows users to switch between different environments as needed.

-  **Generation Scenarios**: Gen4s supports running generation scenarios. These can be used to publish data, wait, and then publish another portion of data, simulating event time processing.

-  **Load Testing**: Gen4s is capable of load testing your system by publishing millions of messages. This can help identify potential performance issues.

-  **Semi-Generation of Data**: Gen4s supports semi-generation of data, where users can generate a CSV file from their database and use it as part of the data generation schema.

-  **Command Line Execution**: Gen4s can be executed directly from the command line, providing a simple and efficient way to generate data.

-  **Environment Variables Profile Loading**: Gen4s allows loading environment variables from a file and applying them, which can be useful for managing different runtime environments.

-  **Support for Multiple Output Formats**: Gen4s supports various output formats including stdout, Kafka, Avro, Protobuf, file system, and HTTP.

-  **Schema Definition and Data Generators**: Gen4s provides a variety of data generators for different data types and structures, including static values, timestamps, numbers, strings, UUIDs, IP addresses, and more.

-  **Scenario Configuration**: Gen4s allows for the configuration of multiple stages in a scenario, with configurable delays between stages and the number of samples to generate.

## Running

Download latest release from [Releases page](https://github.com/xdev-developer/gen4s/releases), unzip archive and execute `./bin/gen4s`

### Using Homebrew
To install Gen4s using Homebrew, you first need to tap into the `xdev-developer/tap` repository. Tapping a repository in Homebrew adds it to the list of formulae that Homebrew tracks, updates, and installs from. Once the repository is tapped, you can install Gen4s. Here are the steps:

1. Open your terminal.
2. Tap into the `xdev-developer/tap` repository by running the command: `brew tap xdev-developer/tap`.
3. Once the repository is tapped, install Gen4s by running the command: `brew install gen4s`.

Please note that Homebrew is a package manager for macOS. If you're using a different operating system, you might need to use a different package manager or install method.

```shell
Gen4s
Usage: gen4s [preview|run|scenario] [options]

  -c, --config <file>      Configuration file. Default ./config.conf
  -p, --profile <file>     Environment variables profile.
  -i, --input-records key=value,key1=value1
                           Key/Value pairs to override generated variable

Command: preview [options]
Preview data generation.
  --pretty                 pretty print
  -s, --samples <number>   Samples to generate, default 1

Command: run [options]
Run data generation stream.
  -s, --samples <number>   Samples to generate, default 1

Command: scenario
Run scenario
  --help                   prints usage info
```

```shell
./bin/gen4s run -c ./examples/playground/config.conf
```


### Running with profile

You can create env vars profile for each runtime env: dev, staging, prod etc.

**Env vars profile file format**

`dev.profile`:

```properties
KAFKA_BOOTSTRAP_SERVERS=dev.kafka:9095
ORG_ID=12345
```

```shell
./bin/gen4s run -c ./examples/playground/config.conf -s 5 -p ./profiles/dev.profile
```

### Running with value override

```shell
./bin/gen4s run -i test-string=hello,test-int=12345 -c ./examples/playground/config.conf
```

### Runninng scenario

```shell
./bin/gen4s scenario -c ./examples/scenario/scenario.conf -p ./profiles/dev.profile
```

## Building from source

Building standalone application:

```shell
sbt 'universal:packageXzTarball' OR
sbt 'universal:packageBin'
```

Building docker image

```shell
sbt 'universal:packageXzTarball'
cd app
docker build -t xdev.developer/gen4s:<version> .
```

Test docker image
```shell
docker run xdev.developer/gen4s:<version> bin/gen4s preview --pretty -c examples/playground/config.conf -s 5
```

## Testing

Benchmarking
```shell
sbt clean "project benchmarks;jmh:run -i 3 -wi 3 -f3 -t1"
```

## Configuration

```properties
input {
    schema = "<path-to>/examples/sample-schema.json"
    template = "<path-to>/examples/sample.template"
}


output {
    writer: {
      type: "std-output"
    }

    transformers: ["json-prettify"]
}
```

### Input

- **schema** - path to schema file

- **template** - path to template file.

- **decode-new-line-as-template** - treat each line in template file as standalone template.

- **csv-records** - csv records input file.

- **global-variables** - list of global variables. Global variable will be generated once per run.

#### CSV Records streaming

Using csv-records streaming you can generate templates using info from csv file with combination of random generators, see `examples/csv-input`.


### Output

#### Stdout output

Console output.

```properties
output {
    writer: {
      type: "std-output"
    }

    transformers = ["json-prettify"] 
    validators = ["json", "missing-vars"]
}
```

#### Kafka output

```properties
output {
    writer {
        type = kafka-output

        topic = "logs"
        topic = ${?KAFKA_TOPIC}

        bootstrap-servers = "localhost:9092"
        bootstrap-servers = ${?KAFKA_BOOTSTRAP_SERVERS}

        batch-size = 1000
                
        headers {
            key = value
        }

        decode-input-as-key-value = true
        
        producer-config {
          compression-type = none # snappy, gzip, lz4
          in-flight-requests =  5
          linger-ms = 15
          max-batch-size-bytes = 1024
          max-request-size-bytes = 512
        }
    }
    transformers = ["json-minify"] 
    validators = ["json", "missing-vars"]
}
```

- **decode-input-as-key-value**: true/false -  decode input template as key/value json.

  key will be produced as 'kafka message key' and value as 'kafka message value'.
  
  ```json
  {
    "key": 1,
    "value": { "id": 1, "timestamp": {{ts}}, "event": "Logged in" }
  }
  ```

#### Kafka AVRO output

```properties
output {
    writer {
        type = kafka-avro-output

        topic = "logs-avro"
        topic = ${?KAFKA_TOPIC}

        bootstrap-servers = "localhost:9092"
        bootstrap-servers = ${?KAFKA_BOOTSTRAP_SERVERS}

        batch-size = 1000
                
        headers {
            key = value
        }

        decode-input-as-key-value = true
        
        producer-config {
          compression-type = gzip
          in-flight-requests =  1
          linger-ms = 15
          max-batch-size-bytes = 1024
          max-request-size-bytes = 512
        }

        avro-config {
          schema-registry-url = "http://localhost:8081"
          schema-registry-url = ${?SCHEMA_REGISTRY_URL}

          key-schema = "/path/to/file/key.avsc"
          value-schema = "/path/to/file/value.avsc"
          auto-register-schemas = false
          registry-client-max-cache-size = 1000
        }
    }
    transformers = []
    validators = ["json", "missing-vars"]
}
```

- **key-schema** - path to key schema, Optional. 
- **value-schema** - path to value schema, Optional. 
- **auto-register-schemas** - register schemas in schema-registry.

How schema resolver works:

- Read from file.
- When file isn't provided, gen4s lookup schema subject from schema registry (topic_name-key or topic_name-value).



#### Kafka Protobuf output

```properties
output {
    writer {
        type = kafka-protobuf-output

        topic = "persons-proto"
        topic = ${?KAFKA_TOPIC}

        bootstrap-servers = "localhost:9092"
        bootstrap-servers = ${?KAFKA_BOOTSTRAP_SERVERS}

        batch-size = 1000

        headers {
            key = value
        }

        decode-input-as-key-value = true

        proto-config {
          schema-registry-url = "http://localhost:8081"
          schema-registry-url = ${?SCHEMA_REGISTRY_URL}
          
          value-descriptor {
            file = "./examples/kafka-protobuf/person-value.desc"
            message-type = "Person"
          }
          auto-register-schemas = true
          registry-client-max-cache-size = 1000
        }
    }

    transformers = []
    validators = ["json", "missing-vars"]
}
```

- **value-descriptor** - path to protobuf descriptor and message type. 
- **auto-register-schemas** - register schemas in schema-registry.

#### Create protobuf descriptor from proto file

Descriptor file can be created using `protoc` command:
```shell
protoc --include_imports --descriptor_set_out=person-value.desc person-value.proto
```

or using `scalapbc`

```shell
scalapbc --include_imports --descriptor_set_out=person-value.desc person-value.proto
```

#### File System output

```properties
output {
    writer {
        type = fs-output
        dir = "/tmp"
        filename-pattern = "my-cool-logs-%s.txt"
    }
    transformers = ["json-prettify"]
    validators = ["json", "missing-vars"]
}
```

#### Http output

```properties
output {
  writer {
    type = http-output
    url = "http://example.com"
    url = ${?REQUEST_URL}

    method = POST
    headers {
        key = value
    }
    parallelism = 3
    content-type = "application/json"
    stop-on-error = true
  }
  transformers = ["json-minify"]
  validators = ["json", "missing-vars"]
}
```


#### Transformers

**json-minify**  - transform generated JSON to _compact_ printed JSON - (removes all new lines and spaces). 

**json-prettify**  - transform generated JSON to _pretty_ printed JSON.


### Scenario configuration

Using scenario you can run multiple stages, configure `delay` between stages and number of samples to generate.

```properties
stages: [
    { name: "Playground", samples: 5, config-file: "./examples/playground/config.conf", delay: 5 seconds},
    { name: "CSV Input",  samples: 3, config-file: "./examples/csv-input/config.conf"}
]
```



## Schema definition and data generators

### Static value generator

This sampler can be used like template constant (static value).

```json
{ "variable": "id", "type": "static", "value": "id-12332221"}
```



### Timestamp generator

```json
{ "variable": "ts", "type": "timestamp", "unit": "sec"}
```

**unit** - timestamp unit, possible values: ms, ns, micros, sec. Default value - ms.

**shiftDays** - shift timestamp to **n** or **-n** days. Optional.

**shiftHours** - shift timestamp to **n** or **-n** hours. Optional.

**shiftMinutes** - shift timestamp to **n** or **-n** minutes. Optional.

**shiftSeconds** - shift timestamp to **n** or **-n** seconds. Optional.

**shiftMillis** - shift timestamp to **n** or **-n** milliseconds. Optional.



### Int number generator.

```json
{ "variable": "my-int", "type": "int", "min": 10, "max": 1000 }
```



### Double number generator.

```json
{ "variable": "test-double", "type": "double", "min": 10.5, "max": 15.5, "scale": 6 }
```



### Boolean generator.

```json
{ "variable": "test-bool", "type": "boolean"}
```



### String generator.

```json
{ "variable": "test-string", "type": "string", "len": 10}
```



### String pattern generator.

```json
{ "variable": "test-string-pattern", "type": "pattern", "pattern": "hello-???-###"} // hello-abc-123
```



### Java UUID field generator.

```json
{ "variable": "test-uuid", "type": "uuid" }
```



### Ip address generator

```json
{ "variable": "test-ip", "type": "ip", "ipv6": false }
```



### Enumeration generator.

```json
{ "variable": "test-enum", "type": "enum", "oneOf": ["hello", "world"] }
```



### Env var generator.

```json
{ "variable": "test-var", "type": "env-var", "name": "ORG_ID" }
```

**Supported env vars:**

```scala
    List(
      "CUSTOMER_ID",
      "USER_ID",
      "USERNAME",
      "ORG_ID",
      "EVENT_ID",
      "user.name",
      "os.name"
    )
```

OR any env var with `G4S_` prefix, for example `G4S_QA_USERNAME`



### DateTime generator

```json
{ "variable": "test-date", "type": "date", "format": "MM/dd/yyyy", "shiftDays": -10 }
```

**format** - date format.

**shiftDays** - shift timestamp to **n** or **-n** days. Optional.

**shiftHours** - shift timestamp to **n** or **-n** hours. Optional.

**shiftMinutes** - shift timestamp to **n** or **-n** minutes. Optional.

**shiftSeconds** - shift timestamp to **n** or **-n** seconds. Optional.



### List generator.

```json
{ "variable": "test-array", "type": "list", "len": 3, "generator": { "variable": "_", "type": "ip" } }
```

Where
**len** - list size to generate.

**generator** - element generator.



## Template syntax

- \* - generates any symbol
  - \*{2} - generates random symbols with 2 symbols size
  - \*{2, 5} - generates random symbols with random size between 2 and 5
- %w - generates random english word
  - %w{4} - generates random english word with fixed length. Max available length is 31
  - %w{2, 6} - generates random english word with random length between 2 and 6
- %n{2} - returns defined number
- %n{4, 10} - returns random number between 4 and 10
- \#{4} - returns random HEX number with provided length (4)
- \#{4, 8} - returns random HEX number of random length between 4 and 8
- %ip4, %ip6, %mac - generates random values for IP v4, IP v6 and mac address respectively
- other values are considered as text tokens
