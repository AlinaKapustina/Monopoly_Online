package monopoly;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.mail.MessagingException;

public class Server {

    static final int TYPE_NEW = 0;
    static final int TYPE_AUTHORIZATION = 1;
    static final int TYPE_RECOVER = 2;
    static final int TYPE_LOBBI = 3;
    static final int TYPE_START_GAME = 4;
    static final int TYPE_UPDATE_GAME = 5;
    static final int TYPE_GAME = 6;
    static final int TYPE_ADD_HOUSE = 7;
    static final int TYPE_DELETE_HOUSE = 8;

    static final int START_SUM = 2000;

    DataBase dataBase;

    List<String> room_two = new ArrayList<>();
    List<String> room_three = new ArrayList<>();
    List<String> room_four = new ArrayList<>();

    HashMap<String, Game> gamer = new HashMap<>();

    Server() throws ClassNotFoundException, MessagingException {
        room_two.add("admin");
        room_two.add("admin1");
        Game game = new Game(room_two, START_SUM);
        game.getPlayer().get(0).setPos(0);
        game.getPlayer().get(1).setPos(0);
        gamer.put("admin", game);
        gamer.put("admin1", game);

        try (ServerSocket serverSocket = new ServerSocket(20202)) {
            while (true) {
                try (Socket socket = serverSocket.accept(); InputStream inputStream = socket.getInputStream(); OutputStream outputStream = socket.getOutputStream()) {
                    JsonObject resultJson = new JsonObject();
                    resultJson.addProperty("Type", 5);
                    resultJson.addProperty("Login", "admin");
                    resultJson.addProperty("Password", "1234");
                    resultJson.addProperty("E-mail", "alinakapystina@gmail.com");
                    resultJson.addProperty("Number", 2);
                    resultJson.addProperty("Cell", 5);

                    System.out.println("Server start");
                    byte[] buffer = new byte[1000];

                    int size = inputStream.read(buffer);
                    String input = new String(buffer, 0, size);
                    System.out.println(input);
                    System.out.println(input.length());
                    String s = resultJson.toString();
                    JsonObject jsonObject = new JsonParser().parse(input).getAsJsonObject();
                    int type = jsonObject.get("Type").getAsInt();
//                try {
//                    dataBase = new DataBase("MONOPOLY", "123456", "USERS", "localhost");
//                } catch (SQLException ex) {
//
//                }
                    String answer = null;
                    switch (type) {
                        case TYPE_NEW:
                            answer = addNewUser(jsonObject);
                            break;
                        case TYPE_AUTHORIZATION:
                            answer = authorization(jsonObject);
                            break;
                        case TYPE_RECOVER:
                            answer = recoveryPassword(jsonObject);
                            break;
                        case TYPE_LOBBI:
                            answer = addInRoom(jsonObject);
                            break;
                        case TYPE_START_GAME:
                            answer = startGame(jsonObject);
                            break;
                        case TYPE_UPDATE_GAME:
                            System.out.println("update");
                            answer = updateGame(jsonObject);
                            break;
                        case TYPE_GAME:
                            answer = doStep(jsonObject);
                            break;
                        case TYPE_ADD_HOUSE:
                            answer = addHouse(jsonObject);
                            break;
                        case TYPE_DELETE_HOUSE:
                            answer = deleteHouse(jsonObject);
                            break;
                        default:
                            break;
                    }
                    System.out.println("b");
                    outputStream.write(answer.getBytes(), 0, answer.length());

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }

    final String addNewUser(JsonObject jsonObject) {
        String s = null;
        try {
            String login = jsonObject.get("Login").getAsString();
            String password = jsonObject.get("Password").getAsString();
            String email = jsonObject.get("E-mail").getAsString();
            Boolean b = true;
            try {
                dataBase.insertNewUser(login, password, email);
            } catch (SQLException ex) {
                b = false;
            }
            JsonObject answerJson = new JsonObject();
            answerJson.addProperty("Type", TYPE_NEW);
            answerJson.addProperty("Status", b);
            s = answerJson.toString();
            return s;

        } catch (JsonIOException ex) {

        }
        return s;
    }

    final String authorization(JsonObject jsonObject) {
        String pas;
        String s = null;
        try {
            String login = jsonObject.get("Login").getAsString();
            String password = jsonObject.get("Password").getAsString();
            pas = dataBase.getPassword(login);

            JsonObject answerJson = new JsonObject();
            if (pas.equals(password)) {
                answerJson.addProperty("Type", TYPE_AUTHORIZATION);
                answerJson.addProperty("Status", true);
            } else {
                answerJson.addProperty("Type", TYPE_AUTHORIZATION);
                answerJson.addProperty("Status", false);
            }
            s = answerJson.toString();
        } catch (SQLException ex) {

        }
        return s;
    }

    final String recoveryPassword(JsonObject jsonObject) {
        String s = null;
        try {
            String pas;
            String address;
            String login = jsonObject.get("Login").getAsString();
            pas = dataBase.getPassword(login);
            address = dataBase.getEmail(login);
            Sender sender = new Sender(address, login, pas);
            JsonObject answerJson = new JsonObject();
            answerJson.addProperty("Type", TYPE_RECOVER);
            answerJson.addProperty("Status", true);
            s = answerJson.toString();
        } catch (SQLException ex) {

        }
        return s;
    }

    final String addInRoom(JsonObject jsonObject) {

        String s = null;
        try {
            Boolean b = true;
            String login = jsonObject.get("Login").getAsString();
            int number = jsonObject.get("Number").getAsInt();
            if (number == 2) {
                if (room_two.size() < 2) {
                    room_two.add(login);
                } else {
                    b = false;
                }
                if (room_two.size() == 2) {
                    Game game = new Game(room_two, START_SUM);
                    for (int i = 0; i < room_two.size(); i++) {
                        gamer.put(room_two.get(i), game);
                    }
                    room_two = new ArrayList<>();
                }
            } else {
                if (number == 3) {
                    if (room_three.size() < 3) {
                        room_three.add(login);
                    } else {
                        b = false;
                    }
                    if (room_three.size() == 3) {
                        Game game = new Game(room_three, START_SUM);
                        for (int i = 0; i < room_three.size(); i++) {
                            gamer.put(room_three.get(i), game);
                        }
                        room_three = new ArrayList<>();
                    }
                } else {
                    if (number == 4) {
                        if (room_four.size() < 4) {
                            room_four.add(login);
                        } else {
                            b = false;
                        }
                        if (room_four.size() == 4) {
                            Game game = new Game(room_four, START_SUM);
                            for (int i = 0; i < room_four.size(); i++) {
                                gamer.put(room_four.get(i), game);
                            }
                            room_four = new ArrayList<>();
                        }
                    } else {
                        b = false;
                    }
                }
            }
            JsonObject answerJson = new JsonObject();
            answerJson.addProperty("Type", TYPE_LOBBI);
            answerJson.addProperty("Status", b);
            s = answerJson.toString();
        } catch (JsonIOException ex) {

        }
        return s;
    }

    final String startGame(JsonObject jsonObject) {
        String s = null;
        try {
            boolean b = false;
            String login = jsonObject.get("Login").getAsString();
            Game game = gamer.get(login);
            if (game != null) {
                b = true;
            }
            JsonObject answerJson = new JsonObject();
            answerJson.addProperty("Type", TYPE_START_GAME);
            answerJson.addProperty("Status", b);
            s = answerJson.toString();
            System.out.println(s);
        } catch (JsonIOException ex) {
        }
        return s;
    }

    final String updateGame(JsonObject jsonObject) {
        String s = null;
        try {
            String login = jsonObject.get("Login").getAsString();
            Game game = gamer.get(login);
            JsonObject answerJson = new JsonObject();
            answerJson.addProperty("Type", TYPE_UPDATE_GAME);
            Gson builder = new GsonBuilder().setPrettyPrinting().create();
            String g = builder.toJson(game);
            answerJson.addProperty("Game", g);
            s = answerJson.toString();
            //System.out.println(s);
        } catch (JsonIOException ex) {
            ex.printStackTrace();
        }
        return s;
    }

    final String doStep(JsonObject jsonObject) {
        boolean b = true;
        String s = null;
        try {
            String login = jsonObject.get("Login").getAsString();
            int number = jsonObject.get("Cell").getAsInt();
            Game game = gamer.get(login);
            Cell cell = game.getCells().get(number);
            if (!cell.getName().isEmpty()) {
                int house = cell.getHouse();
                int cost = cell.getCost()[house];
                Player player = game.getPlayer().get(game.search(login));
                Player player2 = game.getPlayer().get(game.search(cell.getName()));
                if (cost < player.getSum()) {
                    player.setSum(player.getSum() - cost);
                    player2.setSum(player2.getSum() + cost);
                } else {
                    player.setSum(player.getSum() - cost);
                    player2.setSum(player2.getSum() + cost);
                    b = false;
                    //  game.getPlayer().remove(player);
                }
            } else if (number % 7 == 0) {
                int house = cell.getHouse();
                int cost = cell.getCost()[house];
                Player player = game.getPlayer().get(game.search(login));
                if (cost < player.getSum()) {
                    player.setSum(player.getSum() - cost);
                }
            } else {
                int cost = cell.getArenda();
                Player player = game.getPlayer().get(game.search(login));
                if (cost < player.getSum()) {
                    player.setSum(player.getSum() - cost);
                    cell.setName(login);
                }
            }
            game.setOnStep(game.getPlayer().get((game.search(login) + 1) % game.getPlayer().size()).getName());
            game.getPlayer().get(game.search(login)).setPos(number);
            JsonObject answerJson = new JsonObject();
            answerJson.addProperty("Type", TYPE_GAME);
            answerJson.addProperty("Status", b);
            s = answerJson.toString();
            System.out.println(s);
        } catch (JsonIOException ex) {
        }
        return s;
    }

    final String addHouse(JsonObject jsonObject) {
        boolean b = true;
        String s = null;
        try {
            String login = jsonObject.get("Login").getAsString();
            int number = jsonObject.get("Cell").getAsInt();
            Game game = gamer.get(login);
            Player player = game.getPlayer().get(game.search(login));
            Cell cell = game.getCells().get(number);
            player.setSum(player.getSum() - (int) (cell.getArenda() * 0.5));
            cell.setHouse(cell.getHouse() + 1);
            JsonObject answerJson = new JsonObject();
            answerJson.addProperty("Type", TYPE_GAME);
            answerJson.addProperty("Status", b);
            s = answerJson.toString();
        } catch (JsonIOException e) {

        }
        return s;
    }

    final String deleteHouse(JsonObject jsonObject) {
        boolean b = true;
        String s = null;
        try {
            String login = jsonObject.get("Login").getAsString();
            int number = jsonObject.get("Cell").getAsInt();
            Game game = gamer.get(login);
            Player player = game.getPlayer().get(game.search(login));
            Cell cell = game.getCells().get(number);
            cell.setHouse(cell.getHouse() - 1);
            player.setSum(player.getSum() + (int) (cell.getArenda() * 0.5));
            JsonObject answerJson = new JsonObject();
            answerJson.addProperty("Type", TYPE_GAME);
            answerJson.addProperty("Status", b);
            s = answerJson.toString();
        } catch (JsonIOException e) {

        }
        return s;
    }
}
