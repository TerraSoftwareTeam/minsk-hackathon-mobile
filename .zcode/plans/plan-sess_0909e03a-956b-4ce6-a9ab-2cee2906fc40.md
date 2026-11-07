# Исправление фичи profile: тумблер уведомлений и эффект нажатия

## Context

В профиле есть карточка-тумблер «Уведомления» (`NotificationsCard`). Две проблемы:

1. **Тумблер ничего не делает.** Он меняет только локальный флаг в SharedPreferences (`profile_settings/notifications_enabled`), но не влияет на доставку пушей: токен устройства регистрируется на бэкенде (`POST/DELETE api/v1/device-token`) при логине в `PushTokenSync` независимо от флага. Эндпоинта «настроек уведомлений» в API нет — единственный реальный механизм вкл/выкл пушей это регистрация/дерегистрация device-token.
2. **Корявый прямоугольный риппл.** `Row` в `NotificationsCard` имеет `Modifier.toggleable(...)` без `clip` — при нажатии подсветка выходит острым прямоугольником за скруглённые углы карточки (`ProfileCard` клипается `MaterialTheme.shapes.extraLarge`).

**Решение (согласовано с пользователем):** тумблер реально вкл/выкл пуши через register/unregister device-token; флаг учитывается и при старте приложения. Эффект нажатия — скруглённый, внутри карточки.

## Изменения

### 1. Контракт в core:domain (новые файлы)

`core/domain/src/main/kotlin/com/dev/lbsuhack/core/domain/push/NotificationSettings.kt`:
```kotlin
interface NotificationSettingsRepository {
    val isEnabled: Flow<Boolean>
    suspend fun setNotificationsEnabled(enabled: Boolean): Boolean
}
class ObserveNotificationsEnabledUseCase(private val repository: NotificationSettingsRepository) {
    operator fun invoke(): Flow<Boolean> = repository.isEnabled
}
class SetNotificationsEnabledUseCase(private val repository: NotificationSettingsRepository) {
    suspend operator fun invoke(enabled: Boolean): Boolean = repository.setNotificationsEnabled(enabled)
}
```

`core/domain/src/main/kotlin/com/dev/lbsuhack/core/domain/push/PushTokenProvider.kt`:
```kotlin
interface PushTokenProvider {
    suspend fun currentToken(): String?
}
```
(реализация в app-модуле, где живёт Firebase — как `PushTokenSync` уже делает через `KolaNotificationManager.fetchPushToken`)

### 2. Реализация в core:data

Новый `core/data/src/main/kotlin/com/dev/lbsuhack/core/data/push/DefaultNotificationSettingsRepository.kt`:
- Зависимости: `KeyValueStorage` (уже есть, строковый API), `PushTokenProvider`, `PushTokenRepository`, `CoroutineDispatcher = Dispatchers.IO`.
- Флаг хранит в KeyValueStorage (`"notifications_enabled"` → `"true"/"false"`), `isEnabled` = `MutableStateFlow`, засеянный из хранилища.
- `setNotificationsEnabled(true)`: взять токен у `PushTokenProvider` → `register(token)`; при успехе — сохранить флаг, обновить flow, вернуть `true`; иначе `false`.
- `setNotificationsEnabled(false)`: аналогично через `unregister(token)`.
- Регистрация в `core/data/di/DataModule.kt` (там уже регистрируются `PushTokenRepository` и его use cases): `single<NotificationSettingsRepository> { ... }` + `RegisterPushTokenUseCase`-подобные строки для двух новых use cases.

### 3. app-модуль

