# Using Google Cloud Storage (GCS) with the S3 SDK

[Google Cloud Storage][1] features APIs that allows developers to store and
access arbitrarily-large objects. The [GCS XML API][5] provides support for AWS
S3 API users that use S3 SDKs. Learn more about [Migrating to GCS][6].

## Prerequisites

Install [Maven](http://maven.apache.org/).

## Setup

1. Clone this repo.

   ```sh
   git clone https://github.com/GoogleCloudPlatform/java-docs-samples.git
   ```

1. Change into this directory:

   ```sh
   cd java-docs-samples/storage/s3-sdk
   ```

1. Build this project from this directory:

   ```sh
   mvn package
   ```

1. Get your [Interoperable Storage Access Keys][3] and set the following
   environment variables:

## Test Sample

1. Provide a service account which can be used to generate HMAC Key for the
   scope of the tests

   * GOOGLE_APPLICATION_CREDENTIALS=[PATH_TO_SERVICE_ACCOUNT_JSON]

1. Set the following environment variable with the default project for
   Interoperable Storage Access Keys.

   * GOOGLE_CLOUD_PROJECT_S3_SDK_BUCKET_NAME=[GOOGLE_PROJECT_ID]

1. Run test using the following Maven command:

   ```sh
   mvn verify
   ```

## Products

- [Google Cloud Storage][2]

## Language

- [Java][2]

## Dependencies

- [AWS S3 Java SDK][4]

[1]: https://cloud.google.com/storage
[2]: https://java.com
[3]: https://cloud.google.com/storage/docs/migrating#keys
[4]: https://mvnrepository.com/artifact/com.amazonaws/aws-java-sdk-s3
[5]: https://cloud.google.com/storage/docs/xml-api/overview
[6]: https://cloud.google.com/storage/docs/migrating
