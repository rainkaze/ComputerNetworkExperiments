import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Pop3MailReceiver {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("是否显示通信日志 (send/reply)? (y/n): ");
        boolean showLog = scanner.nextLine().trim().equalsIgnoreCase("y");

        System.out.print("接收邮箱地址: ");
        String email = scanner.nextLine().trim();
        System.out.print("授权码: ");
        String password = scanner.nextLine().trim();

        String domain = email.substring(email.indexOf('@') + 1);
        String server = "pop." + domain;
        int port = 995;
        System.out.println("使用 POP3S 服务器: " + server + ", 端口: " + port);

        try {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) factory.createSocket(server, port);
            Charset charset = StandardCharsets.US_ASCII;
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), charset));
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(), charset));

            // 处理欢迎消息
            String welcome = reader.readLine();
            System.out.println(welcome);
            if (!welcome.startsWith("+OK")) {
                socket.close();
                return;
            }

            // 登录
            sendCmd(writer, reader, "USER " + email, false, showLog);
            sendCmd(writer, reader, "PASS " + password, false, showLog);

            // 显示邮箱状态
            System.out.print("是否显示邮箱状态(邮件数量/总大小)? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                List<String> statResponse = sendCmd(writer, reader, "STAT", false, showLog);
                if (!showLog && !statResponse.isEmpty() && statResponse.get(0).startsWith("+OK")) {
                    String[] parts = statResponse.get(0).split("\\s+");
                    if (parts.length >= 3) {
                        System.out.println("邮件数量: " + parts[1] + ", 总大小: " + parts[2] + "字节");
                    }
                }
            }

            // 显示邮件列表
            System.out.print("是否显示邮件列表(编号/大小)? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                List<String> listResponse = sendCmd(writer, reader, "LIST", true, showLog);
                if (!showLog && !listResponse.isEmpty() && listResponse.get(0).startsWith("+OK")) {
                    for (int i = 1; i < listResponse.size(); i++) {
                        String line = listResponse.get(i);
                        if (line.equals(".")) break;
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 2) {
                            System.out.println("邮件编号: " + parts[0] + ", 大小: " + parts[1] + "字节");
                        }
                    }
                }
            }

            // 主循环
            while (true) {
                System.out.println("请选择操作: 1. 查看邮件  2. 删除邮件  3. 退出");
                String choice = scanner.nextLine().trim();
                if ("1".equals(choice)) {
                    System.out.print("请输入要查看的邮件编号: ");
                    List<String> lines = retrieve(writer, reader, scanner.nextLine().trim(), showLog);
                    if (showLog) {
                        printBody(lines);
                    } else {
                        printSummary(lines);
                    }
                } else if ("2".equals(choice)) {
                    System.out.print("请输入要删除的邮件编号: ");
                    sendCmd(writer, reader, "DELE " + scanner.nextLine().trim(), false, showLog);
                } else if ("3".equals(choice)) {
                    break;
                } else {
                    System.out.println("无效选择，请重新输入。");
                }
            }

            sendCmd(writer, reader, "QUIT", false, showLog);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> sendCmd(BufferedWriter writer, BufferedReader reader, String cmd, boolean multiline, boolean showLog) throws IOException {
        List<String> response = new ArrayList<>();
        if (showLog) {
            System.out.println("send: " + cmd);
        }
        writer.write(cmd + "\r\n");
        writer.flush();

        String line = reader.readLine();
        if (line == null) {
            System.err.println("无响应，连接已断开");
            return response;
        }
        response.add(line);
        if (showLog) {
            System.out.println(line);
        }

        if (!line.startsWith("+OK")) {
            return response;
        }

        if (multiline) {
            while ((line = reader.readLine()) != null) {
                response.add(line);
                if (showLog) {
                    System.out.println(line);
                }
                if (".".equals(line)) {
                    break;
                }
            }
        }
        return response;
    }

    private static List<String> retrieve(BufferedWriter writer, BufferedReader reader, String idx, boolean showLog) throws IOException {
        if (showLog) {
            System.out.println("send: RETR " + idx);
        }
        writer.write("RETR " + idx + "\r\n");
        writer.flush();
        List<String> all = new ArrayList<>();
        String line = reader.readLine();
        if (showLog) {
            System.out.println(line);
        }
        all.add(line);
        if (line == null || !line.startsWith("+OK")) {
            return all;
        }
        while ((line = reader.readLine()) != null) {
            all.add(line);
            if (showLog) {
                System.out.println(line);
            }
            if (".".equals(line)) {
                break;
            }
        }
        return all;
    }

    private static void printBody(List<String> lines) {
        int start = 0;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().isEmpty()) {
                start = i + 1;
                break;
            }
        }
        for (int i = start; i < lines.size(); i++) {
            String l = lines.get(i).trim();
            if (l.startsWith("--")) break;
            if (l.isEmpty()) continue;
            try {
                byte[] dec = Base64.getDecoder().decode(l);
                System.out.println(new String(dec, StandardCharsets.UTF_8));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    private static void printSummary(List<String> lines) {
        String from = "", subject = "", date = "";
        boolean inPlain = false;
        StringBuilder b64 = new StringBuilder();

        for (int i = 0; i < lines.size(); i++) {
            String l = lines.get(i);
            if (l.startsWith("From:"))    from    = decodeMimeHeader(l.substring(5).trim());
            else if (l.startsWith("Subject:")) subject = decodeMimeHeader(l.substring(8).trim());
            else if (l.startsWith("Date:"))    date    = l.substring(5).trim();

            if (l.contains("Content-Type: text/plain")) {
                while (i < lines.size() && !lines.get(i).trim().isEmpty()) i++;
                inPlain = true;
                continue;
            }
            if (inPlain) {
                if (l.startsWith("--")) break;
                if (!l.trim().isEmpty()) b64.append(l.trim());
            }
        }

        System.out.println("发件人: " + from);
        System.out.println("主题:   " + subject);
        System.out.println("日期:   " + date);
        System.out.println("正文:");
        try {
            byte[] dec = Base64.getDecoder().decode(b64.toString());
            System.out.println(new String(dec, StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.out.println("[无法解码正文]");
        }
    }

    private static String decodeMimeHeader(String text) {
        Pattern p = Pattern.compile("=\\?utf-8\\?B\\?([^?]+)\\?=", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        StringBuffer sb = new StringBuffer();
        int last = 0;
        while (m.find()) {
            sb.append(text, last, m.start());
            try {
                byte[] dec = Base64.getDecoder().decode(m.group(1));
                sb.append(new String(dec, StandardCharsets.UTF_8));
            } catch (Exception ex) {
                sb.append(m.group(0));
            }
            last = m.end();
        }
        if (last < text.length()) sb.append(text.substring(last));
        return sb.toString();
    }
}