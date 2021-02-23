package test.mail.Automation;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;





public class App {
    public static void main(String[] args) throws IOException, InterruptedException {
    	HttpRequest request = HttpRequest.newBuilder()
    			.uri(URI.create("https://privatix-temp-mail-v1.p.rapidapi.com/request/one_mail/id/test@automation.com/"))
    			.header("x-rapidapi-key", "228cd22e19msh4481721a725bbd4p14f3abjsn609a835b1563")
    			.header("x-rapidapi-host", "privatix-temp-mail-v1.p.rapidapi.com")
    			.method("GET", HttpRequest.BodyPublishers.noBody())
    			.build();
    	HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    	System.out.println(response.body());
    }
}