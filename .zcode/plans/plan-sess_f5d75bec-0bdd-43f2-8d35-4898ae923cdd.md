# Интеграция бэкенда в feature:requests (модерация заявок)

## Context

`feature:requests` сейчас работает на заглушке `HardcodedReviewRequestRepository` (фейковые данные в памяти). Бэкенд реализовал полный цикл модерации, разделённый по ролям. Интеграция замыкает сквозной жизненный цикл мероприятия: создание учеником → одобрение учителем → голосование → одобрение администратором.

Все нужные DTO уже сгенерированы в `core:network` (`EventResponseDto`, `DeclineEventDto`, `UserResponseDto`) — регенерация OpenAPI не нужна. Работаем только в `feature/requests/**` (интегратор-файлы не затрагиваются, модуль уже зарегистрирован).

**Следствие API, меняющее UI:** бэкенд не имеет понятий «участники», «предлагаемое место», «подтверждённая дата/место». Admin approve/decline не принимают тела (кроме опциональной причины отказа). Поэтому:
-sealed `ReviewRequest.Publication/Visit` упрощается до одного data class (без участников/локаций/confirmed-полей);
- у админа диалог одобрения — простое подтверждение, без ввода даты/места;
- раздел «Участники» на карточке удаляется.

## Контракт (api/openapi.yaml)

- `GET api/v1/events/moderation` — учитель: свои events со статусом `ON_MODERATION` → `EventResponseDto[]`
- `GET api/v1/events/admin-moderation` — админ: events со статусом `ON_ADMIN_MODERATION` → `EventResponseDto[]`
- `POST api/v1/events/{id}/approve` — учитель, `ON_MODERATION → ON_VOTE` → `EventResponseDto`
- `POST api/v1/events/{id}/decline` — учитель, опциональное тело `DeclineEventDto(reason)` → `EventResponseDto`
- `POST api/v1/events/{id}/admin/approve` — админ, `ON_ADMIN_MODERATION → FINALIZED` → `EventResponseDto`
- `POST api/v1/events/{id}/admin/decline` — админ, опциональное тело `DeclineEventDto(reason)` → `EventResponseDto`

## Изменения (только feature/requests)

### 1. `build.gradle.kts`
Как в `feature/activities`: плагин `id("tech.dev.ktor")`, `implementation(project(":core:network"))`, `testImplementation("io.ktor:ktor-client-mock:3.0.3")`.

### 2. Domain — `domain/ReviewRequest.kt`
Упростить: один data class вместо sealed interface:
```kotlin
enum class RequestAudience { TEACHER, ADMIN }
enum class ReviewStatus { PENDING_TEACHER, PENDING_ADMIN, APPROVED, DECLINED }
data class RequestAuthor(id, name, pupilClass)
data class ReviewRequest(
    id, title, description, proposedDate, author,
    status, audience, declineReason: String?,
)
```
Расширения `isPending` / `isProcessed` остаются. `RequestParticipant`, `Publication`, `Visit`, `proposedLocation`, `confirmedDate/Location`, `participants` — удалить.

### 3. Domain — `domain/ReviewRequestRepository.kt`
Интерфейс становится audience-явным (эндпоинты разные для учителя/админа):
```kotlin
interface ReviewRequestRepository {
    fun observeRequests(): Flow<List<ReviewRequest>>
    suspend fun refresh(audience: RequestAudience): DomainResult<Unit>
    suspend fun approve(requestId: String, audience: RequestAudience): DomainResult<ReviewRequest>
    suspend fun decline(requestId: String, reason: String?, audience: RequestAudience): DomainResult<ReviewRequest>
}
```

### 4. Data — новые `data/EventReviewRequestRepository.kt` + `data/EventReviewMapper.kt`
Паттерн из `feature/activities/data/ActivityRepositoryImpl.kt`: `HttpClient` + `safeCall` + `NetworkResult.error.toDomain()` → `DomainResult`; кэш `MutableStateFlow<List<ReviewRequest>>` + `observeRequests()`.

- `refresh(TEACHER)` → `GET api/v1/events/moderation`; `refresh(ADMIN)` → `GET api/v1/events/admin-moderation`; успех заменяет кэш целиком.
- `approve`: TEACHER → `POST .../{id}/approve`, ADMIN → `POST .../{id}/admin/approve`; ответ обновляет элемент в кэше по id (обработанные заявки остаются видны на табе «Обработанные» до следующего refresh).
- `decline`: те же пары эндпоинтов; тело `DeclineEventDto(reason)` отправляется только если reason после trim не пустой (в контракте тело опционально).
- Маппер (`EventMapper.kt` как референс): `EventResponseDto.toDomain(audience)`.
  - Статусы: `ON_MODERATION→PENDING_TEACHER`, `ON_ADMIN_MODERATION→PENDING_ADMIN`, `ON_VOTE/FINALIZED→APPROVED`, `DECLINED/DECLINED_BY_ADMIN/EXPIRED→DECLINED`.
  - `title=name`, `description=description.orEmpty()`, `declineReason=declineReason`.
  - Автор: `initiator` → имя+фамилия, класс — `initiator.propertyClass` (как в `EventMapper.kt`).
  - `proposedDate`: `startsAt` (ISO) → локальная дата-время через `java.time` (minSdk 26): «d MMMM, HH:mm» с `Locale.getDefault()`; при ошибке парсинга — сырая строка.

