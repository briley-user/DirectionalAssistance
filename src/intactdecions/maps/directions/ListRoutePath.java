package intactdecions.maps.directions;

import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.*;

import java.util.List;
import java.util.UUID;

import java.io.*;

import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;

import org.joda.time.DateTime;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.*;


public class ListRoutePath {
	private final static String API_FILE ="Directions_Api_Key.txt";
	private final static String MYSQL_FILE = "MySQL_Cache_DB.txt";
	private final static String API_KEY = ReadStringsFromFile(API_FILE).get(0);
	private final static String MYSQL_SERVER_AND_PORT = ReadStringsFromFile(MYSQL_FILE).get(0);
	private final static String MYSQL_DATABASE = ReadStringsFromFile(MYSQL_FILE).get(1);
	private final static String MYSQL_TABLE = ReadStringsFromFile(MYSQL_FILE).get(2);
	private final static String MYSQL_USERNAME = ReadStringsFromFile(MYSQL_FILE).get(3);
	private final static String MYSQL_PASSWORD = ReadStringsFromFile(MYSQL_FILE).get(4);
	private final static String MYSQL_SELECT_STATEMENT = "SELECT * FROM "+ MYSQL_TABLE;
	public static LatLng REQUESTED_ORIGIN;
	public static LatLng REQUESTED_DESTINATION;

	public static void SetRequestedCoodrinates (LatLng origin, LatLng destintation) {
		REQUESTED_ORIGIN = origin;
		REQUESTED_DESTINATION = destintation;
	}
	
	public static List<String> ReadStringsFromFile (String filename) {
	
		
		List<String> fileContent = null;
		try {
		fileContent= FileUtils.readLines(new File(filename),Charset.forName("UTF-8"));
		} catch (IOException e) {
			}
		return fileContent; // return list of strings
				
	
	}
	
	public static void  MySQL_INSERT (String MySQL_Statement) {

		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection con= DriverManager.getConnection("jdbc:mysql://"+ MYSQL_SERVER_AND_PORT +"/"+ MYSQL_DATABASE, MYSQL_USERNAME, MYSQL_PASSWORD);
			Statement stmt = con.createStatement();
			stmt.executeUpdate(MySQL_Statement);
			con.close();
		} catch(Exception e) {
			System.out.println("Error:"+e);
			}
	}

	public static String Check_For_Cached_Request(LatLng origin, LatLng destination) {
		String cached_record= null;
		String MYSQL_SUFFIX_STATEMENT = " WHERE origin_longitude="+origin.lng+" AND origin_lattitude="+origin.lat+
				" AND destination_longitude="+destination.lng+" AND destination_lattitude="+destination.lat;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection con= DriverManager.getConnection("jdbc:mysql://"+ MYSQL_SERVER_AND_PORT +"/"+ MYSQL_DATABASE, MYSQL_USERNAME, MYSQL_PASSWORD);
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(MYSQL_SELECT_STATEMENT + MYSQL_SUFFIX_STATEMENT);
		//	System.out.println(MYSQL_SELECT_STATEMENT + MYSQL_SUFFIX_STATEMENT);
			
			while (rs.next()) {
			cached_record = rs.getString("cached_filename");
							
			}
			con.close();
		} catch(Exception e) {
			System.out.println("Error:"+e);
			}
		//System.out.println("record from database:" + cached_record);
		return cached_record;
	 }
	
	public static DirectionsResult getDirections(LatLng origin, LatLng destination) throws IOException, InterruptedException, ApiException {
		
		
		DateTime now = new DateTime();
		GeoApiContext apiContext = new GeoApiContext.Builder()
				.apiKey(API_KEY)
				.build();
		
		DirectionsApiRequest request = DirectionsApi.newRequest(apiContext);
		
		DirectionsResult result = request
				.mode(TravelMode.DRIVING)
				.origin(origin)
				.destination(destination)
				.departureTime(now)
				.await();

	    
		return result;
		}
	
	public static LatLng setLatLng(double lattitude, double longitude) {
		LatLng location = new LatLng (lattitude, longitude);
				
		return location;
	}
	
	public static void DisplayDirections(DirectionsResult directions) throws IOException {
		
    
    String outputString = "<!DOCTYPE html>" + "\n" + "<html>" + "\n" + "<body>" +"\n";
    String db_statement;
    String randomFilenameString= GenerateRandomUUID().toString();
    String filename = randomFilenameString +".html";
    File out = new File("cache/"+filename);
    double origin_lat = REQUESTED_ORIGIN.lat;
    double origin_long = REQUESTED_ORIGIN.lng;
    double dest_lat = REQUESTED_DESTINATION.lat;
    double dest_long = REQUESTED_DESTINATION.lng;
    
    db_statement = "INSERT INTO " + MYSQL_TABLE+" (uuid_descriptor,origin_longitude,origin_lattitude,destination_longitude,destination_lattitude,cached_filename) "+
    "values("+ "'"+randomFilenameString +"'"+","+origin_long+","+origin_lat+","+dest_long+","+ dest_lat+","+"'"+filename+"'"+")";
    
	for (int i=0; i < directions.routes.length; ++i) {
			
			for (int x=0; x < directions.routes[i].legs.length; ++x) {
				outputString = outputString + "<h2><p>Starting Address: "+ directions.routes[i].legs[x].startAddress + "</p>";
				outputString = outputString + "<p>Destination Address: "+ directions.routes[i].legs[x].endAddress + "</p></h2>";
				outputString = outputString + "<h3><p>Duration of Trip: "+ directions.routes[i].legs[x].duration + "</p>";
				outputString = outputString + "<p>Total Distance: "+ directions.routes[i].legs[x].distance + "</p></h3>";
				
				for (int y=0; y < directions.routes[i].legs[x].steps.length; y++) {
					
					outputString = outputString +"<p>"+y+": " + directions.routes[i].legs[x].steps[y].htmlInstructions.toString()+"\n"+"</p>";
					FileUtils.writeStringToFile(out,outputString, Charset.forName("UTF-8"));
				}
				
			}
		}
	    outputString = outputString + "\n" + "</body" + "\n" + "</html>";
		FileUtils.writeStringToFile(out,outputString, Charset.forName("UTF-8")); // write the full string to the file.
		MySQL_INSERT(db_statement);
		System.out.println("API call generated and file name "+filename+" has been created");
	}
	
	public static UUID GenerateRandomUUID( ) {
		//update here
	return UUID.randomUUID();
	}
	
	public static void main(String[] args) throws IOException, InterruptedException, ApiException {
		// TODO Auto-generated method
	
	String map_directions_filename;
	DirectionsResult dr;
	LatLng origin = setLatLng(0,-0.9) ;
     LatLng destination = setLatLng(1,-1) ;
     map_directions_filename = Check_For_Cached_Request(origin, destination);
 
        //need a loops statement and a check for null so that we do not receive a null pointer exception.
        // should be a conditional statement and a better loop mechanics, to check for duplicate entries.	
    if (map_directions_filename == null)
      { // no match is found in the database
    	SetRequestedCoodrinates(origin, destination); //store the requested coordinated in the global variables
    	dr = getDirections(origin, destination); //get directions using the requested coordinated using Google API Call 
    	if (dr.routes.length >0) {
    		DisplayDirections(dr) ; // store in the cached directions table and create html file
    		System.out.println("Coordinates have been cached");
    	} else {
    		System.out.println("Route is not possible. No results returned"); 
    			}
    	
    } else {
    	System.out.println("Record already cached in the database. Use filename: " + map_directions_filename);
    }
     // can display in an html file
     
	}

}
