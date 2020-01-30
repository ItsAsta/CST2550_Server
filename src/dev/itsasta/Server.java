package dev.itsasta;

import dev.itsasta.utils.ServerUtils;

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

    public static void main(String[] args) throws IOException {


        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost/gym", "root", "");
        } catch (SQLException e) {
            System.err.println("Can't connect to DB: " + e);
            System.exit(0);
        }


        ServerSocket serverSocket = new ServerSocket(4999);
        System.out.println("Waiting for a connection...");
        while (true) {

            Socket socket = serverSocket.accept();

            Thread socketListener = new Thread(() -> {
                try {
                    inputStream = new ObjectInputStream(socket.getInputStream());
                    outputStream = new ObjectOutputStream(socket.getOutputStream());
                    while (socket.isConnected()) {
                        String queryInput = inputStream.readUTF();
                        System.out.println(queryInput);

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

            socketListener.start();
            System.out.println("Client Connected!");
        }
    }

    private static Map<Integer, ArrayList<String>> listAllBookings() {
        Map<Integer, ArrayList<String>> bookings = new HashMap<>();
        bookings.clear();
        ArrayList<String> tempList = new ArrayList<>();
        tempList.clear();

        try {
            ResultSet resultSet = connection.createStatement()
                    .executeQuery("SELECT DISTINCT booking.booking_id, booking.trainer_fk, booking.client_fk, " +
                            "DATE_FORMAT(booking.date_time, '%Y-%m-%d %H:%i'), booking.duration, client.first_name, " +
                            "client.last_name, client.dob, " +
                            "trainer.first_name, trainer.last_name " +
                            "FROM booking JOIN client ON client.client_id = booking.client_fk " +
                            "JOIN trainer ON booking.trainer_fk = trainer.trainer_id");

            while (resultSet.next()) {
                for (int i = 2; i <= 10; i++) {
                    tempList.add(resultSet.getString(i));
                }
                bookings.put(Integer.parseInt(resultSet.getString(1)), new ArrayList<>(tempList));
                tempList.clear();
            }

            return bookings;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Map<Integer, ArrayList<String>> listPTBookings(String trainerId) {
        Map<Integer, ArrayList<String>> bookings = new HashMap<>();
        bookings.clear();
        ArrayList<String> tempList = new ArrayList<>();
        tempList.clear();

        String[] id = trainerId.split(SEPARATOR);

        try {
            ResultSet result = connection.createStatement().executeQuery("SELECT DISTINCT booking.booking_id, booking.trainer_fk, booking.client_fk, " +
                    "DATE_FORMAT(booking.date_time, '%Y-%m-%d %H:%i'), booking.duration, client.first_name, " +
                    "client.last_name, client.dob, " +
                    "trainer.first_name, trainer.last_name " +
                    "FROM booking JOIN client ON client.client_id = booking.client_fk " +
                    "JOIN trainer ON booking.trainer_fk = trainer.trainer_id " +
                    "WHERE trainer.trainer_id = " + id[1]);
            while (result.next()) {
                for (int i = 2; i <= 10; i++) {
                    tempList.add(result.getString(i));
                }
                bookings.put(Integer.parseInt(result.getString(1)), new ArrayList<>(tempList));
                tempList.clear();
            }

            return bookings;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Map<Integer, ArrayList<String>> listClientBookings(String clientId) {
        Map<Integer, ArrayList<String>> bookings = new HashMap<>();
        bookings.clear();
        ArrayList<String> tempList = new ArrayList<>();
        tempList.clear();

        String[] id = clientId.split(SEPARATOR);

        try {
            ResultSet result = connection.createStatement().executeQuery("SELECT DISTINCT booking.booking_id, booking.trainer_fk, booking.client_fk, " +
                    "DATE_FORMAT(booking.date_time, '%Y-%m-%d %H:%i'), booking.duration, client.first_name, " +
                    "client.last_name, client.dob, " +
                    "trainer.first_name, trainer.last_name " +
                    "FROM booking JOIN client ON client.client_id = booking.client_fk " +
                    "JOIN trainer ON booking.trainer_fk = trainer.trainer_id " +
                    "WHERE client.client_id = " + id[1]);
            while (result.next()) {
                for (int i = 2; i <= 10; i++) {
                    tempList.add(result.getString(i));
                }
                bookings.put(Integer.parseInt(result.getString(1)), new ArrayList<>(tempList));
                tempList.clear();
            }

            return bookings;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Map<Integer, ArrayList<String>> listDateBookings(String dates) {
        Map<Integer, ArrayList<String>> bookings = new HashMap<>();
        bookings.clear();
        ArrayList<String> tempList = new ArrayList<>();
        tempList.clear();

        String[] date = dates.split(SEPARATOR);

        try {
            ResultSet result = connection.createStatement().executeQuery(
                    "SELECT DISTINCT booking.booking_id, booking.trainer_fk, booking.client_fk, " +
                            "DATE_FORMAT(booking.date_time, '%Y-%m-%d %H:%i'), booking.duration, " +
                            "client.first_name, " + "client.last_name, client.dob, " +
                            "trainer.first_name, trainer.last_name " +
                            "FROM booking JOIN client ON client.client_id = booking.client_fk " +
                            "JOIN trainer ON booking.trainer_fk = trainer.trainer_id " +
                            "WHERE DATE_FORMAT(booking.date_time, '%Y-%m-%d') BETWEEN '" + date[1] + "' AND '" + date[2] + "'");
            while (result.next()) {
                for (int i = 2; i <= 10; i++) {
                    tempList.add(result.getString(i));
                }
                bookings.put(Integer.parseInt(result.getString(1)), new ArrayList<>(tempList));
                tempList.clear();
            }

            return bookings;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void removeBooking(String bookingId) {
        String[] query = bookingId.split(SEPARATOR);

        try {
            if (connection != null) {
                connection.createStatement().executeUpdate("DELETE FROM booking WHERE booking.booking_id = " + query[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addBooking(String booking) {
        String[] query = booking.split(SEPARATOR);
        try {
            connection.createStatement().executeUpdate("INSERT INTO booking (trainer_fk, client_fk, date_time, duration) VALUES ("
                    + query[1] + ", " + query[2] + ", '" + query[3] + "', " + query[4] + ")");


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateBooking(String stringQuery) {
        String[] query = stringQuery.split(SEPARATOR);

        try {
            connection.createStatement().executeUpdate("UPDATE booking SET " + "trainer_fk = " + query[2] +
                    ", date_time = '" + query[3] + "', duration = " + query[4] +
                    " WHERE " + "booking_id = " + query[1]);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateClient(String stringQuery) {
        String[] query = stringQuery.split(SEPARATOR);

        try {
            connection.createStatement().executeUpdate("UPDATE client SET " + "first_name = '" + query[2] +
                    "', last_name = '" + query[3] + "', dob = '" + query[4] + "', weight = " + query[5] + ", height = " + query[6]
                    + ", mobile_no = '" + query[7] + "', focus = '" + query[8] + "'" +
                    " WHERE " + "client.client_id = " + query[1]);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static List<String> fetchUpdateBooking(String bookingId) {
        List<String> booking = new ArrayList<>();
        String[] query = bookingId.split(SEPARATOR);
        try {
            ResultSet result = connection.createStatement().executeQuery(
                    "SELECT DISTINCT booking.booking_id, booking.trainer_fk, booking.client_fk, " +
                            "DATE_FORMAT(booking.date_time, '%Y-%m-%d %H:%i'), booking.duration, " +
                            "client.first_name, " + "client.last_name, client.dob, " +
                            "trainer.first_name, trainer.last_name " +
                            "FROM booking JOIN client ON client.client_id = booking.client_fk " +
                            "JOIN trainer ON booking.trainer_fk = trainer.trainer_id " +
                            "WHERE booking.booking_id = " + query[1]);
            while (result.next()) {
                for (int i = 1; i <= 10; i++) {
                    booking.add(result.getString(i));
                }
            }


            return booking;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static List<String> fetchClient(String clientId) {
        List<String> client = new ArrayList<>();
        String[] query = clientId.split(SEPARATOR);
        try {
            ResultSet result = connection.createStatement().executeQuery(
                    "SELECT DISTINCT client.client_id, client.first_name, client.last_name, " +
                            "DATE_FORMAT(client.dob, '%Y-%m-%d'), client.weight, " +
                            "client.height, " + "client.mobile_no, client.focus " +
                            "FROM client WHERE client.client_id = "+ query[1]);
            while (result.next()) {
                for (int i = 1; i <= 8; i++) {
                    client.add(result.getString(i));
                }
            }

            return client;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void registerClient(String stringQuery) {
        String[] query = stringQuery.split(SEPARATOR);
        try {
            connection.createStatement().executeUpdate("INSERT INTO client (first_name, last_name, dob, weight, height, mobile_no, focus) VALUES " +
                    "('" + query[1] + "', '" + query[2] + "', '" + query[3] + "', '" + query[4] + "', " + query[5] + ", '" + query[6] + "', '" + query[7] + "')");


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


}
