import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * 使用 Socket 手动实现 POP3 邮件接收客户端（明文命令，不依赖 JavaMail）
 * 实现功能：登录、获取邮件数量、读取邮件列表、查看指定邮件内容
 */
public class Pop3MailReceiver {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("接收邮箱地址（163邮箱）: ");
        String email = scanner.nextLine();
        System.out.print("授权码: ");
        String password = scanner.nextLine();

        try {
            // 连接 POP3 服务器（163邮箱使用 pop.163.com:110）
            Socket socket = new Socket("pop.163.com", 110);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            System.out.println(reader.readLine()); // 服务器欢迎语

            // 1. 用户认证
            send(writer, reader, "USER " + email);
            send(writer, reader, "PASS " + password);

            // 2. 查看邮箱状态（邮件数量、大小）
            send(writer, reader, "STAT");

            // 3. 获取邮件列表（编号+大小）
            send(writer, reader, "LIST");

            System.out.print("是否查看某封邮件内容？(y/n): ");
            if (scanner.nextLine().equalsIgnoreCase("y")) {
                System.out.print("请输入邮件编号: ");
                int index = Integer.parseInt(scanner.nextLine());
                send(writer, reader, "RETR " + index); // 获取具体邮件内容
            }

            // 4. 退出
            send(writer, reader, "QUIT");

            writer.close();
            reader.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 发送指令并打印响应
    private static void send(BufferedWriter writer, BufferedReader reader, String command) throws IOException {
        System.out.println("C: " + command);
        writer.write(command + "\r\n");
        writer.flush();
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("S: " + line);
            if (line.equals(".") || !line.startsWith("+OK") && !line.startsWith("-ERR") && !reader.ready()) break;
        }
    }
}
