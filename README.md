# User Service

`user-service` — микросервис управления пользователями платформы Rent Platform. Он отвечает за регистрацию, аутентификацию, выпуск JWT, работу с refresh-сессиями, личный кабинет пользователя и базовые операции профиля.

## Основной функционал

- регистрация пользователя
- вход по телефону или email
- выпуск `access token` и `refresh token`
- обновление access token через refresh token
- выход из системы
- получение текущего профиля пользователя
- обновление профиля пользователя
- смена пароля
- мягкое удаление аккаунта (`soft delete`)
- хранение refresh-сессий в базе данных
- фоновая очистка истёкших и отозванных сессий

## Технологии

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Security
- Spring OAuth2 Resource Server
- Spring Data JPA
- Hibernate
- PostgreSQL
- Flyway
- Lombok
- SpringDoc OpenAPI / Swagger UI
- Docker
- Docker Compose

## Архитектура пакетов

Проект разделён на слои:

- `api` — контроллеры, DTO, обработчики ошибок
- `core` — бизнес-логика, сервисы, сущности, репозитории, мапперы
- `config` — конфигурация безопасности, JWT, Swagger, scheduler properties

## Аутентификация и безопасность

В сервисе используется JWT-аутентификация.

### Access token

- короткоживущий токен
- используется для доступа к защищённым endpoint'ам
- проверяется `gateway-service`

### Refresh token

- хранится в таблице `sessions` в виде хэша
- используется для обновления access token
- может иметь разный срок жизни в зависимости от `rememberMe`

### Remember Me

При логине фронтенд передаёт флаг `rememberMe`:

- `false` — короткий срок жизни refresh token
- `true` — длинный срок жизни refresh token

### User-Agent

Информация об устройстве пользователя не передаётся в теле запроса. Она считывается на backend из заголовка `User-Agent` и сохраняется в `sessions.device_info`.

## Работа с пользователями

### Регистрация

При регистрации создаётся новый пользователь с обязательными полями:

- `phone`
- `password`
- `confirmPassword`
- `nickname`

Поле `fullName` при создании аккаунта заполняется значением `nickname`, после чего пользователь может изменить его в личном кабинете.

### Логин

Пользователь может входить:

- по телефону
- по email

Если email ещё не указан в профиле, вход доступен только по телефону.

### Профиль

Пользователь может:

- получить свой профиль
- обновить `fullName`, `email`, `bio`, `avatarUrl`
- сменить пароль
- удалить свой аккаунт

## Soft Delete

Удаление пользователя реализовано как мягкое удаление:

- выставляется `deletedAt`
- `isActive` переводится в `false`
- все активные refresh-сессии пользователя отзываются

## Очистка сессий

В проекте реализован scheduler, который периодически:

- удаляет истёкшие сессии
- удаляет старые отозванные сессии

## Конфигурация

### Основные переменные окружения

- `PG_HOST`
- `PG_PORT`
- `PG_DATABASE`
- `PG_USER`
- `PG_PASSWORD`

### JWT настройки

- `security.jwt.access-token-expiration-seconds`
- `security.jwt.refresh-token-short-expiration-seconds`
- `security.jwt.refresh-token-remember-me-expiration-seconds`

## Профили запуска

### Локальный запуск

Используется `application.yaml`:

- `gateway-service` обычно доступен на `8080`
- `user-service` обычно доступен на `8081`

### Docker-запуск

Используется профиль `docker` и `application-docker.yaml`:

- `gateway-service` проброшен на `8180`
- `user-service` проброшен на `8181`

## REST API

### Auth endpoints

#### `POST /api/auth/register`
Регистрация нового пользователя.

#### `POST /api/auth/login`
Вход пользователя и получение пары токенов.

#### `POST /api/auth/refresh`
Обновление access token по refresh token.

#### `POST /api/auth/logout`
Выход пользователя и отзыв refresh token.

### User endpoints

#### `GET /api/users/me`
Получение текущего пользователя.

#### `PUT /api/users/me`
Обновление профиля пользователя.

#### `PUT /api/users/me/password`
Смена пароля текущего пользователя.

#### `DELETE /api/users/me`
Мягкое удаление аккаунта текущего пользователя.

#### `GET /api/users/{id}`
Получение пользователя по `id`.

#### `GET /api/users/test`
Тестовый endpoint для проверки маршрутизации.

## Пример логина

```json
{
  "login": "+79990001122",
  "password": "StrongPass123",
  "rememberMe": true
}
```

## Пример обновления профиля

```json
{
  "fullName": "Example User",
  "email": "user@example.com",
  "bio": "I am using Rent Platform",
  "avatarUrl": "https://example.com/avatar.jpg"
}
```

## Пример смены пароля

```json
{
  "currentPassword": "OldPass123",
  "newPassword": "NewPass123",
  "confirmNewPassword": "NewPass123"
}
```

## Swagger

Swagger UI доступен в зависимости от режима запуска:

- локально: `http://localhost:8081/swagger-ui.html`
- через Docker: `http://localhost:8181/swagger-ui.html`

В Swagger настроен выбор серверов для отправки запросов напрямую в `user-service` или через `gateway-service`.

## Docker

Для `user-service` используется Dockerfile, который:

- копирует собранный jar-файл
- копирует SQL-миграции Flyway
- запускает приложение внутри контейнера