- Новый `app/src/main/kotlin/com/dev/lbsuhack/notification/KolaPushTokenProvider.kt`: `PushTokenProvider` поверх `KolaNotificationManager.fetchPushToken` (suspendCancellableCoroutine — тот же приём, что в `PushTokenSync.currentToken`).
- `app/.../App.kt`: в `appModule` добавить `single<PushTokenProvider> { KolaPushTokenProvider() }`; в конструктор `PushTokenSync` добавить `notificationsEnabled = get()` (use case).
- `app/.../notification/PushTokenSync.kt`: авто-регистрация токена гейтится флагом:
  - `start()`: `combine(observeSession.state.map { (it as? SessionState.Authenticated)?.user?.id }.distinctUntilChanged(), notificationsEnabled())` — регистрировать токен только когда `userId != null && enabled`.
  - `onNewToken(token)`: проверять `notificationsEnabled().first()` перед регистрацией.
  - `beforeClear()` (логаут) — без изменений, unregister всегда.

### 4. feature:profile

- `domain/ProfileRepository.kt`: убрать `isNotificationsEnabled()` / `setNotificationsEnabled()`.
- `data/ProfileRepositoryImpl.kt`: удалить `ProfilePreferences` и флаговые методы; контекст больше не нужен — убрать параметр `context` (и вторичный конструктор), обновить DI.
- `store/profile/ProfileStoreFactory.kt`:
  - конструктор: добавить `observeNotificationsEnabled: ObserveNotificationsEnabledUseCase`, `setNotificationsEnabled: SetNotificationsEnabledUseCase` (Koin подтянет через `singleOf(::ProfileStoreFactory)`);
  - `load()` (два места, строки ~85 и ~103): вместо `profileRepository.isNotificationsEnabled()` → `observeNotificationsEnabled().first()`;
  - `toggleNotifications()`: `val ok = setNotificationsEnabled(enabled)`; при `true` — `dispatch(Message.NotificationsChanged(enabled))`; при `false` — состояние не менять, опубликовать `Label.ShowMessage` с новой строкой `profile_notifications_error` («Не удалось изменить настройки уведомлений»).
- `res/values/strings.xml`: добавить `profile_notifications_error`.
- `di/ProfileModule.kt`: убрать `androidContext()` из `ProfileRepositoryImpl`.
- `ui/profile/component/NotificationsCard.kt`: добавить `.clip(MaterialTheme.shapes.extraLarge)` перед `.toggleable(...)` на `Row` — риппл станет скруглённым и не выйдет за углы карточки.

### 5. Тесты

`feature/profile/src/test/.../store/ProfileStoreTest.kt`:
- `FakeProfileRepository` — убрать флаговые методы.
- Добавить `FakeNotificationSettingsRepository` (внутри теста): `MutableStateFlow<Boolean>`, переключаемая «успешность» операции; в `createStore()` передать реальные `ObserveNotificationsEnabledUseCase` / `SetNotificationsEnabledUseCase` над фейком.
- Обновить тесты тумблера (~строка 117) и добавить тест: операция не удалась → состояние не изменилось + опубликован `Label.ShowMessage`.

## Ограничения по AGENTS.md

- Никаких комментариев в коде, никаких новых зависимостей.
- Команды только module-scoped: `:feature:profile:*`, `:core:data:*`, `:app:compileDebugKotlin`. Глобальный сбор не запускаю.
- Контрактное изменение: `ProfileRepository` теряет два метода (меняется только внутри feature:profile + его тесты); новые интерфейсы в core:domain / core:data — в рамках правила «core repository consumed through narrow use cases».

## Верификация

1. `./gradlew :feature:profile:testDebugUnitTest` — тесты store, включая новые сценарии тумблера.
2. `./gradlew :feature:profile:compileDebugKotlin` и `./gradlew :core:data:compileDebugKotlin`.
3. `./gradlew :app:compileDebugKotlin` — сборка app-модуля с изменённым `PushTokenSync`/`App.kt`.
4. Ручная проверка пользователем: выключить тумблер при сети → пуши перестают приходить (DELETE device-token), при обрыве сети — переключатель не меняется, появляется снекбар с ошибкой; после перезапуска приложения с выключенным тумблером токен не регистрируется заново.
