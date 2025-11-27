# Todolist - Base Module (app)

This folder contains a minimal base project structure for a Todo/Mission app with AI voice assistant integration in mind.

What I added:
- Domain models: Task, Mission, AiResult
- Repository interfaces for Task/Mission/AI
- In-memory repository implementations for quick development/tests
- Use cases for common operations (get/save/delete/complete/convert)
- Simple UI skeletons (Home, Task detail) and common components
- ManualProviders for quick wiring without Hilt
- README with next steps

Next steps and recommendations:
- Replace in-memory repositories with Room (local) and Retrofit (remote) implementations.
- Integrate Hilt or Koin for proper DI (use the `di` folder as starting point).
- Implement voice AI integration in `AiRepositoryImpl` to call a real STT/assistant service.
- Implement notifications using WorkManager/AlarmManager and a concrete implementation of the `SendNotificationUseCase` in the Android layer.
- Wire Compose screens to real ViewModels using Hilt and `collectAsState()` for StateFlows.

If you want, I can:
- Convert ManualProviders to Hilt modules and annotate classes with @Inject/@Singleton.
- Implement Room entities/DAO for Task/Mission.
- Add a small sample UI and a screen to demo the AI voice command parsing.

