This project is a sample project of an application with an Angular 19.x front end and a Spring Boot 3.x backend targeting Java 17. It is designed to demonstrate the integration of these technologies.

The application includes a simple user interface that allows users to interact with the backend services. The backend is built using Spring Boot, providing RESTful APIs for the frontend to consume.

MS Entra External ID is used for user authentication and authorization, allowing users to securely access the application with the backend SSO security provider.  Database authentication is also implemented using a DatabaseSecurityProvider, which allows users to authenticate against a database.

## Folder Structure
- `backend/`: Contains the Spring Boot backend code.
- `frontend/`: Contains the Angular 19 frontend code.   
- `.github/`: Contains GitHub Copilot instructions for both backend and frontend.
- `.github/copilot-instructions.md`: Contains general instructions for GitHub Copilot.
- `.github/instructions/backend-instructions.md`: Contains specific instructions for the backend.
- `.github/instructions/frontend-instructions.md`: Contains specific instructions for the frontend.
- `pom.xml`: Maven configuration file for the backend.
- `package.json`: NPM configuration file for the frontend.
- `README.md`: Project documentation.
- `docs/`: Contains documentation files for the project.
- `plans/`: Contains open or in-progress plans for the project.
- `plans/completed/`: Contains completed plans for the project.

# Authentication and Authorization
 - Front end calls the backend for authentication and authorization.
 - The backend uses MS Entra External ID for Single Sign-On (SSO) authentication for sso users based on the users email domain and the cconfiguration in the `application.properties` file.
 - The backend also supports database authentication for users who do not have a Entra External ID account.
 - Backend should issue JWT tokens for authenticated users.
 - Frontend should store the JWT token in local storage or session storage for subsequent API calls.
- The backend should validate the JWT token for protected routes and return appropriate responses.
- The backend should handle user roles and permissions for authorization.
- The frontend should display different UI elements based on the user's role and permissions.
- Backend should validate expired JWT tokens and handle token refresh.
- Backend should use the MSAL4j library for Microsoft Entra External ID authentication.