/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.TGrid;

import edu.tcnj.TGrid.Exceptions.ResourceException;
import edu.tcnj.TGrid.GridClient.ClientMonitor;
import edu.tcnj.TGrid.GridServer.Monitor;

import java.security.*;
import java.math.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 *
 * @author Dan
 */
public class Main {

    /**
     * The default port to bind to, if none is specified.
     */
    private static final int DEFAULT_PORT = 25064;
    /**
     * The default username to use, if none is specified.
     */
    private static final String DEFAULT_USERNAME = "default";
    /**
     * The default password to use, if none is specified.
     */
    private static final String DEFAULT_PASSWORD = "default";
    /**
     * Server monitor.  Responsible for task delegation.
     */
    private static Monitor monitor = null;
    /**
     * The arraylist holding all users who have access to the grid
     */
    private static ArrayList<User> users = new ArrayList<User>();

    //rules for password
    private static String PASSWORD_MIXED_CASE;
    private static String PASSWORD_MIN_LENGTH;
    private static String PASSWORD_MAX_LENGTH;
    private static String PASSWORD_NUMERIC;
    private static String PASSWORD_SPECIAL;
    private static Scanner scan;

    /**
     * Starts the program
  * @param argv the command line arguments
  */
    public static void main(String[] argv) {
        String username = null;
        String password = null;
        InetAddress host = null;
        boolean exitOnJobResults = false;

        // Client map key and password
        int clientKey = 0, clientPswd = 0;
        readFile();
        readPasswordSettings();

        /*for(int n = 0; n < users.size(); n++){
        System.out.println(users.get(n).toString());
        }*/
        int port = -1;

        boolean argumentsAreValid = true;
        boolean isRunningAsServer = false;


        if (argv.length == 0) {
            showCorrectUsage();
        } else if (argv[0].equals("-server") || argv[0].equals("-client")) {
            isRunningAsServer = argv[0].equals("-server");

            int currentArgument = 1;

            while (currentArgument < argv.length && argumentsAreValid) {
                if (argv[currentArgument].equals("-t")) {
                    if (port == -1) {
                        currentArgument++;
                        try {
                            port = Integer.parseInt(argv[currentArgument]);
                        } catch (NumberFormatException ex) {
                            System.out.println("\"" + argv[currentArgument] + "\" is not a valid port number.");
                            argumentsAreValid = false;
                        } catch (ArrayIndexOutOfBoundsException ex) {
                            System.out.println("The -t argument requires a parameter.");
                            argumentsAreValid = false;
                        }
                    } else {
                        System.out.println("Only one port may be specified.");
                        argumentsAreValid = false;
                    }

                } else if (argv[currentArgument].equals("-s") && !isRunningAsServer) {
                    if (host == null) {
                        currentArgument++;
                        try {
                            host = InetAddress.getByName(argv[currentArgument]);
                        } catch (UnknownHostException ex) {
                            System.out.println("\"" + argv[currentArgument] + "\" does not seem to refer to a valid remote host.");
                            argumentsAreValid = false;
                        } catch (ArrayIndexOutOfBoundsException ex) {
                            System.out.println("The -s argument requires a parameter.");
                            argumentsAreValid = false;
                        }
                    } else {
                        System.out.println("Only one host may be specified.");
                        argumentsAreValid = false;
                    }
                } else if (argv[currentArgument].equals("-u")) {
                    if (username == null) {
                        currentArgument++;
                        try {
                            username = argv[currentArgument];
                        } catch (ArrayIndexOutOfBoundsException ex) {
                            System.out.println("The -u argument requires a parameter.");
                            argumentsAreValid = false;
                        }
                    } else {
                        System.out.println("Only one username may be specified.");
                        argumentsAreValid = false;
                    }
                } else if (argv[currentArgument].equals("-p")) {
                    if (password == null) {
                        currentArgument++;
                        try {
                            password = argv[currentArgument];
                        } catch (ArrayIndexOutOfBoundsException ex) {
                            System.out.println("The -p argument requires a parameter.");
                            argumentsAreValid = false;
                        }
                    } else {
                        System.out.println("Only one password may be specified.");
                        argumentsAreValid = false;
                    }
                } else if (argv[currentArgument].equals("-clientKey")) {
                    currentArgument++;
                    try {
                        clientKey = Integer.parseInt(argv[currentArgument]);
                    } catch (NumberFormatException ex) {
                        System.out.println("\"" + argv[currentArgument] + "\" is not a valid number.");
                        argumentsAreValid = false;
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        System.out.println("The -clientKey argument requires a parameter.");
                        argumentsAreValid = false;
                    }
                } else if (argv[currentArgument].equals("-clientPswd")) {
                    currentArgument++;
                    try {
                        clientPswd = Integer.parseInt(argv[currentArgument]);
                    } catch (NumberFormatException ex) {
                        System.out.println("\"" + argv[currentArgument] + "\" is not a valid number.");
                        argumentsAreValid = false;
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        System.out.println("The -clientPswd argument requires a parameter.");
                        argumentsAreValid = false;
                    }
                } else if (argv[currentArgument].equals("-exitAfterJob")) {
                    exitOnJobResults = true;
                } else {
                    System.out.println("\"" + argv[currentArgument] + "\" is not a valid argument.");
                    argumentsAreValid = false;
                }

                currentArgument++;
            }

            if (!isRunningAsServer && host == null) {
                System.out.println("You must specify a host with the -s argument.");
                argumentsAreValid = false;
            }

            if (!argumentsAreValid) {
                showCorrectUsage();
            } else {
                System.out.println("Welcome to TGrid");
                scan = new Scanner(System.in);
                if (username == null) {
                    System.out.print("Enter your username: ");
                    username = scan.nextLine();
                }
                if (password == null) {
                    Console console = System.console();

                    if (console == null) {
                        System.out.println("Couldn't get Console instance");
                        System.exit(0);
                    }

                    char passwordArray[] = console.readPassword("Enter your password: ");
                    password = new String(passwordArray);
                }

                User user = new User(hashPassword(username), hashPassword(password));
                boolean valid = ValidUser(user);

                if (!valid) {
                    System.out.println("Invalid login");
                    System.exit(0);
                }
                if (port == -1) {

                    port = DEFAULT_PORT;
                }
                while (true) {
                    /*for(int n = 0; n < users.size(); n++){
                    System.out.println(users.get(n).toString());
                    }*/
                    System.out.println("");
                    System.out.println("1. Start server / client");
                    System.out.println("2. Manage Users Settings");
                    System.out.println("3. Manage Jobs");
                    System.out.println("4. Manage Clients");
                    System.out.println("5. Exit");
                    int opt = 0;
                    while (opt < 1 || opt > 5) {
                        System.out.print("Select option: ");
                        try {
                            opt = scan.nextInt();
                        } catch (InputMismatchException e) {
                            scan.nextLine();
                            System.out.println("Invalid input.  Please enter an integer.");
                        }
                    }
                    System.out.println("");
                    switch (opt) {
                        case 1: {

                            if (isRunningAsServer) {
                                System.out.println("Starting TGrid server on port " + port + "...");

                                startGridServer(port, user, exitOnJobResults);
                            } else {
                                System.out.println("Starting TGrid client to server at " + host.getHostAddress() + ":" + port + "...");

                                startGridClient(host, port, user, clientKey, clientPswd);

                            }
                            break;
                        }

                        case 2: {
                            System.out.println("1. Change Password");
                            System.out.println("2. Add a User");
                            System.out.println("3. Remove a User");
                            System.out.println("4. Check password strength");
                            System.out.println("5. Change password requirements");
                            System.out.println("6. Cancel");
                            // Read in option
                            opt = 0;
                            while (opt < 1 || opt > 6) {
                                System.out.print("Select option: ");
                                try {
                                    opt = scan.nextInt();
                                } catch (InputMismatchException e) {
                                    scan.nextLine();
                                    System.out.println("Invalid input.  Please enter an integer.");
                                }
                            }

                            // Process option
                            switch (opt) {
                                case 1: {
                                    scan = new Scanner(System.in);
                                    int n = findUser(user);
                                    User temp = users.get(n);
                                    Console console = System.console();

                                    if (console == null) {
                                        System.out.println("Couldn't get Console instance");
                                        System.exit(0);
                                    }

                                    boolean pwStrength = false;
                                    do {
                                        char passwordArray[] = console.readPassword("Enter the  new password: ");
                                        System.out.println();
                                        password = new String(passwordArray);
                                        pwStrength = CheckPasswordStrength(password);
                                        if (!pwStrength) {
                                            System.out.println("Password not strong enough, try again.");
                                        }
                                        System.out.println();
                                    } while (!pwStrength);

                                    temp.setPassword(hashPassword(password));
                                    user = users.get(n);
                                    try {
                                        writeFile();
                                    } catch (IOException e) {
                                        System.out.println("File write failed.");
                                    }
                                    break;
                                }

                                case 2: {
                                    scan = new Scanner(System.in);
                                    int n = findUser(user);
                                    User temp = users.get(n);
                                    if (temp.getLevel() > 0) {
                                        System.out.println("Enter the new username");
                                        String name = scan.nextLine();
                                        Console console = System.console();

                                        if (console == null) {
                                            System.out.println("Couldn't get Console instance");
                                            System.exit(0);
                                        }
                                        boolean pwStrength = false;
                                        do {
                                            char passwordArray[] = console.readPassword("Enter the  new password: ");
                                            System.out.println();
                                            password = new String(passwordArray);
                                            pwStrength = CheckPasswordStrength(password);
                                            if (!pwStrength) {
                                                System.out.println("Password not strong enough, try again.");
                                            }
                                            System.out.println();
                                        } while (!pwStrength);

                                        System.out.println("Enter the permission level of the new user (0 = regular user, 1 = admin)");
                                        int level = scan.nextInt();
                                        User add = new User(hashPassword(name), hashPassword(password), level);
                                        if (validName(add)) {
                                            users.add(add);
                                            try {
                                                writeFile();
                                            } catch (IOException e) {
                                                System.out.println("File write failed.");
                                            }
                                            System.out.println("User successfully added");
                                        } else {
                                            System.out.println("User already in database");
                                        }
                                    } else {
                                        System.out.println("Permission level not high enough");
                                    }
                                    break;
                                }

                                case 3: {
                                    scan = new Scanner(System.in);
                                    int m = findUser(user);
                                    User temp = users.get(m);
                                    if (temp.getLevel() > 0) {
                                        System.out.println("Enter the user that is to be removed");
                                        String name = scan.nextLine();
                                        int pos = -1;
                                        for (int n = 0; n < users.size(); n++) {
                                            if (users.get(n).getName().equals(hashPassword(name))) {
                                                pos = n;
                                            }
                                        }
                                        if (pos >= 0) {
                                            users.remove(pos);
                                            try {
                                                writeFile();
                                            } catch (IOException e) {
                                                System.out.println("File write failed.");
                                            }
                                            System.out.println("User successfully removed");
                                        } else {
                                            System.out.println("User not found.");
                                        }

                                    } else {
                                        System.out.println("Permission level not high enough");
                                    }
                                    break;
                                }

                                case 4: {
                                    Console console = System.console();

                                    if (console == null) {
                                        System.out.println("Couldn't get Console instance");
                                        System.exit(0);
                                    }

                                    char passwordArray[] = console.readPassword("Enter your password: ");
                                    password = new String(passwordArray);
                                    boolean result = CheckPasswordStrength(password);
                                    if (result) {
                                        System.out.println("Password follows current requirements");
                                    } else {
                                        System.out.println("Password fails to meet current requirements");
                                    }
                                    break;
                                }

                                case 5: {
                                    int m = findUser(user);
                                    User temp = users.get(m);
                                    if (temp.getLevel() > 0) {
                                        System.out.println("1.Display Current Settings");
                                        System.out.println("2.Change the required number of mixed case characters");
                                        System.out.println("3.Change the minimum amount of characters");
                                        System.out.println("4.Change the maximum amount of characters");
                                        System.out.println("5.Change the required number of numeric characters");
                                        System.out.println("6.Change the required number of special characters");
                                        System.out.println("7.Cancel");
                                        System.out.println();
                                        opt = 0;
                                        while (opt < 1 || opt > 7) {
                                            System.out.print("Select option: ");
                                            try {
                                                opt = scan.nextInt();
                                            } catch (InputMismatchException e) {
                                                scan.nextLine();
                                                System.out.println("Invalid input.  Please enter an integer.");
                                            }
                                        }

                                        // Process option
                                        switch (opt) {

                                            case 1: {
                                                System.out.println("The min number of mixed case characters is: " + PASSWORD_MIXED_CASE);
                                                System.out.println("The minmium length of a password is: " + PASSWORD_MIN_LENGTH);
                                                System.out.println("The maximum length of a password is: " + PASSWORD_MAX_LENGTH);
                                                System.out.println("The minmium number of numeric characters is: " + PASSWORD_NUMERIC);
                                                System.out.println("The minmium number of special characters is: " + PASSWORD_SPECIAL);
                                                break;
                                            }

                                            case 2: {
                                                scan = new Scanner(System.in);
                                                System.out.println("Enter the new requirement for mixed case characters: ");
                                                PASSWORD_MIXED_CASE = scan.next();
                                                try {
                                                    writePasswordSettings();
                                                } catch (IOException e) {
                                                    System.out.println("File write failed.");
                                                }
                                                break;
                                            }

                                            case 3: {
                                                scan = new Scanner(System.in);
                                                System.out.println("Enter the new requirement for minimum length of a password: ");
                                                PASSWORD_MIN_LENGTH = scan.next();
                                                try {
                                                    writePasswordSettings();
                                                } catch (IOException e) {
                                                    System.out.println("File write failed.");
                                                }
                                                break;
                                            }

                                            case 4: {
                                                scan = new Scanner(System.in);
                                                System.out.println("Enter the new requirement for maximum length of a password: ");
                                                PASSWORD_MAX_LENGTH = scan.next();
                                                try {
                                                    writePasswordSettings();
                                                } catch (IOException e) {
                                                    System.out.println("File write failed.");
                                                }
                                                break;
                                            }

                                            case 5: {
                                                scan = new Scanner(System.in);
                                                System.out.println("Enter the new requirement for numeric characters: ");
                                                PASSWORD_NUMERIC = scan.next();
                                                try {
                                                    writePasswordSettings();
                                                } catch (IOException e) {
                                                    System.out.println("File write failed.");
                                                }
                                                break;
                                            }

                                            case 6: {
                                                scan = new Scanner(System.in);
                                                System.out.println("Enter the new requirement for special characters: ");
                                                PASSWORD_SPECIAL = scan.next();
                                                try {
                                                    writePasswordSettings();
                                                } catch (IOException e) {
                                                    System.out.println("File write failed.");
                                                }
                                                break;
                                            }

                                        }
                                    } else {
                                        System.out.println("Permission level not high enough");
                                    }
                                }


                            }
                            break;
                        }

                        case 3: {
                            System.out.println("1.Pick a job to run");
                            System.out.println("2.Pick a job to get the results from");
                            System.out.println("3.Pick a job to terminate");
                            System.out.println("4. Cancel");
                            // Read in option
                            opt = 0;
                            while (opt < 1 || opt > 4) {
                                System.out.print("Select option: ");
                                try {
                                    opt = scan.nextInt();
                                } catch (InputMismatchException e) {
                                    scan.nextLine();
                                    System.out.println("Invalid input.  Please enter an integer.");
                                }
                            }

                            // Process option
                            switch (opt) {

                                case 1: {
                                    //display jobs method here
                                    //switch statement to pick a job here
                                    break;
                                }

                                case 2: {
                                    //display jobs method here
                                    //switch statement to pick a job here
                                    //display results method in switch statement
                                    break;
                                }

                                case 3: {
                                    //display jobs method here
                                    //switch statement to pick a job here
                                    //end job method in switch statement
                                    break;
                                }
                            }
                            break;
                        }
                        case 4: {
                            // Check that the program is running as the server
                            if (!isRunningAsServer) {
                                System.out.println("Only server can manage clients.");
                            } // Check that the monitor is running
                            else if (monitor == null) {
                                System.out.println("Server must running first.");
                            } else {
                                // Output options
                                System.out.println("1. Add client");
                                System.out.println("2. Edit client");
                                System.out.println("3. Delete client");
                                System.out.println("4. Cancel");

                                // Read in option
                                opt = 0;
                                while (opt < 1 || opt > 4) {
                                    System.out.print("Select option: ");
                                    try {
                                        opt = scan.nextInt();
                                    } catch (InputMismatchException e) {
                                        scan.nextLine();
                                        System.out.println("Invalid input.  Please enter an integer.");
                                    }
                                }

                                // Process option
                                switch (opt) {
                                    case 1:
                                        monitor.addClient();
                                        break;
                                    case 2:
                                        monitor.editClient();
                                        break;
                                    case 3:
                                        monitor.deleteClient();
                                        break;
                                }
                            }
                            break;
                        }
                        case 5: {
                            System.out.println("Exiting program...");
                            try {
                                writeFile();
                                writePasswordSettings();
                            } catch (IOException e) {
                                System.out.println("File write failed.");
                            }
                            System.exit(0);
                            break;
                        }

                    }

                }
            }

        } else {
            System.out.println("Unknown mode: " + argv[0]);
            showCorrectUsage();
        }
    }

