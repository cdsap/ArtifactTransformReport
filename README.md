# ArtifactTransformReport
This project provides a cli or dependency designed to retrieve and display statistics of artifact transforms executed in a Gradle project. By leveraging the Develocity API, this tool offers detailed insights into the performance and behavior of artifact transforms.

## CLI

Retrieve artifact transforms of the last 50 builds of the project nowinandroid:
```
./artifacttransform --api-key=$DV_KEY --url=$DV_URL --project nowinandroid
```
Retrieve artifact transforms of a single build:
```
./artifacttransform --api-key=$DV_KEY --url=$DV_URL --build-scan-id wwdzlsbbxkopk
```

### Output
#### Summary console
The cli summarizes the information in the console including the following sections:
* Longest durations by Artifacts Transforms by Type
* Slowest Artifact transforms
* Duration by outcome/avoidance outcome
* Longest Negative Avoidance savings
* Heaviest cache size Artifact transforms

```kotlin
┌────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                                                      Artifacts Transforms by Type                                                                      │
├──────────────────────────────────────────┬─────────────────────────────────────────────────────────────────┬───────────────────────────────────────────────────────────┤
│                Transforms                │                            Duration                             │                      Fingerprinting                       │
├─────────────────────────────────┬────────┼──────────────────────────────────────────────┬──────────────────┼──────────────────────────────────────────────┬────────────┤
│ AarTransform                    │ 555032 │ DexingWithClasspathTransform                 │ 1d 23h 6m 15.86s │ ClasspathEntrySnapshotTransform              │ 28m 51.34s │
├─────────────────────────────────┼────────┼──────────────────────────────────────────────┼──────────────────┼──────────────────────────────────────────────┼────────────┤
│ IdentityTransform               │ 183331 │ ClasspathEntrySnapshotTransform              │     3h 34m 44.7s │ ExtractAarTransform                          │    22m 53s │
├─────────────────────────────────┼────────┼──────────────────────────────────────────────┼──────────────────┼──────────────────────────────────────────────┼────────────┤
│ AarToClassTransform             │ 178992 │ AsmClassesTransform                          │     3h 6m 54.92s │ AarTransform                                 │  20m 10.1s │
├─────────────────────────────────┼────────┼──────────────────────────────────────────────┼──────────────────┼──────────────────────────────────────────────┼────────────┤
│ CopyTransform                   │ 157175 │ JacocoTransform                              │    2h 56m 22.45s │ DexingWithClasspathTransform                 │ 17m 31.83s │
├─────────────────────────────────┼────────┼──────────────────────────────────────────────┼──────────────────┼──────────────────────────────────────────────┼────────────┤
│ AggregatedPackagesTransform     │ 157175 │ AarToClassTransform                          │    1h 19m 17.48s │ EnumerateClassesTransform                    │  7m 58.77s │
├─────────────────────────────────┼────────┼──────────────────────────────────────────────┼──────────────────┼──────────────────────────────────────────────┼────────────┤
│ ClasspathEntrySnapshotTransform │ 152334 │ ExtractAarTransform                          │       50m 46.86s │ BuildToolsApiClasspathEntrySnapshotTransform │  5m 10.26s │
├─────────────────────────────────┼────────┼──────────────────────────────────────────────┼──────────────────┼──────────────────────────────────────────────┼────────────┤
│ StructureTransformAction        │ 138867 │ StructureTransformAction                     │       50m 16.73s │ PlatformAttrTransform                        │  2m 45.28s │
├─────────────────────────────────┼────────┼──────────────────────────────────────────────┼──────────────────┼──────────────────────────────────────────────┼────────────┤
│ DexingWithClasspathTransform    │ 130920 │ AarTransform                                 │       46m 37.24s │ Aapt2FromMaven$Companion$Aapt2Extractor      │     30.69s │
├─────────────────────────────────┼────────┼──────────────────────────────────────────────┼──────────────────┼──────────────────────────────────────────────┼────────────┤
│ AsmClassesTransform             │  94891 │ BuildToolsApiClasspathEntrySnapshotTransform │        41m 8.17s │ AarToClassTransform                          │     22.88s │
├─────────────────────────────────┼────────┼──────────────────────────────────────────────┼──────────────────┼──────────────────────────────────────────────┼────────────┤
│ JacocoTransform                 │  93263 │ AggregatedPackagesTransform                  │       32m 56.64s │ JacocoTransform                              │      20.4s │
└─────────────────────────────────┴────────┴──────────────────────────────────────────────┴──────────────────┴──────────────────────────────────────────────┴────────────┘
┌───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                                                  Negative Transforms by Type                                                                  │
├───────────────────────────────────────┬──────────────────────────────────────────┬────────────────────────────────────────────────────────────────────────────┤
│              Transforms               │       Aggregated Negative Duration       │                             Slowest Transform                              │
├─────────────────────────────────┬─────┼─────────────────────────────────┬────────┼───────────────────────────────────────────────────────────────────┬────────┤
│ ClasspathEntrySnapshotTransform │ 117 │ EnumerateClassesTransform       │ -719ms │ AsmClassesTransform-hilt-work-1.1.0-runtime.jar                   │ -110ms │
├─────────────────────────────────┼─────┼─────────────────────────────────┼────────┼───────────────────────────────────────────────────────────────────┼────────┤
│ AggregatedPackagesTransform     │ 102 │ JacocoTransform                 │ -659ms │ JacocoTransform-classes.jar                                       │  -92ms │
├─────────────────────────────────┼─────┼─────────────────────────────────┼────────┼───────────────────────────────────────────────────────────────────┼────────┤
│ EnumerateClassesTransform       │  99 │ LibrarySymbolTableTransform     │ -642ms │ AsmClassesTransform-navigation-compose-2.8.0-alpha06-runtime.jar  │  -85ms │
├─────────────────────────────────┼─────┼─────────────────────────────────┼────────┼───────────────────────────────────────────────────────────────────┼────────┤
│ LibrarySymbolTableTransform     │  89 │ ClasspathEntrySnapshotTransform │ -576ms │ JacocoTransform-classes.jar                                       │  -57ms │
├─────────────────────────────────┼─────┼─────────────────────────────────┼────────┼───────────────────────────────────────────────────────────────────┼────────┤
│ AarResourcesCompilerTransform   │  69 │ AsmClassesTransform             │ -540ms │ JacocoTransform-classes.jar                                       │  -51ms │
├─────────────────────────────────┼─────┼─────────────────────────────────┼────────┼───────────────────────────────────────────────────────────────────┼────────┤
│ JacocoTransform                 │  62 │ AarResourcesCompilerTransform   │ -509ms │ AsmClassesTransform-firebase-messaging-ktx-23.3.0-runtime.jar     │  -36ms │
├─────────────────────────────────┼─────┼─────────────────────────────────┼────────┼───────────────────────────────────────────────────────────────────┼────────┤
│ AsmClassesTransform             │  40 │ AggregatedPackagesTransform     │ -422ms │ ClasspathEntrySnapshotTransform-model.jar                         │  -24ms │
├─────────────────────────────────┼─────┼─────────────────────────────────┼────────┼───────────────────────────────────────────────────────────────────┼────────┤
│ ExtractJniTransform             │  34 │ ExtractJniTransform             │  -93ms │ AggregatedPackagesTransform-core-1.12.0-api.jar                   │  -24ms │
├─────────────────────────────────┼─────┼─────────────────────────────────┼────────┼───────────────────────────────────────────────────────────────────┼────────┤
│ DexingWithClasspathTransform    │   6 │ DexingWithClasspathTransform    │  -25ms │ AsmClassesTransform-firebase-config-21.5.0-runtime.jar            │  -22ms │
├─────────────────────────────────┼─────┼─────────────────────────────────┼────────┼───────────────────────────────────────────────────────────────────┼────────┤
│ DesugarLibConfigExtractor       │   1 │ DesugarLibConfigExtractor       │   -3ms │ ClasspathEntrySnapshotTransform-firebase-analytics-21.4.0-api.jar │  -21ms │
└─────────────────────────────────┴─────┴─────────────────────────────────┴────────┴───────────────────────────────────────────────────────────────────┴────────┘
┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                                          Duration by Artifact transform dependency                                                          │
├──────────────────────────────────────────────────────────────────────────────────┬──────────────────────────────────────────────────────────────────────────┤
│                        Aggregated duration by dependency                         │                  Aggregated duration by input artifact                   │
├─────────────────────────────────────────────────────────────────┬────────────────┼──────────────────────────────────────────────────────────┬───────────────┤
│ androidx.compose.material:material-icons-extended-android:1.6.3 │ 14h 55m 36.64s │ instrumented_material-icons-extended-release-runtime.jar │ 8h 29m 46.35s │
├─────────────────────────────────────────────────────────────────┼────────────────┼──────────────────────────────────────────────────────────┼───────────────┤
│ androidx.compose.material3:material3-android:1.2.1              │  4h 18m 37.19s │ material-icons-extended-release-runtime.jar              │ 5h 51m 39.71s │
├─────────────────────────────────────────────────────────────────┼────────────────┼──────────────────────────────────────────────────────────┼───────────────┤
│ androidx.compose.foundation:foundation-android:1.7.0-alpha06    │  2h 31m 15.33s │ instrumented_material3-release-runtime.jar               │ 2h 31m 55.42s │
├─────────────────────────────────────────────────────────────────┼────────────────┼──────────────────────────────────────────────────────────┼───────────────┤
│ androidx.compose.material:material-android:1.6.3                │  1h 47m 58.57s │ instrumented_foundation-release-runtime.jar              │  2h 3m 24.63s │
├─────────────────────────────────────────────────────────────────┼────────────────┼──────────────────────────────────────────────────────────┼───────────────┤
│ androidx.compose.ui:ui-android:1.7.0-alpha06                    │   1h 30m 9.87s │ instrumented_classes.jar                                 │ 1h 54m 36.18s │
├─────────────────────────────────────────────────────────────────┼────────────────┼──────────────────────────────────────────────────────────┼───────────────┤
│ org.jetbrains.kotlin:kotlin-stdlib:1.9.22                       │  1h 29m 55.49s │ material3-release-runtime.jar                            │ 1h 39m 11.63s │
├─────────────────────────────────────────────────────────────────┼────────────────┼──────────────────────────────────────────────────────────┼───────────────┤
│ com.google.guava:guava:31.1-android                             │  1h 20m 26.69s │ instrumented_guava-31.1-android.jar                      │  1h 7m 31.94s │
├─────────────────────────────────────────────────────────────────┼────────────────┼──────────────────────────────────────────────────────────┼───────────────┤
│ androidx.core:core:1.12.0                                       │    1h 2m 29.9s │ instrumented_ui-release-runtime.jar                      │  1h 7m 13.08s │
├─────────────────────────────────────────────────────────────────┼────────────────┼──────────────────────────────────────────────────────────┼───────────────┤
│ androidx.collection:collection-jvm:1.4.0                        │     58m 12.15s │ foundation-release-runtime.jar                           │   1h 5m 38.7s │
├─────────────────────────────────────────────────────────────────┼────────────────┼──────────────────────────────────────────────────────────┼───────────────┤
│ androidx.work:work-runtime:2.9.0                                │     56m 56.32s │ instrumented_material-release-runtime.jar                │   1h 1m 9.26s │
└─────────────────────────────────────────────────────────────────┴────────────────┴──────────────────────────────────────────────────────────┴───────────────┘
```

