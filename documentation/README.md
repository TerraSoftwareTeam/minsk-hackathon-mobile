# Навигатор по технической документации Kola

Добро пожаловать в центр технической документации экосистемы **Kola («Шестой школьный день»)**. Здесь собраны подробные описания архитектуры, каталога возможностей на разных этапах жизненного цикла, дизайн-системы, сетевого взаимодействия и стандартов тестирования.

---

## 📚 Каталог документов

| Раздел | Файл | Краткое содержание |
| :--- | :--- | :--- |
| 🚀 **Фичи и стадии** | [`FEATURES_AND_STAGES.md`](./FEATURES_AND_STAGES.md) | Полный каталог возможностей, 5 стадий жизненного цикла активности, матрица прав ролей (Ученик, Учитель, Администрация) и демо-плейсхолдеры. |
| 🎬 **Демо и Визуализация** | [`DEMO_AND_MEDIA_GUIDE.md`](./DEMO_AND_MEDIA_GUIDE.md) | Стандарты отображения демо на GitHub: легкие WebP/GIF анимации, мокапы экранов, сравнительные таблицы (Side-by-Side), сценарии записи. |
| 🏗 **Архитектура** | [`ARCHITECTURE.md`](./ARCHITECTURE.md) | Clean Architecture, MVIKotlin (Intent $\to$ Executor $\to$ Reducer $\to$ State), граф модулей Gradle, безопасность и шифрование сессий (AES-256 GCM). |
| 🧭 **Навигация и слои** | [`NAVIGATION_AND_LAYERS.md`](./NAVIGATION_AND_LAYERS.md) | Jetpack Navigation 3, типобезопасные `NavKey`, система слоев (`layers`), шлюз сессий (`AppRootScreen`), глубокие ссылки `kola://`. |
| 🎨 **Дизайн-система** | [`DESIGN_SYSTEM_AND_UI.md`](./DESIGN_SYSTEM_AND_UI.md) | Стилистика HackTheme, расширенные токены `HackExtras` (glow, danger, success), типографика, переиспользуемый UI-кит (`core:ui`), правила декомпозиции. |
| 🌐 **Сеть и API** | [`NETWORK_AND_API.md`](./NETWORK_AND_API.md) | Сетевой клиент Ktor 3.x, спецификация OpenAPI 3.0 (`api/openapi.yaml`), парсер электронных журналов Schools.by, обработка ошибок `NetworkResult`. |
| 🔔 **Push и QR-коды** | [`NOTIFICATIONS_AND_DEEPLINKS.md`](./NOTIFICATIONS_AND_DEEPLINKS.md) | Firebase Cloud Messaging (FCM), синхронизация токенов `PushTokenSync`, сканер QR-кодов Google ML Kit и генератор матриц ZXing. |
| 🧪 **Тесты и качество** | [`TESTING_AND_CI.md`](./TESTING_AND_CI.md) | Юнит-тестирование сторов с Turbine, моки Ktor MockEngine, покрытие Kover, регламент параллельной работы разработчиков (`AGENTS.md`). |

---

## 🧭 Маршруты чтения по ролям

### Разработчику мобильного приложения:
1. Начните с [`ARCHITECTURE.md`](./ARCHITECTURE.md) для понимания потока данных MVIKotlin и структуры модулей.
2. Изучите [`NAVIGATION_AND_LAYERS.md`](./NAVIGATION_AND_LAYERS.md) для работы с Jetpack Navigation 3.
3. Ознакомьтесь с [`DESIGN_SYSTEM_AND_UI.md`](./DESIGN_SYSTEM_AND_UI.md) перед созданием новых экранов.
4. Проверьте [`TESTING_AND_CI.md`](./TESTING_AND_CI.md) для запуска локальных тестов.

### Бэкенд-инженеру и интегратору:
1. Изучите спецификацию эндпоинтов в [`NETWORK_AND_API.md`](./NETWORK_AND_API.md).
2. Ознакомьтесь с моделью состояний в [`FEATURES_AND_STAGES.md`](./FEATURES_AND_STAGES.md).
3. Проверьте жизненный цикл токенов уведомлений в [`NOTIFICATIONS_AND_DEEPLINKS.md`](./NOTIFICATIONS_AND_DEEPLINKS.md).

### Дизайнеру и автору демо-материалов:
1. Ознакомьтесь с правилами записи и вставки гифок в [`DEMO_AND_MEDIA_GUIDE.md`](./DEMO_AND_MEDIA_GUIDE.md).
2. Изучите палитру и токены в [`DESIGN_SYSTEM_AND_UI.md`](./DESIGN_SYSTEM_AND_UI.md).
3. Проверьте сценарии жизненного цикла в [`FEATURES_AND_STAGES.md`](./FEATURES_AND_STAGES.md).
