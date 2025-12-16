# Prode

An easy-to-use and powerful Android logging library with a fluent tag API.
Prode helps you write clean, readable logs with support for trace logging,
JSON formatting, and optional file persistence.

---

## 🌟 Features

- **Simple API**: Minimal setup with intuitive method calls.
- **Default Tag Support**: Set a global default tag for all logs.
- **Fluent Tag API**: Chain custom tags using `tag()` for clean and readable logging.
- **Log Levels**: Supports `DEBUG`, `INFO`, `WARN`, `ERROR`, and `TRACE`.
- **JSON Logging**: Pretty-print JSON strings or objects for better readability.
- **File Logging**: Optionally persist logs to a file for debugging.
- **Flexible Logging Control**: Enable or disable logging dynamically using `isLoggingEnabled`.

---

## 🚀 Installation

```gradle
dependencies {
    implementation "com.github.chani01:Prode:<latest_version>"
}
```

Sync your project after adding the dependency.

---

## 📖 Usage

### Initialization

Initialize `Prode` in your `Application` class or any entry point:

```kotlin
Prode.init(
    defaultTag = "MyAppTag",
    isLoggingEnabled = BuildConfig.DEBUG,
    logFileName = "app_logs.txt" // optional
)
```

#### Parameters
- **`defaultTag`**: Default tag used when no custom tag is provided.
- **`isLoggingEnabled`**: Enables or disables logging dynamically.
- **`logFileName`**: (Optional) File name for saving logs.

---

### Basic Logging

```kotlin
Prode.d("This is a debug message")
Prode.i("This is an info message")
Prode.w("This is a warning")
Prode.e("This is an error message")
Prode.t("This is a trace log")
```

---

### Custom Tag Logging

Use the fluent `tag()` API to override the default tag per log:

```kotlin
Prode.tag("MainActivity").d("Activity started")
Prode.tag("Network").e("API request failed")
```

If no tag is specified, the default tag is used.

---

### JSON Logging

Prode supports logging JSON content in a readable, pretty-printed format.

```kotlin
val jsonString = """
{
  "id": 1,
  "name": "Prode",
  "enabled": true
}
"""

Prode.json(jsonString)
Prode.tag("Response").json(jsonString)
```

JSON output is automatically formatted for improved readability in Logcat.

---

## 🛠 Configuration Example

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Prode.init(
            defaultTag = "MyAppTag",
            isLoggingEnabled = BuildConfig.DEBUG,
            logFileName = "app_logs.txt"
        )
    }
}
```

---

## 🛡️ Logging in Release Builds

For release builds, logging should be disabled for performance and security.

- **Debug builds**: Logging enabled.
- **Release builds**: Logging disabled by setting `isLoggingEnabled = false`.

---

## 📄 License

This project is licensed under the MIT License.
See the [LICENSE](LICENSE) file for details.

---

## 🙌 Contributions

Contributions are welcome!
Feel free to open issues, submit pull requests, or suggest improvements.
