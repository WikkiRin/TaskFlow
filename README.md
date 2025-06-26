## TaskFlow
**TaskFlow** — это проект мини-Jira, реализованный на Kotlin + Spring Boot.  
Поддерживает создание досок, задач, пользователей и авторизацию через JWT.  
Проект разработан с фокусом на чистую архитектуру и покрытие тестами.

## Стек технологий

-  Kotlin 1.9
-  Spring Boot 3.5
-  Spring Security + JWT
-  PostgreSQL + Flyway
-  Testcontainers (PostgreSQL)
-  MockMvc + JUnit5 + MockK

---
##  Запуск проекта

### Требования

- Docker (для тестов с Testcontainers)
- Java 17
- Gradle (или `./gradlew`)

Порт приложения
````
server: 
  port: 8080
````

Swagger доступен по адресу
````
http://localhost:8080/swagger-ui/index.html
````