### 5. `DomainErrorText.kt` (новый, в корне пакета фичи)
Копия паттерна `feature/events/src/main/kotlin/com/dev/lbsuhack/feature/events/DomainErrorText.kt`: `DomainError.toUiText()` на строки `requests_error_*`.

### 6. Store — `store/requests/RequestsStore.kt` + `RequestsStoreFactory.kt`
- Удалить: `ChangeConfirmedDate/ChangeConfirmedLocation` интенты, соответствующие Message, поля `confirmedDate/confirmedLocation/requiresDetails` у `RequestDialog.Approve`, валидацию деталей в `confirmApprove`.
- `Load(role)`: аудит из `toRequestAudience()` (остаётся); далее — observe репозитория (коллектится), плюс разовый `refresh(audience)` при `audience != null`.
- Ошибка refresh: `Message.RequestsLoadFailed` (новый) → `copy(isLoading = false)` + `ShowMessage(error.toUiText())`.
- `confirmApprove/confirmDecline`: вместо try/catch — `DomainResult`: Success → `ActionCompleted` + success-строка, Failure → `ActionFailed` + `ShowMessage(error.toUiText())`.
- Фильтрация по роли/классу учителя в State (`queueRequests`) не меняется.

### 7. UI — `ui/requests/RequestsScreen.kt`
- `RequestCard`: убрать ветки `is ReviewRequest.Visit` (строку места, `ParticipantsSection`/`ParticipantRow`, блок confirmed-даты/места); kind-badge и status-badge остаются, блок причины отказа остаётся.
- Approve-диалог: без полей даты/места; текст для админа по audience.
- `DialogTextField` остаётся (нужен для причины отказа).
- Навигация/Routes — без изменений.

### 8. Строки — `res/values/strings.xml` + `res/values-ru/strings.xml`
- Удалить: `requests_confirm_date_*`, `requests_confirm_location_*`, `requests_participants_*`, `requests_location_format`, `requests_confirmed_*`, `requests_error_details_required`, `requests_dialog_approve_visit_text` (заменить на `requests_dialog_approve_admin_text` с текстом про финальное согласование).
- Добавить: `requests_error_network`, `requests_error_forbidden`, `requests_error_not_found`, `requests_error_unknown` (для `DomainErrorText`). `requests_error_action_failed` удалить — заменяется на error.toUiText().

### 9. DI — `di/RequestsModule.kt`
```kotlin
single<ReviewRequestRepository> {
    EventReviewRequestRepository(httpClient = get(), ioDispatcher = Dispatchers.IO)
}
```
(как в `feature/activities/di/ActivitiesModule.kt`). Остальные регистрации не меняются.

### 10. Тесты
- Удалить `data/HardcodedReviewRequestRepository` и его тест.
- Новый `util/FakeReviewRequestRepository` (in-memory, поведение как у реального: refresh по аудитории, approve/decline обновляют статус) — для store-тестов.
- Новый `data/EventReviewRequestRepositoryTest` по образцу `ActivityRepositoryImplTest` (MockEngine + `AppHttpClientFactory.defaultJson`): refresh учителя/админа с маппингом полей и даты, approve POST-ает в нужный эндпоинт и обновляет кэш, decline отправляет тело с причиной и без неё, 403 → DomainResult.Failure(Forbidden).
- `store/RequestsStoreTest` переписать на fake-репозиторий: маршруты учитель/админ/ученик, approve/decline потоки, табы; Visit-тесты деталей удалить; тест «учитель открывает админ-очередь» остаётся (store-фильтрация), но с audience-параметром.
- `util/FakeSessionRepository`, `util/MainDispatcherRule` — без изменений.

## Verification

1. `.\gradlew.bat :feature:requests:testDebugUnitTest` — все тесты модуля зелёные.
2. `.\gradlew.bat :feature:requests:compileDebugKotlin` — компиляция.
3. `.\gradlew.bat assembleDebug` — полная сборка (проверка, что DI/registration не сломаны).
4. Отчёт: список команд и результатов; вне `feature/requests/**` файлы не меняются, новых зависимостей (кроме уже используемых activities: ktor-mock testImpl) нет.
