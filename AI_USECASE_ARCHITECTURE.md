# AI UseCase Architecture - Hướng dẫn

## 📋 Tổng quan

Dự án đã được tái cấu trúc để tách biệt logic xử lý AI khỏi UI layer bằng cách sử dụng **AI Use Cases**. Điều này tuân theo nguyên tắc Clean Architecture và Dependency Inversion.

## 🏗️ Kiến trúc

```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer                              │
│           (VoiceAssistantViewModel)                      │
└─────────────────┬───────────────────────────────────────┘
                  │ gọi
                  ▼
┌─────────────────────────────────────────────────────────┐
│         AIUseCases (Interface - main/)                   │
│                                                          │
│  1. processVoiceCommand: Parse & Validate                │
│     - Build prompt                                       │
│     - Gọi AI → raw response                              │
│     - Parse JSON                                         │
│     - Validate                                           │
│     → Return VoiceCommand                                │
│                                                          │
│  2. executeVoiceCommand: Execute Business Logic          │
│     - Create/Update/Delete Task/Mission                  │
│     - Gọi TaskUseCases/MissionUseCases                   │
│     → Return response message                            │
└─────────────────┬───────────────────────────────────────┘
                  │
         ┌────────┴────────┐
         ▼                 ▼
┌─────────────────┐ ┌──────────────────┐
│  Release Build  │ │   Debug Build    │
│                 │ │                  │
│ RealProcess...  │ │ MockProcess...   │
│ ↓               │ │ ↓                │
│ GeminiService   │ │ Keyword-based    │
│ (CHỈ giao tiếp) │ │ (Mock AI)        │
│                 │ │                  │
│ RealExecute...  │ │ MockExecute...   │
│ ↓               │ │ ↓                │
│ VoiceCommand    │ │ VoiceCommand     │
│ Executor        │ │ Executor         │
└─────────────────┘ └──────────────────┘
```

## 🔄 Flow HOÀN CHỈNH - Từ Voice Input đến Thực thi Task

```
User: "Tạo task họp team lúc 2 giờ chiều"
    ↓
1️⃣ UI Layer (VoiceAssistantViewModel)
    ↓
    aiUseCases.processVoiceCommand("Tạo task họp team...")
    ↓
┌─────────────────────────────────────────────────┐
│ 2️⃣ ProcessVoiceCommandUseCase                   │
│                                                  │
│ Step 1: Build prompt                            │
│   prompt = geminiService.buildVoiceCommand...() │
│                                                  │
│ Step 2: Gọi AI - CHỈ lấy raw response           │
│   rawJson = geminiService.sendPrompt(prompt)    │
│   Result: "{action: CREATE_TASK, ...}"          │
│                                                  │
│ Step 3: Parse (UseCase xử lý)                   │
│   command = parser.parseResponse(rawJson)       │
│   → VoiceCommand {                              │
│       action: CREATE_TASK,                      │
│       params: {title: "họp team", time: "14:00"}│
│     }                                            │
│                                                  │
│ Step 4: Validate (UseCase xử lý)                │
│   parser.validateCommand(command) ✓             │
└─────────────────────────────────────────────────┘
    ↓
    Return VoiceCommand to ViewModel
    ↓
3️⃣ UI Layer (VoiceAssistantViewModel)
    ↓
    aiUseCases.executeVoiceCommand(command)
    ↓
┌─────────────────────────────────────────────────┐
│ 4️⃣ ExecuteVoiceCommandUseCase                   │
│                                                  │
│ executor.execute(command)                       │
│   ↓                                              │
│   VoiceCommandExecutor                          │
│     ↓                                            │
│     when (command.action) {                     │
│       CREATE_TASK ->                            │
│         val task = Task(...)                    │
│         taskUseCases.createTask(task) ✅         │
│       CREATE_MISSION ->                         │
│         missionUseCases.createMission(...)      │
│       COMPLETE_TASK ->                          │
│         taskUseCases.toggleTaskCompleted(...)   │
│       LIST_TASKS ->                             │
│         taskUseCases.getTasks()                 │
│       ...                                        │
│     }                                            │
└─────────────────────────────────────────────────┘
    ↓
    Return response message
    ↓
5️⃣ UI Layer (VoiceAssistantViewModel)
    ↓
    speakResponse("Đã tạo task...")
    updateUIState()
```

## 🎯 Trách nhiệm từng Layer

### **1. GeminiService** (AI Package)
**Chức năng:** CHỈ giao tiếp với Gemini AI
```kotlin
class GeminiService {
    suspend fun sendPrompt(prompt: String): Result<String>
    fun buildVoiceCommandPrompt(userInput: String): String
}
```
- ✅ Gửi prompt → Nhận raw JSON response
- ❌ KHÔNG parse
- ❌ KHÔNG validate
- ❌ KHÔNG execute

