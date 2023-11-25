import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class ChatGUI extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private Map<String, JTextArea> userChats;
    private String userName;
    private Envia enviar;

    public Envia getEnviar() {
        return enviar;
    }

    public void setEnviar(Envia enviar) {
        this.enviar = enviar;
    }

    public ChatGUI(String name) {
        this.userName = name; 
        setTitle("Chat Multicast - "+name);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout());

        // Panel de chat
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        // Panel de entrada y lista de usuarios
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

        inputField = new JTextField();
        inputPanel.add(inputField, BorderLayout.CENTER);

        sendButton = new JButton("Enviar");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        inputPanel.add(sendButton, BorderLayout.EAST);

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.addListSelectionListener(e -> {
            // Muestra el chat del usuario seleccionado
            String selectedUser = userList.getSelectedValue();
            if (selectedUser != null) {
                showChat(selectedUser);
            }
        });

        JScrollPane userListScrollPane = new JScrollPane(userList);
        userListScrollPane.setPreferredSize(new Dimension(150, 0));
        inputPanel.add(userListScrollPane, BorderLayout.WEST);

        add(inputPanel, BorderLayout.SOUTH);

        userChats = new HashMap<>();

        setVisible(true);
    }

    ChatGUI() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void sendMessage() {
        String message = inputField.getText();
        String selectedUser = userList.getSelectedValue();

        if (selectedUser != null) {
            // Aquí debes enviar el mensaje al usuario seleccionado
            //displayMessage("Tú", message, selectedUser);
            enviar.enviarMsg(message, selectedUser);
            // Limpia el campo de entrada
            inputField.setText("");
        }
    }

    private void showChat(String user) {
        JTextArea userChat = userChats.get(user);
        if (userChat == null) {
            // Si el chat del usuario no existe, créalo
            userChat = new JTextArea();
            userChat.setEditable(false);
            userChats.put(user, userChat);
        }

        // Muestra el chat del usuario en el área de chat principal
        chatArea.setText(userChat.getText());
    }

    public void displayMessage(String sender, String message, String recipient) {
        JTextArea userChat = userChats.get(recipient);
        if (userChat == null) {
            // Si el chat del usuario no existe, créalo
            userChat = new JTextArea();
            userChat.setEditable(false);
            userChats.put(recipient, userChat);
        }

        // Muestra el mensaje en el chat del usuario y en el área de chat principal
        userChat.append(sender + ": " + message + "\n");
        chatArea.setText(userChat.getText());
    }
    
    public void addMessage(String sender, String message, String recipient) {
        JTextArea userChat = userChats.get(recipient);
        if (userChat == null) {
            // Si el chat del usuario no existe, créalo
            userChat = new JTextArea();
            userChat.setEditable(false);
            userChats.put(recipient, userChat);
        }

        // Agrega el mensaje en el chat del usuario
        userChat.append(sender + ": " + message + "\n");
    }

    public void addUser(String user) {
        // Agrega un usuario a la lista
        userListModel.addElement(user);
    }
    
    public void removeUser(String user){
        userListModel.removeElement(user);
    }

//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                ChatGUI chatGUI = new ChatGUI("demian");
//                chatGUI.addUser("diego");
//                chatGUI.addUser("daniel");
//            }
//        });
//    }
}
