# AI Assistant Instructions for Gen4s

## Project Overview
Gen4s is a Scala-based data generation tool designed for developers and QA engineers. The tool generates test data according to configurable schemas and publishes it to various output targets including Kafka, filesystem, S3, and HTTP endpoints.

## Key Architecture Components
- **Core Module** (`core/`): Base abstractions and utilities
- **Generators Module** (`generators/`): Data generation logic and types
- **Outputs Module** (`outputs/`): Output adapters (Kafka, File, HTTP, etc.)
- **App Module** (`app/`): CLI interface and orchestration
- **Examples** (`examples/`): Reference configurations for different use cases

## Development Workflow
1. **Build & Test**
   ```shell
   sbt clean test
   ```

2. **Package App**
   ```shell
   sbt 'universal:packageXzTarball'
   # or
   sbt 'universal:packageBin'
   ```

3. **Benchmarking**
   ```shell
   sbt clean "project benchmarks;jmh:run -i 3 -wi 3 -f3 -t1"
   ```

## Critical Files & Patterns
- **Configuration**: `examples/*/config.conf` - Reference configurations showing input/output setup
- **Schema Definition**: `examples/*/input.schema.json` - Data generation schema examples
- **Templates**: `examples/*/input.template.json` - Output templates with variable substitution

## Key Conventions
1. **Generator Configuration**:
   - Each generator must define its schema in JSON format
   - See `examples/playground/input.schema.json` for reference implementation

2. **Output Configuration**:
   - Must specify `type` and format-specific parameters
   - Common pattern in `examples/*/config.conf`
   ```hocon
   output {
     writer {
       type = "[std|kafka|file|http]-output"
       // format-specific config
     }
     transformers = ["json-prettify"] 
     validators = ["json", "missing-vars"]
   }
   ```

3. **Testing & Examples**:
   - Use `preview` command to validate configurations:
   ```shell
   gen4s preview --pretty -c examples/playground/config.conf -s 5
   ```

## Integration Points
1. **Kafka Integration**
   - Supports plain, AVRO, and Protobuf serialization
   - Configure schema registry for AVRO/Protobuf
   - See `examples/kafka-*/config.conf` for setup

2. **File-Based Integration** 
   - CSV record streaming: `examples/csv-input/`
   - File system output: Configured via `file-output` type

## Common Development Tasks
1. **Adding New Generator**
   - Implement generator in `generators/src/main/scala/io/gen4s/generators/`
   - Add schema definition support 
   - Create example in `examples/`

2. **Adding Output Format**
   - Implement writer in `outputs/src/main/scala/io/gen4s/outputs/`
   - Add configuration parsing
   - Provide example config

3. **Testing Changes**
   ```shell
   # Run unit tests
   sbt test
   # Test with example config
   sbt run preview -c examples/playground/config.conf
   ```
