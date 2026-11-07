# AGENTS.md

Local !!! contract for AI agents working in this repository.

This file is intentionally untracked. Do not `git add -f` it and do not commit it.

## Project

Android hackathon app built with Kotlin, Jetpack Compose, Navigation 3, Koin,
MVIKotlin, Ktor, kotlinx.serialization, and OpenAPI code generation.

Gradle multi-module project with convention plugins in `build-logic`.

## Commands

Run from the repository root:

- Build the app: `./gradlew assembleDebug`
- Run all unit tests: `./gradlew testDebugUnitTest`
- Test one feature: `./gradlew :feature:<name>:testDebugUnitTest`
- Test one core module: `./gradlew :core:<name>:testDebugUnitTest`
- Compile one feature: `./gradlew :feature:<name>:compileDebugKotlin`
- Lint: `./gradlew lintDebug`
- Merged coverage, HTML: `./gradlew :koverHtmlReport`
- Merged coverage, XML: `./gradlew :koverXmlReport`

On Windows use `.\gradlew.bat ...`.

Always keep the leading colon for the merged coverage report: bare
`koverHtmlReport` also generates a report per module and takes longer.
Reports land in `build/reports/kover/` and are git-ignored. Coverage is
informational, there is no minimum threshold that fails the build.

A command is local when it names a module (`:feature:<name>`, `:core:<name>`)
and global when it does not. Local commands are safe to run while other
agents work. Global commands are covered by "Parallel work" below.

## Parallel work

Assume other agents are working in this repository at the same time. Work so
that your edits and commands cannot break theirs.

### Isolation

- Use one git worktree per parallel agent. Two agents must never edit the
  same checkout or run Gradle in each other's worktree.
- Worktrees keep source and `build/` separate but share the Gradle user home
  and dependency caches. Parallel Gradle runs are acceptable only because of
  that separation, and only when they are module-scoped.
- Never create, move, or remove a worktree that belongs to another agent.

### Command scope

Classify every Gradle command before running it.

Local, allowed while other agents work:

- `./gradlew :feature:<name>:testDebugUnitTest`
- `./gradlew :core:<name>:testDebugUnitTest`
- `./gradlew :feature:<name>:compileDebugKotlin`
- `./gradlew :core:<name>:compileDebugKotlin`
- `./gradlew :<module>:clean`, only for a module you built yourself

Global, reserved for the integrator or a checkout with no other active agent:

- `./gradlew assembleDebug` and `./gradlew build`
- `./gradlew testDebugUnitTest` without a module prefix
- `./gradlew lintDebug`
- `./gradlew clean`
- `./gradlew :koverHtmlReport` and `./gradlew :koverXmlReport`
- `./gradlew --stop`, daemon restarts, and Gradle JVM setting changes

A module agent runs only the tasks of the module it owns. Do not run a global
command as a final sanity check: the integrator does that after the handoff.
If a global run seems required, report it in the handoff instead of running
it.

### Shared state

- Do not stop, kill, or reconfigure Gradle daemons. `--stop` kills builds
  other agents may be running; `--no-daemon` only makes every later command
  slower.
- Do not delete `build/` or `.gradle/` of a module you did not build in this
  session. Clean only the module you own, and only when stale output is the
  suspected cause.
- Do not run `git clean`, `git stash`, `git checkout -- .`, `git reset
  --hard`, `git rebase`, or any other command that discards uncommitted
  work. Keep history operations inside your own worktree and your own
  commits.

### When a build fails

- A lock, "file in use", "another Gradle instance", or a suddenly unresolved
  reference usually means another agent is building or editing. Retry the
  same module command once.
- Do not "fix" the failure by cleaning the project, stopping the daemon, or
  deleting caches. Do not disable tests, lint, or code generation to make it
  pass.
- If the retry fails too, stop and report the exact command and output. The
  integrator resolves cross-agent collisions.

## Module map

- `app` — application shell and composition root. Owns `App.kt`,
  `MainActivity`, `AppRootScreen` (the session gate), `AppTheme` (the
  controller → theme bridge), and the final registration of features.
- `core:designsystem` — the single app design system. Owns `HackTheme`, the
  dark and light colour schemes, `HackExtras`, typography, shapes, and the
  persisted `ThemeController`. It holds tokens and the theme only, not
  composite components.
- `core:ui` — shared Compose component kit. Owns custom composite components
  used by two or more features (top bar, loading, empty state, labels,
  primary button, selectable rows). Depends on `core:designsystem` and never
  on a feature.
- `core:domain` — pure session contract (`SessionRepository`, `SessionState`,
  `SessionUser`) and the session use cases, plus result/error primitives and
  global value objects. Core logic is exposed only through use cases.
- `core:data` — app-level data implementations. Currently owns
  `DefaultSessionRepository` and `dataModule`.
