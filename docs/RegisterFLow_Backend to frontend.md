🔄 Register Flow (Preparation)
[Frontend Client]
      |
      | 1. POST /api/auth/register
      v
[Backend API]
      |
      | 2. Backend calls Keycloak Admin REST API
      |    (requires service account access token)
      v
[Keycloak]
      |
      | 3. Validates access token (backend-admin client)
      |    + Checks realm-management roles
      |
      | 4. Creates new user in realm (recipe-app)
      v
[Keycloak User Store]
      |
      | 5. User saved (with username, email, password, etc.)
      v
[Backend API]
      |
      | 6. Return response → 201 Created
      v
[Frontend Client]
      |
      | 7. Shows success (e.g. “Account created”)
________________________________________
🧩 Expanded Step Detail
1.	Frontend → Backend
o	User submits registration form (username, email, password, …).
o	Request hits POST /api/auth/register.
2.	Backend → Keycloak
o	Backend retrieves a service account access token (client: backend-admin).
o	Calls Keycloak Admin REST API:
o	POST /admin/realms/recipe-app/users
3.	Keycloak → Validation
o	Keycloak checks if the token is valid & has realm-management roles.
o	If valid → continues, else → 401 Unauthorized.
4.	Keycloak → User Creation
o	User is added to the realm with provided attributes.
5.	Backend → Response
o	If Keycloak returns 201 Created, backend returns success response to frontend.
o	If Keycloak returns error (e.g., duplicate email), backend translates error into meaningful API response.

