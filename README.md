# CRA Platform

![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-6DB33F?logo=springboot&logoColor=white)
![Angular](https://img.shields.io/badge/Angular-17.2.x-DD0031?logo=angular&logoColor=white)

Application web de gestion des CRA (Compte Rendu d Activite), avec deux espaces distincts: Admin et Collaborateur.

## Sommaire
- [Quick Start en 60 secondes](#quick-start-en-60-secondes)
- [Presentation](#presentation)
- [Stack technique](#stack-technique)
- [Fonctionnalites](#fonctionnalites)
- [Architecture du projet](#architecture-du-projet)
- [Donnees demo](#donnees-demo)
- [Configuration](#configuration)
- [Test API avec Swagger](#test-api-avec-swagger)


## Quick Start en 60 secondes
1. Configurer le backend:
	- creer ou mettre a jour les donnees critiques de l'application [CRA-backend/.env](CRA-backend/.env)
2. Lancer le backend (depuis [CRA-backend](CRA-backend)):
	
3. Lancer le frontend (depuis [CRA-frontend](CRA-frontend)):
	- `npm install`
	- `ng serve`
4. Ouvrir:
	- Frontend: http://localhost:4200
	- Swagger: http://localhost:8080/swagger-ui/index.html

## Presentation
L application permet:
- la saisie des CRA cote collaborateur
- la gestion des collaborateurs, missions, affectations et validation des CRA cote admin

## Stack technique
- Backend: Spring Boot 3.2.3, Spring Data JPA, Spring Security
- Base de donnees: H2 (en developpement)
- Frontend: Angular 17.2.x
- Authentification: JWT
- API docs: Swagger / OpenAPI

Versions utilisees:
- Java: 17
- Node.js: 20+ (recommande)
- npm: 10+

## Fonctionnalites

### Espace Admin
- Gestion des collaborateurs
- Gestion des missions
- Affectation des missions aux collaborateurs
- Validation des CRA

### Espace Collaborateur
- remplissage de ses CRA
- Soumission de CRA

## Architecture du projet
- Backend: [CRA-backend](CRA-backend)
- Frontend: [CRA-frontend](CRA-frontend)

## Donnees demo
Les donnees de demonstration sont initialisees dans:
- [CRA-backend/src/main/java/com/example/crabackend/config/DataInitializer.java](CRA-backend/src/main/java/com/example/crabackend/config/DataInitializer.java)

Pour tester l activation par email d un collaborateur:
1. Modifier l email d un collaborateur dans DataInitializer.
2. Mettre une adresse email a laquelle vous avez acces.
3. Relancer le backend.
4. Activer le collaborateur depuis l espace Admin.


## Configuration
Les donnees Admin sont configurees dans:
- [CRA-backend/.env](CRA-backend/.env)

Variables importantes:
- JWT_SECRET
- ADMIN_EMAIL
- ADMIN_PASSWORD
- SMTP_USERNAME
- SMTP_PASSWORD
- FRONTEND_ACTIVATION_URL
- APP_TIMEZONE
- CRA_WINDOW_START_DAY
- CRA_WINDOW_END_DAY


## Test API avec Swagger
La documentation API est auto-generee via Swagger/OpenAPI (plus rapide a exploiter dans ce projet que Postman).

Lien:
- http://localhost:8080/swagger-ui/index.html