- `feature:<name>` — vertical feature slice. Owns domain, data, presentation,
  UI, navigation, DI, resources, and tests for that feature. Current features:
  `feature:auth`, `feature:events`, `feature:feed`, `feature:profile`,
  `feature:tournaments`.
- `core:<infra>` — technical infrastructure without business knowledge:
  navigation engine, network engine, localization, testing utilities.
- `core:<capability>` — shared business capability used by two or more
  features, with a single owner.
- `layers` — app-level navigation layers. Owns `AuthLayer`, `GlobalLayer`, and
  the shared `LayerScene` / `LayerSceneStrategy`. There is no bottom bar.
- `build-logic` — Gradle convention plugins. Integrator-owned.
- `api/openapi.yaml` — shared API contract. Integrator-owned.

Package layout inside the UI-facing core modules:

```text
core/designsystem/.../designsystem/
  theme/    HackTheme, colour schemes, typography, shapes, previews
  mode/     AppThemeMode, ThemeModeCodec, ThemeController, ThemePreferences
  di/       designSystemModule

core/ui/.../core/ui/
  component/ HackTopBar, HackLoading, HackEmptyState, HackLabels,
             HackPrimaryButton, previews

core/localization/.../localization/
  language/ AppLanguage, LanguageController, AndroidLanguageController
  text/     UiText and its Compose/Context resolvers
  di/       localizationModule
```

Do not split these packages further until a second real concern appears;
small flat packages are preferred over one file per package.

## Roles and file ownership

Feature and core agents are module agents: each owns one module, its tests,
and the local commands for that module.

### Feature agent

If you are assigned `feature:<name>`, you may edit only:

- `feature/<name>/**`

Do not edit:

- `feature/<other>/**`
- `core/**`
- `app/**`
- `layers/**`
- `build-logic/**`
- `settings.gradle.kts`
- `app/build.gradle.kts`
- `gradle/libs.versions.toml`
- `api/openapi.yaml`
- `.gitignore`

Run only local commands for the module you own,
`:feature:<name>:testDebugUnitTest` and `:feature:<name>:compileDebugKotlin`.
Global builds, the full test suite, lint, and coverage are the integrator's
job.

If a change outside your area is required, stop and report the exact file,
line, and change needed. Do not make it yourself.

### Integrator agent

The integrator owns all shared files and performs the final wiring:

- adds the feature to `settings.gradle.kts`
- adds the project dependency in `app/build.gradle.kts`
- adds the feature Koin module in `App.kt`
- adds the module to the Kover aggregation in the root `build.gradle.kts`
- resolves dependency/catalog changes
- runs the full build before merging, alone, after the module agents have
  handed off and stopped building
- owns every global command and is the only role that runs one

## Architecture rules

### Code style

- No comments in code. No KDoc, no inline `//`, no section markers, no `TODO`
  comments. This applies to main and test sources.
- Names, types, and structure must carry the intent. If a rule needs an
  explanation, document it in this file, not in the source.
- Comments in files that predate this rule are legacy. Do not copy the pattern
  and do not add new ones.

### Feature slice

Use this package layout inside a feature:

```text
feature/<name>/src/main/kotlin/.../feature/<name>/
  data/          DTOs, mappers, API services, repository implementations
  domain/        models and repository interfaces (pure Kotlin, no use cases)
  presentation/  one subpackage per screen, ViewModel per screen
  store/         one subpackage per screen, Store contract and factory per screen
  ui/            one subpackage per screen (`<screen>/<Screen>Screen.kt`, `<screen>/component/`),
                 optional feature-level shared UI in `ui/component/`
  navigation/    routes and NavigationInstaller
  di/            Koin module
```

- Domain models must be pure Kotlin: no Android types, no `R`, no `UiText`.
- `UiText` belongs to presentation/store state, not domain models.
- UI must not contain business logic. Put it in the store or domain.
- No monolithic UI files. Never dump multiple Composable functions, cards, forms,
  pickers, banners, or dialogs into catch-all files (such as `*Components.kt`).
- Decompose UI into single-concern files: each distinct component, section, card,
  dialog, or sub-screen content view must live in its own dedicated file (one primary
  Composable per file).
- Screen UI structure: The screen entry Composable lives in
  `ui/<screen>/<ScreenName>Screen.kt`. Subcomponents specific to that screen go into
  `ui/<screen>/component/`, dialogs go into `ui/<screen>/dialog/` (or `ui/dialog/`),
  and components reused across multiple screens within the feature go into
  `ui/component/`.
- Keep one public Koin module per feature: `val <name>Module = module { ... }`.
- Use constructor injection. Do not use a service locator inside features.
- One screen owns exactly one route, one `ViewModel`, and one `Store`. Stores
  are per-screen and are never shared between screens. DI registers the store
  *factory* as a `single`; the `ViewModel` creates and disposes its own store.
