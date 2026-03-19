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
- [Demarrage rapide](#demarrage-rapide)
- [Compilation](#compilation)
- [Test API avec Swagger](#test-api-avec-swagger)
- [Video de demonstration](#video-de-demonstration)

## Quick Start en 60 secondes
1. Configurer le backend:
	- Creer ou mettre a jour [CRA-backend/.env](CRA-backend/.env)
2. Lancer le backend (depuis [CRA-backend](CRA-backend)):
	- Windows: `mvnw.cmd spring-boot:run`
	- Linux/macOS: `./mvnw spring-boot:run`
3. Lancer le frontend (depuis [CRA-frontend](CRA-frontend)):
	- `npm install`
	- `ng serve`
4. Ouvrir:
	- Frontend: http://localhost:4200
	- Swagger: http://localhost:8080/swagger-ui/index.html

## Presentation
L application permet:
- la saisie et la gestion des CRA cote collaborateur
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
- Gestion de ses CRA
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

Les donnees Admin sont configurees dans:
- [CRA-backend/.env](CRA-backend/.env)

## Configuration
1. Creer [CRA-backend/.env](CRA-backend/.env)
2. Renseigner les variables necessaires

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
- APP_DEMO_ENABLED

## Demarrage rapide

### 1. Backend
Depuis [CRA-backend](CRA-backend):

Sous Linux/macOS:
```bash
./mvnw spring-boot:run
```

Sous Windows:
```bash
mvnw.cmd spring-boot:run
```

Backend disponible sur:
- http://localhost:8080

### 2. Frontend
Depuis [CRA-frontend](CRA-frontend):

```bash
npm install
ng serve
```

Frontend disponible sur:
- http://localhost:4200

## Compilation

### Backend
Depuis [CRA-backend](CRA-backend):
```bash
./mvnw -DskipTests compile
```

### Frontend
Depuis [CRA-frontend](CRA-frontend):
```bash
npm run build
```

## Test API avec Swagger
La documentation API est auto-generee via Swagger/OpenAPI (plus rapide a exploiter dans ce projet que Postman).

Lien:
- http://localhost:8080/swagger-ui/index.html

## Video de demonstration
Oui, vous pouvez inserer une video dans ce README.

Miniature cliquable vers la video (remplacer l URL par votre video finale):

[![Apercu video CRA Platform](CRA-frontend/public/assets/CBX.png)](https://www.youtube.com/watch?v=XXXXXXXXXXX)

Option 1: video versionnee dans le repository
1. Ajouter le fichier video, par exemple: docs/demo.mp4
2. Ajouter un lien Markdown:

```markdown
[Voir la video de demonstration](docs/demo.mp4)
```

Option 2: video hebergee (YouTube, Vimeo, etc.)

```markdown
[Voir la video de demonstration](https://www.youtube.com/watch?v=XXXXXXXXXXX)
```
