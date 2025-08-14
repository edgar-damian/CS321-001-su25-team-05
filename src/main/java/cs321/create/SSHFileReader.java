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


    private static final Pattern USER_PATTERN_OTHER = Pattern.compile("\\b([a-zA-Z0-9._-]{1,32})\\b");



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

                /*
            case "reverseaddress-ip":
                if (line.matches(".*\\breverse\\b.*") || line.contains("Address")) {
                    //if (line.contains("reverse") || line.contains("Address")) {
                    Matcher m = IP_PATTERN.matcher(line);
                    if (m.find()) {
//                        return "Reverse-" + m.group(0);
                        return "Address-" + m.group(0);
                    } else {
                        return null;
                    }
                }
                return null;
                 */ //original
            case "reverseaddress-ip":
                if (line.contains("reverse")) {
                    Matcher m = Pattern.compile("reverse\\s+(?:\\S+\\s+)?((?:\\d{1,3}\\.){3}\\d{1,3})").matcher(line);
                    if (m.find()) {
                        return "reverse-" + m.group(1);
                    }
                }
                else if (line.contains("Address")) {
//                    Matcher m = Pattern.compile("Address\\s+(?:\\S+\\s+)?((?:\\d{1,3}\\.){3}\\d{1,3})").matcher(line);
//                    if (m.find()) {
//                        return "Address-" + m.group(1);
//                    }

                    Matcher m = Pattern.compile("Address\\s+(\\d{1,3}(?:\\.\\d{1,3}){3})").matcher(line);
                    if (m.find()) return "Address-" + m.group(1);
                }
                return null;


                /*
            case "reverseaddress-time":
                if (line.matches(".*\\breverse\\b.*") || line.contains("Address")) {
                    //if (line.contains("reverse") || line.contains("Address")) {
                    Matcher m = TIME_PATTERN.matcher(line);
                    if (m.find()) {
//                        return "Reverse-" + m.group(0);
                        return "Address-" + m.group(0);
                    } else {
                        return null;
                    }
                }
                return null;

                 */ //original
            case "reverseaddress-time":
                if (line.contains("reverse")) {
                    Matcher m = TIME_PATTERN.matcher(line);
                    if (m.find()) {
                        return "reverse-" + m.group(0);
                    }
                }
                else if (line.contains("Address")) {
                    Matcher m = TIME_PATTERN.matcher(line);
                    if (m.find()) {
                        return "Address-" + m.group(0);
                    }
                }
                return null;



            //case "userip":
            /*
            case "user-ip": //edgar
                 final Pattern USER_PATTERN1 = Pattern.compile("Accepted\\s+(\\S+)");
                 final Pattern IP_PATTERN1 = Pattern.compile("(\\d{1,3}(?:\\.\\d{1,3}){3})");

                if (line.contains("Accepted") && !line.contains("reverse") && !line.contains("Address")) {
                    Matcher um = USER_PATTERN1.matcher(line);
                    Matcher im = IP_PATTERN1.matcher(line);
                    if (um.find() && im.find()) {
                        return um.group(1) + "-" + im.group(1);
                        //return um.group(1) + "-" + im.group(0);
                    }
                }
                return null;

             */ //original

            /*
            case "user-ip":
                if (line.contains("Accepted") || line.contains("Failed") || line.contains("Invalid")) {
                    Matcher ipMatcher = IP_PATTERN.matcher(line);
                    if (ipMatcher.find()) {
                        String ip = ipMatcher.group(0);


                        String beforeIp = line.substring(0, ipMatcher.start());


                        Matcher userMatcher = Pattern.compile("([*.a-zA-Z0-9_-]{1,32})").matcher(beforeIp);
                        String user = null;
                        while (userMatcher.find()) {
                            user = userMatcher.group(1);
                        }

                        if (user != null) {
                            return user + "-" + ip;
                        }
                    }
                }
                return null;

             */ //works even better

            /*
            case "user-ip":
                if (line.contains("Accepted") || line.contains("Failed") || line.contains("Invalid")) {
                    Matcher ipMatcher = IP_PATTERN.matcher(line);
                    if (ipMatcher.find()) {
                        String ip = ipMatcher.group(0);
                        if (!ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) return null;

                        String beforeIp = line.substring(0, ipMatcher.start()).trim();

                        Matcher userMatcher = Pattern.compile("([*\\.\\\\a-zA-Z0-9_-]{1,32})").matcher(beforeIp);
                        String user = null;
                        while (userMatcher.find()) user = userMatcher.group(1);

                        if (user != null) return user + "-" + ip;
                    }
                }

                return null;

             */ //best one yet, only 1 diff

