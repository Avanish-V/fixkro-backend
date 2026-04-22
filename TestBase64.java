import java.util.Base64;

public class TestBase64 {
    public static void main(String[] args) {
        String unpadded = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjYxNDg4NjMzNWNjNDhmNGMyNmNjZjQyZGU1ODBkZGMwODhiNDNiMTEiLCJ0eXAiOiJKV1QifQ";
        try {
            byte[] result = Base64.getUrlDecoder().decode(unpadded);
            System.out.println("Success: " + new String(result));
        } catch (Exception e) {
            System.out.println("Failed: " + e.getMessage());
        }
    }
}