- A feature exposes its business operations through its own repository. Stores
  call that repository directly. Do not add use case classes inside a feature:
  the logic is local, so the extra layer only adds indirection.
- `core:domain` is the exception. Any core repository is consumed through a
  narrow use case, one per operation, so a consumer never receives the whole
  repository API. Use cases live in `core:domain`; their DI registration lives
  in the module that owns the implementation (`core:data`).

### Feature isolation

- `feature:*` must not depend on another `feature:*`.
- Cross-feature contracts go through `core:<capability>` or a small API
  contract approved by the integrator.
- Do not share entities, DTOs, or stores between features by copying them
  into `core:domain`. Extract a `core:<capability>` only after two or more
  real usages.
- Do not share UI components by copying them between features. A composite
  component needed by two or more features moves to `core:ui`.
- `core:domain` stays small: result/error primitives, the session contract with
  its use cases, and truly global value objects only.

### Session and layers

- `core:domain` owns the `SessionRepository` contract, `SessionState`
  (`Unknown`, `Unauthenticated`, `Authenticated`), and the session use cases
  (`ObserveSessionUseCase`, `RestoreSessionUseCase`, `SaveSessionUseCase`,
  `ClearSessionUseCase`). `core:data` owns the persisted implementation and
  registers the use cases. Features and `app` consume the session through the
  use cases, never through `SessionRepository` directly.
- Features write the session; they never navigate across features to log in or
  out.
- `AppRootScreen` is the session gate: `Unknown` shows a splash,
  `Unauthenticated` shows the auth nav host, `Authenticated` shows the global
  nav host. The gate re-keys the host so login and logout reset the back stack.
- `Authentication` starts at `AuthLoginRoute`; the authenticated app starts at
  `FeedRoute`.
- Screens register a layer with `AuthLayerMeta.entry()` or
  `GlobalLayerMeta.entry()`. `layers` renders the matching `LayerScene`.
- There is no bottom bar and no tab concept. Do not reintroduce one without an
  explicit request.

### Navigation

- New routes belong to the feature that owns the screen:
  `feature/<name>/navigation/Routes.kt`.
- Register screens through `NavigationInstaller`.
- Do not make a feature depend on another feature just to navigate. Expose the
  destination through `AppNavigation` / `LocalAppNavigation` in
  `core:navigation` and implement it in `app`, where all feature routes are
  visible.
- Each NavEntry receives its own `ViewModelStore` through
  `rememberViewModelStoreNavEntryDecorator`. Keep it in `AppRootScreen` so
  per-screen ViewModels never share the Activity scope.

### Networking

- `core:network` owns transport, auth, and `HttpClient` factories only.
- Do not add feature endpoints or DTOs to `core:network`.
- Inject `HttpClient` into feature data sources. Do not create clients inside
  a feature unless the integrator approves it.
- Use `safeCall { ... }` and `NetworkResult` for network calls.
- Feature DTOs and API services live in `feature/<name>/data/`.
- Do not edit generated OpenAPI code under `build/generated/`.

### Localization and UI

- Feature strings stay in `feature/<name>/src/main/res/values/`.
- Shared strings go to `core:localization`.
- Use `UiText` for user-visible text in presentation.
- Use Material 3 components and the existing app theme.
- Do not add a new theme, color palette, or typography scale without the
  integrator's approval.
- Do not duplicate a shared component. If it is needed by two features, move
  it to `core:ui` instead.
- Components in `core:ui` take plain `String` text and callbacks, not
  `UiText`, `@StringRes`, domain models, or navigation. The caller owns
  localization and wiring.
- Decompose screens into small, single-purpose components in dedicated files
  under `ui/<screen>/component/`. Never bundle unrelated widgets into large
  multi-component files.

### Design system and theming

- `core:designsystem` owns the single app theme. Screens render inside
  `HackTheme`; features never wrap content in their own `MaterialTheme`.
- Read colours from `MaterialTheme.colorScheme`, text styles from
  `MaterialTheme.typography`, corners from `MaterialTheme.shapes`. Do not
  hardcode `Color(0xFF...)` in a feature.
- Tokens Material 3 does not model (glow, danger, success, gridLine) come from
  `MaterialTheme.hack` (`HackExtras`).
- Technical text uses `HackMonoTextStyle`; countdowns and big numbers use
  `HackTimerTextStyle`. Both live in `core:designsystem`.
- Do not add a second palette, typography scale, or `Shapes` block inside a
  feature. Report the missing token instead.
- Do not enable Material `dynamicColor` (Monet): it overwrites the brand.
- Theme selection goes through `ThemeController.setMode()`. `AppThemeMode` is
  pure Kotlin; the persisted string format belongs to `ThemeModeCodec`.
  Features must not read or write the storage key directly.