//            case "user-ip":
//                if (line.contains("Accepted") || line.contains("Failed") || line.contains("Invalid")) {
//                    Matcher ipMatcher = IP_PATTERN.matcher(line);
//                    if (ipMatcher.find()) {
//                        String ip = ipMatcher.group(0);
//
//
//                        if (!ip.matches("(\\d{1,3}\\.){3}\\d{1,3}")) return null;
//
//                        String beforeIp = line.substring(0, ipMatcher.start());
//
//
//                        Matcher userMatcher = Pattern.compile("([*\\.\\\\a-zA-Z0-9_-]{1,32})").matcher(beforeIp);
//                        String user = null;
//                        while (userMatcher.find()) {
//                            user = userMatcher.group(1);
//                        }
//
//                        if (user != null) {
//
//                            user = user.replaceAll("[\\\\.]+$", "").trim();
//                            return user + "-" + ip;
//                        }
//                    }
//                }
//                return null;

            /*
            case "user-ip":
                if (line.contains("Accepted") || line.contains("Failed") || line.contains("Invalid")) {
                    Matcher ipMatcher = IP_PATTERN.matcher(line);
                    if (ipMatcher.find()) {
                        String ip = ipMatcher.group(0);

                        // Skip invalid/truncated IPs
                        if (!ip.matches("(\\d{1,3}\\.){3}\\d{1,3}")) return null;

                        String beforeIp = line.substring(0, ipMatcher.start());

                        // Capture last “word” before IP (allow *, ., \, -, _)
                        Matcher userMatcher = Pattern.compile("([*\\.\\\\a-zA-Z0-9_-]{1,32})").matcher(beforeIp);
                        String user = null;
                        while (userMatcher.find()) {
                            user = userMatcher.group(1);
                        }

                        if (user != null) {
                            // Normalize the key: combine Failed/Invalid for the same user-IP
                            String key = user + "-" + ip;

                            // Strip trailing dots/backslashes, trim, remove control chars, normalize whitespace
                            key = key.replaceAll("[\\\\.]+$", "")
                                    .replaceAll("\\s+", " ")
                                    .replaceAll("\\p{C}", "")
                                    .trim();

                            // Return key with count 1 (you’ll handle increment in BTree insert)
                            return key;
                        }
                    }
                }
                return null;
                */

            case "user-ip":
                if (line.contains("Accepted") || line.contains("Failed") || line.contains("Invalid")) {
                    Matcher ipMatcher = IP_PATTERN.matcher(line);
                    if (ipMatcher.find()) {
                        String ip = ipMatcher.group(0);

                        // Skip invalid/truncated IPs
                        if (!ip.matches("(\\d{1,3}\\.){3}\\d{1,3}")) return null;

                        String beforeIp = line.substring(0, ipMatcher.start());

                        // Capture last “word” before IP (allow *, ., \, -, _)
                        Matcher userMatcher = Pattern.compile("([*\\.\\\\a-zA-Z0-9_-]{1,32})").matcher(beforeIp);
                        String user = null;
                        while (userMatcher.find()) {
                            user = userMatcher.group(1);
                        }

                        if (user != null) {
                            // Normalize user
                            user = user.replaceAll("[\\\\.]+$", "").trim();

                            // Only use user-IP, ignore status
                            return user + "-" + ip;
                        }
                    }
                }
                return null;











            default:
                return null;

             /*
            default:
                Matcher im = IP_PATTERN.matcher(line);
                Matcher um = USER_PATTERN_OTHER.matcher(line);
                if (um.find() && im.find()) {
                    return um.group(1) + "-" + im.group(0);
                }
                return null;

              */

        }
    }
}