import org.mindrot.jbcrypt.BCrypt;
public class TempBCryptGen {
    public static void main(String[] args) {
        String password = "Secret123!";
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        System.out.println("hash = " + hash);
        System.out.println("checkpw result = " + BCrypt.checkpw(password, hash));
    }
}
