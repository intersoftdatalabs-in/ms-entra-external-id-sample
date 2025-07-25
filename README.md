# Angular + Spring + Microsoft Entra External ID Sample

This repository demonstrates a full-stack application using Angular (standalone components) for the frontend and Spring (Jakarta EE) for the backend, with authentication integrated via Microsoft Entra External ID.

## Features
- **Frontend:** Angular 16+ with standalone components, routing, and Bootstrap styling.
- **Backend:** Java (Jakarta EE/Spring style), Hibernate ORM, HSQLDB in-memory database.
- **Authentication:** Login flow with backend validation, ready for integration with Microsoft Entra External ID.

## Project Structure
```
ms-entra-external-id-sample/
├── backend/   # Java backend (Jakarta EE, Hibernate, HSQLDB)
└── frontend/  # Angular standalone frontend
```

## Getting Started

### Prerequisites
- Node.js (18+ recommended)
- Java 17+
- Maven 3.8+

### Backend Setup
1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Build and run the backend server:
   ```bash
   mvn clean package
   # Deploy the generated WAR to your servlet container (e.g., Tomcat)
   # Or use an embedded server if configured
   ```

### Frontend Setup
1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the Angular development server:
   ```bash
   npm start
   ```
4. Open [http://localhost:4200](http://localhost:4200) in your browser.

## Authentication Flow
- The login screen is shown at the root route (`/`).
- User credentials are sent to the backend `/login` endpoint.
- The backend validates credentials and responds with success or error.
- Ready for integration with Microsoft Entra External ID for external authentication scenarios.

## Customization
- Update backend authentication logic in `backend/src/main/java/com/example/auth/UserService.java`.
- Adjust frontend login UI in `frontend/src/app/components/login/`.
- Integrate Microsoft Entra External ID as needed for your scenario.

## License
This sample is provided for educational purposes. See LICENSE for details.
