import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import GoogleServices.CalendarService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class CalendarQuickstart {
    
    static Void printCalendars(CalendarService calendarService) throws IOException {
        List<CalendarListEntry> items = calendarService.getCalendarList();
        
        for (CalendarListEntry calendarListEntry : items) {
            System.out.printf("Calendar: %s \n", calendarListEntry.getSummary());
        }
        return null;
    }

    static Void printEvents(CalendarService calendarService) throws IOException {
        List<Event> items = calendarService.getEventList();
        if (items.isEmpty()) {
            System.out.println("No upcoming events found.");
        } else {
            System.out.println("Upcoming events:");
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                System.out.printf("Event: %s (%s)\n", event.getSummary(), start);
            }
        }
        return null;
    }
    
    static String printInsertEvent(CalendarService calendarService) throws IOException {
        Event createdEvent = calendarService.insertEvent();
        System.out.printf("Event created: %s \n", createdEvent.toString());
        String eventId = createdEvent.getId();
        return eventId;
    }
    
    static Void printGetEvent(CalendarService calendarService, final String eventId) throws IOException {
        Event eventReturned = calendarService.getEvent(eventId);
        System.out.printf("Event return HangOut: %s \n", eventReturned.getHangoutLink());
        return null;
    }
    
    static Void printEventChangeDate(CalendarService calendarService, final String eventId) throws IOException {
        Event eventReturned = calendarService.eventChangeDate(eventId);
        System.out.printf("Event chage Date: %s \n", eventReturned.getStart().toPrettyString());
        return null;
    }
    
    static Void printDeleteEvent(CalendarService calendarService, final String eventId) {
        try {
            calendarService.deleteEvent(eventId);            
            System.out.printf("Event deleted: %s \n", eventId);
        } catch (Exception e) {
            System.out.printf("Event deleted (Error) %s: %s \n", eventId, e.getMessage());
        }
        return null;
    }
    
    public static void main(String... args) throws GeneralSecurityException, IOException {
        // Instance class: try email not exists, user not has calendar active
        //CalendarService calendarService = new CalendarService();
        CalendarService calendarService = new CalendarService("user.exist@mygsuiedomain.com");

        // Call methods: try many requests, and events that not exists 
        int count = 0;
        while (count < 1) {
            //System.out.println("iteration: " + count );
            count +=1;
            printCalendars(calendarService);
            printEvents(calendarService);
            String eventId = printInsertEvent(calendarService);
            printGetEvent(calendarService, eventId);
            printEventChangeDate(calendarService, eventId);
            printDeleteEvent(calendarService, eventId);
        }
        
    }
}
