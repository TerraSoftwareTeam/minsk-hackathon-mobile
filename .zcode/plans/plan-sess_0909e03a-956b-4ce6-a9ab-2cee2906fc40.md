# Оптимистичные обновления: кнопки голосования/лайка и тумблер уведомлений

## Context

Кнопки по всему приложению реагируют с задержкой, потому что UI обновляется только **после** ответа сети:

- **feature:activities, список и детали** — `ActivityRepositoryImpl.mutate()` (строки 212–238): перед запросом ещё дергается `getEventById` (для PUPIL может вызвать полный `refresh()` = 2–3 round-trip), кэш обновляется только после ответа. Оба стора наблюдают `observeActivities()`, поэтому кнопка переворачивается на круг later. Стор-гуарды (`mutating` в списке, `voting`/`liking` в деталях) блокируют повторные нажатия до конца сети — быстрое «подписаться → отписаться» невозможно.
- **feature:profile, тумблер уведомлений** — `ProfileStoreFactory.toggleNotifications()`: состояние меняется только после сетевого register/unregister device-token, гуард `toggleJob` блокирует перевыключение в полёте.

**Решение** — оптимистичные обновления с откатом и последовательной очередью сетевых операций:

- Для activities — оптимистику делает **репозиторий** (единственный источник истины для обоих экранов): кэш переворачивается мгновенно, запрос уходит в фоновую очередь (по Mutex, порядок сохраняется), при неудаче — откат кэша + событие ошибки, которое сторы показывают снекбаром. Быстрое vote→unvote работает: каждая операция мгновенно переворачивает флаг, запросы исполняются по порядку, сервер сходится к последнему намерению.
- Для уведомлений — оптимистику делает **стор** (UI-флаг): мгновенный `dispatch`, затем сеть «сходится к последнему значению» под Mutex; при неудаче — откат и снекбар.

## Изменения

### 1. feature:activities — domain

- Перенести `ActivityOperation` из `store/ActivityErrorText.kt` (там `internal enum`) в `domain/ActivityOperation.kt` — событие ошибки репозитория должно нести операцию для выбора текста снекбара.
- В `domain/ActivityRepository.kt`: добавить
  ```kotlin
  data class ActivityMutationFailure(val eventId: String, val operation: ActivityOperation, val error: DomainError)
  ```
  и `val mutationFailures: Flow<ActivityMutationFailure>` в интерфейс.

### 2. feature:activities — `ActivityRepositoryImpl.kt` (ядро фикса)

- Добавить `mutationScope = CoroutineScope(SupervisorJob() + ioDispatcher)` и `MutableSharedFlow<ActivityMutationFailure>(extraBufferCapacity = 16)`.
- `changeVote`/`changeLike` получают вторую трансформацию — флаговую, без ответа сервера:
  - vote: `copy(hasVoted = true, votesCount = votesCount + 1, voters = voters + voterName)`;
  - unvote: `copy(hasVoted = false, votesCount = (votesCount - 1).coerceAtLeast(0), voters = voters - voterName)`;
  - like: `copy(likedByMe = true, likesCount = likesCount + 1)`; unlike — зеркально.
- Новый `mutate`:
  1. `prepareCache()`, `existing = getEventById(id)` (как сейчас; при тёплом кэше — мгновенно; Failure → вернуть Failure как сейчас).
  2. Под `mutationMutex`: `mutations.incrementAndGet()`, применить флаговую трансформацию в кэш (`replaceOrAdd(optimistic)`) — оба экрана перерисуются мгновенно через `observeActivities()`.
  3. Сразу вернуть `DomainResult.Success(optimistic)`.
  4. `mutationScope.launch { mutationMutex.withLock { ... } }`: выполнить `safeCall { request() }`; затем под мьютексом: если текущий элемент кэша **не равен** optimistic (его перекрыла более новая операция) — ничего не писать; иначе при Success применить ответ сервера (`withVoteResult`/`withLikeResult`, как сейчас), при Error — **откат** к `existing` и `tryEmit(ActivityMutationFailure(...))`.
  - Логика «применять только если кэш == мой optimistic» корректно обрабатывает очереди: vote→unvote даёт POST затем DELETE, финал = последний запрос; откат срабатывает только у последней операции цепочки, промежуточные сбои не дёргают UI.
  - Инкремент `mutations` при optimistic-применении заодно защищает кэш от перезаписи параллельным `refresh()` (существующий механизм в `refresh()` строк 96–100, 138–142).