#### Summary text file
Text file with the same information displayed in the console

#### CSV
Complete list of artifact transforms by entity:
```csv
transformActionType,duration,avoidanceOutcome,buildScan,artifactTransformExecutionName,avoidanceSavings,fingerprintingDuration,changedAttributes.from,changedAttributes.to,cacheSize
org.jetbrains.kotlin.gradle.internal.transforms.ClasspathEntrySnapshotTransform,99,executed_cacheable,4qujllmrddxqe,groovy-astbuilder-3.0.17.jar [artifactType=classpath-entry-snapshot],null,2,jar,classpath-entry-snapshot,837
org.jetbrains.kotlin.gradle.internal.transforms.ClasspathEntrySnapshotTransform,92,executed_cacheable,4qujllmrddxqe,groovy-ant-3.0.17.jar [artifactType=classpath-entry-snapshot],null,2,jar,classpath-entry-snapshot,1678
...
```

### Usage
#### Using Binary
Github release repository contains the ArtifactTransformReport binary. After downloading the binary you can execute:
```
 curl -L https://github.com/cdsap/ArtifactTransformReport/releases/download/v0.1.1/artifacttransform --output artifacttransform
 chmod 0757 artifacttransform
./artifacttransform --api-key=$KEY --url=$URL --project nowinandroid
```
Where:
* `api-key` represents Develocity API token
* `url` Develocity instance

