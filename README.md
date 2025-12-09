# Бэкенд веб-приложения полублога

## Описание

Бэкенд веб-приложения полублога, написанный на **Java 21** с использованием **Spring Framework 6.2.1**.
Приложение предоставляет **REST API** для управления постами и комментариями.

## Технологии

- **Java 21**
- **Spring Framework 6.1.14**
- **Spring MVC**
- **Spring JDBC**
- **H2 Database** (in-memory)
- **Maven**
- **Lombok**
- **JUnit 5**

---

## Сборка и запуск

### Запуск тестов

```bash
mvn test
```

### Сборка проекта

```bash
mvn clean package
```

### Размещение в Tomcat

После успешной сборки:

1. В папке `target` появится файл `my-blog-back-app.war`.
2. Скопируйте этот файл в директорию `webapps` вашего **Tomcat**.
3. Запустите/перезапустите Tomcat
4. Приложение будет доступно по адресу:
   ```
   http://localhost:8080/my-blog-back-app/
   http://localhost:8080/my-blog-back-app/api/posts?search=&pageNumber=1&pageSize=10
   ```
