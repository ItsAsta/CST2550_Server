package org.abdz;

import org.abdz.utils.ServerUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {
    private static Connection connection;
    private static ObjectOutputStream outputStream;
    private static ObjectInputStream inputStream;
    private static String SEPARATOR = "=";
    private static String host;
    private static int port;
    private static String user;
    private static String password;
    private static String databaseName;

    /**
     * @param args E.g. localhost 4999 root pass gym
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

//        Check if there are 5 arguments
        if (args.length == 5) {
            host = args[0];
            port = Integer.parseInt(args[1]);
            user = args[2];
            password = args[3];
            databaseName = args[4];

            try {
//                Connect to database
                connection = DriverManager.getConnection("jdbc:mysql://" + host + "/" + databaseName, user, password);
            } catch (SQLException e) {
                System.err.println("Can't connect to DB: " + e);
                System.exit(0);
            }
        } else {
            System.err.println("NOTE: Type \"\" for an empty argument!");
            System.err.println("Wrong argument format, e.g.: [Hostname] [Port] [User] [Password] [Database Name]");
            System.exit(0);
        }

//        Wait for a Socket to connect
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Waiting for a connection...");
        while (true) {

//            Accept new socket
            Socket socket = serverSocket.accept();

//            Create a new thread for the socket
            Thread socketListener = new Thread(() -> {
                try {
//                    Initiate output and input streams
                    inputStream = new ObjectInputStream(socket.getInputStream());
                    outputStream = new ObjectOutputStream(socket.getOutputStream());
//                    While the socket is connected loop
                    while (socket.isConnected()) {
//                        Wait for input from the client, reading from the input stream
                        String queryInput = inputStream.readUTF();

                        if (queryInput.contains("listall")) {
                            ServerUtils.passData(outputStream, listAllBookings());
                            System.out.println("Showing all bookings!");
                        }

                        if (queryInput.contains("listpt")) {
                            ServerUtils.passData(outputStream, listPTBookings(queryInput));
                            System.out.println("Showing specific trainers!");
                        }

                        if (queryInput.contains("listclient")) {
                            ServerUtils.passData(outputStream, listClientBookings(queryInput));
                            System.out.println("Showing specific clients!");
                        }

                        if (queryInput.contains("listdate")) {
                            ServerUtils.passData(outputStream, listDateBookings(queryInput));
                            System.out.println("Showing specific dates!");
                        }

                        if (queryInput.contains("add")) {
                            addBooking(queryInput);
                            System.out.println("Added booking!");
                        }

                        if (queryInput.contains("remove")) {
                            removeBooking(queryInput);
                            System.out.println("Removed booking!");
                        }

                        if (queryInput.contains("updatebooking")) {
                            updateBooking(queryInput);
                            System.out.println("Updated booking!");
                        }

                        if (queryInput.contains("updateclient")) {
                            updateClient(queryInput);
                            System.out.println("Updated client!");
                        }

                        if (queryInput.contains("fetchbooking")) {
                            ServerUtils.passData(outputStream, fetchUpdateBooking(queryInput));
                            System.out.println("Fetching booking!");
                        }

                        if (queryInput.contains("fetchclient")) {
                            ServerUtils.passData(outputStream, fetchClient(queryInput));
                            System.out.println("Fetching client!");
                        }

                        if (queryInput.contains("register")) {
                            registerClient(queryInput);
                            System.out.println("Registered client!");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

//            Start the new thread
            socketListener.start();
            System.out.println("Client Connected!");
        }
    }

    /**
     * Creates a sql query getting the bookings in our database
     *
     * @return a Map with Integer and a list as parameters
     */
    private static Map<Integer, ArrayList<String>> listAllBookings() {
//        Initialise a map to store our bookings
        Map<Integer, ArrayList<String>> bookings = new HashMap<>();
//        Initialise a list to store strings temporarily
        ArrayList<String> tempList = new ArrayList<>();

        try {
//            Execute a query
            ResultSet resultSet = connection.createStatement()
                    .executeQuery("SELECT DISTINCT booking.booking_id, booking.trainer_fk, booking.client_fk, " +
                            "DATE_FORMAT(booking.date_time, '%Y-%m-%d %H:%i'), booking.duration, client.first_name, " +
                            "client.last_name, client.dob, " +
                            "trainer.first_name, trainer.last_name " +
                            "FROM booking JOIN client ON client.client_id = booking.client_fk " +
                            "JOIN trainer ON booking.trainer_fk = trainer.trainer_id");

//            A while loop iterating over the results
            while (resultSet.next()) {
//                A for loop starting from index 2 to until 10
                for (int i = 2; i <= 10; i++) {
//                    Add each field into our temporary list
                    tempList.add(resultSet.getString(i));
                }
//                Add the data to our bookings map, with booking id as a key in our map
                bookings.put(Integer.parseInt(resultSet.getString(1)), new ArrayList<>(tempList));
//                clear the list for the next row of data
                tempList.clear();
            }

//            Finally return the completed list
            return bookings;

        } catch (SQLException e) {
            e.printStackTrace();
        }
//      Else return null
        return null;
    }

    /**
     * Method used to display all bookings for a specific trainer
     *
     * @param trainerId id of the trainer you want to see the bookings for
     * @return returns a map with the data
     */
    private static Map<Integer, ArrayList<String>> listPTBookings(String trainerId) {
//        Initialise a map to store our bookings
        Map<Integer, ArrayList<String>> bookings = new HashMap<>();
//        Initialise a list to store strings temporarily
        ArrayList<String> tempList = new ArrayList<>();
//      Split the input from the client with a specific key and store the words into an array
        String[] id = trainerId.split(SEPARATOR);

        try {
//            Execute a query
            ResultSet result = connection.createStatement().executeQuery("SELECT DISTINCT booking.booking_id, booking.trainer_fk, booking.client_fk, " +
                    "DATE_FORMAT(booking.date_time, '%Y-%m-%d %H:%i'), booking.duration, client.first_name, " +
                    "client.last_name, client.dob, " +
                    "trainer.first_name, trainer.last_name " +
                    "FROM booking JOIN client ON client.client_id = booking.client_fk " +
                    "JOIN trainer ON booking.trainer_fk = trainer.trainer_id " +
                    "WHERE trainer.trainer_id = " + id[1]);
//            A while loop iterating over the results
            while (result.next()) {
//                A for loop starting from index 2 to until 10
                for (int i = 2; i <= 10; i++) {
//                    Add each field into our temporary list
                    tempList.add(result.getString(i));
                }
//                Add the data to our bookings map, with booking id as a key in our map
                bookings.put(Integer.parseInt(result.getString(1)), new ArrayList<>(tempList));
//                clear the list for the next row of data
                tempList.clear();
            }

//            Finally return the completed list
            return bookings;
        } catch (SQLException e) {
            e.printStackTrace();
        }

//      Else return null
        return null;
    }

    /**
     * A method to list all bookings for a certain client with a given ID
     *
     * @param clientId id of the client you would like to look up
     * @return returns a Map with the data
     */
    private static Map<Integer, ArrayList<String>> listClientBookings(String clientId) {
//        Initialise a map to store our bookings
        Map<Integer, ArrayList<String>> bookings = new HashMap<>();
//        Initialise a list to store strings temporarily
        ArrayList<String> tempList = new ArrayList<>();
//        Split the input from the client with a specific key and store the words into an array
        String[] id = clientId.split(SEPARATOR);

        try {
//            Execute a query
            ResultSet result = connection.createStatement().executeQuery("SELECT DISTINCT booking.booking_id, booking.trainer_fk, booking.client_fk, " +
                    "DATE_FORMAT(booking.date_time, '%Y-%m-%d %H:%i'), booking.duration, client.first_name, " +
                    "client.last_name, client.dob, " +
                    "trainer.first_name, trainer.last_name " +
                    "FROM booking JOIN client ON client.client_id = booking.client_fk " +
                    "JOIN trainer ON booking.trainer_fk = trainer.trainer_id " +
                    "WHERE client.client_id = " + id[1]);
//            A while loop iterating over the results
            while (result.next()) {
//                A for loop starting from index 2 to until 10
                for (int i = 2; i <= 10; i++) {
//                    Add each field into our temporary list
                    tempList.add(result.getString(i));
                }
//                Add the data to our bookings map, with booking id as a key in our map
                bookings.put(Integer.parseInt(result.getString(1)), new ArrayList<>(tempList));
//                clear the list for the next row of data
                tempList.clear();
            }
//            Finally return the completed list
            return bookings;
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        Else return null
        return null;
    }

    /**
     * A method used to list all bookings within certain 2 dates that is given
     *
     * @param dates A string containing 2 dates
     * @return Returns a map with the data
     */
    private static Map<Integer, ArrayList<String>> listDateBookings(String dates) {
//        Initialise a map to store our bookings
        Map<Integer, ArrayList<String>> bookings = new HashMap<>();
//        Initialise a list to store strings temporarily
        ArrayList<String> tempList = new ArrayList<>();
//        Split the input from the client with a specific key and store the words into an array
        String[] date = dates.split(SEPARATOR);

        try {
//            Execute query
            ResultSet result = connection.createStatement().executeQuery(
                    "SELECT DISTINCT booking.booking_id, booking.trainer_fk, booking.client_fk, " +
                            "DATE_FORMAT(booking.date_time, '%Y-%m-%d %H:%i'), booking.duration, " +
                            "client.first_name, " + "client.last_name, client.dob, " +
                            "trainer.first_name, trainer.last_name " +
                            "FROM booking JOIN client ON client.client_id = booking.client_fk " +
                            "JOIN trainer ON booking.trainer_fk = trainer.trainer_id " +
                            "WHERE DATE_FORMAT(booking.date_time, '%Y-%m-%d') BETWEEN '" + date[1] + "' AND '" + date[2] + "'");
//            A while loop iterating over the results
            while (result.next()) {
//                A for loop starting from index 2 to 10
                for (int i = 2; i <= 10; i++) {
//                    Add each field into our temporary list
                    tempList.add(result.getString(i));
                }
//                Add the data to our bookings map, with booking id as a key in our map
                bookings.put(Integer.parseInt(result.getString(1)), new ArrayList<>(tempList));
//                clear the list for the next row of data
                tempList.clear();
            }
//            Finally return the completed list
            return bookings;
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        Else return null
        return null;
    }

    /**
     * A method used to remove a specific booking
     *
     * @param bookingId A booking ID you want to remove
     */
    private static void removeBooking(String bookingId) {
//        Split the input from the client with a specific key and store the words into an array
        String[] query = bookingId.split(SEPARATOR);

        try {
//            Execute update query
            connection.createStatement().executeUpdate("DELETE FROM booking WHERE booking.booking_id = " + query[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A method used to add a booking
     *
     * @param booking A string query with the fields
     */
    private static void addBooking(String booking) {
//        Split the input from the client with a specific key and store the words into an array
        String[] query = booking.split(SEPARATOR);
        try {
//            Execute update query
            connection.createStatement().executeUpdate("INSERT INTO booking (trainer_fk, client_fk, date_time, duration) VALUES ("
                    + query[1] + ", " + query[2] + ", '" + query[3] + "', " + query[4] + ")");


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * A method used to update a booking
     *
     * @param stringQuery A string query with the fields
     */
    private static void updateBooking(String stringQuery) {
//        Split the input from the client with a specific key and store the words into an array
        String[] query = stringQuery.split(SEPARATOR);

        try {
//            Execute update query
            connection.createStatement().executeUpdate("UPDATE booking SET " + "trainer_fk = " + query[2] +
                    ", date_time = '" + query[3] + "', duration = " + query[4] +
                    " WHERE " + "booking_id = " + query[1]);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * A method used to update a client
     *
     * @param stringQuery A string query with the fields
     */
    private static void updateClient(String stringQuery) {
//        Split the input from the client with a specific key and store the words into an array
        String[] query = stringQuery.split(SEPARATOR);

        try {
//            Execute update query
            connection.createStatement().executeUpdate("UPDATE client SET " + "first_name = '" + query[2] +
                    "', last_name = '" + query[3] + "', dob = '" + query[4] + "', weight = " + query[5] + ", height = " + query[6]
                    + ", mobile_no = '" + query[7] + "', focus = '" + query[8] + "'" +
                    " WHERE " + "client.client_id = " + query[1]);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * A method used to get a specific booking to be updated
     *
     * @param bookingId A booking id used to get a specific booking
     * @return Returns a list with the bookings
     */
    private static List<String> fetchUpdateBooking(String bookingId) {
//        A list used to store the query results in
        List<String> booking = new ArrayList<>();
//        Split the input from the client with a specific key and store the words into an array
        String[] query = bookingId.split(SEPARATOR);
        try {
//            Execute query
            ResultSet result = connection.createStatement().executeQuery(
                    "SELECT DISTINCT booking.booking_id, booking.trainer_fk, booking.client_fk, " +
                            "DATE_FORMAT(booking.date_time, '%Y-%m-%d %H:%i'), booking.duration, " +
                            "client.first_name, " + "client.last_name, client.dob, " +
                            "trainer.first_name, trainer.last_name " +
                            "FROM booking JOIN client ON client.client_id = booking.client_fk " +
                            "JOIN trainer ON booking.trainer_fk = trainer.trainer_id " +
                            "WHERE booking.booking_id = " + query[1]);
//            A while loop iterating over the results
            while (result.next()) {
//                A for loop starting from index 1 to 10
                for (int i = 1; i <= 10; i++) {
//                    Add each field into our booking list
                    booking.add(result.getString(i));
                }
            }

//            Return booking list
            return booking;
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        Else return null
        return null;
    }

    /**
     * A method used to get a clients information from the database
     *
     * @param clientId The ID of the client you wish to grab
     * @return Returns a list with the bookings
     */
    private static List<String> fetchClient(String clientId) {
//        A list used to store the query results in
        List<String> client = new ArrayList<>();
//        Split the input from the client with a specific key and store the words into an array
        String[] query = clientId.split(SEPARATOR);
        try {
//            Execute query
            ResultSet result = connection.createStatement().executeQuery(
                    "SELECT DISTINCT client.client_id, client.first_name, client.last_name, " +
                            "DATE_FORMAT(client.dob, '%Y-%m-%d'), client.weight, " +
                            "client.height, " + "client.mobile_no, client.focus " +
                            "FROM client WHERE client.client_id = " + query[1]);
//            A while loop iterating over the results
            while (result.next()) {
//                A for loop starting from index 1 to 8
                for (int i = 1; i <= 8; i++) {
//                    Add each field into our client list
                    client.add(result.getString(i));
                }
            }

//            Return a list with the client data
            return client;
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        Else return null
        return null;
    }

    /**
     * A method used to register a new client into the database
     *
     * @param stringQuery A string query for the SQL
     */
    private static void registerClient(String stringQuery) {
//        Split the input from the client with a specific key and store the words into an array
        String[] query = stringQuery.split(SEPARATOR);
        try {
//            Execute update query
            connection.createStatement().executeUpdate("INSERT INTO client (first_name, last_name, dob, weight, height, mobile_no, focus) VALUES " +
                    "('" + query[1] + "', '" + query[2] + "', '" + query[3] + "', '" + query[4] + "', " + query[5] + ", '" + query[6] + "', '" + query[7] + "')");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