#### Using sources
```
./gradlew :cli:fatBinary
cd cli
./artifacttransform --api-key=$KEY --url=$URL --project nowinandroid
```
### Parameters aggregating multiple builds

| Name       | Description                              | Default | Required | Example                         |
|------------|------------------------------------------|---------|----------|---------------------------------|
| api-key    | String token                             |         | Yes      | --api-key=$DV_KEY               |
| url        | Gradle Enterprise instance               |         | Yes      | --url=https://ge.acme.dev       |
| max-builds | Max builds to be processed               | 100     | No       | --max-builds=500                |
| project    | Root project in Gradle Enterprise        |         | Yes      | --project=acme                  |
| tags       | One or more tags included in the build.  |         | No       | --tags=main --tags=not:local    |
| user       | Author of the build                      |         | No       | --user=leo                      |

#### Parameters single build

| Name          | Description                | Default | Required | Example                   |
|---------------|----------------------------|---------|----------|---------------------------|
| api-key       | String token               |         | Yes      | --api-key=$DV_KEY         |
| url           | Gradle Enterprise instance |         | Yes      | --url=https://ge.acme.dev |
| build-scan-id | Build scan id              |         | Yes      | --build-scan-id=iwuduw8   |

## Dependency

If you don't want to use the CLI, we are providing a dependency to retrieve the Artifact transforms:
```kotlin
implementation("io.github.cdsap:artifacttransform:0.1")
```
### Usage
Retrieve artifact transforms of the last 50 builds of the project nowinandroid:
```kotlin
 val filter = Filter(
    maxBuilds = 50,
    project = "nowinandroid",
    clientType = ClientType.API
)
val repository = GradleRepositoryImpl(GEClient(apiKey, url))
val transforms = GetArtifactTransforms(filter, repository).get()
```
Retrieve artifact transforms of a single build:
```kotlin
val repository = GradleRepositoryImpl(GEClient(apiKey, url))
val transforms = GetSingleArtifactTransform(repository).get("skek220x")
```

The library provides different extensions in `ArtifactTransforms.kt`:
```kotlin
...
fun List<ArtifactTransform>.totalNegativeAvoidanceSavingsByTransformArtifactType() =
    this.filter { it.avoidanceSavings != null && it.avoidanceSavings!!.toInt() < 0 }
        .groupingBy { it.transformActionType }
        .eachCount()
        .toList()
        .sortedByDescending { it.second }
        .flatMap {
            listOf(Pair(it.first, it.second))
        }
...
```

## Notes
* Artifact Transforms information is available in the Develocity API 2023.4+
* In highly modularized builds the number of artifact transforms can be high. The cli and dependency provides a way to filter the information by user, project, and tags.

## Libraries used
* [clikt](https://github.com/ajalt/clikt)
* [picnic](https://github.com/JakeWharton/picnic)
* [geapiData](https://github.com/cdsap/GEApiData)
* [fatBinary](https://github.com/cdsap/FatBinary)

