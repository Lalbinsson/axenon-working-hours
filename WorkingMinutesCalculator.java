import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

//Example times input in UNIX:
// Monday 08:30 - Monday 10:00
// 1709541000 1709546400
// 90 min

// Monday 16.30 - Tuesday 09:00
// 1709569800 1709629200
// 90 min

// Monday 16:00 - Tuesday 10:00
// 1709568000 1709632800
// 180 min

// Friday 16:00 - Monday 10:00
// 1709913600 1710151200
// 180 min

// // Monday 15:00 - Monday 18:00
// 1709564400 1709575200
// 120 min

// More than one day
// Monday 16:00 - Wednesday 10:00
// 1709568000 1709719200
// 720 min

// From end of working hours to start of next working hours
// Monday 17:00 - Tuesday 08:00
// 1709571600 1709625600
// 0 min

// Outside of working hours
// Monday 18:00 - Tuesday 07:00
// 1709575200 1709622000
// 0 min

// Edge cases:

// EndTime is before startTime
// Monday 15:00 - Monday 10:00
// 1709564400 1709546400

// startTime is equal to endTime
// Monday 15:00 - Monday 15:00
// 1709564400 1709564400

public class WorkingMinutesCalculator {

    public static void main(String[] args) {
        // Only used to convert string to unix when testing.
        // System.out.println(convertStringToUnix("2024-03-04 15:00:00") + ", " + convertStringToUnix("2024-03-04 10:00:00"));

        // Validate the input, should be two arguments with digits
        if (args.length != 2 || !args[0].matches("\\d+") || !args[1].matches("\\d+")) {
            System.out.println("Incorrect input, arguments: <start_time_unix> <end_time_unix>");
            return;
        }

        // Parse input to long times and handle exception
        long startTime, endTime;
        try {
            startTime = Long.parseLong(args[0]);
            endTime = Long.parseLong(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input format. Please provide valid Unix timestamps.");
            return;
        }

        // Feedback of start and end for clarity
        System.out.println("Calculating working minutes from: "+ convertUnixToString(startTime)[0] + " " + convertUnixToString(startTime)[1] + " to " + convertUnixToString(endTime)[0] + " " + convertUnixToString(endTime)[1]);

        long workingMinutes = calculateWorkingMinutes(startTime, endTime);
        System.out.println("Working minutes: " + workingMinutes);
    }

    public static long calculateWorkingMinutes(long startTimeUnix, long endTimeUnix) {
        // edge case if start is after or equal to end, return 0 straight away for efficiency
        if (startTimeUnix >= endTimeUnix) {
            return 0;
        }

        // converts the unix times to LocalDateTime Objects for better handling of the time
        LocalDateTime start = LocalDateTime.ofEpochSecond(startTimeUnix, 0, ZoneOffset.UTC);
        LocalDateTime end = LocalDateTime.ofEpochSecond(endTimeUnix, 0, ZoneOffset.UTC);

        int minutes = 0;
        LocalDateTime current = start;
        
        // iterate over all minutes from start to end, using current. Stop when endTime is reached.
        while (current.isBefore(end)) {
            DayOfWeek w = current.getDayOfWeek();
            int h = current.getHour();

            // If current is inside working hour times, increment minutes
            if (w != DayOfWeek.SATURDAY && w != DayOfWeek.SUNDAY && h >= 8 && h < 17) {
                minutes++;
            }

            // increment current
            current = current.plusMinutes(1);
        }
        return minutes;
    }

    public static String[] convertUnixToString(long unixTime) {
        // Convert Unix time to LocalDateTime
        LocalDateTime dateTime = LocalDateTime.ofEpochSecond(unixTime, 0, ZoneOffset.UTC);

        // Get weekday and time for dateTime
        String day = dateTime.getDayOfWeek().toString();
        String time = dateTime.toLocalTime().toString();

        // Return weekday and time as an aeeay
        String[] res = {day, time};
        return res;
    }


    public static long convertStringToUnix(String formattedDateTime) {
        // Define the format corresponding to the input string
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Parse the input string to a dateTime object using the formatter
        LocalDateTime dateTime = LocalDateTime.parse(formattedDateTime, formatter);

        // Return the Unix time of the datTime object
        return dateTime.toEpochSecond(ZoneOffset.UTC);
    }
}
