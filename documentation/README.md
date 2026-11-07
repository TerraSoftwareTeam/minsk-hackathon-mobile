# Навигатор по документации Kola

Центр документации экосистемы **Kola («Шестой школьный день»)**. Документация разделена на продуктовую часть (понятную школьникам, педагогам, продактам и жюри) и инженерную спецификацию для разработчиков.

---

## Каталог документов

| Раздел | Файл | Аудитория / Краткое содержание |
| :--- | :--- | :--- |
| **Путь пользователя и пайплайн** | [`USER_JOURNEY.md`](./USER_JOURNEY.md) | **Для всех / Продакт**: Дорожная карта инициативы, 5 этапов пути от идеи до субботы, персонажи (Ученик, Учитель, Дирекция), воронка отбора и сценарии использования простым языком. |
| **Спецификация фичей** | [`FEATURES_SPEC.md`](./FEATURES_SPEC.md) | **Для разработчиков**: Полная инженерная спецификация модулей (`feature:*`), MVI-сторы, доменные модели, валидация полей, кворумы и матрица серверных скоупов. |
| **Архитектура** | [`ARCHITECTURE.md`](./ARCHITECTURE.md) | Clean Architecture, MVIKotlin (Intent $\to$ Executor $\to$ Reducer $\to$ State), граф модулей Gradle, безопасность и шифрование сессий (AES-256 GCM). |
| **Навигация и слои** | [`NAVIGATION_AND_LAYERS.md`](./NAVIGATION_AND_LAYERS.md) | Jetpack Navigation 3, типобезопасные `NavKey`, система слоев (`layers`), шлюз сессий (`AppRootScreen`), глубокие ссылки `kola://`. |
| **Дизайн-система** | [`DESIGN_SYSTEM_AND_UI.md`](./DESIGN_SYSTEM_AND_UI.md) | Стилистика HackTheme, расширенные токены `HackExtras` (glow, danger, success), типографика, переиспользуемый UI-кит (`core:ui`), правила декомпозиции. |
| **Сеть и API** | [`NETWORK_AND_API.md`](./NETWORK_AND_API.md) | Сетевой клиент Ktor 3.x, спецификация OpenAPI 3.0 (`api/openapi.yaml`), парсер электронных журналов Schools.by, обработка ошибок `NetworkResult`. |
| **Push и QR-коды** | [`NOTIFICATIONS_AND_DEEPLINKS.md`](./NOTIFICATIONS_AND_DEEPLINKS.md) | Firebase Cloud Messaging (FCM), синхронизация токенов `PushTokenSync`, сканер QR-кодов Google ML Kit и генератор матриц ZXing. |

---

## Маршруты чтения по ролям

### Продакт-менеджеру, дизайнеру и жюри хакатона:
1. Начните с [`USER_JOURNEY.md`](./USER_JOURNEY.md) — здесь описан пользовательский опыт, персонажи и жизненный цикл мероприятия без погружения в код.
2. Ознакомьтесь с палитрой и визуальными принципами в [`DESIGN_SYSTEM_AND_UI.md`](./DESIGN_SYSTEM_AND_UI.md).

### Разработчику мобильного приложения:
1. Начните с [`ARCHITECTURE.md`](./ARCHITECTURE.md) для понимания потока данных MVIKotlin и структуры модулей.
2. Изучите технические контракты модулей в [`FEATURES_SPEC.md`](./FEATURES_SPEC.md).
3. Изучите [`NAVIGATION_AND_LAYERS.md`](./NAVIGATION_AND_LAYERS.md) для работы с Jetpack Navigation 3.
4. Ознакомьтесь с [`DESIGN_SYSTEM_AND_UI.md`](./DESIGN_SYSTEM_AND_UI.md) перед созданием новых экранов.

### Бэкенд-инженеру и интегратору:
1. Изучите спецификацию эндпоинтов в [`NETWORK_AND_API.md`](./NETWORK_AND_API.md).
2. Ознакомьтесь с матрицей разрешений и моделей в [`FEATURES_SPEC.md`](./FEATURES_SPEC.md).
3. Проверьте жизненный цикл токенов уведомлений в [`NOTIFICATIONS_AND_DEEPLINKS.md`](./NOTIFICATIONS_AND_DEEPLINKS.md).
