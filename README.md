# User Service

`user-service` — микросервис управления пользователями платформы Rent Platform. 
Отвечает за регистрацию, аутентификацию, выпуск JWT, работу с refresh-сессиями, управление ролями, 
личный кабинет пользователя, базовые операции профиля, блокировку пользователей и платёжный профиль.

## Основной функционал

- регистрация пользователя
- вход по телефону или email
- выпуск `access token` и `refresh token`
- обновление access token через refresh token
- выход из системы (отзыв refresh-сессии)
- получение текущего профиля пользователя
- получение и обновление профиля пользователя
- смена пароля
- мягкое удаление аккаунта (`soft delete`)
- публичный профиль пользователя (никнейм, аватар, рейтинг)
- управление ролями: `user`, `moderator`, `admin`, `super_admin`
- блокировка / разблокировка пользователей
- платёжный профиль (customerId и paymentMethodId ЮKassa)
- хранение refresh-сессий в БД
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
- MapStruct
- Lombok
- RestClient
- SpringDoc OpenAPI / Swagger UI
- Docker

---

## Ports

| Service      | Port |
|-------------|------|
| Gateway     | 8080 |
| User        | 8081 |
| Catalog     | 8082 |
| Deal-Payment | 8083 |
| Communication | 8084 |

---

## Архитектура пакетов

Проект разделён на слои:

- `api` — контроллеры, DTO, обработчики ошибок
- `core` — бизнес-логика, сервисы, сущности, репозитории, мапперы
- `config` — конфигурация безопасности, JWT, Swagger, scheduler properties

## Base URL

Через gateway:
- /api/auth
-  /api/users
- /api/admin
- /api/users/me/billing

---

## Domain Model

### User

- id (UUID)
- email, phone
- passwordHash
- fullName, nickname
- avatarUrl, bio
- role: `user` | `moderator` | `admin` | `super_admin`
- isActive
- blockedAt, blockedBy, blockedReason
- lastLoginAt
- createdAt, updatedAt, deletedAt

### Session

- id (UUID)
- userId
- refreshTokenHash
- deviceInfo
- expiresAt, revokedAt, createdAt

### UserBillingProfile

- id (UUID)
- userId (unique)
- customerId (ЮKassa)
- defaultPaymentMethodId
- createdAt, updatedAt

---

## Роли и иерархия

| Роль         | Может назначать         | Может блокировать      |
|-------------|------------------------|------------------------|
| user        | —                      | —                      |
| moderator   | —                      | user                   |
| admin       | moderator              | moderator, user         |
| super_admin | admin, moderator       | admin, moderator, user |

- `super_admin` назначается вручную через БД
- Нельзя изменить роль самому себе
- Нельзя изменить роль равного или старшего по рангу
- `admin` может назначить только `moderator`
- `super_admin` может назначить `admin` и `moderator`

---

## Аутентификация и безопасность

### Access token

- короткоживущий JWT (по умолчанию 1200 секунд)
- содержит: `sub`, `nickname`, `role`
- проверяется gateway и downstream-сервисами

### Refresh token

- хранится в таблице `sessions` в виде хэша
- используется для обновления access token
- срок жизни зависит от флага `rememberMe`:
    - `false` — 86400 секунд (1 день)
    - `true` — 2592000 секунд (30 дней)

### User-Agent

Информация об устройстве считывается из заголовка `User-Agent` и сохраняется в `sessions.device_info`.

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

## Переменные окружения

| Переменная               | Описание                  | По умолчанию |
|--------------------------|--------------------------|-------------|
| PG_HOST                  | PostgreSQL хост           | localhost   |
| PG_PORT                  | PostgreSQL порт           | 5433        |
| PG_DATABASE              | Имя БД                   | user_db     |
| PG_USER                  | Пользователь БД           | postgres    |
| PG_PASSWORD              | Пароль БД                | 12345       |

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

####  `GET /api/users/{userId}/public`
Публичный профиль (без авторизации).

#### `GET /api/users/test`
Тестовый endpoint для проверки маршрутизации.

### Admin Endpoints

#### `PUT /api/admin/users/{userId}/role`
Назначение роли пользователю. Требуется `admin` или `super_admin`.

#### `PUT /api/admin/users/{userId}/block`
Блокировка пользователя.

#### `PUT /api/admin/users/{userId}/unblock`
Разблокировка пользователя.

### Billing Profile Endpoints
#### `GET /api/users/me/billing`
Получение платёжного профиля текущего пользователя.


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
---

## Error Handling

### 400 Bad Request

- validation errors
- passwords do not match
- user already has this role
- cannot block yourself

### 401 Unauthorized

- invalid credentials (логин/пароль)
- refresh token expired or revoked
- account is inactive/blocked

### 403 Forbidden

- access denied (недостаточно прав для смены роли/блокировки)
- cannot change role of user with equal or higher rank

### 404 Not Found

- user not found

### 409 Conflict

- user with this phone already exists
- user with this email already exists
- user with this nickname already exists

### 500 Internal Server Error

- internal server error

---

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

Команда для запуска сборки проекта и докера
```bash
./gradlew build -x test
docker compose up --build
```

## MVP Features
- Регистрация / вход / обновление токенов / выход

- Профиль пользователя (просмотр, обновление, удаление)

- Смена пароля

- Публичный профиль с рейтингом

- Роли: user, moderator, admin, super_admin

- Иерархическое управление ролями

- Блокировка / разблокировка пользователей

- Refresh-сессии с remember-me

- Фоновая очистка истёкших сессий

- Платёжный профиль (ЮKassa)

- Интеграция с deal-payment-service