# Gen4s

Data generator tool for developers and QA engineers.

## Building


## Running

### Running with profile

You can create env vars profile for each runtime env: dev, staging, prod etc.

**Env vars profile file format**

`dev.profile`:

```properties
KAFKA_BOOTSTRAP_SERVERS=dev.kafka:9095
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
}
```

### Input

- **schema** - path to schema file

- **template** - path to template file.


### Output

#### Stdout output

Console output.

```properties
output {
    writer: {
      type: "std-output"
    }
}
```

## Schema definition and data generators

Big thanks to https://github.com/azakordonets/fabricator  random data generator project!
