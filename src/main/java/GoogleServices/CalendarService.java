package GoogleServices;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.services.calendar.model.Events;

import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class CalendarService {
    public static final String APPLICATION_NAME = "Google Calendar Services";
    public static final String DEFAULT_CALENDAR = "primary";
    public static final String TIME_ZONE = "America/Bogota";
    // List scopes https://developers.google.com/calendar/auth#OAuth2Authorizing
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    
    // For G Suite Domain delegation method:
    // How to create key service account with delegate: https://developers.google.com/admin-sdk/directory/v1/guides/delegation
    private static final String SERVICE_ACCOUNT_FILE_PATH = "/service-account-key.json";
    private HttpTransport httpTransport = null;
    JsonFactory jsonFactory = null;
    GoogleCredential creds = null;

    // For OAuth user Token method
    // How to create credential OAuth Client ID + json    https://support.google.com/cloud/answer/6158849?hl=en
    //                                    More explained:    https://developers.google.com/adwords/api/docs/guides/authentication
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private GoogleClientSecrets clientSecrets;
    
    // main calendar service
    Calendar service = null;
    
    /**
     * Constructor for OAuth user Token method
     * @throws IOException
     * @throws GeneralSecurityException
     * @see <a href="https://developers.google.com/calendar/quickstart/java">quickstart</a>
     */
    public CalendarService() throws IOException, GeneralSecurityException {
        InputStream in = CalendarService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        
        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
    
    /**
     * Constructor for G Sute Domain delegation
     * @param userEmail
     * @throws IOException
     * @throws FileNotFoundException
     * @see <a href="https://developers.google.com/cloud-search/docs/guides/delegation?hl=es">G Suite Domain Delegate</a>
     */
    public CalendarService(String userEmail) throws IOException, FileNotFoundException {
        String keyPath = CalendarService.class.getResource(SERVICE_ACCOUNT_FILE_PATH).toString().replace("file:", "");
        
        FileInputStream credsFile = new FileInputStream(keyPath);
        GoogleCredential init = GoogleCredential.fromStream(credsFile);
        httpTransport = init.getTransport();
        jsonFactory = init.getJsonFactory();
        
        creds = new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setServiceAccountId(init.getServiceAccountId())
                .setServiceAccountPrivateKey(init.getServiceAccountPrivateKey())
                .setServiceAccountScopes(Collections.singletonList(CalendarScopes.CALENDAR))
                .setServiceAccountUser(userEmail)
                .build();
        
        service = new Calendar.Builder(httpTransport, jsonFactory, creds)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
    
    /**
     * Authorization flow, in web app is replaced for DB save tokens
     * @param HTTP_TRANSPORT
     * @return Credential
     * @throws IOException
     */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
    
    /**
     * <a href="https://developers.google.com/calendar/v3/reference/events/list">User events list</a>
     * @return List<Event>
     * @throws IOException
     */
    public List<Event> getEventList() throws IOException {
        
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = service.events().list(DEFAULT_CALENDAR)
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();
        
        return items;
    }

    /**
     * <a href="https://developers.google.com/calendar/v3/reference/calendarList/list">User calendars</a>
     * @return List<CalendarListEntry>
     * @throws IOException
     */
    public List<CalendarListEntry> getCalendarList() throws IOException {
        CalendarList calendarList = service.calendarList().list().execute();
        List<CalendarListEntry> items = calendarList.getItems();
        
        return items;
    }
    
    /**
     * TODO: Add parameters for creation event
     * <a href="https://developers.google.com/calendar/v3/reference/events/insert">Create event</a>
     * @return Event
     * @throws IOException
     */
    public Event insertEvent() throws IOException {
        Event event = new Event()
            .setSummary("Cita médica")
            .setLocation("Virtual")
            .setDescription("Cita médica Virtual");
        
        DateTime startDateTime = new DateTime("2020-03-25T16:00:00-05:00");
        EventDateTime start = new EventDateTime()
            .setDateTime(startDateTime)
            .setTimeZone(TIME_ZONE);
        event.setStart(start);
        
        DateTime endDateTime = new DateTime("2020-03-25T17:00:00-05:00");
        EventDateTime end = new EventDateTime()
            .setDateTime(endDateTime)
            .setTimeZone(TIME_ZONE);
        event.setEnd(end);
        
        EventAttendee[] attendees = new EventAttendee[] {
            new EventAttendee().setEmail("oscar.ortiz@xertica.com"),
            new EventAttendee().setEmail("admin@david.eforcers.com.co"),
        };
        event.setAttendees(Arrays.asList(attendees));
        
        EventReminder[] reminderOverrides = new EventReminder[] {
            new EventReminder().setMethod("popup").setMinutes(10)
        };
        Event.Reminders reminders = new Event.Reminders()
            .setUseDefault(false)
            .setOverrides(Arrays.asList(reminderOverrides));
        event.setReminders(reminders);
        
        String calendarId = DEFAULT_CALENDAR;
        event = service.events().insert(calendarId, event).execute();
        
        return event;
    }
    
    /**
     * <a href="https://developers.google.com/calendar/v3/reference/events/get">Get event object</a>
     * @param eventId
     * @return Event
     * @throws IOException
     */
    public Event getEvent(final String eventId) throws IOException {
        Event enventReturn = service.events().get(DEFAULT_CALENDAR, eventId).execute();
        return enventReturn;
    }
    
    /**
     * TODO: Personalize parameters to update
     * <a href="https://developers.google.com/calendar/v3/reference/events/patch">Get event object</a>
     * @param eventId
     * @return Event
     * @throws IOException
     */
    public Event eventChangeDate(final String eventId) throws IOException {
        Event event = getEvent(eventId);
        
        // change title
        event.setSummary("Cita médica (reagendada)");
        
        // edit event
        DateTime startDateTime = new DateTime("2020-03-27T16:00:00-05:00");
        EventDateTime start = new EventDateTime()
            .setDateTime(startDateTime)
            .setTimeZone(TIME_ZONE);
        event.setStart(start);
        
        DateTime endDateTime = new DateTime("2020-03-27T17:00:00-05:00");
        EventDateTime end = new EventDateTime()
            .setDateTime(endDateTime)
            .setTimeZone(TIME_ZONE);
        event.setEnd(end);
        // update event
        
        Event enventPatched = service.events().patch(DEFAULT_CALENDAR, eventId, event).execute();
        return enventPatched;
    }
    
    /**
     * <a href="https://developers.google.com/calendar/v3/reference/events/delete">Delete event</a>
     * @param eventId
     * @return Void
     * @throws IOException
     */
    public Void deleteEvent(final String eventId) throws IOException {
        /**/
        service.events().delete(DEFAULT_CALENDAR, eventId).execute();
        return null;
    }

}