- Switching the theme is a recomposition, not an `Activity` recreation. A
  feature that needs the current mode collects `ThemeController.current`.
- Previews must pass the mode explicitly:
  `HackTheme(mode = AppThemeMode.DARK) { ... }`.
- Regenerating the palette is a two-block edit in `HackColorScheme.kt`; the
  default mode is the single `DefaultThemeMode` constant in `AppThemeMode.kt`.

### Shared UI components

- `core:designsystem` holds design tokens and the theme only.
- `core:ui` holds reusable composite Composables shared by two or more
  features. Keep feature-specific components in `feature/<name>/ui/` until a
  second real consumer appears.
- Shared components must not depend on feature modules, business logic,
  routes, ViewModels, stores, or repositories.
- Every shared component must have a preview that passes
  `HackTheme(mode = AppThemeMode.DARK)` and `AppThemeMode.LIGHT`.
- Place one concern per file in `component/`. Do not create a package per
  component.
- `core:ui` may depend on `core:designsystem` and Compose only. Any other
  dependency needs the integrator's approval.
- A feature consumes shared components with
  `implementation(project(":core:ui"))`.

### Dependencies

- Do not add a dependency without the integrator's approval.
- Do not edit `gradle/libs.versions.toml` or `build-logic/**` as a feature
  agent.
- Prefer existing libraries and the `android.dev.feature` convention plugin.

## Integration checklist for a new feature

A feature agent must hand off:

1. The exact module name, for example `:feature:search`.
2. The `build.gradle.kts` with `id("android.dev.feature")` and the namespace.
3. The Koin module name and the DI file path.
4. The `NavigationInstaller` and the navigation layer (`auth` or `global`).
5. The routes it owns.
6. The module test command that passes, for example
   `./gradlew :feature:<name>:testDebugUnitTest`.
7. Any shared contract or dependency the integrator must add.

The integrator then adds the registration lines (`settings.gradle.kts`,
`app/build.gradle.kts`, `App.kt`, and `kover(project(...))` in the root
`build.gradle.kts`) and runs the full build once the module agents have
stopped building.

## Current known deviations

Do not copy these patterns into new code:

- `AppEventBus` lives in `core:navigation/event`. Use it only for global UI
  events (snackbar/toast), never for business data.
- The launch window background is always the dark brand colour
  (`@color/hack_window_background`). With a light theme selected the app may
  flash dark for one frame at startup. Do not "fix" it by hardcoding a light
  window background; the correct fix is the SplashScreen API reading the
  stored mode before `setContent`.
- `MainDispatcherRule` is duplicated in each feature. Keep it local for now;
  do not extract it without the integrator's approval.
- `feature/home` build artifacts may remain locally after the module was
  removed; the directory is git-ignored. Do not re-add the module.

## Testing

- Store tests use `DefaultStoreFactory`, Turbine, and the feature-local
  `MainDispatcherRule`.
- Network tests use Ktor `MockEngine`.
- Test business logic and store behavior, not Compose rendering, unless the
  task explicitly asks for UI tests.
- A feature is not done until its module tests pass. Run the module task
  (`:feature:<name>:testDebugUnitTest`), not the full suite; the integrator
  runs the full suite before merging.
- Coverage uses Kover and is aggregated in the root project. Composable UI,
  DI modules, `BuildConfig`, palette files, and generated OpenAPI code are
  excluded on purpose, so the percentage reflects testable logic.
- A new module is only counted after the integrator adds
  `kover(project(":module"))` to the root `build.gradle.kts`. Instrumentation
  itself is automatic through the `android.dev.*` convention plugins.

## Definition of done

Before reporting completion:

- `./gradlew :<module>:testDebugUnitTest` passes for the module you changed.
- For a module agent: only module-scoped commands were run. Global build,
  full test suite, lint, and coverage runs belong to the integrator; do not
  run them as a sanity check.
- For the integrator: the full build passes after the module agents stopped
  building.
- No files outside the allowed paths were changed.
- No new dependency was added without approval.
- No generated file was edited manually.
- No secret, keystore, build output, or local properties file was added.
- Other agents' worktrees, `build/` directories, and uncommitted changes were
  left untouched.
- The report lists the commands run, whether each was local or global, and
  their results.

## Conflict policy

- If instructions conflict, stop and ask. Do not guess.
- If a required change touches an integrator-owned file, report it instead of
  editing it.
- Do not refactor unrelated code, rename public APIs, or fix legacy issues
  globally unless the task explicitly asks for it.
- Keep changes small and local to the assigned feature.
- If a file changes unexpectedly, assume another agent is working and stop
  before overwriting it. Do not resolve a cross-agent edit with a reset or a
  force checkout.
- If a local command fails because of a concurrent build or edit, do not
  escalate to a global command or a clean. Report the collision.
