# Running Code Submission on Windows (development and testing)

How to build and run the Code Submission service, its sandbox and the student web editor on a Windows computer, then connect ILEP to it.

> **Synthetic data only.** Use the fake roster in step 9, never real rosters. This local setup is for development; it is not approved for real student records (see [`policy-compliance.md`](policy-compliance.md)).

All commands are for **PowerShell** (Start menu → "PowerShell"). Run each block separately.

---

## 1. What you need

| Tool | Version | With admin rights (studio desktop) | Without admin rights (office desktop, work laptop) |
|---|---|---|---|
| **Docker Desktop** | current | Installer from docker.com (enable WSL 2 when asked) | Needs admin; requires an IT ticket. There is no zip version. |
| **Java JDK** | 25 | Installer, e.g. Eclipse Temurin 25 (adoptium.net) | Temurin 25 **.zip**; see step 2 |
| **Node.js** | 22 or 24 LTS (at least 20.19) | Installer from nodejs.org | Node.js **.zip** (Windows binary); see step 2 |
| **Git** | any | Git for Windows, or Eclipse's EGit | Portable Git, or Eclipse's EGit |

Check what you have:

```powershell
docker --version
```

```powershell
java -version
```

```powershell
node --version
```

`java -version` must show **25**. `node --version` must be **v20.19 or newer**; v22 or v24 is recommended.

## 2. Without admin rights: use zip versions (skip on the studio desktop)

1. Download the **.zip** versions of Temurin JDK 25 (adoptium.net) and Node.js 22 or 24 LTS (nodejs.org → "Windows Binary (.zip)").
2. Unzip both into a folder in your user profile, for example `C:\Users\<you>\tools\jdk-25` and `C:\Users\<you>\tools\node-22`.
3. In each PowerShell window you use for this guide, put them first on the path. This lasts only for that window:

```powershell
$env:JAVA_HOME = "$HOME\tools\jdk-25"
$env:Path = "$env:JAVA_HOME\bin;$HOME\tools\node-22;$env:Path"
```

To make it permanent for your account without admin: Start menu → "Edit environment variables for your account" → edit `Path` → add both `bin` folders.

## 3. Check Docker

Start **Docker Desktop** and wait until it says "Engine running". Then:

```powershell
docker run --rm hello-world
```

You should see "Hello from Docker!".

## 4. Get the code

In Eclipse: **Team → Pull** on ILEP (or `git pull` in the ILEP folder). Then go to the service folder; adjust the path to where your ILEP clone lives:

```powershell
cd "$HOME\git\ILEP\code-submission-service"
```

> If you cloned ILEP **before** the line-ending fix (`.gitattributes`), refresh the sandbox script once so it has Linux line endings:
>
> ```powershell
> Remove-Item runner-image\run-submission.sh; git checkout -- runner-image/run-submission.sh
> ```

## 5. Build the sandbox image (once, and after changes in `runner-image/`)

```powershell
docker build -t ilep-java-runner:25 runner-image
```

## 6. Start the database

```powershell
docker compose up -d
```

`docker compose ps` should show `database` as "running". Its data is kept in a Docker volume between restarts.

## 7. Create your keys (once) and load them (every new PowerShell window)

The service needs two secrets:
- an **instructor API key**, which ILEP uses to log in
- a **course-secret encryption key**

The block below creates them once, in a file in your user profile (outside the repository, so they are never committed), and loads them into the current window. Run the whole block each time you open a new PowerShell window:

```powershell
$keyFolder = Join-Path $HOME ".ilep-code-submission"
$keyFile = Join-Path $keyFolder "local-keys.txt"
if (-not (Test-Path $keyFile)) {
    New-Item -ItemType Directory -Force $keyFolder | Out-Null
    $random = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    $apiKeyBytes = New-Object byte[] 24; $random.GetBytes($apiKeyBytes)
    $encryptionKeyBytes = New-Object byte[] 32; $random.GetBytes($encryptionKeyBytes)
    Set-Content $keyFile @(
        "INSTRUCTOR_API_KEY=" + (($apiKeyBytes | ForEach-Object { $_.ToString("x2") }) -join ""),
        "COURSE_SECRET_ENCRYPTION_KEY=" + [Convert]::ToBase64String($encryptionKeyBytes))
}
Get-Content $keyFile | ForEach-Object { $name, $value = $_ -split "=", 2; Set-Item "env:$name" $value }
"Instructor API key: $env:INSTRUCTOR_API_KEY"
```

Keep the same keys. A new encryption key can't read course secrets saved with the old one, and a new API key won't match what ILEP saved.

## 8. Build and start the service

```powershell
cd backend
```

```powershell
.\gradlew.bat bootJar
```

