EAT-PROPOSALS
==============

About the team
---------------	

The year 2022 marked the fourth consecutive participation of the Additional Testsuite research project in the Google Summer of Code. This year the team comprised of Panagiotis Soritopoulos (Ph.D. Candidate in the Department of Informatics and Telecommunications) and Stefanos Vlachos (graduate student of the Department of Informatics and Telecommunications).

About EAT
---------------	

EAT (Eap Additional Testsuite) is a testsuite to develop tests against infinite number of JBoss servers. It’s an innovative tool because it’s creating the test once and testing with any version of the tested software. EAT is available for a number of server configurations and a CI pipeline can be built using the maven tool. 

About EAT-PROPOSALS
---------------	

The idea around the EAT-PROPOSALS app was to develop an application that would enable users to upload, view and download proposals and Additional Testsuite related function/program descriptions.

### Tech Stack

EAT-PROPOSALS is an **Android** application developed on the **IntelliJ** IDE using the **Java** programming language. It was compiled using the **Android SDK 32.0.0** and requires:
- an Android version ≥ 7.0 
- an Android SDK ≥ 24.

**Firebase** forms the backbone of the application. In the case of EAT-PROPOSALS, the following services are utilized:
- **Firebase Authentication:** Used to authenticate users when Signing In to the app
- **Firebase Firestore Database:** Used to store (i) the credentials of the registered users and (ii) information related to the accepted types of files
- **Firebase Storage:** Used to store resources uploaded by the users

### App Structure

The EAT-PROPOSALS app is structured by 6 Activities, which can be found under [this directory](https://github.com/stefanos-vlachos/EAT-PROPOSALS/tree/master/app/src/main/java/com/example/firebasegsocapp/activity) and are the following.

#### MainActivity

The root activity of the app, from which a user can:
- Sign Up
- Sign In
- Learn more about the app
- View Files
- Upload a File or Folder

#### CreateAccountActivity

Enables user to fill in and submit a form in order to create a new profile in the app.

#### LoginActivty

Enables user to fill in and submit a form in order to Login to the app.

#### ResetPasswordActivity

Enables users to reset their password in case they forget it.

#### Upload a File/Folder

Enables users to upload one ore multiple files or folders, selected from the internal storage of the user’s mobile device. This feature is implemented and integrated into the [MainActivity](https://github.com/stefanos-vlachos/EAT-PROPOSALS/blob/master/app/src/main/java/com/example/firebasegsocapp/activity/MainActivity.java) of the app

#### ViewFilesActivity

Enables users to view the uploaded/approved resources (folders, files).

#### LearnMoreActivity

Enables users to read a short description of our team’s participation in Google Summer of Code 2022 and find some useful resources.


