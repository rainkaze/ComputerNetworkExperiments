import java.io.*;
import java.net.Socket;
import java.util.Base64;

public class SMTPClient {
    private static final String SMTP_SERVER = "smtp.163.com";
    private static final int SMTP_PORT = 25;
    private static final String USERNAME = "15865177153@163.com";
    private static final String PASSWORD = "RWUQzhcaYkubUFmt"; // 使用授权码

    public static void main(String[] args) {
        try (Socket socket = new Socket(SMTP_SERVER, SMTP_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            // 1. 接收服务器欢迎消息
            System.out.println("Server: " + in.readLine());

            // 2. 发送EHLO并读取所有响应行
            sendCommand(out, in, "EHLO client.example.com");
            String response;
            while ((response = in.readLine()) != null) {
                System.out.println("Server: " + response);
                if (response.startsWith("250 ")) break; // 结束多行响应
            }

            // 3. 认证（仅在服务器支持时执行）
            sendCommand(out, in, "AUTH LOGIN");
            sendCommand(out, in, Base64.getEncoder().encodeToString(USERNAME.getBytes()));
            sendCommand(out, in, Base64.getEncoder().encodeToString(PASSWORD.getBytes()));

            // 4. 发送邮件
            sendCommand(out, in, "MAIL FROM: <" + USERNAME + ">");
            sendCommand(out, in, "RCPT TO: <rainkaze@qq.com>");
            sendCommand(out, in, "DATA");

            // 邮件内容
            out.write("From: " + USERNAME + "\r\n");
            out.write("To: rainkaze@qq.com\r\n");
            out.write("Subject: Test SMTP Client\r\n\r\n");
            out.write("Hello, this is a test email!\r\n");
            sendCommand(out, in, "."); // 结束DATA

            // 5. 退出
            sendCommand(out, in, "QUIT");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendCommand(BufferedWriter out, BufferedReader in, String command) throws IOException {
        System.out.println("Client: " + command);
        out.write(command + "\r\n");
        out.flush();
        System.out.println("Server: " + in.readLine());
    }
}