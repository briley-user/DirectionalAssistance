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
			System.out.println(MYSQL_SELECT_STATEMENT + MYSQL_SUFFIX_STATEMENT);
			
			while (rs.next()) {
			cached_record = rs.getString("cached_filename");
							
			}
			con.close();
		} catch(Exception e) {
			System.out.println("Error:"+e);
			}
		System.out.println("record from database:" + cached_record);
		return cached_record;
	 }
	
	public static DirectionsResult getDirections(LatLng origin, LatLng destination) throws IOException, InterruptedException, ApiException {
		
		
		DateTime now = new DateTime();
		GeoApiContext apiContext = new GeoApiContext.Builder()
				.apiKey(API_KEY)
				.build();
		
		DirectionsApiRequest request = DirectionsApi.newRequest(apiContext);
	/*	request.origin(origin); // set the origin
		request.destination(destination); // set the destination
		request.mode(TravelMode.DRIVING); //set travelling mode
		request.departureTime(now);
		request.await(); */
		
		DirectionsResult result = request
				.mode(TravelMode.DRIVING)
				.origin(origin)
				.destination(destination)
				.departureTime(now)
				.await();
/*		request.setCallback(new PendingResult.Callback<DirectionsResult>() {
			@Override
			public void onResult(DirectionsResult result) {
				DirectionsRoute[] routes = result.routes;
				System.out.println(routes.toString());
			}
			
			@Override
			public void onFailure(Throwable e) {
			}
		});
	*/
	    
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
    File out = new File(filename);
    double origin_lat = directions.routes[0].legs[0].startLocation.lat;
    double origin_long = directions.routes[0].legs[0].startLocation.lng;
    double dest_lat = directions.routes[0].legs[0].endLocation.lat;
    double dest_long = directions.routes[0].legs[0].endLocation.lng;
    
    db_statement = "INSERT INTO " + MYSQL_TABLE+" (uuid_descriptor,origin_longitude,origin_lattitude,destination_longitude,destination_lattitude,cached_filename) "+
    "values("+ "'"+randomFilenameString +"'"+","+origin_lat+","+origin_long+","+dest_lat+","+ dest_long+","+"'"+filename+"'"+")";
    
  
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
	LatLng origin = setLatLng(40.7128, -79.0060) ;
     LatLng destination = setLatLng(36.1699, -115.1398) ;
     map_directions_filename = Check_For_Cached_Request(origin, destination);
     System.out.println(map_directions_filename);
        //need a loops statement and a check for null so that we do not receive a null pointer exception.
        // should be a conditional statement and a better loop mechanics, to check for duplicate entries.	
    if (map_directions_filename != null) {
    	DisplayDirections(getDirections(origin, destination)) ; //execute the google API Call
    	// need to separate these two methods and and a new method that stores the data needed into global variables that can be used in the insert statement
    	// need to store the search request coordinates and not the retrieved coordinates into the cached database table
    	// the level of percision will differ from what is requested by the user and what is retrieved from the service
    } else {
    	System.out.println("Record already cached in the database. Use filename: " + map_directions_filename);
    }
     // can display in an html file
     
	}

}
