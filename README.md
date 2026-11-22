# my-blog-backend-app

Бэкенд приложения-блога для третьего спринта курса Яндекс Практикума. Написан на Java 21 и Spring Framework 6.2 без Spring Boot. Приложение собирается в WAR и запускается в сервлет-контейнере Apache Tomcat.

## Возможности

- Создание, просмотр, редактирование и удаление постов с текстом в формате .md
- Загрузка и получение картинки поста
- Добавление, просмотр, редактирование и удаление комментариев
- Добавление лайков
- Поиск по подстроке в заголовке и тегам, пагинация ленты
- Проверка входных данных и ответы об ошибках в формате json

## Технологии и структура

Используются Spring MVC, Spring Data JDBC, H2, Jackson, JUnit 5 и Spring TestContext. Точные версии зависимостей указаны в [pom.xml](pom.xml)

| Пакет | Назначение |
| --- | --- |
| `controller` | REST-эндпоинты и обработка ошибок |
| `service` | Бизнес-правила, валидация и транзакции |
| `repository` | Spring Data JDBC-репозитории и DAO с SQL-запросами |
| `model` | Модели хранения постов, комментариев, тегов и изображений |
| `dto` | Объекты запросов и ответов API |
| `config` | Настройка Spring MVC, CORS, загрузки файлов и базы данных |
| `exception` | Исключения приложения |

Spring-контекст и `DispatcherServlet` запускаются через [web.xml](src/main/webapp/WEB-INF/web.xml)
Схема базы данных создаётся автоматически из [schema.sql](src/main/resources/schema.sql)

## Требования к окружению

- И Maven, и Tomcat должны использовать версию Java 21
- Maven Wrapper в репозитории отсутствует, нужен установленный mvn
- Apache Tomcat 10.1.x с поддержкой Jakarta Servlet 6.0
- Docker с Compose v2 — для запуска готового фронтенда из задания

## Сборка и тесты

Команды ниже предназначены для Bash/Zsh на macOS и Linux и выполняются из корня репозитория, если явно не указано другое. Полная проверка с чистой сборкой, запуском тестов и созданием WAR:

```bash
mvn clean verify
```

Результат сборки: `target/my-blog-backend-app-1.0-SNAPSHOT.war`. В проекте есть следующие проверки:

- `SearchCriteriaTest` — разбор поисковой строки и сокращение текста для ленты
- `PostRepositoryIntegrationTest` — сохранение данных через Spring Data JDBC и каскадное удаление комментариев
- `PostServiceIntegrationTest` — бизнес-правила, поиск и пагинация с реальной H2
- `PostControllerMvcIntegrationTest` — HTTP-контракт через MockMvc: посты, комментарии, картинки, лайки, ошибки и CORS

Интеграционные тесты использует Spring TestContext и встроенную H2. Совместимые конфигурации контекста кешируются между тестами, изменения данных  в транзакционных тестах откатываются. Отдельно запускать Tomcat, Docker или сервер базы данных для `mvn test` не требуется.

## Развёртывание в Tomcat

Скопируйте WAR под именем `ROOT.war`:

   ```bash
   cp target/my-blog-backend-app-1.0-SNAPSHOT.war "$CATALINA_BASE/webapps/ROOT.war"
   ```

Проверьте, что HTTP-коннектор в `$CATALINA_BASE/conf/server.xml` использует порт `8080` и этот порт свободен. Запустите Tomcat в текущем терминале:

   ```bash
   "$CATALINA_HOME/bin/catalina.sh" run
   ```

Имя `ROOT.war` задаёт корневой контекст: API будет доступен по `http://localhost:8080/api/...` без дополнительного префикса имени приложения.  В другом терминале можно проверить запуск:

```bash
curl -i http://localhost:8080/api/hello
curl -i 'http://localhost:8080/api/posts?search=&pageNumber=1&pageSize=5'
```

Оба запроса должны вернуть `200 OK`. Сразу после запуска лента пуста:

```json
{"posts":[],"hasPrev":false,"hasNext":false,"lastPage":1}
```

Бэкенд предоставляет api; пользовательский интерфейс запускается отдельно на `http://localhost/`

## База данных

H2 работает в памяти процесса приложения. При запуске создаются таблицы `posts`, `comments`, `tags`, `post_tags` и `post_images`. Картинки хранятся в базе как массивы байт. **После перезапуска или повторного развёртывания приложения все данные теряются.** Это относится к постам, комментариям, тегам, лайкам и изображениям. При удалении поста каскадно удаляются его комментарии, картинка и связи с тегами.

