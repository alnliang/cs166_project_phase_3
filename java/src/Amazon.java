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


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Amazon {

   // reference to physical database connection.
   private Connection _connection = null;


   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Amazon store
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Amazon(String dbname, String dbport, String user, String passwd) throws SQLException {

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
   }//end Amazon

   // Method to calculate euclidean distance between two latitude, longitude pairs. 
   public double calculateDistance (double lat1, double long1, double lat2, double long2){
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

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   boolean loggedIn = false;
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Amazon.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      Amazon esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Amazon object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Amazon (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Stores within 30 miles");
                System.out.println("2. View Product List");
                System.out.println("3. Place a Order");
                System.out.println("4. View 5 recent orders");

                //the following functionalities basically used by managers
                System.out.println("5. Update Product");
                System.out.println("6. View 5 recent Product Updates Info");
                System.out.println("7. View 5 Popular Items");
                System.out.println("8. View 5 Popular Customers");
                System.out.println("9. Place Product Supply Request to Warehouse");

                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: viewStores(esql, authorisedUser); break;
                   case 2: viewProducts(esql); break;
                   case 3: placeOrder(esql, authorisedUser); break;
                   case 4: viewRecentOrders(esql,authorisedUser); break;
                   case 5: updateProduct(esql, authorisedUser); break;
                   case 6: viewRecentUpdates(esql, authorisedUser); break;
                   case 7: viewPopularProducts(esql, authorisedUser); break;
                   case 8: viewPopularCustomers(esql, authorisedUser); break;
                   case 9: placeProductSupplyRequests(esql, authorisedUser); break;
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
   public static void CreateUser(Amazon esql){
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();
         System.out.print("\tEnter latitude: ");   
         String latitude = in.readLine();       //enter lat value between [0.0, 100.0]
         System.out.print("\tEnter longitude: ");  //enter long value between [0.0, 100.0]
         String longitude = in.readLine();
         
         String type="Customer";

			String query = String.format("INSERT INTO USERS (name, password, latitude, longitude, type) VALUES ('%s','%s', %s, %s,'%s')", name, password, latitude, longitude, type);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Amazon esql){
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USERS WHERE name = '%s' AND password = '%s'", name, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0){
		return name;
    }
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

// Rest of the functions definition go in here
   public static void viewStores(Amazon esql, String authorized) {
      try{
         String query = String.format("SELECT latitude, longitude FROM USERS WHERE name = '%s'", authorized);
         List<List<String> > res = esql.executeQueryAndReturnResult(query);
         double latInt = 0;
         double longInt = 0;
         for(int i = 0; i < res.size(); i++){
            String latString = res.get(i).get(0);
            String longString = res.get(i).get(1);
            latInt = Double.parseDouble(latString);
            longInt = Double.parseDouble(longString);
         }
         String storeQuery = String.format("SELECT storeid, latitude, longitude FROM STORE");
         List<List<String> > storeList = esql.executeQueryAndReturnResult(storeQuery);
         for(int j = 0; j < storeList.size(); j++){
            String storeID = storeList.get(j).get(0);
            String storeLat = storeList.get(j).get(1);
            String storeLong = storeList.get(j).get(2);
            double storeLatInt = Double.parseDouble(storeLat);
            double storeLongInt = Double.parseDouble(storeLong);
            double distFromStore = esql.calculateDistance(latInt, longInt, storeLatInt, storeLongInt);
            if(distFromStore <= 30.0){
               System.out.println("Store ID: " + storeID + ", Distance: " + distFromStore);
            }
         }
      } catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   public static void viewProducts(Amazon esql) {
      try{
         System.out.print("\tEnter Store ID: ");
         String storeID = in.readLine();
         String query = String.format ("SELECT p.productname, p.numberofunits, p.priceperunit FROM product p WHERE p.storeid = '%s'",storeID);
         esql.executeQueryAndPrintResult(query);
      }
      catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }
   public static void placeOrder(Amazon esql, String authorized) {
      try{
         String query = String.format("SELECT latitude, longitude, userid FROM USERS WHERE name = '%s';", authorized);
         List<List<String> > res = esql.executeQueryAndReturnResult(query);
         double latInt = 0;
         double longInt = 0;
         String userID = "";
         for(int i = 0; i < res.size(); i++){
            String latString = res.get(i).get(0);
            String longString = res.get(i).get(1);
            userID = res.get(i).get(2);
            latInt = Double.parseDouble(latString);
            longInt = Double.parseDouble(longString);
         }
         System.out.print("\tEnter Store ID: ");
         String storeID = in.readLine();
         System.out.print("\tEnter Product Name: ");
         String prodName = in.readLine();
         System.out.print("\tEnter Quantity Purchased: ");
         String numBoughtString = in.readLine();
         int numBought = Integer.parseInt(numBoughtString);
         String storeQuery = String.format("SELECT latitude, longitude FROM STORE WHERE storeid = %s;", storeID);
         List<List<String> > storeList = esql.executeQueryAndReturnResult(storeQuery);
         String storeLatString = storeList.get(0).get(0);
         double storeLat = Double.parseDouble(storeLatString);
         String storeLongString = storeList.get(0).get(1);
         double storeLong = Double.parseDouble(storeLongString);
         if(esql.calculateDistance(latInt, longInt, storeLat, storeLong) > 30){
	    //System.out.println(esql.calculateDistance(latInt, longInt, storeLat, storeLong));
            System.out.println("Store not within 30 mile radius");
            return;
         }
         String getQuantity = String.format("SELECT numberofunits FROM PRODUCT WHERE storeid = %s AND productname = '%s';", storeID, prodName);
         //System.out.println(getQuantity);
         List<List<String> > quantityList = esql.executeQueryAndReturnResult(getQuantity);
         String quantityString = quantityList.get(0).get(0);
         int quantity = Integer.parseInt(quantityString) - numBought;
         String updateQuery = String.format("UPDATE product SET numberofunits = %s WHERE storeid = %s AND productname = '%s';", quantity, storeID, prodName);
         //System.out.println(updateQuery);
         esql.executeUpdate(updateQuery);
         //7up 12
         LocalDateTime datetime = LocalDateTime.now();
         DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
         String formattedDate = datetime.format(formatterDate);
         String getOrderNumQuery = String.format("SELECT MAX(ordernumber) FROM orders");
         List<List<String> > getMax = esql.executeQueryAndReturnResult(getOrderNumQuery);
         String orderNumString = getMax.get(0).get(0);
         int orderNum = Integer.parseInt(orderNumString);
         orderNum += 1;
         String insertQuery = String.format("INSERT INTO ORDERS(ordernumber, customerid, storeid, productname, unitsordered, ordertime) VALUES(%s, %s, %s, '%s', %s, '%s');", orderNum, userID, storeID, prodName, numBought, formattedDate);
         //System.out.println(insertQuery);
         esql.executeUpdate(insertQuery);
      } catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   public static void viewRecentOrders(Amazon esql, String authorisedUser) {
      try{
        String getUserIDQuery = String.format ("SELECT u.UserID FROM Users u WHERE u.name = '%s'", authorisedUser);
        String result = esql.executeQueryAndReturnResult(getUserIDQuery).get(0).get(0);
        int customerID = Integer.parseInt(result);
        String getOrdersQuery = String.format ("SELECT o.storeID, o.productName, o.unitsOrdered, o.orderTime FROM Orders o Where o.customerid = %s ORDER BY orderTime DESC", customerID);
        System.out.print(getOrdersQuery);
        List<List<String> > OrdersTable = esql.executeQueryAndReturnResult(getOrdersQuery) ;
         for(int i = 0; i < 5; i++){
            System.out.println("Store ID: " + OrdersTable.get(i).get(0) + "Product Name: " + OrdersTable.get(i).get(1) + "Units Ordered: "+ OrdersTable.get(i).get(2) +  "Order Time: "+ OrdersTable.get(i).get(3));
         }

      }
      catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }
   public static void updateProduct(Amazon esql, String authorized) {
      try{
         String query = String.format("SELECT userid FROM USERS WHERE name = '%s';", authorized);
         List<List<String> > res = esql.executeQueryAndReturnResult(query);
         String userID = "";
         for(int i = 0; i < res.size(); i++){
            userID = res.get(i).get(0);
         }
         System.out.print("\tEnter store ID: ");
         String storeID = in.readLine();
         String storeQuery = String.format("SELECT managerid FROM STORE WHERE storeid = %s AND managerid = %s", storeID, userID);
         int storeNum = esql.executeQuery(storeQuery);
         if(storeNum <= 0){
            System.out.println("You don't manage this store.");
            return;
         }
         System.out.print("\tWhich product do you want to edit? ");
         //Switch case later
         String productName = in.readLine();
         System.out.print("\tWhat do you want to edit? ");
         //Switch case later
         String columnToEdit = in.readLine();
         System.out.print("\tWhat do you want to change it to? ");
         String newValue = in.readLine();
         String updateQuery = "";
         String newUpdate = String.format("SELECT MAX(updatenumber) FROM PRODUCTUPDATES");
         List<List<String> > updates = esql.executeQueryAndReturnResult(newUpdate);
         int updateNum = Integer.parseInt(updates.get(0).get(0));
         updateNum += 1;
         if(columnToEdit == "productname"){
            updateQuery = String.format("UPDATE PRODUCT SET productname = '%s' WHERE productname = '%s' AND storeid = %s", newValue, productName, storeID);
         }
         else{
            updateQuery = String.format("UPDATE PRODUCT SET %s = %s WHERE productname = '%s' AND storeid = %s", columnToEdit, newValue, productName, storeID);
         }
         LocalDateTime datetime = LocalDateTime.now();
         DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
         String formattedDate = datetime.format(formatterDate);
         String addToUpdates = String.format("INSERT INTO PRODUCTUPDATES(updatenumber, managerid, storeid, productname, updatedon) VALUES(%s, %s, %s, '%s', '%s')", updateNum, userID, storeID, productName, formattedDate);
         esql.executeUpdate(addToUpdates);
         //System.out.println(updateQuery);
         esql.executeUpdate(updateQuery);
      } catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   public static void viewRecentUpdates(Amazon esql, String authorisedUser) {
      try{
         String getUserIDQuery = String.format ("SELECT u.UserID FROM Users u WHERE u.name = '%s'", authorisedUser);
         String result = esql.executeQueryAndReturnResult(getUserIDQuery).get(0).get(0);
         int manangerID = Integer.parseInt(result);
         String getRecentUpdatesQuery = String.format ("SELECT prod_update.updateNumber, prod_update.storeID, prod_update.productName,prod_update.updatedOn FROM productUpdates prod_update WHERE prod_update.managerID = %s ORDER BY prod_update.updatedON DESC", manangerID);
         List<List<String> > UpdatesTable = esql.executeQueryAndReturnResult(getRecentUpdatesQuery) ;
         for(int i = 0; i < 5; i++){
            System.out.println("Update Number: " + UpdatesTable.get(i).get(0) + "Store ID: " + UpdatesTable.get(i).get(1) + "Product Name: "+ UpdatesTable.get(i).get(2) +  "Updated On: "+ UpdatesTable.get(i).get(3));
         }
      }
      catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }
   public static void viewPopularProducts(Amazon esql, String authorized) {
      try{
         String query = String.format("SELECT userid FROM USERS WHERE name = '%s';", authorized);
         List<List<String> > res = esql.executeQueryAndReturnResult(query);
         String userID = "";
         for(int i = 0; i < res.size(); i++){
            userID = res.get(i).get(0);
         }
         String popQuery = String.format("SELECT ORDERS.productname, COUNT(ORDERS.unitsordered) AS s FROM ORDERS, STORE WHERE ORDERS.storeid = STORE.storeid AND STORE.managerid = %s GROUP BY ORDERS.productname ORDER BY s", userID);
         List<List<String> > popular = esql.executeQueryAndReturnResult(popQuery);
         for(int i = popular.size() - 5; i < popular.size(); i++){
            System.out.println("Product name: " + popular.get(i).get(0) + "Number of Orders: " + popular.get(i).get(1));
         }
      } catch(Exception e){
         System.err.println(e.getMessage());
      }
   }
   public static void viewPopularCustomers(Amazon esql,String authorisedUser) {
      try{
         String getUserIDQuery = String.format ("SELECT u.UserID FROM Users u WHERE u.name = '%s'", authorisedUser);
         String result = esql.executeQueryAndReturnResult(getUserIDQuery).get(0).get(0);
         int manangerID = Integer.parseInt(result);
         String popCustomerQuery = String.format("SELECT c.userID,c.name, c.latitude,c.longitude, order_count.numOrders FROM (SELECT customerID, Count(distinct(orderNumber)) as numOrders FROM store s INNER JOIN product p ON p.storeID = s.storeID  INNER JOIN orders o ON o.storeID = s.storeID WHERE s.managerID = %s  GROUP BY customerID) order_count INNER JOIN users c ON c.userID = order_count.customerID ORDER BY order_count.numOrders", manangerID);
         List<List<String> > popCustomerTable = esql.executeQueryAndReturnResult(popCustomerQuery) ;
         for(int i = 0; i < 5; i++){
            System.out.println("User ID: " + popCustomerTable.get(i).get(0) + "Name: " + popCustomerTable.get(i).get(1) + "Latitude: "+ popCustomerTable.get(i).get(2) +  "Longitude: "+ popCustomerTable.get(i).get(3) +  "Num Orders: "+ popCustomerTable.get(i).get(4));
         }
      }
      catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }
   public static void placeProductSupplyRequests(Amazon esql, String authorized) {
      try{
         String query = String.format("SELECT userid FROM USERS WHERE name = '%s';", authorized);
         List<List<String> > res = esql.executeQueryAndReturnResult(query);
         String userID = "";
         for(int i = 0; i < res.size(); i++){
            userID = res.get(i).get(0);
         }
         System.out.print("\tEnter store ID: ");
         String storeID = in.readLine();
         String storeQuery = String.format("SELECT managerid FROM STORE WHERE storeid = %s AND managerid = %s", storeID, userID);
         int storeNum = esql.executeQuery(storeQuery);
         if(storeNum <= 0){
            System.out.println("You don't manage this store.");
            return;
         }
         System.out.print("\tEnter product name: ");
         String productName = in.readLine();
         System.out.print("\tEnter number of units: ");
         String numUnitssString = in.readLine();
         int numUnits = Integer.parseInt(numUnitssString);
         System.out.print("\tEnter warehouse ID: ");
         String warehouseID = in.readLine();
         String warehouseQuery = String.format("SELECT * FROM WAREHOUSE WHERE warehouseid = %s", warehouseID);
         int warehouseNum = esql.executeQuery(warehouseQuery);
         if(warehouseNum <= 0){
            System.out.println("Warehouse does not exist");
            return;
         }
         String getCurrentInventoryQuery = String.format("SELECT numberofunits FROM product WHERE storeid = %s AND productname = '%s'", storeID, productName);
         //System.out.println(getCurrentInventoryQuery);
         List<List<String> > inventory = esql.executeQueryAndReturnResult(getCurrentInventoryQuery);
         String currInventoryString = inventory.get(0).get(0);
         int currInventory = Integer.parseInt(currInventoryString);
         int newInventory = currInventory + numUnits;
         String getOrderNum = String.format("SELECT MAX(requestnumber) FROM productsupplyrequests");
         //System.out.println(getOrderNum);
         List<List<String> > getOrderNumList = esql.executeQueryAndReturnResult(getOrderNum);
         int newOrderNum = Integer.parseInt(getOrderNumList.get(0).get(0)) + 1;
         String updateProduct = String.format("UPDATE PRODUCT SET numberofunits = %s WHERE storeid = %s AND productname = '%s'", newInventory, storeID, productName);
         String addSupplyOrder = String.format("INSERT INTO productsupplyrequests(requestnumber, managerid, warehouseid, storeid, productname, unitsrequested) VALUES(%s, %s, %s, %s, '%s', %s)", newOrderNum, userID, warehouseID, storeID, productName, numUnits);
         //System.out.println(updateProduct);
         esql.executeUpdate(updateProduct);
         //System.out.println(addSupplyOrder);
         esql.executeUpdate(addSupplyOrder);
      } catch(Exception e){
         System.err.println(e.getMessage());
      }
   }

}//end Amazon

