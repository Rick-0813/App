# App-master + assigned review module

This project uses the team's uploaded `App-master` as the base. Existing teammate modules were preserved:

- Login and registration
- Role selection
- Worker job search, saved jobs, and applications
- Employer applicant management and job posting
- Profile editing and logout

## Added assigned functions

- Job Details route for both Worker and Employer
- Worker job request/application from Job Details
- Save/remove favourite from Job Details
- Two-way review and rating inside Job Details
  - Worker reviews company
  - Employer reviews worker
- Employer awards an optional Skill Badge
- Persistent reviews, ratings, badges, applications, and favourites
- Employer flow: Approve → Mark Job Completed → Review Worker directly inside Boss Panel
- Dedicated Boss Panel bottom-navigation **Reviews** button showing only Completed workers
- Worker application cards open Job Details

All newly written feature UI is intentionally concentrated in:

`app/src/main/java/com/example/myapplication/myfeature/MyResponsiblePart.kt`

Small integration changes were required in `MainActivity.kt`, `MainViewModel.kt`, `worker/JobSearchScreen.kt`, and `employer/EmployerScreen.kt` so the team app can navigate to and store the feature.

## Quick review demonstration

### Worker reviews company

1. Login with `derrick@test.com` / `123456`.
2. Select the Worker role.
3. Open **Delivery Helper** from Jobs or My Applications.
4. The included demonstration application is Completed.
5. Submit **Worker → Review Company**.

### Employer reviews worker and awards badge

1. Log out from Profile.
2. Login with `boss@test.com` / `123456`.
3. Select the Employer role.
4. In Apps, find Derrick Tan's completed Delivery Helper application.
5. Press **Review Worker** directly on the completed application card.
6. Submit a rating, comment, and optional Skill Badge. **View Job Details** remains available separately.

For a new real application, the Employer must Approve it and then press **Mark Job Completed** before either side can review.

## Build

The project retains the team's Android configuration (`compileSdk 37`, `targetSdk 37`, `minSdk 24`).

```bash
./gradlew assembleDebug
```