## Запуск фронтенда

Готовый фронтенд поставляется отдельным архивом в задании Практикума и не входит в этот репозиторий. Его можно распаковать рядом с бэкендом так, чтобы структура каталогов была следующей:

```text
рабочий-каталог/
├── my-blog-backend-app/
│   └── docker-compose.frontend.yaml
└── my-blog-front-app/
    ├── Dockerfile
    ├── nginx.conf
    └── dist/
```

Для просмотра надо запустить Docker и бэкенд в Tomcat, затем из корня репозитория бэкенда выполнить:

```bash
docker compose -f docker-compose.frontend.yaml up -d --build
docker compose -f docker-compose.frontend.yaml ps
```

Compose собирает фронтенд из `../my-blog-front-app` и публикует порт `80`. Откройте `http://localhost/`. Браузер обращается к API на `http://localhost:8080`; CORS настроен для `localhost` и `127.0.0.1` на порту 80. В интерфейсе можно добавить пост с картинкой, текстом Markdown и тегами, открыть его, поставить лайки, добавить комментарии, отредактировать или удалить пост и комментарии. В ленте доступны поиск и переключение страниц. Остановить фронтенд:

```bash
docker compose -f docker-compose.frontend.yaml down
```

## Использование API

Тела запросов и ответов используют json, кроме загрузки и получения изображений. Авторизация не требуется. Пример создания поста:

```bash
curl -i -X POST http://localhost:8080/api/posts \
  -H 'Content-Type: application/json' \
  -d '{"title":"Первый пост","text":"Текст **Markdown**","tags":["java","spring"]}'
```

В ответе вернётся созданный пост с `id`, `title`, `text`, `tags`, `likesCount: 0` и `commentsCount: 0`. В следующих примерах замените `1` в URL и JSON на полученный `id`. Редактирование поста:

```bash
curl -i -X PUT http://localhost:8080/api/posts/1 \
  -H 'Content-Type: application/json' \
  -d '{"id":1,"title":"Обновлённый пост","text":"Новый текст","tags":["java"]}'
```

Добавление комментария и лайка:

```bash
curl -i -X POST http://localhost:8080/api/posts/1/comments \
  -H 'Content-Type: application/json' \
  -d '{"text":"Первый комментарий","postId":1}'

curl -i -X POST http://localhost:8080/api/posts/1/likes
```

Загрузка картинки — укажите путь к существующему PNG-файлу:

```bash
curl -i -X PUT http://localhost:8080/api/posts/1/image \
  -F 'image=@/path/to/photo.png;type=image/png'
curl -f http://localhost:8080/api/posts/1/image -o downloaded-image.png
```

Поддерживаются JPEG, PNG, GIF и WebP размером до 10 МиБ. Основные эндпоинты:

| Метод | Путь | Действие |
| --- | --- | --- |
| `GET` | `/api/posts?search=&pageNumber=1&pageSize=5` | Получить ленту |
| `POST` | `/api/posts` | Создать пост |
| `GET`, `POST` | `/api/posts/{id}` | Получить полный пост; `POST` поддерживается по контракту задания |
| `PUT` | `/api/posts/{id}` | Отредактировать пост |
| `DELETE` | `/api/posts/{id}` | Удалить пост |
| `POST` | `/api/posts/{id}/likes` | Добавить лайк и получить новое значение счётчика |
| `PUT`, `GET` | `/api/posts/{id}/image` | Загрузить или получить картинку |
| `GET`, `POST` | `/api/posts/{postId}/comments` | Получить комментарии или создать комментарий |
| `GET`, `PUT`, `DELETE` | `/api/posts/{postId}/comments/{id}` | Получить, изменить или удалить комментарий |

При редактировании поста обязательны `id`, `title`, `text`, `tags`.  При создании комментария обязательны `text` и `postId`, при редактировании — также `id`. Идентификаторы в JSON должны совпадать с соответствующими идентификаторами в URL. Посты отсортированы по убыванию ID. В ленте текст длиннее 128 символов сокращается до 128 символов с добавлением `…`; отдельный пост возвращается с полным текстом. Ответ ленты содержит `posts`, `hasPrev`, `hasNext` и `lastPage`.