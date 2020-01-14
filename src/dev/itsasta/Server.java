package dev.itsasta;

/*
 * Developed By Muhammed-Abdi on 24/12/2019 18:53.
 * Last Modified 24/12/2019 22:33.
 * Credits: Yasper (AI Developer of NeuralLib), Burak (Owner of BurakLite Robot), Dogerina (Founder of all RS Bots)
 * RSPeer.org
 * Copyright (c) 2019. All rights reserved.
 */

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
            System.out.println("Can't connect to DB: " + e);
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
                        String str = inputStream.readUTF();
                        System.out.println(str);

                        if (str.contains("listall")) {
                            ServerUtils.passData(outputStream, listAllBookings());
                        }

                        if (str.contains("listpt")) {
                            ServerUtils.passData(outputStream, listPTBookings(str));
                            System.out.println("Showing specific trainers!");
                        }

                        if (str.contains("listclient")) {
                            ServerUtils.passData(outputStream, listClientBookings(str));
                            System.out.println("Showing specific clients!");
                        }

                        if (str.contains("listdate")) {
                            ServerUtils.passData(outputStream, listDateBookings(str));
                            System.out.println("Showing specific dates!");
                        }

                        if (str.contains("add")) {
                            addBooking(str);
                            System.out.println("Added booking!");
                        }

                        if (str.contains("remove")) {
                            removeBooking(str);
                            System.out.println("Removed booking!");
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
                    .executeQuery("SELECT DISTINCT bookings.booking_id, bookings.trainer_fk, bookings.client_fk, " +
                            "DATE_FORMAT(bookings.date_time, '%Y-%m-%d %H:%i'), bookings.duration, clients.first_name, " +
                            "clients.last_name, clients.dob, " +
                            "trainers.first_name, trainers.last_name " +
                            "FROM bookings JOIN clients ON clients.client_id = bookings.client_fk " +
                            "JOIN trainers ON bookings.trainer_fk = trainers.trainer_id");

            while (resultSet.next()) {
                for (int i = 2; i <= 10; i++) {
//                    System.out.println(resultSet.getString(i));
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
            ResultSet result = connection.createStatement().executeQuery("SELECT DISTINCT bookings.booking_id, bookings.trainer_fk, bookings.client_fk, " +
                    "DATE_FORMAT(bookings.date_time, '%Y-%m-%d %H:%i'), bookings.duration, clients.first_name, " +
                    "clients.last_name, clients.dob, " +
                    "trainers.first_name, trainers.last_name " +
                    "FROM bookings JOIN clients ON clients.client_id = bookings.client_fk " +
                    "JOIN trainers ON bookings.trainer_fk = trainers.trainer_id " +
                    "WHERE trainers.trainer_id = " + id[1]);
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
            ResultSet result = connection.createStatement().executeQuery("SELECT DISTINCT bookings.booking_id, bookings.trainer_fk, bookings.client_fk, " +
                    "DATE_FORMAT(bookings.date_time, '%Y-%m-%d %H:%i'), bookings.duration, clients.first_name, " +
                    "clients.last_name, clients.dob, " +
                    "trainers.first_name, trainers.last_name " +
                    "FROM bookings JOIN clients ON clients.client_id = bookings.client_fk " +
                    "JOIN trainers ON bookings.trainer_fk = trainers.trainer_id " +
                    "WHERE clients.client_id = " + id[1]);
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
                    "SELECT DISTINCT bookings.booking_id, bookings.trainer_fk, bookings.client_fk, " +
                            "DATE_FORMAT(bookings.date_time, '%Y-%m-%d %H:%i'), bookings.duration, " +
                            "clients.first_name, " + "clients.last_name, clients.dob, " +
                            "trainers.first_name, trainers.last_name " +
                            "FROM bookings JOIN clients ON clients.client_id = bookings.client_fk " +
                            "JOIN trainers ON bookings.trainer_fk = trainers.trainer_id " +
                            "WHERE DATE_FORMAT(bookings.date_time, '%Y-%m-%d') BETWEEN '" + date[1] + "' AND '" + date[2] + "'");
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
                connection.createStatement().executeUpdate("DELETE FROM bookings WHERE bookings.booking_id = " + query[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addBooking(String booking) {
        String[] query = booking.split(SEPARATOR);
        try {
            connection.createStatement().executeUpdate("INSERT INTO bookings (trainer_fk, client_fk, date_time, duration) VALUES ("
                    + query[1] + ", " + query[2] + ", '" + query[3] + "', " + query[4] + ")");


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateBooking(int bookingId, int trainer_id, int client_id, String dateTime, String duration) {
        try {
            connection.createStatement().executeUpdate("UPDATE bookings SET " + "trainer_fk = " + trainer_id + ", client_fk = " +
                    client_id + ", date_time = '" + dateTime + "', duration = " + duration +
                    " WHERE " + "booking_id = " + bookingId);

            System.out.println("Successfully updated booking with ID: " + bookingId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
