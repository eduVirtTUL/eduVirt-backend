| ENV                             | Description                                      | Default                            |
|---------------------------------|--------------------------------------------------|------------------------------------|
| HOST                            | Hostname of the server that application runs on. | -                                  |
| MAIL_HOST                       |                                                  |                                    |
| MAIL_PORT                       |                                                  |                                    |
| MAIL_USERNAME                   |                                                  |                                    |
| MAIL_PASSWORD                   |                                                  |                                    |
| MAIL_SENDER                     |                                                  |                                    |
| MAIL_PROTOCOL                   |                                                  | smtp                               |
| MAIL_PROPERTIES_ENABLE_AUTH     |                                                  | true                               |
| MAIL_PROPERTIES_STARTTLS        |                                                  | true                               |
| MAIL_PROPERTIES_SSL_ENABLE      |                                                  | true                               |
| MAIL_PROPERTIES_SSL_TRUST       |                                                  | value of $HOST                     |
| ISSUER_URI                      |                                                  |                                    |
| EDUVIRT_DB_URL                  |                                                  |                                    |
| EDUVIRT_DB_USERNAME             |                                                  |                                    |
| EDUVIRT_DB_PASSWORD             |                                                  |                                    |
| EDUVIRT_DB_DRIVER_CLASS         |                                                  | org.postgresql.Driver              |
| LIQUIBASE_USER                  |                                                  |                                    |
| LIQUIBASE_PASSWORD              |                                                  |                                    |
| ENGINE_URL                      |                                                  |                                    |
| ENGINE_USERNAME                 |                                                  |                                    |
| ENGINE_PASSWORD                 |                                                  |                                    |
| JKS_FILE                        |                                                  | ovirte-tst-keystore.jks            |
| JKS_PASSWORD                    |                                                  | password                           |
| KEYCLOAK_URL                    |                                                  |                                    |
| KEYCLOAK_REALM                  |                                                  |                                    |
| KEYCLOAK_PROTOCOL               |                                                  |                                    |
| KEYCLOAK_REDIRECT_LOGIN_URI     |                                                  |                                    |
| KEYCLOAK_REDIRECT_LOGOUT_URI    |                                                  |                                    |
| KEYCLOAK_CLIENT_ID              |                                                  |                                    |
| KEYCLOAK_CLIENT_SECRET          |                                                  |                                    |
| DEFAULT_MAIL_TIMEZONE           |                                                  | CET                                |
| DEFAULT_MAIL_LANGUAGE           |                                                  | pl                                 |
| FRONTEND_LOGIN                  |                                                  |                                    |
| FRONTEND_CALLBACK               |                                                  |                                    |
| MAINTENANCE_INTERVAL_MIN_AHEAD  |                                                  |                                    |
| RESERVATION_WINDOW_LENGTH       |                                                  | 15                                 |
| RESOURCE_WARNING_MAILS_ENABLED  |                                                  | true                               |
| JWS_SECRET                      |                                                  |                                    |
| EXECUTOR_TASK_TIME_TOLERANCE    |                                                  | 3                                  |
| EXECUTOR_VM_SHUTDOWN_GRACE_TIME |                                                  | 2                                  |
| FRONTEND_RESERVATIONS_VIEW      |                                                  | http://localhost:5173/reservations |