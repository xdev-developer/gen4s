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