# Calendar API for JAVA with user authorization and Domain Delegation
## Documentation
- [Quick start Calendar APIs](https://developers.google.com/calendar/quickstart/java)
- [Calendar API Reference - Crear evento](https://developers.google.com/calendar/v3/reference/events/insert)
- [Create Service account](https://developers.google.com/cloud-search/docs/guides/project-setup?hl=es#create_service_account_credentials)
- [G Suite Delegation Service](https://developers.google.com/cloud-search/docs/guides/delegation?hl=es)


## Build and Run
- Require:
  - ***Java (JDK) 1.8+***
  - ***Gradle 3.2+ (Enviroment Variable)***
  
  ```bash
  # Install dependences
  gradle build;

  # Run code
  gradle run;
  ```

### Integrate with Eclipse
- File > Import > Existing Gradle Project
- Next and finish
- Execute or debug CalendarQuickstart.java


## PREPARE

#### GCP - Enable Apis 
- Go to Apis & Services > Libraries, and search Calendar API
- [Calendar API](https://console.cloud.google.com/apis/library/calendar-json.googleapis.com)

#### GCP - Config concent Screen
- Go to Apis & Services > OAuth consent screen:
- Complete auth screen info as internal app (only domain users), add calendar scopes

#### GCP - Create Credentials (Only for works with OAuth user token)
- Go to Apis & Services > Credentials
- Create OAuth Client ID "OAuth Calendar" and add "Authorized redirect URIs" http://localhost:8888/Callback
    - Download JSON file and rename "***credentials.json***".
    - Paste in ***src/main/resources***
    - Open json file and replace key "web" for "installed".

#### GCP - Credentials key Service account (for OAUTH G Suite Delegation Domain)
- IAM > Service accounts
- Create new service account calendar-delegation
  - Enable G Suite Delegation Domain (check)
  - Download json key and rename "***service-account-key.json***"
  - Paste in ***src/main/resources***
  - Copy unique ID (service account ID) or email service account:
 
 #### G Suite Control Panel - Authorize service account ID and Scopes APIs
- Go to G Suite Control Panel [admin.google.com](https://admin.google.com)
  - Security > Advance > Admin access client API
  - Paste unique ID and scopes:
    - 116475445730569755048
    - https://www.googleapis.com/auth/calendar
    - Authorize
