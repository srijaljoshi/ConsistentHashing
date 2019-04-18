import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class BootstrapServer {

  private static int ID;
  private static int nsPort;
  private static final int CPORT = 51000; // the port where commands are sent

  private static HashMap<Integer, String> map = new HashMap<>();

  public static void main(String[] args) throws IOException {
    String configFile = args[0];

    BootstrapServer bServer = new BootstrapServer();

    bServer.readfile(configFile);

    // listen on nsPort for nameservers
    ServerSocket server = new ServerSocket(nsPort);
    ServerSocket cmdServer = new ServerSocket(CPORT);

    boolean cmdThreadRunning = false;

    while (true) {
      // opens socket for the nameserver
      // Socket nsSocket = server.accept();

      // USING A FLAG TO GET THE CLIENT RUNNING ONLY ONCE.
      /**
       * The common way would be to do : Socket cmdSocket = cmdServer.accept(); first
       * but that would block the Server until it receives a request from the client.
       * But as we do not have a separate client script to interrupt the server, and
       * because we are using the client/cmd
       */
      if (!cmdThreadRunning) { // if not checked, this will keep creating infinite threads.
        cmdThreadRunning = true;
        Thread cmdThread = new Thread(new BootstrapServer().new Client());
        cmdThread.start();
      }
      Socket cmdSocket = cmdServer.accept();

      // start new thread for each NS instance
    }

  }

  public String lookup(Integer key) {
    return map.containsKey(key) ? map.get(key) : "Key not found";
  }

  public void insert(Integer key, String value) {
    if (!map.containsKey(key)) {
      map.put(key, value);
      System.out.println("Inserted " + key + ": " + value + " successfully!");
    } else {
      System.out.println("Entry already exists!");
    }
  }

  public void delete(Integer key) {
    if (map.containsKey(key)) {
      map.remove(key);
      System.out.println("Deleted " + key + " successfully!");
    } else {
      System.out.println("Entry doesn't exist!");
    }
  }

  public void readfile(String config) {
    try (BufferedReader fileReader = new BufferedReader(new FileReader(config))) {
      // read first two lines
      ID = Integer.parseInt(fileReader.readLine());
      nsPort = Integer.parseInt(fileReader.readLine());

      System.out.println(ID + " " + nsPort);

      // parse the rest
      String line = null;
      String[] pair = null;
      while ((line = fileReader.readLine()) != null) {
        pair = line.split("\\s+");
        map.put(Integer.parseInt(pair[0]), pair[1]);
      }

      System.err.println("Printing hashtable: ");
      System.err.println(map);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // Inner class to make accessing variables easier things easier
  class Client implements Runnable {

    private static final String HOST = "localhost";
    private Socket cmdSocket;

    public Client() throws IOException {
      try {
        Thread.sleep(500);
      } catch (InterruptedException ie) {
        ie.printStackTrace();
      }
      cmdSocket = new Socket(HOST, CPORT);
    }

    @Override
    public void run() {
      // Insert logic here. Use methods to modularize code.
      System.err.println("Created a new NameServer thread!");
      String cmdInput = "";
      String[] cmdArray = null;

      String key;
      String value;

      do {

        System.out.print("prompt> ");

        Scanner scan = new Scanner(System.in);
        cmdInput = scan.nextLine();
        cmdArray = cmdInput.split("\\s+");

        switch (cmdArray[0]) {

        case "lookup":
          key = cmdArray[1];
          value = lookup(Integer.parseInt(key));
          System.out.println("Response -> " + key + ": " + value);
          break;

        case "insert":
          key = cmdArray[1];
          value = cmdArray[2];
          insert(Integer.parseInt(key), value);
          break;

        case "delete":
          key = cmdArray[1];
          delete(Integer.parseInt(key));
          break;

        default:
          break;
        }

      } while (!cmdArray[0].equals("quit"));

    }

  }

}