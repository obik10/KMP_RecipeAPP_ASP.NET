📘 Register Preparation — Keycloak + Postman
This document explains how to prepare Keycloak and verify the user registration flow manually with Postman before integrating it into the backend API.
________________________________________
1. Keycloak Setup
1.1. Create the Realm
•	Realm name: recipe-app
1.2. Create the Admin Client
•	Navigate to Clients → Create
•	Client ID: backend-admin
•	Name: Backend Admin Client
•	Access Type: Confidential
•	Service Accounts Enabled: ✅ ON
•	Save
1.3. Configure Client Credentials
•	Go to Clients → backend-admin → Credentials
•	Copy the Client Secret (used later in Postman)
1.4. Assign Roles to Service Account
•	Go to Clients → backend-admin → Service Account Roles
•	Assign roles from realm-management:
o	manage-users
o	view-users
o	query-users
o	query-groups
This allows the service account to create and manage users.
________________________________________
2. Get Access Token with Postman
2.1. Token Endpoint
POST http://localhost:8080/realms/recipe-app/protocol/openid-connect/token
2.2. Request Body (x-www-form-urlencoded)
Key	Value
grant_type	client_credentials
client_id	backend-admin
client_secret	<your-client-secret-here>
2.3. Example Response
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOi...",
  "expires_in": 300,
  "token_type": "Bearer"
}
•	Copy the access_token.
2.4. Verify Token in jwt.io
Check claims:
•	client_id: backend-admin
•	resource_access.realm-management.roles: should include manage-users
________________________________________
3. Use Access Token to Call Admin REST API
3.1. Create a User
POST http://localhost:8080/admin/realms/recipe-app/users
Headers
Authorization: Bearer <access_token>
Content-Type: application/json
Body (JSON)
{
  "username": "newuser",
  "enabled": true,
  "email": "newuser@example.com",
  "firstName": "New",
  "lastName": "User",
  "credentials": [
    {
      "type": "password",
      "value": "password123",
      "temporary": false
    }
  ]
}
Response
201 Created
3.2. Verify User Exists
GET http://localhost:8080/admin/realms/recipe-app/users
Authorization: Bearer <access_token>
Response Example
[
  {
    "id": "f6d1b18f-13e2-41a0-b79a-4829b9c9be1b",
    "username": "newuser",
    "email": "newuser@example.com",
    "enabled": true
  }
]
________________________________________
✅ Result
•	You have verified that:
o	The backend-admin service account can request tokens.
o	The token includes proper realm-management roles.
o	The Keycloak Admin API allows user creation.
o	Users appear in the realm after successful POST.


