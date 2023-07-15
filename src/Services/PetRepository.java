package Services;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

import Controller.PetController;
import Model.*;
import UserInterface.ConsoleMenu;


public class PetRepository implements IRepository<Pet> {

    private Creator petCreator;
    private Statement sqlSt;
    private ResultSet resultSet;
    private String SQLstr;

    public PetRepository() {
        this.petCreator = new PetCreator();
    };

    @Override
    public List<Pet> getAll() {
        List<Pet> farm = new ArrayList<Pet>();
        Pet pet;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection dbConnection = getConnection()) {
                sqlSt = dbConnection.createStatement();
                SQLstr = "SELECT Genus_id, Id, Name, Birthday FROM pet_list";
                resultSet = sqlSt.executeQuery(SQLstr);
                while (resultSet.next()) {

                    PetType type = PetType.getType(resultSet.getInt(1));
                    int id = resultSet.getInt(2);
                    String name = resultSet.getString(3);
                    LocalDate birthday = resultSet.getDate(4).toLocalDate();
                    pet = petCreator.createPet(type, name, birthday);
                    pet.setPetId(id);
                    farm.add(pet);
                }
                return farm;
            }
        } catch (ClassNotFoundException | IOException | SQLException ex) {
            Logger.getLogger(PetRepository.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public int create(Pet pet) {
        int rows;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection dbConnection = getConnection()) {

                SQLstr = "INSERT INTO pet_list (Name, Birthday, Genus_id) VALUES (?, ?, ?)";
                PreparedStatement prepSt = dbConnection.prepareStatement(SQLstr);
                prepSt.setString(1, pet.getName());
                prepSt.setDate(2, Date.valueOf(pet.getBirthdayDate()));
                String typeP = pet.getClass().getSimpleName();
                System.out.println("typeP is: "+typeP);

                if(pet.getClass().getSimpleName().equals("Cat")) prepSt.setInt(3, 1);
                else if(pet.getClass().getSimpleName().equals("Dog")) prepSt.setInt(3, 2);
                else prepSt.setInt(3, 3);


                rows = prepSt.executeUpdate();
                return rows;
            }
        } catch (ClassNotFoundException | IOException | SQLException ex) {
            Logger.getLogger(PetRepository.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public int update(Pet pet) {
        int rows;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection dbConnection = getConnection()) {
                SQLstr = "UPDATE pet_list SET Name = ?, Birthday = ? WHERE Id = ?";
                PreparedStatement prepSt = dbConnection.prepareStatement(SQLstr);

                prepSt.setString(1, pet.getName());
                prepSt.setDate(2, Date.valueOf(pet.getBirthdayDate()));
                prepSt.setInt(3,pet.getPetId());

                rows = prepSt.executeUpdate();
                return rows;
            }
        } catch (ClassNotFoundException | IOException | SQLException ex) {
            Logger.getLogger(PetRepository.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex.getMessage());
        }
    }
    @Override
    public Pet getById(int petId) {
        Pet pet = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection dbConnection = getConnection()) {

                SQLstr = "SELECT Genus_Id, Id, Name, Birthday FROM pet_list WHERE Id = ?";
                PreparedStatement prepSt = dbConnection.prepareStatement(SQLstr);
                prepSt.setInt(1, petId);
                resultSet = prepSt.executeQuery();

                if (resultSet.next()) {

                    PetType type = PetType.getType(resultSet.getInt(1));
                    int id = resultSet.getInt(2);
                    String name = resultSet.getString(3);
                    LocalDate birthday = resultSet.getDate(4).toLocalDate();

                    pet = petCreator.createPet(type, name, birthday);
                    pet.setPetId(id);
                }
                return pet;
            }
        } catch (ClassNotFoundException | IOException | SQLException ex) {
            Logger.getLogger(PetRepository.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex.getMessage());
        }
    }
    public List<String> getCommandsById (int petId){


        List <String> commands = new ArrayList <>();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection dbConnection = getConnection()) {

                    SQLstr = "SELECT Command FROM pet_list WHERE Id = ?";

                PreparedStatement prepSt = dbConnection.prepareStatement(SQLstr);
                prepSt.setInt(1, petId);
                resultSet = prepSt.executeQuery();
                while (resultSet.next()) {
                    commands.add(resultSet.getString(1));
                }
                return commands;
            }
        } catch (ClassNotFoundException | IOException | SQLException ex) {
            Logger.getLogger(PetRepository.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex.getMessage());
        }
    }
    public void train (int id, String command){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection dbConnection = getConnection()) {

                String temp = getCommandsById(id).get(0);
                if(temp==null){
                    System.out.println("cell is null");

                    SQLstr = "UPDATE pet_list SET Command = ? WHERE Id = ?";
                }
                else {
                    System.out.println("cell is not null" + temp);
                    SQLstr = "UPDATE pet_list SET Command=concat( Command , ? ) WHERE id = ?";
                }
                PreparedStatement prepSt = dbConnection.prepareStatement(SQLstr);
                String commandtemp = ", "+ command;
                prepSt.setString(1, commandtemp);
                prepSt.setInt(2, id);

                prepSt.executeUpdate();
            }
        } catch (ClassNotFoundException | IOException | SQLException ex) {
            Logger.getLogger(PetRepository.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex.getMessage());
        }
    }
    @Override
    public void delete (int id) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection dbConnection = getConnection()) {
                SQLstr = "DELETE FROM pet_list WHERE Id = ?";
                PreparedStatement prepSt = dbConnection.prepareStatement(SQLstr);
                prepSt.setInt(1,id);
                prepSt.executeUpdate();
            }
        } catch (ClassNotFoundException | IOException | SQLException ex) {
            Logger.getLogger(PetRepository.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex.getMessage());
        }
    }
    public static Connection getConnection() throws SQLException, IOException {

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("src/Resources/database.properties")) {

            props.load(fis);
            String url = props.getProperty("url");
            String username = props.getProperty("username");
            String password = props.getProperty("password");

            return DriverManager.getConnection(url, username, password);
        }
    }
}