import java.io.*;
import java.net.Socket;
import java.util.Base64;
import java.util.Scanner;

/**
 * 使用Socket 手动实现SMTP邮件发送客户端，支持自动推断SMTP服务器和群发
 * 发送流程：连接服务器 -> HELO -> AUTH LOGIN -> MAIL FROM -> RCPT TO xN -> DATA -> QUIT
 */
public class SmtpMailSender {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("发送方邮箱: ");
        String fromEmail = scanner.nextLine().trim();
        System.out.print("授权码: ");
        String authCode = scanner.nextLine().trim();
        System.out.print("接收方邮箱（多个请用空格分隔）: ");
        String toEmailsLine = scanner.nextLine().trim();
        System.out.print("邮件主题: ");
        String subject = scanner.nextLine().trim();
        System.out.print("邮件正文内容: ");
        String body = scanner.nextLine().trim();

        // 拆分收件人
        String[] toEmails = toEmailsLine.split("\\s+");

        // 自动推断SMTP服务器及端口
        String domain = fromEmail.substring(fromEmail.indexOf('@') + 1);
        String smtpServer = "smtp." + domain;
        int smtpPort = 25; // 普通SMTP端口
        System.out.println("自动使用 SMTP 服务器: " + smtpServer + ", 端口: " + smtpPort);

        try (Socket socket = new Socket(smtpServer, smtpPort);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            // 服务器欢迎语
            System.out.println(reader.readLine());

            // 1. HELO
            sendCommand(writer, reader, "HELO localhost");

            // 2. AUTH LOGIN
            sendCommand(writer, reader, "AUTH LOGIN");
            sendCommand(writer, reader, Base64.getEncoder().encodeToString(fromEmail.getBytes()));
            sendCommand(writer, reader, Base64.getEncoder().encodeToString(authCode.getBytes()));

            // 3. MAIL FROM
            sendCommand(writer, reader, "MAIL FROM:<" + fromEmail + ">");

            // 4. 多个 RCPT TO
            for (String to : toEmails) {
                sendCommand(writer, reader, "RCPT TO:<" + to + ">");
            }

            // 5. DATA
            sendCommand(writer, reader, "DATA");

            // 构建邮件头
            StringBuilder toHeader = new StringBuilder();
            for (int i = 0; i < toEmails.length; i++) {
                toHeader.append(toEmails[i]);
                if (i < toEmails.length - 1) toHeader.append(", ");
            }
            writer.write("Subject: " + subject + "\r\n");
            writer.write("From: " + fromEmail + "\r\n");
            writer.write("To: " + toHeader + "\r\n");
            writer.write("\r\n"); // 空行后正文
            writer.write(body + "\r\n");
            writer.write(".\r\n");
            writer.flush();
            System.out.println(reader.readLine());

            // 6. QUIT
            sendCommand(writer, reader, "QUIT");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 发送命令并接收响应
    private static void sendCommand(BufferedWriter writer, BufferedReader reader, String command) throws IOException {
        System.out.println("send: " + command);
        writer.write(command + "\r\n");
        writer.flush();
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("reply: " + line);
            if (line.length() >= 4 && line.charAt(3) != '-') break;
        }
    }
}
