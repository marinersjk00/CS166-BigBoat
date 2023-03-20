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
      Font largeFont = new Font("Liberation Serif", Font.PLAIN, 25);
      Font smallFont = new Font("Liberation Serif", Font.PLAIN, 15);

      JLabel userLabel, passLabel, errorLabel;
      JTextField userField, passField;
      JDialog dialog = new JDialog(frame,"Login", true);
      JPanel loginPanel = new JPanel();
      loginPanel.setLayout(new GridLayout(3,2));

      userLabel = new JLabel("username:");
      userLabel.setFont(smallFont);
      userField = new JTextField(15);
      loginPanel.add(userLabel);
      loginPanel.add(userField);

      passLabel = new JLabel("password:");
      passLabel.setFont(smallFont);
      passField = new JTextField(15);
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
      dialog.setVisible(true);
      
      // continue execution here
      return userField.getText();
   }

   public static int mainMenu(JFrame frame, Map<String,Integer> options){
      Font largeFont = new Font("Liberation Serif", Font.PLAIN, 25);
      Font smallFont = new Font("Liberation Serif", Font.PLAIN, 15);
      JDialog dialog = new JDialog(frame,"Main Menu", true);
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
               case 1: CreateUser(esql); break;
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
               if(userIsManager){
                  switch (userChoice){
                     case 1: viewHotels(esql); break;
                     case 2: viewRooms(esql); break;
                     case 3: bookRooms(esql, authorisedUserID); break;
                     case 4: viewRecentBookingsfromCustomer(esql,authorisedUserID); break;
                     case 5: updateRoomInfo(esql, authorisedUserID); break;
                     case 6: viewRecentUpdates(esql, authorisedUserID); break;
                     case 7: viewBookingHistoryofHotel(esql, authorisedUserID); break;
                     case 8: viewRegularCustomers(esql, authorisedUserID); break;
                     case 9: placeRoomRepairRequests(esql, authorisedUserID); break;
                     case 10: viewRoomRepairHistory(esql,authorisedUserID); break;
                     case 20: usermenu = false; break;
                     default : System.out.println("Unrecognized choice!"); break;
                  }
               }
               else{
                  switch (userChoice){
                     case 1: viewHotels(esql); break;
                     case 2: viewRooms(esql); break;
                     case 3: bookRooms(esql, authorisedUserID); break;
                     case 4: viewRecentBookingsfromCustomer(esql,authorisedUserID); break;
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
               System.out.println("Done\n\nBye ! (use Ctrl+C to exit)");
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
   public static void CreateUser(Hotel esql){
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine(); 
         String type="Customer";
			String query = String.format("INSERT INTO USERS (name, password, userType) VALUES ('%s','%s', '%s')", name, password, type);
         esql.executeUpdate(query);
         System.out.println ("User successfully created with userID = " + esql.getNewUserID("SELECT last_value FROM users_userID_seq"));
      
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
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
	public static void viewHotels(Hotel esql) {
      try{
         System.out.println("Enter latitude: ");
         double lat = Double.parseDouble(in.readLine());
         System.out.println("Enter longitude: ");
         double lon = Double.parseDouble(in.readLine());
         
         String getHotelPoints = "SELECT hotelName, latitude, longitude, hotelID FROM Hotel";
         List<List<String>> res = esql.executeQueryAndReturnResult(getHotelPoints);
         int rowCount = 1;
         for(int i = 0; i < res.size(); i++){
            double dis = calculateDistance(lat, lon, Double.parseDouble(res.get(i).get(1)), Double.parseDouble(res.get(i).get(2)));
            if(dis <= 30){
               System.out.println(String.valueOf(rowCount) + ". Name: " + res.get(i).get(0) + "Hotel ID: " + res.get(i).get(3));
               rowCount++;
            }
         }
      }
      catch(Exception e){
         System.err.println(e.getMessage());
      }
   }

   public static void viewRooms(Hotel esql){
      try{
         System.out.println("Enter Hotel ID: ");
         String id = in.readLine();
         System.out.println("Enter date (yyyy-mm-dd): ");
         String date = in.readLine();

         String query = "SELECT DISTINCT Rooms.roomNumber, Rooms.price, RoomBookings.bookingDate FROM Rooms, RoomBookings WHERE Rooms.hotelID = " + String.valueOf(id) + " AND RoomBookings.hotelID = " + id;

         List<List<String>> res = esql.executeQueryAndReturnResult(query);

         int rowCount = 1;
         for(int i = 0; i < res.size(); i++){
            boolean available = true;
            if(res.get(i).get(2).equals(date)){
                  available = false;
            }
            String avail = "";
            if(available){
               avail = "Available!";
            } else{
               avail = "Unavailable";
            }
            System.out.println(String.valueOf(rowCount)+". " + res.get(i).get(0) + "\t" + res.get(i).get(1) + "\t" + avail + res.get(i).get(2) );
            rowCount++;
         }
      }
      catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   public static void bookRooms(Hotel esql, String userID) {
      try{
         System.out.println("Enter Hotel ID: ");
         String id = in.readLine();
         System.out.println("Enter Room Number: ");
         String roomNum = in.readLine();
         System.out.println("Enter date (yyyy-mm-dd): ");
         String date = in.readLine();

         String query = "SELECT bookingDate FROM RoomBookings WHERE roomNumber = " + roomNum + " AND hotelID = " + id;	
         List<List<String>> res1 = esql.executeQueryAndReturnResult(query);
         for(int i = 0; i < res1.size(); i++){
            if(res1.get(i).get(0).equals(date)){
               System.out.println("Room Unavailable on Selected Date. Please Try Again Later.");
               return;
            }
         }
         String query2 = "SELECT price FROM Rooms WHERE HotelId = " + id + " AND roomNumber = " + roomNum;
         List<List<String>> res2 = esql.executeQueryAndReturnResult(query2);
         String price = String.format("Room #%s booked successfully for %s", roomNum, res2.get(0).get(0));
         System.out.println(price);
         String update = String.format("INSERT INTO RoomBookings (bookingID, customerID, hotelID, roomNumber, bookingDate) values (%s, %s, %s, %s,'" + date + "')", ++esql.bookingId, userID, id, roomNum);
	 esql.executeUpdate(update);
         } catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   public static void viewRecentBookingsfromCustomer(Hotel esql, String authorisedUserId) {
      // TODO: how to get billing information?
      try{
         String query = "SELECT RB.hotelId, RB.roomNumber, RB.bookingDate " +
            "FROM RoomBookings RB " +
            "WHERE RB.customerID=" + authorisedUserId + " " +
            "ORDER BY bookingDate DESC " +
            "LIMIT 5";
         int res = esql.executeQueryAndPrintResult(query);
      }
      catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   public static void updateRoomInfo(Hotel esql, String userID) {
	try{
	System.out.println("Enter Hotel ID: ");
	String hotelID = in.readLine();
	String idcheck = "SELECT managerUserId From Hotel Where hotelID = " + hotelID;
	List<List<String>> checker = esql.executeQueryAndReturnResult(idcheck);

	if(!checker.get(0).get(0).equals(userID)){ 
	System.out.println("Invalid Manager ID!"); 
	return;
	}else{
		System.out.println("Manager Validated!");
		System.out.println("1. Price Update\n2. image url update\nEnter choice: ");
		String choice = in.readLine();
		System.out.println("Enter Room Number: ");
		String roomNum = in.readLine();
		if(choice.equals("1")){
			System.out.println("Enter new price for Room #" + roomNum +": ");
			String newPrice = in.readLine();
			String update = "UPDATE Rooms SET price = " + newPrice + " WHERE hotelId = " + hotelID + " AND roomNumber = " + roomNum;	
			esql.executeUpdate(update);
			System.out.println("Successfully update Room #" + roomNum + " to: " + newPrice);
		}else if(choice.equals("2")){
			System.out.println("Enter new image url for Room #" + roomNum);
			String url = in.readLine();
			String update = "UPDATE ROOMS Set imageurl = '" + url + "' WHERE hotelId = " + hotelID + " AND roomNumber = " + roomNum;
			esql.executeUpdate(update);
			System.out.println("Successfully update Room #" +roomNum + " to: " + url);
	}
	SimpleDateFormat tstamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Timestamp curr = new Timestamp(System.currentTimeMillis());
	String time = tstamp.format(curr);
	
	String insertUpdate = String.format("INSERT INTO RoomUpdatesLog (updateNumber, managerID, hotelID, roomNumber, updatedOn) values (%s, %s, %s, %s,'" + curr + "')", ++esql.updateNumber, userID, hotelID, roomNum);
	esql.executeUpdate(insertUpdate); 

	}
	 
	}catch(Exception e){
	System.err.println(e.getMessage());
	}	
	}
   public static void viewRecentUpdates(Hotel esql, String userID) {
	
	try{
		String query = "SELECT * FROM RoomUpdatesLog WHERE managerID = " + userID + " ORDER BY updatedOn DESC LIMIT 5";
	System.out.println("ismanager called");
	int res = esql.executeQueryAndPrintResult(query);
	}catch(Exception e){
	System.err.println(e.getMessage());
	}
	}
   public static void viewBookingHistoryofHotel(Hotel esql, String userID) {
	try{
		System.out.println("Enter date range? (y/n)");
		String choice = in.readLine();
		String query = "";
		if(choice.equals("y")){
			System.out.println("Enter start date (yyyy-mm-dd): ");
			String startDate = in.readLine();
			System.out.println("Enter end date (yyyy-mm-dd): ");
			String endDate = in.readLine();
			  query = "SELECT RB.bookingID, U.name, RB.hotelID, RB.roomNumber, RB.bookingDate FROM Users U, Hotel H, RoomBookings RB WHERE RB.customerID = u.userID AND H.HotelID = RB.HotelId AND H.managerUserID = " + userID + " AND RB.bookingDate >= '" + startDate + "' AND RB.bookingDate <= '" + endDate +"'";		
		}else if(choice.equals("n")){
		query = "SELECT RB.bookingID, U.name, RB.hotelID, RB.roomNumber, RB.bookingDate FROM Users U, Hotel H, RoomBookings RB WHERE RB.customerID = u.userID AND H.HotelID = RB.HotelId AND H.managerUserID = " + userID;
	}
		int res = esql.executeQueryAndPrintResult(query);
	}catch(Exception e){
	System.err.println(e.getMessage());
	}
	
	}
   public static void viewRegularCustomers(Hotel esql, String userID) {

	try{
		System.out.println("Enter Hotel ID: ");
        String hotelID = in.readLine();
        String idcheck = "SELECT managerUserId From Hotel Where hotelID = " + hotelID;
        List<List<String>> checker = esql.executeQueryAndReturnResult(idcheck);

        if(!checker.get(0).get(0).equals(userID)){
        System.out.println("Invalid Manager ID!");
        return;
        }else{
		System.out.println("Manager Validated!");
		String query = "SELECT U.name FROM Users U, RoomBookings R1		    WHERE R1.hotelID = " + hotelID + " AND R1.customerID = U.userID GROUP BY U.name ORDER BY COUNT(*) DESC  LIMIT 5";
		int res = esql.executeQueryAndPrintResult(query);
	}		
	}catch(Exception e){
		System.err.println(e.getMessage());
	}

	}
   public static void placeRoomRepairRequests(Hotel esql, String userID) {
  try{
                System.out.println("Enter Hotel ID: ");
        String hotelID = in.readLine();
        String idcheck = "SELECT managerUserId From Hotel Where hotelID = " + hotelID;
        List<List<String>> checker = esql.executeQueryAndReturnResult(idcheck);

        if(!checker.get(0).get(0).equals(userID)){
        System.out.println("Invalid Manager ID!");
        return;
        }else{

		SimpleDateFormat tstamp = new SimpleDateFormat("yyyy-MM-dd");
        Timestamp curr = new Timestamp(System.currentTimeMillis());
        String time = tstamp.format(curr);

                System.out.println("Manager Validated!");
		System.out.println("Enter Room Number: ");
		String roomNum = in.readLine();
		System.out.println("Enter companyID to handle repair: ");
		String companyID = in.readLine();
		String query = String.format("INSERT INTO RoomRepairs (repairID, companyID, hotelID, roomNumber, repairDate) values (" + ++esql.repairId + ", %s, %s, %s, '" + time + "')", companyID, hotelID, roomNum);
	esql.executeUpdate(query);
		String tempQuery = "SELECT repairID from RoomRepairs WHERE hotelID = " + hotelID + " ORDER BY repairDate DESC";
		List<List<String>> res1 = esql.executeQueryAndReturnResult(tempQuery);
		String repairId = res1.get(0).get(0);
		String anotherQuery = String.format("INSERT INTO RoomRepairRequests (requestNumber, managerID, repairID) values (" + ++esql.requestNumber + ", %s, " + repairId + ")" , userID); 
		esql.executeUpdate(anotherQuery);
        }
        }catch(Exception e){
                System.err.println(e.getMessage());
        }
	}
   public static void viewRoomRepairHistory(Hotel esql, String userID) {
 	try{
	 System.out.println("Enter Hotel ID: ");
        String hotelID = in.readLine();
        String idcheck = "SELECT managerUserId From Hotel Where hotelID = " + hotelID;
        List<List<String>> checker = esql.executeQueryAndReturnResult(idcheck);

        if(!checker.get(0).get(0).equals(userID)){
        System.out.println("Invalid Manager ID!");
        return;
        }else{
		String query = "SELECT RR.companyID, RR.hotelID, RR.roomNumber, RR.repairDate FROM RoomRepairRequests RRR, RoomRepairs RR WHERE RRR.managerID = " + userID + " AND RRR.repairID = RR.repairID";
	int res = esql.executeQueryAndPrintResult(query);
	}
	}catch(Exception e){
		System.err.println(e.getMessage());
	}
	
	 }

}//end Hotel
//And the boat floats...

