# Бэкенд веб-приложения полублога

## Описание

Бэкенд веб-приложения полублога, написанный на **Java 21** с использованием **Spring Boot 3.5.9**. Приложение предоставляет **REST API** для управления постами и комментариями со встроенным сервером.

## Технологии

- **Java 21**
- **Spring Boot 3.5.9**
- **Spring Web MVC**
- **Spring Data JDBC**
- **H2 Database** (in-memory, с консолью)
- **Gradle**
- **Lombok**
- **JUnit 5**

---

## Сборка и запуск

### Сборка проекта

```bash
./gradlew build
```

### Создание исполняемого JAR

```bash
./gradlew bootJar
```

JAR-файл будет создан в папке: `build/libs/my-blog-back-app-0.0.1-SNAPSHOT.jar`

### Запуск приложения

#### Для разработки (с перезагрузкой):
```bash
./gradlew bootRun
```

#### Из готового JAR-файла:
```bash
java -jar build/libs/my-blog-back-app-0.0.1-SNAPSHOT.jar
```

#### С кастомными настройками:
```bash
java -jar my-blog-back-app-0.0.1-SNAPSHOT.jar --server.port=8081
```

### Запуск тестов

```bash
./gradlew test
```

---

## Доступ к приложению

После запуска приложение доступно по адресам:

   ```
   http://localhost:8080/my-blog-back-app/
   http://localhost:8080/my-blog-back-app/api/posts?search=&pageNumber=1&pageSize=10
   ```

## Конфигурация

Основные настройки в `src/main/resources/application.yml`:

- Работа с файлами до 10MB
- Автоматическое создание таблиц из SQL-скриптов