    /**
     * Prints out the correct way to start and run program
     * @param void
     * @return void
     */
    private static void showCorrectUsage() {
        System.out.println("Usage: tgrid -client [-s hostname] [-t port]");
        System.out.println("             (to act as a client)");
        System.out.println("   or  tgrid -server [-t port]");
        System.out.println("             (to act as a server)");
    }

    /**
     * makes monitor object out of the params
     * and sets it to begin and then sends it a job
     * @param int port, User user, boolean exitOnJobResults
     * @return void
     */
    private static void startGridServer(int port, User user, boolean exitOnJobResults) {
        monitor = new Monitor(port, user.getName(), user.getPassword(), exitOnJobResults);

        monitor.begin();

        // Add shutdown hook to save clients file on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread() {

            public void run() {
                monitor.saveClients();
            }
        });


        // Factorial job
        int factorialNum = 0, factorialTasks = 0;
        while (factorialNum <= 0) {
            System.out.print("Enter n to compute n!: ");
            try {
                factorialNum = scan.nextInt();
                if (factorialNum <= 0) {
                    System.out.println("n must be a positive integer.");
                }
            } catch (InputMismatchException e) {
                scan.nextLine();
                System.out.println("Invalid input.  Please enter an integer.");
            }
        }
        while (factorialTasks <= 0) {
            System.out.print("Enter number of tasks to break factorial into: ");
            try {
                factorialTasks = scan.nextInt();
                if (factorialTasks <= 0) {
                    System.out.println("Number of tasks must be a positive integer.");
                }
            } catch (InputMismatchException e) {
                scan.nextLine();
                System.out.println("Invalid input.  Please enter an integer.");
            }
        }

        monitor.addJob(new FactorialJob(factorialNum, factorialTasks));


        //monitor.addJob(new TestJob());
    }

    private static void startGridClient(InetAddress addressOfServer, int port, User user, int clientKey, int clientPswd) {
        ClientMonitor clientMonitor = new ClientMonitor(addressOfServer, port, user.getName(), user.getPassword(), clientKey, clientPswd);

        try {
            clientMonitor.connectToServer();
        } catch (ResourceException ex) {
            System.out.println("Could not connect to server.  The error was reported as:");
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Reads users.txt in "/src/edu/tcnj/TGrid/users.txt"
     * and creates a User object for each user
     * @param void
     * @return void
     */
    private static void readFile() {
        try {
            Scanner scan = new Scanner(new File("../../src/edu/tcnj/TGrid/users.txt"));
            Scanner lineScan;
            String name, pass, line;
            int level;
            level = 0;
            name = "";
            pass = "";
            line = "";
            while (scan.hasNext()) {
                line = scan.nextLine();
                lineScan = new Scanner(line);
                lineScan.useDelimiter(" ");
                while (lineScan.hasNext()) {
                    name = lineScan.next();
                    pass = lineScan.next();
                    level = lineScan.nextInt();
                }
                User user = new User(name, pass, level);
                users.add(user);
            }
        } catch (FileNotFoundException exception) {
            System.out.println("User file not found");
        }
    }

    /**
     * Reads the password settings file
     * "/src/edu/tcnj/TGrid/passwordRequirements.txt"
     * prints an error if file can't be found
     * @param void
     * @return void
     */
    private static void readPasswordSettings() {
        try {
            Scanner scan = new Scanner(new File("../../src/edu/tcnj/TGrid/passwordRequirements.txt"));
            Scanner lineScan;
            int requirement;
            requirement = 0;
            while (scan.hasNext()) {
                PASSWORD_MIXED_CASE = scan.next();
                PASSWORD_MIN_LENGTH = scan.next();
                PASSWORD_MAX_LENGTH = scan.next();
                PASSWORD_NUMERIC = scan.next();
                PASSWORD_SPECIAL = scan.next();
            }
        } catch (FileNotFoundException exception) {
            System.out.println("Password settings file not found");
        }
    }

    /**
     * Writes the user file
     * "/src/edu/tcnj/TGrid/users.txt"
     * prints an error if file not writable
     * @param void
     * @return void
     */
    private static void writeFile() throws IOException {
        try {
            PrintWriter out = new PrintWriter(new FileWriter("../../src/edu/tcnj/TGrid/users.txt"), false);
            for (int n = 0; n < users.size(); n++) {
                out.println(users.get(n).getName() + " " + users.get(n).getPassword() + " " + users.get(n).getLevel());
            }
            out.close();
        } catch (IOException e) {
            System.out.println("Write failed");
        }
    }

    /**
     * Writes the password settings to a file
     * "/src/edu/tcnj/TGrid/passwordRequirements.txt"
     * prints an error if write fails
     * @param void
     * @return void
     */
    private static void writePasswordSettings() throws IOException {
        try {
            PrintWriter out = new PrintWriter(new FileWriter("../../src/edu/tcnj/TGrid/passwordRequirements.txt"), false);
            out.println(PASSWORD_MIXED_CASE);
            out.println(PASSWORD_MIN_LENGTH);
            out.println(PASSWORD_MAX_LENGTH);
            out.println(PASSWORD_NUMERIC);
            out.println(PASSWORD_SPECIAL);
            out.close();
        } catch (IOException e) {
            System.out.println("Write failed");
        }
    }

    /**
     * Takes a user and returns true if that user is
     * a valid user in the system already otherwise returns
     * false
     * @param User u
     * @return boolean
     */
    private static boolean ValidUser(User u) {
        boolean valid = false;
        for (int n = 0; n < users.size(); n++) {
            if (u.equals(users.get(n))) {
                valid = true;
            }
        }
        return valid;
    }

    /**
     * Takes a User and returns the location of that User
     * in the list else returns -1 for not found
     * @param User u
     * @return int
     */
    private static int findUser(User u) {
        int k = -1;
        for (int n = 0; n < users.size(); n++) {
            if (u.equals(users.get(n))) {
                k = n;
            }
        }
        return k;
    }

    /**
     * Takes a User and tries to find it in the list if found
     * returns true otherwise returns false
     * @param User u
     * @return boolean
     */
    private static boolean validName(User u) {
        boolean valid = true;
        for (int n = 0; n < users.size(); n++) {
            if (u.getName().equals((users.get(n).getName()))) {
                valid = false;
            }
        }
        return valid;
    }

    /**
     * Hashes the password using MD5 and a key for security
     * takes password as a String and returns a hashed String
     * @param String password
     * @return String
     */
    private static String hashPassword(String password) {
        String hashword = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            password += "pH4r3t0S4ltP4ssW0rD";
            for (int n = 0; n < 100; n++) {
                md5.update(password.getBytes());
                BigInteger hash = new BigInteger(1, md5.digest());
                password = hash.toString(16);
            }
        } catch (NoSuchAlgorithmException nsae) {
            //caught
        }

        return pad(password, 32, '0');

    }
    
    /**
     * Takes a String, a length, and a padding char
     * and then pads the string with the char to meet
     * the length given
     * @param String s, int length, char pad
     * @return String
     */
    private static String pad(String s, int length, char pad) {
        StringBuffer buffer = new StringBuffer(s);
        while (buffer.length() < length) {
            buffer.insert(0, pad);
        }
        return buffer.toString();
    }

    /**
     * Takes a String password and runs tests checking how
     * safe/strong that password is and returns true if it meets
     * the password policy on strength and false if it fails
     * @param String passwd
     * @return boolean
     */
    public static boolean CheckPasswordStrength(String passwd) {
        int upper = 0, lower = 0, numbers = 0, special = 0, length = 0;
        int strength = 0, intScore = 0;
        String strVerdict = "none", strLog = "";
        Pattern p;
        Matcher m;
        if (passwd == null) {
            return false;
        }
        // PASSWORD LENGTH
        length = passwd.length();
        if (length < 5) // length 4 or less
        {
            intScore = (intScore + 3);
            strLog = strLog + "3 points for length (" + length + ")\n";
        } else if (length > 4 && passwd.length() < 8) // length between 5 and 7
        {
            intScore = (intScore + 6);
            strLog = strLog + "6 points for length (" + length + ")\n";
        } else if (length > 7 && passwd.length() < 16) // length between 8 and 15
        {
            intScore = (intScore + 12);
            strLog = strLog + "12 points for length (" + length + ")\n";
        } else if (length > 15) // length 16 or more
        {
            intScore = (intScore + 18);
            strLog = strLog + "18 point for length (" + length + ")\n";
        }
        // LETTERS
        p = Pattern.compile(".??[a-z]");
        m = p.matcher(passwd);
        while (m.find()) // [verified] at least one lower case letter
        {
            lower += 1;
        }
        if (lower > 0) {
            intScore = (intScore + 1);
            strLog = strLog + "1 point for a lower case character\n";
        }
        p = Pattern.compile(".??[A-Z]");
        m = p.matcher(passwd);
        while (m.find()) // [verified] at least one upper case letter
        {
            upper += 1;
        }
        if (upper > 0) {
            intScore = (intScore + 5);
            strLog = strLog + "5 point for an upper case character\n";
        }
        // NUMBERS
        p = Pattern.compile(".??[0-9]");
        m = p.matcher(passwd);
        while (m.find()) // [verified] at least one number
        {
            numbers += 1;
        }
        if (numbers > 0) {
            intScore = (intScore + 5);
            strLog = strLog + "5 points for a number\n";
            if (numbers > 1) {
                intScore = (intScore + 2);
                strLog = strLog + "2 points for at least two numbers\n";
                if (numbers > 2) {
                    intScore = (intScore + 3);
                    strLog = strLog + "3 points for at least three numbers\n";
                }
            }
        }
        // SPECIAL CHAR
        p = Pattern.compile("\\W");
        m = p.matcher(passwd);
        while (m.find()) // [verified] at least one special character
        {
            special += 1;
        }
        if (special > 0) {
            intScore = (intScore + 5);
            strLog = strLog + "5 points for a special character\n";
            if (special > 1) {
                intScore += (intScore + 5);
                strLog =
                        strLog + "5 points for at least two special characters\n";
            }
        }
        // COMBOS
        if (upper > 0 && lower > 0) // [verified] both upper and lower case
        {
            intScore = (intScore + 2);
            strLog = strLog + "2 combo points for upper and lower letters\n";
        }
        if ((upper > 0 || lower > 0)
                && numbers > 0) // [verified] both letters and numbers
        {
            intScore = (intScore + 2);
            strLog = strLog + "2 combo points for letters and numbers\n";
        }
        if ((upper > 0 || lower > 0)
                && numbers > 0
                && special > 0) // [verified] letters, numbers, and special characters
        {
            intScore = (intScore + 2);
            strLog =
                    strLog
                    + "2 combo points for letters, numbers and special chars\n";
        }
        if (upper > 0 && lower > 0 && numbers > 0 && special > 0) // [verified] upper, lower, numbers, and special characters
        {
            intScore = (intScore + 2);
            strLog =
                    strLog
                    + "2 combo points for upper and lower case letters, numbers and special chars\n";
        }
        if (intScore < 16) {
            strVerdict = "very weak";
        } else if (intScore > 15 && intScore < 25) {
            strVerdict = "weak";
        } else if (intScore > 24 && intScore < 35) {
            strVerdict = "mediocre";
        } else if (intScore > 34 && intScore < 45) {
            strVerdict = "strong";
        } else {
            strVerdict = "very strong";
        }
        System.out.println(strVerdict + " - " + intScore + "\n" + strLog);
        // Does it meet the password policy?
        try {
            int min = Integer.parseInt(PASSWORD_MIN_LENGTH);
            if (length < min) {
                return false;
            }
        } catch (Exception e) {
            //not catching these
        } // undefined
        try {
            int max = Integer.parseInt(PASSWORD_MAX_LENGTH);
            if (length > max) {
                return false;
            }
        } catch (Exception e) {
            //not catching these
        } // undefined
        try {
            int num = Integer.parseInt(PASSWORD_NUMERIC);
            if (numbers <= num) {
                return false;
            }
        } catch (Exception e) {
            //not catching these
        } // undefined
        try {
            int mix = Integer.parseInt(PASSWORD_MIXED_CASE);
            if (upper < mix || lower < mix) {
                return false;
            }
        } catch (Exception e) {
            //not catching these
        } // undefined
        try {
            int spec = Integer.parseInt(PASSWORD_SPECIAL);
            if (special < spec) {
                return false;
            }
        } catch (Exception e) {
            //not catching these
        } // undefined
        return true;
    }
}
