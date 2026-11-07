# Стратегия тестирования и контроль качества (Testing & Quality)

В данном документе описана методология тестирования, структура юнит-тестов MVIKotlin сторов, сетевых моков и правила параллельной разработки в репозитории **Kola («Шестой школьный день»)**.

---

## 1. Пирамида тестирования и стек инструментов

Основной акцент тестирования сделан на бизнес-логику, контракты взаимодействия и переходы состояний в MVI-сторах, что обеспечивает высокую надежность без хрупких UI-тестов.

### Используемые библиотеки:
* **JUnit 4**: Базовый раннер тестов.
* **Turbine (`app.cash.turbine`)**: Реактивное тестирование Kotlin Coroutines `Flow` и `StateFlow`.
* **MVIKotlin Testing**: Тестирование сторов через `DefaultStoreFactory`.
* **Ktor MockEngine**: Изолированное тестирование сетевых запросов и парсеров без реального бэкенда.
* **Kover (`kotlinx-kover`)**: Агрегация и генерация отчетов о покрытии кода тестами.

---

## 2. Паттерн тестирования MVIKotlin Store

Каждый экран покрыт тестами стора, проверяющими реакцию на `Intent`, корректность мутации `State` и генерацию `Label`:

```kotlin
@Test
fun `when Vote intent received then store emits optimistic updated state`() = runTest {
    val fakeRepo = FakeActivityRepository()
    val store = ActivitiesStoreFactory(
        storeFactory = DefaultStoreFactory(),
        repository = fakeRepo,
    ).create()

    store.states.test {
        val initialState = awaitItem()
        assertFalse(initialState.isLoading)

        store.accept(ActivitiesStore.Intent.Vote(activityId = "act-123"))

        val loadingState = awaitItem()
        assertTrue(loadingState.isVoting)

        val updatedState = awaitItem()
        assertEquals(1, updatedState.items.first().votesCount)
        assertTrue(updatedState.items.first().hasVoted)
    }
}
```

---

## 3. Фикстуры и фейковые репозитории (Test Fixtures)

В проекте намеренно сведены к минимуму тяжелые Mock-библиотеки (вроде Mockk/Mockito) в пользу **In-Memory Fakes**:

* `FakeSessionRepository`: Эмулирует состояние авторизации и смену пользователей в памяти.
* `FakeActivityRepository`: Управляет списком инициатив, имитируя задержки сети и генерацию ошибок.
* `FakeReviewRepository`: Эмулирует очередь модерации для учителей и дирекции.
* `FakeKeyValueStorage`: Реализация key-value хранилища на базе `ConcurrentHashMap`.

---

## 4. Справочник Gradle-команд для тестирования

Команды классифицируются на **локальные** (безопасные при параллельной работе) и **глобальные** (зарезервированные за интегратором).

### Локальные команды тестирования модулей:
```bash
# Тестирование модуля активностей:
./gradlew :feature:activities:testDebugUnitTest

# Тестирование авторизации и парсера Schools.by:
./gradlew :feature:auth:testDebugUnitTest

# Тестирование модерации заявок:
./gradlew :feature:requests:testDebugUnitTest

# Тестирование профиля и тем оформления:
./gradlew :feature:profile:testDebugUnitTest

# Тестирование сетевого слоя и шифрования токенов:
./gradlew :core:network:testDebugUnitTest

# Компиляция модуля без запуска тестов:
./gradlew :feature:activities:compileDebugKotlin
```

### Глобальные команды (запускаются интегратором):
```bash
# Полный прогон всех юнит-тестов репозитория:
./gradlew testDebugUnitTest

# Генерация объединенного HTML-отчета о покрытии кода (Kover):
./gradlew :koverHtmlReport

# Генерация XML-отчета для CI/CD:
./gradlew :koverXmlReport

# Проверка линтером:
./gradlew lintDebug
```

> [!NOTE]
> Отчеты Kover сохраняются в директорию `build/reports/kover/html/index.html`. В покрытие намеренно не включаются сгенерированные классы OpenAPI, Composable-превью и DI-модули Koin.

---

## 5. Регламент параллельной работы агентов (`AGENTS.md`)

Репозиторий спроектирован для безопасной параллельной разработки:
1. **Изоляция рабочих деревьев (Worktrees)**: Каждый агент работает в выделенном git worktree.
2. **Строгая юрисдикция файлов**: Агент фичи имеет право модифицировать исключительно файлы внутри своей директории `feature/<name>/**`.
3. **Общие файлы**: Корневые скрипты (`settings.gradle.kts`, `app/build.gradle.kts`, `api/openapi.yaml`) модифицируются только интегратором.
4. **Запрет на деструктивные команды**: Запрещено выполнять `./gradlew --stop`, `git clean -fd`, удаление кэшей Gradle во время работы других агентов.
