import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Compiler {
    public static void main(String[] args) {
        // 读 testfile.txt 中的测试程序
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get("testfile.txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(content);
    }
}
