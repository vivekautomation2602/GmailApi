package test.mail.Automation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.util.StringUtils;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

public class GmailAPI {

	
	/*
	1.Get code : 
https://accounts.google.com/o/oauth2/v2/auth?
 scope=https://mail.google.com&
 access_type=offline&
 redirect_uri=http://localhost&
 response_type=code&
 client_id=[Client ID]
2. Get access_token and refresh_token
 curl \
--request POST \
--data "code=[Authentcation code from authorization link]&client_id=[Application Client Id]&client_secret=[Application Client Secret]&redirect_uri=http://localhost&grant_type=authorization_code" \
https://accounts.google.com/o/oauth2/token
3.Get new access_token using refresh_token
curl \
--request POST \
--data "client_id=[your_client_id]&client_secret=[your_client_secret]&refresh_token=[refresh_token]&grant_type=refresh_token" \
https://accounts.google.com/o/oauth2/token
	
*/
	private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String user = "me";
	static Gmail service = null;
	private static File filePath = new File("credentials.json");

	public static void main(String[] args) throws IOException, GeneralSecurityException {

		getGmailService();
		
		getMailBody("Google");

	}

	public static void getMailBody(String searchString) throws IOException {

		// Access Gmail inbox

		Gmail.Users.Messages.List request = service.users().messages().list(user).setQ(searchString);

		ListMessagesResponse messagesResponse = request.execute();
		request.setPageToken(messagesResponse.getNextPageToken());

		// Get ID of the email you are looking for
		String messageId = messagesResponse.getMessages().get(0).getId();

		Message message = service.users().messages().get(user, messageId).execute();

		// Print email body

		String emailBody = StringUtils
				.newStringUtf8(Base64.decodeBase64(message.getPayload().getParts().get(0).getBody().getData()));

		System.out.println("Email body : " + emailBody);

	}

	public static Gmail getGmailService() throws IOException, GeneralSecurityException {

		InputStream in = new FileInputStream(filePath); // Read credentials.json
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Credential builder

		Credential authorize = new GoogleCredential.Builder().setTransport(GoogleNetHttpTransport.newTrustedTransport())
				.setJsonFactory(JSON_FACTORY)
				.setClientSecrets(clientSecrets.getDetails().getClientId().toString(),
						clientSecrets.getDetails().getClientSecret().toString())
				.build().setAccessToken(getAccessToken()).setRefreshToken(
						"YOUR_REFRESH_TOKEN");//Replace this

		// Create Gmail service
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, authorize)
				.setApplicationName(GmailAPI.APPLICATION_NAME).build();

		return service;
	}

	private static String getAccessToken() {

		try {
			Map<String, Object> params = new LinkedHashMap<>();
			params.put("grant_type", "refresh_token");
			params.put("client_id", "YOUR_CLIENT_ID"); //Replace this
			params.put("client_secret", "YOUR_CLIENT_SECRET"); //Replace this
			params.put("refresh_token",
					"YOUR_REFRESH_TOKEN"); //Replace this

			StringBuilder postData = new StringBuilder();
			for (Map.Entry<String, Object> param : params.entrySet()) {
				if (postData.length() != 0) {
					postData.append('&');
				}
				postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				postData.append('=');
				postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			}
			byte[] postDataBytes = postData.toString().getBytes("UTF-8");

			URL url = new URL("https://accounts.google.com/o/oauth2/token");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setUseCaches(false);
			con.setRequestMethod("POST");
			con.getOutputStream().write(postDataBytes);

			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuffer buffer = new StringBuffer();
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				buffer.append(line);
			}

			JSONObject json = new JSONObject(buffer.toString());
			String accessToken = json.getString("access_token");
			return accessToken;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

}