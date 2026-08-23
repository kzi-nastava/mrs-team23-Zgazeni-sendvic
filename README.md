# MRS Group Project — "Zgaženi sendvič"

A ride-hailing application (Uber-like) built for the SIIT 2025/26 group project.
It consists of a Spring Boot backend, an Angular web client, and a native Android app.

**Team members**

- Marko Đorđević — SV28/2023
- Aleksa Nenadović — SV79/2023
- Stefan Nalčić — SV64/2023

## Repository layout

| Folder | Component | Stack |
| --- | --- | --- |
| `Back-end (ISS)/Server-Back-ISS` | REST API + WebSocket server | Java 17, Spring Boot 4.0.1, Maven, PostgreSQL |
| `Web Front-end (IKS)/Web-Front-IKS` | Web client | Angular 21, TypeScript, npm |
| `Mobile Front-end (MA)/Mobile-Front-MA` | Android client | Java, Android SDK 36, Gradle |

All three talk to the same backend on `http://localhost:8080`.

## Prerequisites

- **JDK 17** — required by both the backend and the Android build
- **PostgreSQL** running on `localhost:5432`
- **Node.js** and **npm** — for the web client
- **IntelliJ IDEA** — to run the backend
- **Android Studio** — to run the mobile app
- An **Android device or emulator running Android 13 (API 33) or newer** — the app sets `minSdk 33`

## 1. Database

Create the database the backend expects. With the default configuration that is `Testing1`:

```bash
createdb -U postgres Testing1
```

The schema is generated automatically on startup (`spring.jpa.hibernate.ddl-auto=update`), and
`DataLoader` seeds test accounts, drivers, vehicles and rides the first time the app runs against
an empty database. There is no migration step to run.

> If the JPA entities change in a way Hibernate cannot reconcile with an existing schema, the
> startup will fail with a mapping error. Drop and recreate the database, or point
> `spring.datasource.url` at a fresh database name.

## 2. Backend (ISS)

Open `Back-end (ISS)/Server-Back-ISS` in IntelliJ IDEA as a Maven project and let it resolve
dependencies.

Before the first run, set your local values in
`src/main/resources/application.properties`:

| Property | What to set |
| --- | --- |
| `spring.datasource.username` / `spring.datasource.password` | Your PostgreSQL credentials |
| `spring.datasource.url` | The database you created above |
| `spring.mail.username` / `spring.mail.password` | A Gmail address and a Google **app password** (not the account password) |
| `ors.api.key` | An [openrouteservice.org](https://openrouteservice.org/) API key |

Mail is used for registration activation links and password resets; route estimation uses
openrouteservice. The rest of the application runs without them.

Then run **`ServerBackIssApplication`** from IntelliJ (green ▶ next to the class, or right-click →
Run). The API comes up on `http://localhost:8080`.

## 3. Web front-end (IKS)

```bash
cd "Web Front-end (IKS)/Web-Front-IKS"
npm install
npm start
```

The dev server serves the app on `http://localhost:4200`. The backend URL is hardcoded to
`http://localhost:8080`, so the backend must be running on the same machine.

## 4. Mobile app (MA)

Open `Mobile Front-end (MA)/Mobile-Front-MA` in Android Studio and wait for the Gradle sync to
finish. Then pick a device and press **Run ▶**.

### On an emulator

Works with no configuration. The app defaults to `http://10.0.2.2:8080/`, which is the alias the
Android emulator maps to the host machine's `localhost`.

### On a physical phone

`10.0.2.2` does not exist outside the emulator, so the app has to be pointed at your computer's
LAN address instead. Do **not** edit `ApiClient.java` — the value comes from the build.

1. Connect the phone by USB with **USB debugging** enabled, and put it on the **same Wi-Fi network**
   as your computer.
2. Find your computer's LAN IP (`ip addr` on Linux, `ipconfig` on Windows) — something like
   `192.168.1.113`.
3. Add a line to `Mobile-Front-MA/local.properties` (git-ignored, so it stays local to you):

   ```properties
   backendUrl=http://192.168.1.113:8080/
   ```

4. Add the same IP to `app/src/main/res/xml/network_security_config.xml`, next to the entries
   already there. Android blocks plaintext HTTP by default and the backend does not use TLS, so
   an address that is not listed there will silently fail to connect.
5. Re-sync Gradle and run.

Emulators keep working with this override in place, so there is no need to switch back and forth.

## Test accounts

Seeded by `DataLoader` on first startup. All of them use the password **`password123`**.

| Email | Role |
| --- | --- |
| `user1@gmail.com` | Passenger |
| `user2@gmail.com` | Passenger |
| `driver1@gmail.com` | Driver |
| `driver2@gmail.com` | Driver |
| `admina@gmail.com` | Administrator |

These accounts are already activated, so they can be used to log in without going through
registration and email confirmation.

## Troubleshooting

**Backend fails on startup with a schema or mapping error.** The entities no longer match the
existing tables. Drop and recreate the database, or switch `spring.datasource.url` to a new name.

**Mobile app cannot reach the backend.** On a physical phone this is nearly always step 4 above —
either `backendUrl` still points at `10.0.2.2`, or the LAN IP is missing from
`network_security_config.xml`. Check that the phone and computer are on the same network and that
no firewall blocks port 8080.

**Android Studio reports "Activity class ... does not exist".** The Gradle model is stale, usually
after `build.gradle` changed outside the IDE. Run **File → Sync Project with Gradle Files**.

**The map shows a "403 Access blocked" tile instead of the map.** OpenStreetMap rejects requests
whose User-Agent is an unidentified default. osmdroid derives it from the `applicationId`, so it
must not be a `com.example.*` value. Note that OSM serves this notice with HTTP 200, so osmdroid
caches it like a real tile — clear the app's data after fixing it, or the stale notices keep
rendering.
