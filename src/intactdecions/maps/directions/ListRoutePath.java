package intactdecions.maps.directions;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.errors.ApiException;
import com.google.maps.model.*;

import java.io.IOException;
import java.time.*;

import org.joda.time.DateTime;
public class ListRoutePath {
	private final static String API_KEY = "AIzaSyBMT0TjgaTNozCM-pb832MUf9RG7V5_-Sc";
			 
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
	
	public static void DisplayDirections(DirectionsResult directions) {
		

	for (int i=0; i < directions.routes.length; ++i) {
			for (int x=0; x < directions.routes[i].legs.length; ++x) {
				for (int y=0; y < directions.routes[i].legs[x].steps.length; y++) {
					System.out.print("<b> Route: "+i);
					System.out.print(" Leg: "+x);
					System.out.println(" Step: "+y+"</b>");
					System.out.println("<b> "+ directions.routes[i].legs[x].steps[y].htmlInstructions.toString()+"</b>");
				}
				
			}
		}
	}
	public static void main(String[] args) throws IOException, InterruptedException, ApiException {
		// TODO Auto-generated method
     // ListRoutePath aRoute = new ListRoutePath();
     LatLng origin = setLatLng(40.7128, -79.0060) ;
     LatLng destination = setLatLng(36.1699, -115.1398) ;
     
     DisplayDirections(getDirections(origin, destination)) ;
     // can display in an html file
     // or store as a blob with html code into a database, and then query it out to document...randomized  html filename to be genreated
	}

}