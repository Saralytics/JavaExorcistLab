import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.specification.User;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import com.wrapper.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Scanner;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class SpotifyApp {
    private static final URI redirectUri = SpotifyApi.builder().build().getRedirectURI().create("http://example.com");

    public static void main(String[] args) {

        Properties props = new Properties();
        try {
            // Load the properties from the .env file
            props.load(new FileInputStream(".env"));

            // Access the properties
            String clientId = props.getProperty("clientId");
            String clientSecret = props.getProperty("clientSecret");

        Scanner scanner = new Scanner(System.in);

        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRedirectUri(redirectUri)
                .build();
        System.out.println("What era are we travelling to? Input in this format:" +
                "YYYY-MM-DD ");
        String dateStr = scanner.nextLine();
        String url = "https://www.billboard.com/charts/hot-100/" + dateStr;

        try {
            Document doc = Jsoup.connect(url).get();
            Elements titles = doc.select("li ul li h3");
            for (Element title : titles) {
                System.out.println(title.text().trim());
            }

            AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
                    .scope("playlist-modify-private")
                    .build();

            final URI uri = authorizationCodeUriRequest.execute();

            System.out.println("Open the following URI: " + uri.toString());

            // Need to implement a way to retrieve the code from the redirect URI.
            System.out.println("Paste the code below (not the whole URL): ");
            String code = scanner.nextLine();
            // Retrieve the code from the redirect URI.

            AuthorizationCodeCredentials credentials = spotifyApi.authorizationCode(code).build().execute();
            spotifyApi.setAccessToken(credentials.getAccessToken());

            GetCurrentUsersProfileRequest getCurrentUsersProfileRequest = spotifyApi.getCurrentUsersProfile().build();
            User user = getCurrentUsersProfileRequest.execute();

            System.out.println("User ID: " + user.getId());

        } catch (IOException | SpotifyWebApiException e) {
            e.printStackTrace();
        }
    } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
