Unit tests (src/test):
- domain/usecase/GetTasksUseCaseTest.kt  - simple test using FakeTaskRepository
- data/mapper/TaskMapperTest.kt - tests mapping Task <-> TaskEntity
- ui/home/HomeViewModelTest.kt - placeholder test

Instrumented tests (src/androidTest):
- di/TestAppModule.kt - ensures AppDatabase can be instantiated in instrumented environment
- data/local/AppDatabaseTest.kt - basic in-memory db test
- ui/home/HomeScreenTest.kt - placeholder UI test

Notes:
- These are minimal placeholders. For robust tests add kotlinx-coroutines-test, Compose testing rule, and AndroidX test rules as needed.
- Run unit tests with: gradlew.bat test
- Run instrumentation tests with: gradlew.bat connectedAndroidTest
This folder contains placeholder unit tests and instrumented test stubs to demonstrate the test structure.


