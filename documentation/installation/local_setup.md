## Configuration

### Environment Variables

This application uses MicroProfile Config to load all sensitive settings from environment variables. In your `src/main/resources/application.properties`, you should refer to:

```properties
quarkus.datasource.jdbc.url=${DB_URL}
quarkus.datasource.username=${DB_USERNAME}
quarkus.datasource.password=${DB_PASSWORD}
```

At runtime, Quarkus will resolve these ${…} placeholders from your environment.

#### Local Setup: Using a .env File
To simplify running the project locally, you can create a file named .env in the project root:

```
DB_URL=jdbc:postgresql://localhost:5432/myapp
DB_USERNAME=your_local_username
DB_PASSWORD=your_local_password
```

#### Production / CI-CD: Using Secrets
Never commit real credentials to the repository. Instead:

CI Pipelines (GitHub Actions, GitLab CI, etc.): configure DB_URL, DB_USERNAME and DB_PASSWORD as pipeline secrets.

Kubernetes: create Kubernetes Secrets and mount them as environment variables or files.

For more info : https://quarkus.io/guides/config-reference