The first build downloads Gradle and the libraries, and builds the student editor with npm (a few minutes). Later builds are faster. Then start it:

```powershell
java -jar build\libs\code-submission-service-0.1.0.jar
```

Wait for `Started CodeSubmissionApplication` and `Registered the development instructor`. Open **http://localhost:8080** in Edge or Chrome: you should see the "Join your assignment" page. Leave this window open; **Ctrl+C** stops the service.

> **Windows Firewall** may ask whether Java can use the network. For testing on this computer only, choose **Cancel**; `localhost` doesn't need it. (Letting students' devices reach your computer is a later step.)

## 9. Connect ILEP with a fake roster

1. **Make a test data folder** outside the repository, e.g. `C:\ilep-test-data`, and save this as `00001-Fall 2026-CSCI1301T.csv` in it. The file name must keep that exact pattern.

```text
Last Name,First Name,Middle Name,Preferred First Name,Email,Eagle ID,Major,Campus,First Day of Isolation/Quarantine Modification,Last Day of Isolation/Quarantine Modification,Registered Late,Attendance Status
One,Student,Test,Student,student.one@example.edu,900000001,Computer Science,Statesboro Campus,null,null,null,N
Two,Student,Test,Student,student.two@example.edu,900000002,Computer Science,Statesboro Campus,null,null,null,N
Three,Student,Test,Student,student.three@example.edu,900000003,Computer Science,Statesboro Campus,null,null,null,N
Four,Student,Test,Student,student.four@example.edu,900000004,Computer Science,Statesboro Campus,null,null,null,N
Five,Student,Test,Student,student.five@example.edu,900000005,Computer Science,Statesboro Campus,null,null,null,N
Six,Student,Test,Student,student.six@example.edu,900000006,Computer Science,Statesboro Campus,null,null,null,N
```

2. **Point ILEP at it temporarily.** In `src/models/RosterLoader.java`, set `DATA_FOLDER_PATH = new File("C:\\ilep-test-data")`. Don't commit this line; undo it afterwards with Eclipse **Replace With → HEAD Revision**. This keeps your real rosters out of testing.
3. **Run ILEP from Eclipse.** Go to **Attendance**, select the CSCI1301T roster, Start Roll Call, mark a few students present, then **Class Session → Activities → Code Submission**.
4. **Settings (first time):** service address `http://localhost:8080`, instructor API key from step 7.
5. **Create an assignment.** Fill in the title and starter code, then **Sync roster & create assignment**.
6. **Join as a student.** Click **Copy link** and open it in the browser. Join with Eagle ID `900000001`, accept the notice, write code, **Run**, then **Submit**. For a second student, use an InPrivate window and `900000002`.
7. **Watch ILEP.** The table updates within 10 seconds. Then try **Close assignment**.

## 10. Run the service tests

With Docker Desktop running:

```powershell
.\gradlew.bat test
```

All tests should pass. The first run downloads a PostgreSQL test image. To skip the sandbox tests: `.\gradlew.bat test -PexcludeDockerTests`.

## 11. Stop, restart, reset

| To | Do |
|---|---|
| Stop the service | **Ctrl+C** in its window |
| Stop the database | `docker compose down` (data is kept) |
| Start again later | Step 6, step 7 (load keys), then the `java -jar` line from step 8 |
| Start completely fresh | `docker compose down -v` (deletes the database), then delete ILEP's `src\appData\code-submission-settings.properties` and `src\appData\code-submission-courses.properties` |

## Troubleshooting

| Problem | Fix |
|---|---|
| Run says "The sandbox could not start a container" | Docker Desktop isn't running, or step 5 wasn't done. |
| Run fails with `/bin/sh^M` or "not found" | The sandbox script has Windows line endings: do the note in step 4, then step 5 again. |
| `docker compose up` says port 5432 is in use | Another PostgreSQL is running; stop it, or change `127.0.0.1:5432:5432` to `127.0.0.1:5433:5432` in `docker-compose.yml` and start the service with `$env:DATABASE_URL = "jdbc:postgresql://localhost:5433/code_submission"`. |
| Port 8080 is in use | Add `--server.port=8081` to the `java -jar` line, set `$env:PUBLIC_BASE_URL = "http://localhost:8081"` first, and use that address in ILEP. |
| `COURSE_SECRET_ENCRYPTION_KEY is required` | The keys weren't loaded in this window: run step 7's block. |
| ILEP says "did not accept the API key" | The key in ILEP's settings doesn't match `$env:INSTRUCTOR_API_KEY`. Re-enter it via **Settings** in the Code Submission panel. |
| Build fails in the npm step | `node --version` is below 20.19: update Node.js (step 1 or 2). |
| `java -version` isn't 25 | Set `JAVA_HOME` and `Path` as in step 2 for that window. |