### 3. feature:activities — сторы

- `store/activities/ActivitiesStoreFactory.kt`: убрать гуард `mutating: MutableSet`; `act()` → `scope.launch { if (result is Failure) dispatch(Message.Failed(...)) }` (быстрый возврат). Добавить коллекцию `activityRepository.mutationFailures` → `dispatch(Message.Failed(error.toUiText(operation)))`. Существующие гуарды `hasVoted`/`likedByMe` теперь работают по оптимистичному состоянию = натуральный toggle.
- `store/details/ActivityDetailsStoreFactory.kt`: то же в `act()`; удалить поля `voting`/`liking`, сообщения `VotingStarted/Finished/LikingStarted/Finished` и поля `isVoting`/`isLiking` из `ActivityDetailsStore.State` (UI их не потребляет — проверено). Добавить коллекцию `mutationFailures` → `Message.ActionFailed(...)`. Мгновенное обновление экрана деталей идёт через уже существующую подписку `observeActivities()` (строки 95–101).
- Мапперы `withVoteResult`/`withLikeResult` (`data/EventMapper.kt`) не меняются.

### 4. feature:profile — тумблер уведомлений

`store/profile/ProfileStoreFactory.kt`, `toggleNotifications()`: убрать гуард `toggleJob`, сделать converge-to-latest:
```kotlin
private val toggleMutex = Mutex()

private fun toggleNotifications() {
    dispatch(Message.NotificationsChanged(!state().notificationsEnabled))  // мгновенный flip
    scope.launch {
        toggleMutex.withLock {
            val target = state().notificationsEnabled
            if (setNotificationsEnabled(target)) return@withLock
            if (state().notificationsEnabled == target) {
                dispatch(Message.NotificationsChanged(!target))
                publish(ProfileStore.Label.ShowMessage(UiText.Resource(R.string.profile_notifications_error)))
            }
        }
    }
}
```
Быстрое двойное переключение → мгновенные flip'ы, один-два сетевых вызова по порядку, финал = последнее значение. Контракт `SetNotificationsEnabledUseCase` и `DefaultNotificationSettingsRepository` не меняются.

### 5. Тесты

- `util/FakeActivityRepository.kt`: если `voteResult`/`unvoteResult`/... = Failure → применить optimistic-трансформацию к фейковому флоу, вернуть Success и эмитнуть `ActivityMutationFailure` (имитация нового контракта). Добавить `MutableSharedFlow` в фейк.
- `ActivitiesStoreTest` / `ActivityDetailsStoreTest`: обновить failure-тесты (ошибка теперь асинхронная — через flow); добавить тесты: (а) vote→unvote подряд → финально `hasVoted = false`, (б) network-failure → состояние откатилось + ошибка в state (errorId bump).
- `ProfileStoreTest`: существующие toggle-тесты проходят как есть (после `runCurrent`); добавить тест двойного переключения подряд → финальное значение = исходное, фейковый флаг сходится.

## Ограничения

- Без новых зависимостей, без комментариев в коде. Только module-scoped команды (`:feature:activities:*`, `:feature:profile:*`). Глобальный сбор — интегратору.
- UI-компоненты не меняются (перерисовка идёт от state/flow). Create-экран не трогаем (там честный прогресс и нужен серверный id).

## Верификация

1. `./gradlew :feature:activities:testDebugUnitTest :feature:profile:testDebugUnitTest` — все тесты зелёные.
2. `./gradlew :feature:activities:compileDebugKotlin :feature:profile:compileDebugKotlin`.
3. Ручная проверка: тап по стрелке/кнопке «Иду» — флаг и счётчик меняются мгновенно; быстрое «Иду → Отмена» подряд — корректный финал; самолётный режим — откат + снекбар; тумблер уведомлений — мгновенный Switch, при обрыве сети откат и снекбар.
