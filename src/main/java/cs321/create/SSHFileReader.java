package cs321.create;

import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
/**
 * Extracts the SSH log file entries.
 *
 * @author 
 */
public class SSHFileReader {
    /**
     * NOTE: Had to look this up because had no idea how extract the IPs, time, or user since string length can be different
     * Description of variable: precompiled regex patterns to quickely identify SSH log lines
     * **/
    private static final Pattern IP_PATTERN   = Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");
    private static final Pattern TIME_PATTERN = Pattern.compile("\\b\\d{2}:\\d{2}\\b");
    private static final Pattern USER_PATTERN = Pattern.compile("\\bfor\\s+(\\w+)\\b");

    private SSHFileReader() {}

    public static String extractSSHLogEntries(String line, String treeType){
        // just making sure everything has a value
        if (line == null || line.isEmpty() || treeType == null) {
            return null;
        }

        // checking each log and check each type
        switch (treeType) {
            case "failed-ip":
                if (line.contains("Failed")) {
                    Matcher m = IP_PATTERN.matcher(line); // I check the pattern of each line, here i check if it
                    // has a IP pattern since I know I am checking for a failed ip
                    if (m.find()) {
                        return "Failed-" + m.group(0); // I return the ssh log
                    } else {
                        return null; // if no ip patter was found i just return null
                    }
                }
                return null;
                /** Following cases will be almost the as the first but will check patterns based off what im really
                 * looking for, example below i am looking for a failed time so i check if it follows a correct time pattern**/
            case "failed-time":
                if (line.contains("Failed")) {
                    Matcher m = TIME_PATTERN.matcher(line);
                    if (m.find()) {
                        return "Failed-" + m.group(0);
                    } else {
                        return null;
                    }
                }
                return null;

            case "accepted-ip":
                if (line.contains("Accepted")) {
                    Matcher m = IP_PATTERN.matcher(line);
                    if (m.find()) {
                        return "Accepted-" + m.group(0);
                    } else {
                        return null;
                    }
                }
                return null;

            case "accepted-time":
                if (line.contains("Accepted")) {
                    Matcher m = TIME_PATTERN.matcher(line);
                    if (m.find()) {
                        return "Accepted-" + m.group(0);
                    } else {
                        return null;
                    }
                }
                return null;

            case "invalid-ip":
                if (line.contains("Invalid")) {
                    Matcher m = IP_PATTERN.matcher(line);
                    if (m.find()) {
                        return "Invalid-" + m.group(0);
                    } else {
                        return null;
                    }
                }
                return null;

            case "invalid-time":
                if (line.contains("Invalid")) {
                    Matcher m = TIME_PATTERN.matcher(line);
                    if (m.find()) {
                        return "Invalid-" + m.group(0);
                    } else {
                        return null;
                    }
                }
                return null;

            case "reverseaddress-ip":
                if (line.contains("reverse") || line.contains("Address")) {
                    Matcher m = IP_PATTERN.matcher(line);
                    if (m.find()) {
                        return "Reverse-" + m.group(0);
                    } else {
                        return null;
                    }
                }
                return null;

            case "reverseaddress-time":
                if (line.contains("reverse") || line.contains("Address")) {
                    Matcher m = TIME_PATTERN.matcher(line);
                    if (m.find()) {
                        return "Reverse-" + m.group(0);
                    } else {
                        return null;
                    }
                }
                return null;

            //case "userip":
            case "user-ip": //edgar
                if (line.contains("Accepted") && !line.contains("reverse") && !line.contains("Address")) {
                    Matcher um = USER_PATTERN.matcher(line);
                    Matcher im = IP_PATTERN.matcher(line);
                    if (um.find() && im.find()) {
                        return um.group(1) + "-" + im.group(0);
                    }
                }
                return null;

            default:
                return null;
        }
    }
}