import java.io.*;
import java.net.Socket;
import java.util.Base64;
import java.util.Scanner;

/**
 * 使用 Socket 手动实现 SMTP 邮件发送客户端（明文协议，不依赖 JavaMail）
 * 发送流程：连接服务器 -> HELO -> AUTH LOGIN -> MAIL FROM -> RCPT TO -> DATA -> QUIT
 */
public class SmtpMailSender {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("发送方邮箱（163邮箱）: ");
        String fromEmail = scanner.nextLine();
        System.out.print("授权码: ");
        String authCode = scanner.nextLine();
        System.out.print("接收方邮箱: ");
        String toEmail = scanner.nextLine();
        System.out.print("邮件主题: ");
        String subject = scanner.nextLine();
        System.out.print("邮件正文内容: ");
        String body = scanner.nextLine();

        try {
            // 连接 SMTP 服务器
            Socket socket = new Socket("smtp.163.com", 25);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            System.out.println(reader.readLine()); // 服务器欢迎语

            // 1. 发送 HELO 指令
            sendCommand(writer, reader, "HELO localhost");

            // 2. 认证：AUTH LOGIN
            sendCommand(writer, reader, "AUTH LOGIN");
            sendCommand(writer, reader, Base64.getEncoder().encodeToString(fromEmail.getBytes()));  // 邮箱 base64
            sendCommand(writer, reader, Base64.getEncoder().encodeToString(authCode.getBytes()));   // 授权码 base64

            // 3. 指定发件人
            sendCommand(writer, reader, "MAIL FROM:<" + fromEmail + ">");

            // 4. 指定收件人
            sendCommand(writer, reader, "RCPT TO:<" + toEmail + ">");

            // 5. 开始输入邮件内容
            sendCommand(writer, reader, "DATA");
            writer.write("Subject: " + subject + "\r\n");
            writer.write("From: " + fromEmail + "\r\n");
            writer.write("To: " + toEmail + "\r\n");
            writer.write("\r\n"); // 空行后是正文
            writer.write(body + "\r\n");
            writer.write(".\r\n"); // 结束标志
            writer.flush();
            System.out.println(reader.readLine());

            // 6. 结束连接
            sendCommand(writer, reader, "QUIT");

            writer.close();
            reader.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 用于发送命令并接收服务器响应
    private static void sendCommand(BufferedWriter writer, BufferedReader reader, String command) throws IOException {
        System.out.println("C: " + command);
        writer.write(command + "\r\n");
        writer.flush();
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("S: " + line);
            if (line.length() >= 4 && line.charAt(3) != '-') break; // 多行响应判断
        }
    }
}
