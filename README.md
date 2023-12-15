# Gen4s

Data generator tool for developers and QA engineers.

[TOC]

## Building

Building standalone application:

```shell
sbt 'universal:packageXzTarball' OR
sbt 'universal:packageBin'
```

## Running

```shell
Gen4s version
Usage: gen4s [preview|run] [options]

  -s, --samples <number>  Samples to generate, default 1
  -c, --config <file>     Configuration file. Default ./config.conf
  -p, --profile <file>    Environment variables profile.
Command: preview
Preview data generation.
Command: run
Run data generation stream.
```

```shell
./bin/gen4s -c ./examples/playground/config.conf -s 5 -p ./profiles/dev.profile
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
./bin/gen4s -c ./examples/playground/config.conf -s 5 -p ./profiles/dev.profile
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

    transformers: ["json-prettify"] 
    validators = ["json", "missing-vars"]
}
```

#### Kafka output

```properties
output {
    writer {
        type = kafka-output

        topic = ${?KAFKA_TOPIC}
        topic = "logs"

        bootstrap-servers = ${?KAFKA_BOOTSTRAP_SERVERS}
        bootstrap-servers = "localhost:9095"

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
    }
    transformers = ["json-minify"] 
    validators = ["json", "missing-vars"]
}
```

- **decode-input-as-key-value**: true/false -  decode input template as key/value json

  ```json
  {
  	"key": ...
    "value": {...}
  }
  ```

#### Kafka AVRO output

```properties
output {
    writer {
        type = kafka-avro-output

        topic = ${?KAFKA_TOPIC}
        topic = "logs-avro"

        bootstrap-servers = ${?KAFKA_BOOTSTRAP_SERVERS}
        bootstrap-servers = "localhost:9095"

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
          key-schema = "/path/to/file/key.avsc"
          value-schema = "/path/to/file/key.avsc"
          auto-register-schemas = false
          registry-client-max-cache-size = 1000
        }
    }
    transformers = ["json-minify"]
    validators = ["json", "missing-vars"]
}
```

- **key-schema** - path to key schema, Optional. 
- **value-schema** - path to value schema, Optional. 
- **auto-register-schemas** - register schemas in schema-registry.

How schema resolver works:

- Read from file.
- When file isn't provided, gen4s lookup schema subject from schema registry (topic_name-key or topic_name-value).



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