/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */

import java.sql.Timestamp; //For passing timestamp into RoomUpdatesLog
import java.util.Date; //For passing dates into SQL
import java.text.SimpleDateFormat; //Also for passing dates into SQL
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.lang.Math;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Hotel {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));
   int bookingId = 500; //for bookingID PKeys
   int updateNumber = 50; //for updateNumber pkeys
   int repairId = 500; //for repairID keys
   int requestNumber = 500;

   static Font largeFont = new Font("Liberation Serif", Font.BOLD, 25);
   static Font smallFont = new Font("Liberation Serif", Font.PLAIN, 15);

   /**
    * Creates a new instance of Hotel 
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Hotel(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Hotel

   // Method to calculate euclidean distance between two latitude, longitude pairs. 
   public static double calculateDistance (double lat1, double long1, double lat2, double long2){
      double t1 = (lat1 - lat2) * (lat1 - lat2);
      double t2 = (long1 - long2) * (long1 - long2);
      return Math.sqrt(t1 + t2); 
   }
   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
      Statement stmt = this._connection.createStatement ();

      ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
      if (rs.next())
         return rs.getInt(1);
      return -1;
   }

   public int getNewUserID(String sql) throws SQLException {
      Statement stmt = this._connection.createStatement ();
      ResultSet rs = stmt.executeQuery (sql);
      if (rs.next())
         return rs.getInt(1);
      return -1;
   }
   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   // gui methods

   public static String loginMenu(JFrame frame, Hotel esql){
      JLabel userLabel, passLabel, errorLabel;
      JTextField userField, passField;
      JDialog dialog = new JDialog(frame,"Login", true);
      JPanel loginPanel = new JPanel();
      loginPanel.setLayout(new GridLayout(3,2));

      userLabel = new JLabel("User ID:");
      userLabel.setFont(smallFont);
      userField = new JTextField();
      loginPanel.add(userLabel);
      loginPanel.add(userField);

      passLabel = new JLabel("Password:");
      passLabel.setFont(smallFont);
      passField = new JTextField();
      loginPanel.add(passLabel);
      loginPanel.add(passField);

      JButton button = new JButton("Login");
      errorLabel = new JLabel();

      button.addActionListener(e -> {
         String username = userField.getText();
         String password = passField.getText();
         String userId = LogIn(esql,username,password);
         if(userId == null){
            errorLabel.setText("Invalid Credentials");
         }
         else{
            dialog.dispose();
         }
      });

      loginPanel.add(errorLabel);
      loginPanel.add(button);

      dialog.add(loginPanel);
      dialog.setSize(500,300);
      dialog.setLocation(200,100);
      dialog.setResizable(false);
      dialog.setVisible(true);
      
      // continue execution here
      return userField.getText();
   }

   public static int mainMenu(JFrame frame, Map<String,Integer> options){
      JDialog dialog = new JDialog(frame,"Big Boat Lodge", true);
      DefaultListModel<String> templist = new DefaultListModel<>();
      options.entrySet()
         .stream()
         .forEach(e ->{ 
            templist.addElement(e.getKey());
         });

      JPanel menuPanel = new JPanel();
      menuPanel.setLayout(new GridLayout(1,2));

      JList<String> opsList = new JList<>(templist);
      opsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      JButton button = new JButton("Submit");
      button.addActionListener(e -> {
         if(opsList.getSelectedValue() != null){
            dialog.dispose();
         }
      });

      menuPanel.add(opsList);
      menuPanel.add(button);

      dialog.add(menuPanel);
      dialog.setSize(500,300);
      dialog.setLocation(200,100);
      dialog.setResizable(false);
      dialog.setVisible(true);
      return options.get(opsList.getSelectedValue());
   }

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      // get fonts
      // String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
      // for(int i = 0; i < fonts.length; ++i)
      //    System.out.println(fonts[i]);
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Hotel.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if
      Greeting();
      Hotel esql = null;

      JFrame frame = new JFrame("Big Boat Lodge");
      frame.setBounds(300,90,900,600);
      frame.setResizable(false);
      // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance();
         // instantiate the Hotel object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Hotel (dbname, dbport, user, "");

         boolean keepon = true;
         Map<String,Integer> ops = new HashMap<>();
         while(keepon) {
            // add options to menu before sign in
            ops.clear();
            ops.put("Create user",1);
            ops.put("Log in", 2);
            ops.put("Exit", 9);
            String authorisedUserID = null;
            boolean userIsManager = false;
            switch (mainMenu(frame,ops)){
               case 1: CreateUser(frame,esql); break;
               case 2: 
                  authorisedUserID = loginMenu(frame,esql);
                  userIsManager = isManager(esql,authorisedUserID);
                  break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUserID == null) continue;
            boolean usermenu = true;
            System.out.println(userIsManager + " " + authorisedUserID);
            while(usermenu) {
               ops.clear();
               ops.put("View Hotels within 30 units",1);
               ops.put("View Rooms",2);
               ops.put("Book a Room",3);
               ops.put("View recent booking history",4);

               //the following functionalities basically used by managers
               if(userIsManager){
                  ops.put("Update Room Information",5);
                  ops.put("View 5 recent Room Updates Info",6);
                  ops.put("View booking history of the hotel",7);
                  ops.put("View 5 regular Customers",8);
                  ops.put("Place room repair Request to a company",9);
                  ops.put("View room repair Requests history",10);
               }

               ops.put("Log out",20);
               int userChoice = mainMenu(frame,ops);
               frame.setVisible(false);
               if(userIsManager){
                  switch (userChoice){
                     case 1: viewHotels(esql,frame); break;
                     case 2: viewRooms(esql,frame); break;
                     case 3: bookRooms(esql,authorisedUserID,frame); break;
                     case 4: viewRecentBookingsfromCustomer(esql,authorisedUserID,frame); break;
                     case 5: updateRoomInfo(esql, authorisedUserID,frame); break;
                     case 6: viewRecentUpdates(esql, authorisedUserID,frame); break;
                     case 7: viewBookingHistoryofHotel(esql, authorisedUserID,frame); break;
                     case 8: viewRegularCustomers(esql, authorisedUserID,frame); break;
                     case 9: placeRoomRepairRequests(esql, authorisedUserID,frame); break;
                     case 10: viewRoomRepairHistory(esql,authorisedUserID,frame); break;
                     case 20: usermenu = false; break;
                     default : System.out.println("Unrecognized choice!"); break;
                  }
               }
               else{
                  switch (userChoice){
                     case 1: viewHotels(esql,frame); break;
                     case 2: viewRooms(esql,frame); break;
                     case 3: bookRooms(esql, authorisedUserID,frame); break;
                     case 4: viewRecentBookingsfromCustomer(esql,authorisedUserID,frame); break;
                     case 20: usermenu = false; break;
                     default : System.out.println("Unrecognized choice!"); break;
                  }
               }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Destroying GUI...");
               frame.dispose();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(JFrame frame, Hotel esql){
      JDialog dialog = new JDialog(frame, "Big Boat Lodge", true);
      JLabel userLabel, passLabel, errorLabel;
      JTextField userField, passField;
      userLabel = new JLabel("Username:");
      passLabel = new JLabel("Password:");
      errorLabel = new JLabel ("");
      userLabel.setFont(smallFont);
      passLabel.setFont(smallFont);
      userField = new JTextField();
      passField = new JTextField();

      JButton submitButton = new JButton("Register");
      submitButton.addActionListener(e -> {
         try{
            // people can have the same names i guess
            String name = userField.getText();
            String password = passField.getText();
            if(name.length() == 0 || password.length() == 0){
               errorLabel.setText("ERROR: invalid name or password");
               return;
            }
            String type="Customer";
            String query = String.format("INSERT INTO USERS (name, password, userType) VALUES ('%s','%s', '%s')", name, password, type);
            esql.executeUpdate(query);
            System.out.println ("User successfully created with userID = " + esql.getNewUserID("SELECT last_value FROM users_userID_seq"));
            errorLabel.setText("User create with userID = " + esql.getNewUserID("SELECT last_value FROM users_userID_seq"));
         }catch(Exception err){
            errorLabel.setText("ERROR: " + err.getMessage());
         }
      });
      
      JPanel formPanel = new JPanel();
      formPanel.setLayout(new GridLayout(3,2));
      formPanel.add(userLabel);
      formPanel.add(userField);
      formPanel.add(passLabel);
      formPanel.add(passField);
      formPanel.add(errorLabel);
      formPanel.add(submitButton);

      dialog.add(formPanel);
      dialog.setSize(500,300);
      dialog.setLocation(200,100);
      dialog.setResizable(false);
      dialog.setVisible(true);
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Hotel esql, String userID, String password){
     try{
         String query = String.format("SELECT * FROM USERS WHERE userID = '%s' AND password = '%s'", userID, password);
         int userNum = esql.executeQuery(query);
         if (userNum > 0)
            return userID;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end


   /*
    * Check if user is a manager or not
    * @return true if manager, false if userID does not exist or is customer
    **/
   public static boolean isManager(Hotel esql, String userID){
      try{
         String query = String.format("SELECT DISTINCT userType FROM USERS WHERE userID = %s", userID);
	      String manager = "manager", admin = "admin";
         List<List<String>> res = esql.executeQueryAndReturnResult(query);
         if(res.size() > 0 && res.get(0).size() > 0 && res.get(0).get(0).contains(manager) || res.get(0).get(0).contains(admin)){
            return true;
         }
         return false;
      }
      catch(Exception e){
         System.err.println (e.getMessage ());
         return false;
      }
   }//end
   // Rest of the functions definition go in here

   /*
    * Get hotels from latitude and longitude
    **/
	public static void viewHotels(Hotel esql, JFrame frame) {
      try{
         JDialog dialog = new JDialog(frame,"Big Boat Lodge", true);
         JLabel latLabel, lonLabel;
         JTextField latField, lonField;
         JButton submitButton, exitButton;
         JPanel inputPane = new JPanel();
         DefaultTableModel model = new DefaultTableModel(new String[]{"Name","Hotel ID"},0);
         JTable resTable = new JTable(model);
         inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.Y_AXIS));

         latLabel = new JLabel("Latitude:");
         latField = new JTextField();
         latField.setFont(smallFont);
         inputPane.add(latLabel);
         inputPane.add(latField);

         lonLabel = new JLabel("Longitude:");
         lonField = new JTextField();
         lonField.setFont(smallFont);
         inputPane.add(lonLabel);
         inputPane.add(lonField);

         submitButton = new JButton("Search");
         submitButton.addActionListener(e -> {
            try{
               DefaultTableModel tableModel = (DefaultTableModel) resTable.getModel();
               tableModel.setRowCount(0);
               double lat = Double.parseDouble(latField.getText());
               double lon = Double.parseDouble(lonField.getText());
               String getHotelPoints = "SELECT hotelName, latitude, longitude, hotelID FROM Hotel";
               List<List<String>> res = esql.executeQueryAndReturnResult(getHotelPoints);
               int rowCount = 1;
               for(int i = 0; i < res.size(); i++){
                  double dis = calculateDistance(lat, lon, Double.parseDouble(res.get(i).get(1)), Double.parseDouble(res.get(i).get(2)));
                  if(dis <= 30){
                     System.out.println(String.valueOf(rowCount) + ". Name: " + res.get(i).get(0) + "Hotel ID: " + res.get(i).get(3));
                     rowCount++;
                     tableModel.addRow(new Object[]{res.get(i).get(0),res.get(i).get(3)});
                  }
               }
            }catch(Exception err){
               DefaultTableModel errorModel = (DefaultTableModel) resTable.getModel();
               errorModel.setRowCount(0);
               errorModel.addRow(new Object[]{"ERROR",err.getMessage()});
            }
         });
         inputPane.add(submitButton);

         exitButton = new JButton("Go back");
         exitButton.addActionListener(e -> {
            dialog.dispose();
         });
         inputPane.add(exitButton);

         JSplitPane queryPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,inputPane,new JScrollPane(resTable));
         dialog.add(queryPane);
         dialog.setSize(500,300);
         dialog.setLocation(200,100);
         dialog.setResizable(false);
         dialog.setVisible(true);
      }
      catch(Exception e){
         System.err.println(e.getMessage());
      }
   }

   public static void viewRooms(Hotel esql, JFrame frame){
      try{
         // System.out.println("Enter Hotel ID: ");
         // String id = in.readLine();
         // System.out.println("Enter date (yyyy-mm-dd): ");
         // String date = in.readLine();
         JDialog dialog = new JDialog(frame,"Big Boat Lodge", true);
         JLabel idLabel, dateLabel;
         JTextField idField, dateField;
         JButton submitButton, exitButton;
         JPanel inputPane = new JPanel();
         DefaultTableModel model = new DefaultTableModel(new String[]{"Room #","Price","Availability","Booking Date"},0);
         JTable resTable = new JTable(model);
         inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.Y_AXIS));

         idLabel = new JLabel("Hotel ID:");
         idField = new JTextField();
         idLabel.setFont(smallFont);
         inputPane.add(idLabel);
         inputPane.add(idField);

         dateLabel = new JLabel("Date (YYYY-MM-DD):");
         dateField = new JTextField();
         dateLabel.setFont(smallFont);
         inputPane.add(dateLabel);
         inputPane.add(dateField);

         submitButton = new JButton("Search");
         submitButton.addActionListener(e -> {
            try{
               DefaultTableModel tableModel = (DefaultTableModel) resTable.getModel();
               tableModel.setRowCount(0);
               int id = Integer.parseInt(idField.getText());
               String date = dateField.getText();
               String query = "SELECT DISTINCT Rooms.roomNumber, Rooms.price, RoomBookings.bookingDate " +
                  "FROM Rooms, RoomBookings WHERE Rooms.hotelID = " + 
                  id + " AND RoomBookings.hotelID = " + id;
               List<List<String>> res = esql.executeQueryAndReturnResult(query);
               int rowCount = 1;
               for(int i = 0; i < res.size(); i++){
                  boolean available = true;
                  if(res.get(i).get(2).contains(date)){
                     available = false;
                  }
                  String avail = "";
                  if(available){
                     avail = "Available!";
                  } else{
                     avail = "Unavailable";
                  }
                  System.out.println(String.valueOf(rowCount)+". " + res.get(i).get(0) + "\t" + res.get(i).get(1) + "\t" + avail + res.get(i).get(2) );
                  tableModel.addRow(new Object[]{res.get(i).get(0),res.get(i).get(1),avail,res.get(i).get(2)});
                  rowCount++;
               }
            }catch(Exception err){
               DefaultTableModel errorModel = (DefaultTableModel) resTable.getModel();
               errorModel.setRowCount(0);
               errorModel.addRow(new Object[]{"ERROR",err.getMessage(),"",""});
            }
         });
         inputPane.add(submitButton);

         exitButton = new JButton("Go back");
         exitButton.addActionListener(e -> {
            dialog.dispose();
         });
         inputPane.add(exitButton);

         JSplitPane queryPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,inputPane,new JScrollPane(resTable));
         dialog.add(queryPane);
         dialog.setSize(500,300);
         dialog.setLocation(200,100);
         dialog.setResizable(false);
         dialog.setVisible(true);
      }
      catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   public static void bookRooms(Hotel esql, String userID, JFrame frame) {
      try{
         JDialog dialog = new JDialog(frame,"Big Boat Lodge", true);
         JLabel idLabel, roomLabel, dateLabel, statusLabel, detailLabel; 
         JTextField idField, roomField, dateField;
         JButton submitButton, exitButton;
         JPanel inputPane = new JPanel(), outputPane = new JPanel();
         inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.Y_AXIS));
         outputPane.setLayout(new BoxLayout(outputPane, BoxLayout.Y_AXIS));

         idLabel = new JLabel("Hotel ID:");
         idField = new JTextField();
         idLabel.setFont(smallFont);
         inputPane.add(idLabel);
         inputPane.add(idField);

         roomLabel = new JLabel("Room Number:");
         roomField = new JTextField();
         roomLabel.setFont(smallFont);
         inputPane.add(roomLabel);
         inputPane.add(roomField);

         dateLabel = new JLabel("Date (YYYY-MM-DD):");
         dateField = new JTextField();
         dateLabel.setFont(smallFont);
         inputPane.add(dateLabel);
         inputPane.add(dateField);

         statusLabel = new JLabel("");
         statusLabel.setFont(largeFont);
         outputPane.add(statusLabel);
         detailLabel = new JLabel("");
         detailLabel.setFont(smallFont);
         outputPane.add(detailLabel);

         submitButton = new JButton("Book Room");
         submitButton.addActionListener(e -> {
            try{
               int id = Integer.parseInt(idField.getText());
               String date = dateField.getText();
               String roomNum = roomField.getText();
               String query = "SELECT bookingDate FROM RoomBookings WHERE roomNumber = " + roomNum + " AND hotelID = " + id;	
               List<List<String>> res1 = esql.executeQueryAndReturnResult(query);
               for(int i = 0; i < res1.size(); i++){
                  if(res1.get(i).get(0).contains(date)){
                     statusLabel.setText("FAILURE");
                     detailLabel.setText("Room Unavailable on Selected Date. Please Try Again Later.");
                     System.out.println("Room Unavailable on Selected Date. Please Try Again Later.");
                     return;
                  }
               }
               String query2 = "SELECT price FROM Rooms WHERE HotelId = " + id + " AND roomNumber = " + roomNum;
               List<List<String>> res2 = esql.executeQueryAndReturnResult(query2);
               String price = String.format("Room #%s booked successfully for %s", roomNum, res2.get(0).get(0));
               System.out.println(price);
               statusLabel.setText("SUCCESS");
               detailLabel.setText(price);
               String update = String.format("INSERT INTO RoomBookings (bookingID, customerID, hotelID, roomNumber, bookingDate) values (%s, %s, %s, %s,'" + date + "')", ++esql.bookingId, userID, id, roomNum);
               esql.executeUpdate(update);
            }catch(Exception err){
               statusLabel.setText("SYSTEM ERROR");
               detailLabel.setText("Error: " + err.getMessage());
            }
         });
         inputPane.add(submitButton);

         exitButton = new JButton("Go back");
         exitButton.addActionListener(e -> {
            dialog.dispose();
         });
         inputPane.add(exitButton);

         JSplitPane queryPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,inputPane,outputPane);
         dialog.add(queryPane);
         dialog.setSize(500,300);
         dialog.setLocation(200,100);
         dialog.setResizable(false);
         dialog.setVisible(true);
      } catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   public static void viewRecentBookingsfromCustomer(Hotel esql, String authorisedUserId, JFrame frame) {
      JDialog dialog = new JDialog(frame,"Big Boat Lodge", true);
      JButton exitButton;
      JPanel inputPane = new JPanel(), outputPane = new JPanel();
      inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.PAGE_AXIS));
      outputPane.setLayout(new BoxLayout(outputPane, BoxLayout.PAGE_AXIS));
      DefaultTableModel model = new DefaultTableModel(new String[]{"Hotel ID","Room #","Price","Booking Date"},0);
      JTable resTable = new JTable(model);
      try{
         String query = "SELECT DISTINCT RB.hotelId, RB.roomNumber, R.price, RB.bookingDate " +
            "FROM RoomBookings RB, Rooms R " +
            "WHERE RB.customerID=" + Integer.parseInt(authorisedUserId) + " AND RB.roomNumber = R.roomNumber " +
            "ORDER BY bookingDate DESC " +
            "LIMIT 5";
         List<List<String>> res = esql.executeQueryAndReturnResult(query);
         DefaultTableModel tableModel = (DefaultTableModel) resTable.getModel();
         tableModel.setRowCount(0);
         for(int i = 0; i < res.size(); ++i){
            tableModel.addRow(new Object[]{res.get(i).get(0),res.get(i).get(1),res.get(i).get(2),res.get(i).get(3)});
         }
         exitButton = new JButton("Go back");
         exitButton.addActionListener(e -> {
            dialog.dispose();
         });
         inputPane.add(exitButton);
         JSplitPane queryPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,new JScrollPane(resTable),inputPane);
         queryPane.setResizeWeight(1.0);
         dialog.add(queryPane);
         dialog.setSize(500,300);
         dialog.setLocation(200,100);
         dialog.setResizable(false);
         dialog.setVisible(true);
      }
      catch(Exception err){
         System.out.println("huh??");
         System.err.println(err.getMessage());
         DefaultTableModel errorModel = (DefaultTableModel) resTable.getModel();
         errorModel.setRowCount(0);
         errorModel.addRow(new Object[]{"ERROR",err.getMessage(),"",""});
      }
   }

   public static void updateRoomInfo(Hotel esql, String userID, JFrame frame) {
      JDialog dialog = new JDialog(frame,"Big Boat Lodge", true);
      JLabel roomLabel1, roomLabel2, priceLabel, imageLabel, status1, status2, detail1, detail2, hotelLabel1, hotelLabel2;
      JTextField roomField1, roomField2, priceField, imageField, hotelField1, hotelField2;
      JButton submitButton1, submitButton2, exitButton1, exitButton2;
      JPanel inputPane1 = new JPanel(), inputPane2 = new JPanel();
      JPanel outputPane1 = new JPanel(), outputPane2 = new JPanel();
      inputPane1.setLayout(new BoxLayout(inputPane1, BoxLayout.Y_AXIS));
      inputPane2.setLayout(new BoxLayout(inputPane2, BoxLayout.Y_AXIS));
      outputPane1.setLayout(new BoxLayout(outputPane1, BoxLayout.Y_AXIS));
      outputPane2.setLayout(new BoxLayout(outputPane2, BoxLayout.Y_AXIS));

      hotelLabel1 = new JLabel("Hotel ID:");
      hotelLabel2 = new JLabel("Hotel ID:");
      hotelField1 = new JTextField();
      hotelField2 = new JTextField();
      hotelLabel1.setFont(smallFont);
      hotelLabel2.setFont(smallFont);
      inputPane1.add(hotelLabel1);
      inputPane2.add(hotelLabel2);
      inputPane1.add(hotelField1);
      inputPane2.add(hotelField2);

      roomLabel1 = new JLabel("Room Number:");
      roomLabel2 = new JLabel("Room Number:");
      roomField1 = new JTextField();
      roomField2 = new JTextField();
      roomLabel1.setFont(smallFont);
      roomLabel2.setFont(smallFont);
      inputPane1.add(roomLabel1);
      inputPane2.add(roomLabel2);
      inputPane1.add(roomField1);
      inputPane2.add(roomField2);

      priceLabel = new JLabel("Price:");
      priceField = new JTextField();
      priceLabel.setFont(smallFont);
      inputPane1.add(priceLabel);
      inputPane1.add(priceField);

      imageLabel = new JLabel("Image URL:");
      imageField = new JTextField();
      imageLabel.setFont(smallFont);
      inputPane2.add(imageLabel);
      inputPane2.add(imageField);

      status1 = new JLabel("");
      status2 = new JLabel("");
      detail1 = new JLabel("");
      detail2 = new JLabel("");
      status1.setFont(largeFont);
      status2.setFont(largeFont);
      detail1.setFont(smallFont);
      detail2.setFont(smallFont);
      outputPane1.add(status1);
      outputPane1.add(detail1);
      outputPane2.add(status2);
      outputPane2.add(detail2);

      submitButton1 = new JButton("Update Price");
      submitButton1.addActionListener(e -> {
         try{
            String hotelID = hotelField1.getText();
            String roomNum = roomField1.getText();
            String newPrice = priceField.getText();
            String idcheck = "SELECT managerUserId From Hotel Where hotelID = " + hotelID;
            List<List<String>> checker = esql.executeQueryAndReturnResult(idcheck);
            if(!checker.get(0).get(0).equals(userID)){ 
               System.out.println("Invalid Manager ID!"); 
               status1.setText("UNAUTHORIZED");
               detail1.setText("User does not manage Hotel " + hotelID + ".");
               return;
            }
            System.out.println("Manager Validated!");
            String update = "UPDATE Rooms SET price = " + newPrice + " WHERE hotelId = " + hotelID + " AND roomNumber = " + roomNum;	
            esql.executeUpdate(update);
            System.out.println("Successfully update Room #" + roomNum + " to: " + newPrice);
            status1.setText("SUCCESS");
            detail1.setText("Successfully update Room #" + roomNum + " to: " + newPrice + ".");
            SimpleDateFormat tstamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Timestamp curr = new Timestamp(System.currentTimeMillis());
            String time = tstamp.format(curr);
            
            String insertUpdate = String.format("INSERT INTO RoomUpdatesLog (updateNumber, managerID, hotelID, roomNumber, updatedOn) values (%s, %s, %s, %s,'" + curr + "')", ++esql.updateNumber, userID, hotelID, roomNum);
            esql.executeUpdate(insertUpdate);
         }catch(Exception err){
            status1.setText("SYSTEM ERROR");
            detail1.setText("Error: " + err.getMessage());
         }
      });
      inputPane1.add(submitButton1);

      submitButton2 = new JButton("Update Url");
      submitButton2.addActionListener(e -> {
         try{
            String hotelID = hotelField1.getText();
            String roomNum = roomField1.getText();
            String url = imageField.getText();
            String idcheck = "SELECT managerUserId From Hotel Where hotelID = " + hotelID;
            List<List<String>> checker = esql.executeQueryAndReturnResult(idcheck);
            if(!checker.get(0).get(0).equals(userID)){ 
               System.out.println("Invalid Manager ID!"); 
               status1.setText("UNAUTHORIZED");
               detail1.setText("User does not manage Hotel " + hotelID + ".");
               return;
            }
            System.out.println("Manager Validated!");
            String update = "UPDATE ROOMS Set imageurl = '" + url + "' WHERE hotelId = " + hotelID + " AND roomNumber = " + roomNum;
            esql.executeUpdate(update);
            System.out.println("Successfully update Room #" +roomNum + " to: " + url);
            status2.setText("SUCCESS");
            detail2.setText("Successfully update image url of Room #" +roomNum + " to: " + url + ".");
            SimpleDateFormat tstamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Timestamp curr = new Timestamp(System.currentTimeMillis());
            String time = tstamp.format(curr);
            
            String insertUpdate = String.format("INSERT INTO RoomUpdatesLog (updateNumber, managerID, hotelID, roomNumber, updatedOn) values (%s, %s, %s, %s,'" + curr + "')", ++esql.updateNumber, userID, hotelID, roomNum);
            esql.executeUpdate(insertUpdate); 
         }catch(Exception err){
            status2.setText("SYSTEM ERROR");
            detail2.setText("Error: " + err.getMessage());
         }
      });
      inputPane2.add(submitButton2);
      exitButton1 = new JButton("Go back");
      exitButton1.addActionListener(e -> {
         dialog.dispose();
      });
      exitButton2 = new JButton("Go back");
      exitButton2.addActionListener(e -> {
         dialog.dispose();
      });
      inputPane1.add(exitButton1);
      inputPane2.add(exitButton2);

      JSplitPane sPane1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,inputPane1,outputPane1);
      JSplitPane sPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,inputPane2,outputPane2);
      JTabbedPane tPane = new JTabbedPane();
      tPane.addTab("Update Price",sPane1);
      tPane.addTab("Update URL",sPane2);
      dialog.add(tPane);
      dialog.setSize(500,300);
      dialog.setLocation(200,100);
      dialog.setResizable(false);
      dialog.setVisible(true);
	}
   public static void viewRecentUpdates(Hotel esql, String userID, JFrame frame) {
      JDialog dialog = new JDialog(frame,"Big Boat Lodge", true);
      JButton exitButton;
      JPanel inputPane = new JPanel(), outputPane = new JPanel();
      inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.PAGE_AXIS));
      outputPane.setLayout(new BoxLayout(outputPane, BoxLayout.PAGE_AXIS));
      DefaultTableModel model = new DefaultTableModel(new String[]{"Update #","Manager ID","Hotel ID","Room #","Updated Date"},0);
      JTable resTable = new JTable(model);
      try{
         String query = "SELECT * FROM RoomUpdatesLog WHERE managerID = " + userID + " ORDER BY updatedOn DESC LIMIT 5";
         List<List<String>> res = esql.executeQueryAndReturnResult(query);
         DefaultTableModel tableModel = (DefaultTableModel) resTable.getModel();
         tableModel.setRowCount(0);
         for(int i = 0; i < res.size(); ++i){
            tableModel.addRow(new Object[]{res.get(i).get(0),res.get(i).get(1),res.get(i).get(2),res.get(i).get(3),res.get(i).get(4)});
         }
         exitButton = new JButton("Go back");
         exitButton.addActionListener(e -> {
            dialog.dispose();
         });
         inputPane.add(exitButton);
         JSplitPane queryPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,new JScrollPane(resTable),inputPane);
         queryPane.setResizeWeight(1.0);
         dialog.add(queryPane);
         dialog.setSize(500,300);
         dialog.setLocation(200,100);
         dialog.setResizable(false);
         dialog.setVisible(true);
      }
      catch(Exception err){
         System.err.println(err.getMessage());
         DefaultTableModel errorModel = (DefaultTableModel) resTable.getModel();
         errorModel.setRowCount(0);
         errorModel.addRow(new Object[]{"ERROR",err.getMessage(),"",""});
      }
	}
   public static void viewBookingHistoryofHotel(Hotel esql, String userID, JFrame frame) {
      JDialog dialog = new JDialog(frame,"Big Boat Lodge", true);
      JCheckBox rangeBox = new JCheckBox("Provide date range?",true);
      JLabel startLabel, endLabel;
      JTextField startField, endField;
      JPanel inputPane = new JPanel(), fieldsPane = new JPanel(), buttonPane = new JPanel();
      JButton submitButton, exitButton;
      DefaultTableModel model = new DefaultTableModel(new String[]{"Booking ID","User Name","Hotel ID","Room Number","Booking Date"},0);
      JTable resTable = new JTable(model);

      inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.Y_AXIS));
      fieldsPane.setLayout(new BoxLayout(fieldsPane, BoxLayout.Y_AXIS));
      buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.Y_AXIS));

      startLabel = new JLabel("Start Date (YYYY-MM-DD):");
      startField = new JTextField();
      startLabel.setFont(smallFont);
      fieldsPane.add(startLabel);
      fieldsPane.add(startField);
      endLabel = new JLabel("End Date (YYYY-MM-DD):");
      endLabel.setFont(smallFont);
      endField = new JTextField();
      fieldsPane.add(endLabel);
      fieldsPane.add(endField);
      
      rangeBox.addActionListener(e -> {
         inputPane.remove(buttonPane);
         if(rangeBox.isSelected()){
            inputPane.add(fieldsPane);
         }
         else{
            inputPane.remove(fieldsPane);
         }
         inputPane.add(buttonPane);
         inputPane.revalidate();
         inputPane.repaint();
      });

      submitButton = new JButton("Search");
      submitButton.addActionListener(e -> {
         try{
            String query = null;
            if(rangeBox.isSelected()){
               String startDate = startField.getText();
               String endDate = endField.getText();
               query = "SELECT RB.bookingID, U.name, RB.hotelID, RB.roomNumber, RB.bookingDate " +
                  "FROM Users U, Hotel H, RoomBookings RB " + 
                  "WHERE RB.customerID = u.userID AND " +
                  "H.HotelID = RB.HotelId AND H.managerUserID = " + userID + 
                  " AND RB.bookingDate >= '" + startDate + "' AND RB.bookingDate <= '" + endDate +"'";		
            }
            else{
               query = "SELECT RB.bookingID, U.name, RB.hotelID, RB.roomNumber, RB.bookingDate " +
                  "FROM Users U, Hotel H, RoomBookings RB WHERE " +
                  "RB.customerID = u.userID AND " +
                  "H.HotelID = RB.HotelId AND H.managerUserID = " + userID;
            }
            List<List<String>> res = esql.executeQueryAndReturnResult(query);
            DefaultTableModel tableModel = (DefaultTableModel) resTable.getModel();
            tableModel.setRowCount(0);
            System.out.println("PRINTING TABLE " + res.size());
            for(int i = 0; i < res.size(); ++i){
               System.out.println(res.get(i));
               tableModel.addRow(new Object[]{res.get(i).get(0),res.get(i).get(1),res.get(i).get(2),res.get(i).get(3),res.get(i).get(4)});
            }
         }catch(Exception err){
            System.err.print(err.getMessage());
            DefaultTableModel errorModel = (DefaultTableModel) resTable.getModel();
            errorModel.setRowCount(0);
            errorModel.addRow(new Object[]{"ERROR",err.getMessage(),"","",""});
         }
      });	
      buttonPane.add(submitButton);

      exitButton = new JButton("Go back");
      exitButton.addActionListener(e -> {
         dialog.dispose();
      });
      buttonPane.add(exitButton);

      inputPane.add(rangeBox);
      inputPane.add(fieldsPane);
      inputPane.add(buttonPane);


      JSplitPane queryPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,inputPane,new JScrollPane(resTable));
      dialog.add(queryPane);
      dialog.setSize(500,300);
      dialog.setLocation(200,100);
      dialog.setResizable(false);
      dialog.setVisible(true);
	}
   public static void viewRegularCustomers(Hotel esql, String userID, JFrame frame) {
      JDialog dialog = new JDialog(frame,"Big Boat Lodge",true);
      JPanel inputPane = new JPanel();
      DefaultTableModel model = new DefaultTableModel(new String[]{"Name"},0);
      JTable resTable = new JTable(model); 
      JLabel hotelLabel = new JLabel("Hotel ID:");
      JTextField hotelField = new JTextField();
      JButton submitButton, exitButton;

      inputPane.setLayout(new BoxLayout(inputPane,BoxLayout.Y_AXIS));
      hotelLabel.setFont(smallFont);
      inputPane.add(hotelLabel);
      inputPane.add(hotelField);

      submitButton = new JButton("Search");
      submitButton.addActionListener(e -> {
         try{
            String hotelID = hotelField.getText();
            String idcheck = "SELECT managerUserId From Hotel Where hotelID = " + hotelID;
            List<List<String>> checker = esql.executeQueryAndReturnResult(idcheck);
            DefaultTableModel tableModel = (DefaultTableModel) resTable.getModel();
            tableModel.setRowCount(0);
            if(!checker.get(0).get(0).equals(userID)){
               System.out.println("Invalid Manager ID!");
               tableModel.addRow(new Object[]{"Error: Unauthorized operation"});
               return;
            }
            System.out.println("Manager Validated!");
            String query = "SELECT U.name FROM Users U, RoomBookings R1 WHERE R1.hotelID = " + hotelID + " AND R1.customerID = U.userID GROUP BY U.name ORDER BY COUNT(*) DESC  LIMIT 5";
            List<List<String>> res = esql.executeQueryAndReturnResult(query);
            for(int i = 0; i < res.size(); ++i){
               tableModel.addRow(new Object[]{res.get(i).get(0)});
            }
         }
         catch(Exception err){
            System.err.print(err.getMessage());
            DefaultTableModel errorModel = (DefaultTableModel) resTable.getModel();
            errorModel.setRowCount(0);
            errorModel.addRow(new Object[]{"ERROR " + err.getMessage()});
         }
      });
      inputPane.add(submitButton);

      exitButton = new JButton("Go back");
      exitButton.addActionListener(e -> {
         dialog.dispose();
      });
      inputPane.add(exitButton);

      JSplitPane queryPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,inputPane,new JScrollPane(resTable));
      dialog.add(queryPane);
      dialog.setSize(500,300);
      dialog.setLocation(200,100);
      dialog.setResizable(false);
      dialog.setVisible(true);
	}
   public static void placeRoomRepairRequests(Hotel esql, String userID, JFrame frame) {
      JDialog dialog = new JDialog(frame,"Big Boat Lodge",true);
      JLabel hotelLabel, roomLabel, companyLabel, statusLabel, detailLabel;
      JTextField hotelField, roomField, companyField;
      JPanel inputPane = new JPanel(), outputPane = new JPanel();
      inputPane.setLayout(new BoxLayout(inputPane,BoxLayout.Y_AXIS));
      outputPane.setLayout(new BoxLayout(outputPane,BoxLayout.Y_AXIS));

      hotelLabel = new JLabel("Hotel ID:");
      roomLabel = new JLabel("Room Number:");
      companyLabel = new JLabel("Company ID:");
      statusLabel = new JLabel("");
      detailLabel = new JLabel("");
      hotelLabel.setFont(smallFont);
      statusLabel.setFont(largeFont);
      detailLabel.setFont(smallFont);
      roomLabel.setFont(smallFont);
      companyLabel.setFont(smallFont);
      hotelField = new JTextField();
      roomField = new JTextField();
      companyField = new JTextField();
      inputPane.add(hotelLabel);
      inputPane.add(hotelField);
      inputPane.add(roomLabel);
      inputPane.add(roomField);
      inputPane.add(companyLabel);
      inputPane.add(companyField);
      outputPane.add(statusLabel);
      outputPane.add(detailLabel);

      JButton submitButton = new JButton("Place Request");
      submitButton.addActionListener(e -> {
         try{
            String hotelID = hotelField.getText();
            String roomNum = roomField.getText();
            String companyID = companyField.getText();
            String idcheck = "SELECT managerUserId From Hotel Where hotelID = " + hotelID;
            List<List<String>> checker = esql.executeQueryAndReturnResult(idcheck);
            if(!checker.get(0).get(0).equals(userID)){
               System.out.println("Invalid Manager ID!");
               statusLabel.setText("UNAUTHORIZED");
               detailLabel.setText("User not authorized to perform operation on hotel " + hotelID + ".");
               return;
            }
            SimpleDateFormat tstamp = new SimpleDateFormat("yyyy-MM-dd");
            Timestamp curr = new Timestamp(System.currentTimeMillis());
            String time = tstamp.format(curr);

            System.out.println("Manager Validated!");
            String query = String.format("INSERT INTO RoomRepairs (repairID, companyID, hotelID, roomNumber, repairDate) values (" + ++esql.repairId + ", %s, %s, %s, '" + time + "')", companyID, hotelID, roomNum);
            esql.executeUpdate(query);
            String tempQuery = "SELECT repairID from RoomRepairs WHERE hotelID = " + hotelID + " ORDER BY repairDate DESC";
            List<List<String>> res1 = esql.executeQueryAndReturnResult(tempQuery);
            String repairId = res1.get(0).get(0);
            String anotherQuery = String.format("INSERT INTO RoomRepairRequests (requestNumber, managerID, repairID) values (" + ++esql.requestNumber + ", %s, " + repairId + ")" , userID); 
            esql.executeUpdate(anotherQuery);
            statusLabel.setText("SUCCESS");
            detailLabel.setText("Successfully placed room repair request (" + esql.requestNumber + "," + repairId + "," + userID + ")!");
         }
         catch(Exception err){
            System.err.print(err.getMessage());
            statusLabel.setText("FAILURE");
            detailLabel.setText("ERROR: " + err.getMessage());
         }
      });
      inputPane.add(submitButton);

      JButton exitButton = new JButton("Go back");
      exitButton.addActionListener(e -> {
         dialog.dispose();
      });
      inputPane.add(exitButton);

      JSplitPane queryPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,inputPane,outputPane);
      dialog.add(queryPane);
      dialog.setSize(500,300);
      dialog.setLocation(200,100);
      dialog.setResizable(false);
      dialog.setVisible(true);
	}
   public static void viewRoomRepairHistory(Hotel esql, String userID, JFrame frame) {
      JDialog dialog = new JDialog(frame,"Big Boat Lodge",true);
      DefaultTableModel model = new DefaultTableModel(new String[]{"Company ID","Hotel ID","Room Number","Repair Date"},0);
      JTable resTable = new JTable(model);
      try{
         DefaultTableModel tableModel = (DefaultTableModel) resTable.getModel();
         String query = "SELECT RR.companyID, RR.hotelID, RR.roomNumber, RR.repairDate FROM RoomRepairRequests RRR, RoomRepairs RR WHERE RRR.managerID = " + userID + " AND RRR.repairID = RR.repairID";
         List<List<String>> res = esql.executeQueryAndReturnResult(query);
         for(int i = 0; i < res.size(); ++i){
            tableModel.addRow(new Object[]{res.get(i).get(0),res.get(i).get(1),res.get(i).get(2),res.get(i).get(3)});
         }
      }catch(Exception e){
         DefaultTableModel errorModel = (DefaultTableModel) resTable.getModel();
         errorModel.addRow(new Object[]{"ERROR",e.getMessage(),"",""});
         System.err.println(e.getMessage());
      }
      JPanel buttonPane = new JPanel();
      buttonPane.setLayout(new BoxLayout(buttonPane,BoxLayout.PAGE_AXIS));
      JButton exitButton = new JButton("Go back");
      exitButton.addActionListener(e -> {
         dialog.dispose();
      });
      buttonPane.add(exitButton);
      JSplitPane sPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,new JScrollPane(resTable),buttonPane);
      sPane.setResizeWeight(1.0);
      dialog.add(sPane);
      dialog.setSize(500,300);
      dialog.setLocation(200,100);
      dialog.setResizable(false);
      dialog.setVisible(true);
   }

}//end Hotel
//And the boat floats...