### **2. ProcessVoiceCommandUseCase** (UseCase)
**Chức năng:** Parse & Validate AI response
```kotlin
interface ProcessVoiceCommandUseCase {
    suspend operator fun invoke(userInput: String): Result<VoiceCommand>
}
```
- ✅ Build prompt
- ✅ Gọi GeminiService
- ✅ Parse JSON response
- ✅ Validate command
- ❌ KHÔNG execute business logic

### **3. ExecuteVoiceCommandUseCase** (UseCase)
**Chức năng:** Thực thi command
```kotlin
interface ExecuteVoiceCommandUseCase {
    suspend operator fun invoke(command: VoiceCommand): Result<String>
}
```
- ✅ Execute command (create/update/delete)
- ✅ Gọi TaskUseCases/MissionUseCases
- ✅ Return response message
- ❌ KHÔNG parse AI response

### **4. VoiceCommandExecutor** (Helper trong UseCase)
**Chức năng:** Routing logic
```kotlin
class VoiceCommandExecutor {
    suspend fun execute(command: VoiceCommand): Result<String> {
        when (command.action) {
            CREATE_TASK -> taskUseCases.createTask(...)
            CREATE_MISSION -> missionUseCases.createMission(...)
            COMPLETE_TASK -> taskUseCases.toggleTaskCompleted(...)
            ...
        }
    }
}
```
- ✅ Route command to correct UseCase
- ✅ Convert VoiceCommand → Task/Mission object
- ✅ Parse date/time strings

### **5. ViewModel** (UI Layer)
**Chức năng:** Coordinate UI flow
```kotlin
class VoiceAssistantViewModel {
    private fun processVoiceInput(input: String) {
        // Step 1: Parse & Validate
        val command = aiUseCases.processVoiceCommand(input).getOrNull()
        
        // Step 2: Execute
        val response = aiUseCases.executeVoiceCommand(command).getOrNull()
        
        // Step 3: Update UI
        speakResponse(response)
    }
}
```
- ✅ Gọi AIUseCases
- ✅ Update UI state
- ✅ Handle errors
- ❌ KHÔNG gọi trực tiếp AI service
- ❌ KHÔNG gọi trực tiếp executor

## 📍 ĐÂY LÀ NƠI THỰC THI TASK

**Câu hỏi:** "Hiện tại chỗ nào đang thực thi task sau khi xử lý dữ liệu từ AI?"

**Trả lời:** 

### Flow thực thi:
1. **ViewModel** gọi `aiUseCases.executeVoiceCommand(command)`
2. **ExecuteVoiceCommandUseCase** gọi `executor.execute(command)`
3. **VoiceCommandExecutor** gọi `taskUseCases.createTask(task)` ← **ĐÂY!**
4. **TaskUseCases** gọi repository → Database

### Code cụ thể:

**File:** `VoiceCommandExecutor.kt` (line 70-93)
```kotlin
private suspend fun createTask(command: VoiceCommand): Result<String> {
    val task = Task(
        id = 0,
        title = params.title,
        startTime = LocalDateTime.of(date, time),
        durationMinutes = duration,
        repeatType = RepeatType.NONE
    )

    taskUseCases.createTask(task)  // ← THỰC THI TASK Ở ĐÂY!
    
    return Result.success(command.responseText)
}
```

**File:** `VoiceAssistantViewModel.kt` (line 116-120)
```kotlin
// Step 2: Execute command
val executeResult = aiUseCases.executeVoiceCommand(command)
// ↑ Gọi UseCase → Executor → taskUseCases.createTask()
```

## 🎯 Tại sao lại thiết kế như vậy?

### ❌ TRƯỚC ĐÂY (Sai)
```
ViewModel → GeminiService → Parse → Validate → Executor → TaskUseCases
         (trộn lẫn tất cả logic)
```

### ✅ BÂY GIỜ (Đúng)
```
ViewModel 
  → AIUseCases.processVoiceCommand   (Parse & Validate)
  → AIUseCases.executeVoiceCommand   (Execute)
       → VoiceCommandExecutor
           → TaskUseCases/MissionUseCases
```

**Lợi ích:**
1. **ViewModel** không biết về AI parsing, chỉ biết về AIUseCases
2. **AI package** chỉ giao tiếp với AI, không biết về Task/Mission
3. **UseCase** xử lý business logic, dễ test, dễ maintain
4. **Executor** chỉ là helper để route commands

---

**Kết luận:** Task được thực thi ở `VoiceCommandExecutor.createTask()` → gọi `taskUseCases.createTask()` → lưu vào database. Tất cả được wrap trong `ExecuteVoiceCommandUseCase`, được gọi từ ViewModel qua `aiUseCases.executeVoiceCommand(command)`.
